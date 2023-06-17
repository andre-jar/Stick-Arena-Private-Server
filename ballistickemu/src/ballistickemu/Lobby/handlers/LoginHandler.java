/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ballistickemu.Lobby.handlers;

import java.math.BigDecimal;
import java.security.NoSuchAlgorithmException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ballistickemu.Main;
import ballistickemu.Tools.DatabaseTools;
import ballistickemu.Tools.PasswordHasher;
import ballistickemu.Tools.StickPacketMaker;
import ballistickemu.Tools.StringTool;
import ballistickemu.Types.StickClient;
import ballistickemu.Types.StickColour;
import ballistickemu.Types.StickItem;

/**
 *
 * @author Simon
 */
public class LoginHandler {
	private static final Logger LOGGER = LoggerFactory.getLogger(LoginHandler.class);

	public static void HandlePacket(StickClient client, String packet) {
		// ip banning:
		DatabaseTools.lock.lock();
		try {
			try {
				PreparedStatement ps = DatabaseTools.getDbConnection()
						.prepareStatement("SELECT * from `ipbans` where `ip` = ? ORDER BY id DESC LIMIT 1");
				ps.setString(1, client.getIoSession().getRemoteAddress().toString().substring(1).split(":")[0]);
				ResultSet rs = ps.executeQuery();
				if (rs.next()) {
					BigDecimal dec = rs.getBigDecimal("enddate");
					if (dec.longValue() < System.currentTimeMillis()) {
						PreparedStatement ps2 = DatabaseTools.getDbConnection()
								.prepareStatement("DELETE FROM `ipbans` WHERE `ip` = ?");
						ps2.setString(1,
								client.getIoSession().getRemoteAddress().toString().substring(1).split(":")[0]);
						ps2.execute(); // unban if time elapsed
					} else {
						client.write(StickPacketMaker.getErrorPacket("1"));
						return;
					}
				}
			} catch (SQLException e) {
				LOGGER.warn("Exception checking IP ban tables: ", e);
			}
		} finally {
			DatabaseTools.lock.unlock();
		}

		String[] splitted = packet.replaceAll("\0", "").substring(2).split(";");
		if (splitted[0].length() > 20) // 20 is max name length - above is a nono
		{
			client.getIoSession().close(true);
			return;
		}
		String MD5Pass = "";
		try {
			MD5Pass = PasswordHasher.generateHashedPassword(splitted[1]);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}

		try {
			if (DatabaseTools.getDbConnection().isClosed() || DatabaseTools.getDbConnection() == null)
				DatabaseTools.dbConnect();

			PreparedStatement ps = DatabaseTools.getDbConnection()
					.prepareStatement("SELECT * FROM `users` WHERE `USERname` = ? AND `USERpass` = ?");
			ps.setString(1, splitted[0]);
			ps.setString(2, MD5Pass);

			ResultSet rs = ps.executeQuery();
			rs.last();
			int rowCount = rs.getRow();

			if (rowCount < 1) {
				client.write(StickPacketMaker.getLoginFailed());
				return;
			}

			/*
			 * A <UID> <username padded to 20> <colour> <colour> <kills> <deaths> <wins>
			 * <losses> <rounds> <has lab pass> <pass expiry days> <ticket waiting> <cash>
			 * <user level>
			 */

			String paddedUN = StringTool.PadStringLeft(splitted[0], "#", 20);
			String red = StringTool.PadStringLeft(String.valueOf(rs.getInt("red")), "0", 3);
			String green = StringTool.PadStringLeft(String.valueOf(rs.getInt("green")), "0", 3);
			String blue = StringTool.PadStringLeft(String.valueOf(rs.getInt("blue")), "0", 3);

			String colour = red + green + blue;
			int kills = rs.getInt("kills");
			int deaths = rs.getInt("deaths");
			int wins = rs.getInt("wins");
			int losses = rs.getInt("losses");
			int rounds = rs.getInt("rounds");
			int expiry = rs.getInt("passexpiry");
			int cash = rs.getInt("cash");
			int ticket = rs.getInt("ticket");
			int labpass = rs.getInt("labpass");
			int user_level = rs.getInt("user_level");
			int dbID = rs.getInt("UID");

			if (ticket != 1) {
				long lastTicket = rs.getBigDecimal("lastticket").longValue();
				// 8 hours
				if ((lastTicket + (28800000L)) <= System.currentTimeMillis()) {
					PreparedStatement ps2 = DatabaseTools.getDbConnection()
							.prepareStatement("UPDATE `users` SET `ticket` = 1 WHERE `UID` = ?");
					ps2.setInt(1, client.getDbID());
					ps2.execute();
					ticket = 1;
				}
			}

			// check if the same user already exists in server
			StickClient SC = Main.getLobbyServer().getClientRegistry().getClientfromName(splitted[0]);
			if (SC != null)
				SC.getSecondaryLoginPacket();

			// and pass these to the client
			client.setName(splitted[0]);
			client.setColour1(colour);
			client.setColour2(colour);
			client.setKills(kills);
			client.setDeaths(deaths);
			client.setWins(wins);
			client.setLosses(losses);
			client.setRounds(rounds);
			client.setPassExpiry(expiry);
			client.setCash(cash);
			client.setTicket(ticket);
			client.setPass(labpass == 1);
			client.setModStatus(user_level > 0);
			client.setUserLevel(user_level);
			client.setDbID(dbID);

			Boolean ready = false;
			Boolean skipCheck = false;
			// and now for the inventory

			if (rs.getInt("ban") == 1) {
				PreparedStatement ps1 = DatabaseTools.getDbConnection()
						.prepareStatement("SELECT id, enddate FROM `bans` WHERE `userid` = ? ORDER BY id DESC LIMIT 1");
				ps1.setInt(1, client.getDbID());
				ResultSet rs1 = ps1.executeQuery();
				rs1.next();
				BigDecimal dec = rs1.getBigDecimal("enddate");
				if (dec.longValue() < System.currentTimeMillis()) {
					PreparedStatement ps2 = DatabaseTools.getDbConnection()
							.prepareStatement("UPDATE `users` SET `ban` = 0 WHERE `UID` = ?");
					ps2.setInt(1, client.getDbID());
					ps2.execute(); // unban if time elapsed
				} else {
					client.write(StickPacketMaker.getErrorPacket("1"));
					return;
				}
			}
			while (!ready) {
				try (PreparedStatement psz = DatabaseTools.getDbConnection()
						.prepareStatement("SELECT * FROM `inventory` WHERE `userid` = ?")) {
					psz.setInt(1, dbID);
					// System.out.println(psz.toString());
					rs = psz.executeQuery();

					rs.last();
					if (rs.getRow() > 1)
						skipCheck = false;

					if (rs.getRow() < 2) {
						PreparedStatement delete = DatabaseTools.getDbConnection()
								.prepareStatement("DELETE FROM `inventory` where `userid` = ?");
						delete.setInt(1, dbID);
						delete.executeUpdate();
						try (PreparedStatement psx1 = DatabaseTools.getDbConnection().prepareStatement(
								"INSERT INTO `inventory` (`userid`, `itemid`, `itemtype`, `red1`, `green1`, `blue1`, `red2`, `green2`, `blue2`, `selected`) VALUES"
										+ " (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)")) {
							psx1.setInt(1, client.getDbID());
							psx1.setInt(2, 100);
							psx1.setInt(3, 1);
							psx1.setInt(4, client.getStickColour().getRed1());
							psx1.setInt(5, client.getStickColour().getGreen1());
							psx1.setInt(6, client.getStickColour().getBlue1());
							psx1.setInt(7, client.getStickColour().getRed2());
							psx1.setInt(8, client.getStickColour().getGreen2());
							psx1.setInt(9, client.getStickColour().getBlue2());
							psx1.setInt(10, 1);
							psx1.executeUpdate();
						}

						try (PreparedStatement psx2 = DatabaseTools.getDbConnection().prepareStatement(
								"INSERT INTO `inventory` (`userid`, `itemid`, `itemtype`, `red1`, `green1`, `blue1`, `red2`, `green2`, `blue2`, `selected`) VALUES"
										+ " (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)")) {
							psx2.setInt(1, client.getDbID());
							psx2.setInt(2, 200);
							psx2.setInt(3, 2);
							psx2.setInt(4, 0);
							psx2.setInt(5, 0);
							psx2.setInt(6, 0);
							psx2.setInt(7, 0);
							psx2.setInt(8, 0);
							psx2.setInt(9, 0);
							psx2.setInt(10, 1);
							psx2.executeUpdate();
						}
						skipCheck = true;
					}

					if (!skipCheck) {
						try (PreparedStatement psy = DatabaseTools.getDbConnection()
								.prepareStatement("SELECT * FROM `inventory` WHERE `userid` = ?")) {
							psy.setInt(1, dbID);
							ResultSet rs1 = psy.executeQuery();
							while (rs1.next()) {
								StickColour _colour = new StickColour(rs1.getInt("red1"), rs1.getInt("green1"),
										rs1.getInt("blue1"), rs1.getInt("red2"), rs1.getInt("green2"),
										rs1.getInt("blue2"));
								int itemDBID = rs1.getInt("id");
								int itemID = rs1.getInt("itemid");

								client.getInventory().put(itemDBID, new StickItem(itemID, itemDBID, dbID,
										rs1.getInt("itemtype"), rs1.getInt("selected") == 1, _colour));

								// int _ItemID, int _dbID, int _userDBID, Boolean _selected, StickColour _colour
							}
							ready = true;
							rs1.close();
						}
					}
					rs.close();
				}

			}

			// rs.last();
			// int rowCount = rs.getRow();
			// System.out.println(rowCount);

			// and send the packet
			colour = client.getSelectedSpinner().getColour().getColour1AsString();
			String colour2 = client.getSelectedSpinner().getColour().getColour2AsString();
			updateLastLoginDate(client.getDbID(),
					client.getIoSession().getRemoteAddress().toString().substring(1).split(":")[0]);
			client.write(StickPacketMaker.getLoginSuccess(client.getUID(), paddedUN, colour, colour2, kills, deaths,
					wins, losses, rounds, labpass, expiry, ticket, cash, user_level));
			Main.getLobbyServer().getClientRegistry().registerClient(client);
			client.setIsReal(true);

		} catch (SQLException e) {
			LOGGER.warn("Exception at login: + ", e);
		}
	}

	private static void updateLastLoginDate(int dbID, String ip) {

		PreparedStatement ps1;
		try {
			ps1 = DatabaseTools.getDbConnection()
					.prepareStatement("UPDATE `users` SET `lastlogindate` = ?, `ip` = ? WHERE `UID` = ?");
			ps1.setLong(1, System.currentTimeMillis());
			ps1.setString(2, ip);
			ps1.setInt(3, dbID);
			ps1.executeUpdate();
		} catch (SQLException e) {
			LOGGER.warn("Error while update last login date for ID: " + dbID);
		}
	}
}
