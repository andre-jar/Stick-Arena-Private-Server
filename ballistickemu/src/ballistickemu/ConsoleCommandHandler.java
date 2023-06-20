package ballistickemu;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.StringJoiner;

import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ballistickemu.Lobby.handlers.ModCommandHandler;
import ballistickemu.Lobby.handlers.PlayerCommandHandler;
import ballistickemu.Tools.DatabaseTools;
import ballistickemu.Tools.StickPacketMaker;
import ballistickemu.Types.StickClient;
import ballistickemu.Types.StickColour;
import ballistickemu.Types.StickItem;
import ballistickemu.Types.StickPacket;
import ballistickemu.Types.StickRoom;

public class ConsoleCommandHandler {
	protected static final Logger LOGGER = LoggerFactory.getLogger(ConsoleCommandHandler.class);
	private static final StickClient consoleClient;
	static {
		if (Main.isPromptEnabled()) {
			consoleClient = new ConsoleClient();
		} else {
			consoleClient = null;
		}
	}

	public static void handle(String next) {
		String[] args = next.split(" ");
		if (args[0].equalsIgnoreCase("modpromote")) {
			boolean success = false;
			if (args.length != 2) {
				LOGGER.info("Usage: modpromote <user>");
			} else {
				StickClient client = Main.getLobbyServer().getClientRegistry().getClientfromName(args[1]);
				if (client != null) {
					client.setModStatus(true);
					PlayerCommandHandler.updatePlayer(client);
					success = true;
				} else {
					try {
						PreparedStatement ps = DatabaseTools.getDbConnection()
								.prepareStatement("UPDATE users SET `user_level` = 2 WHERE `username` = ?");
						ps.setString(1, args[1]);
						int affectedRows = ps.executeUpdate();
						if (affectedRows != 0) {
							success = true;
						}
					} catch (SQLException e) {
						LOGGER.warn("Error promoting user to moderator.");
					}
					if (!success) {
						LOGGER.warn("User does not exist.");
					}
				}
			}
			return;
		} else if (args[0].equalsIgnoreCase("moddemote")) {
			boolean success = false;
			if (args.length != 2) {
				LOGGER.info("Usage: moddemote <user>");
			} else {
				StickClient client = Main.getLobbyServer().getClientRegistry().getClientfromName(args[1]);
				if (client != null) {
					client.setModStatus(false);
					PlayerCommandHandler.updatePlayer(client);
					success = true;
				} else {
					try {
						PreparedStatement ps = DatabaseTools.getDbConnection()
								.prepareStatement("UPDATE users SET `user_level` = 0 WHERE `username` = ?");
						ps.setString(1, args[1]);
						int affectedRows = ps.executeUpdate();
						if (affectedRows != 0) {
							success = true;
						}
					} catch (SQLException e) {
						LOGGER.warn("Error demoting user to normal player.");
					}
					if (!success) {
						LOGGER.warn("User does not exist.");
					}
				}
			}
			return;
		} else if (args[0].equalsIgnoreCase("find")) {
			if (args.length != 2) {
				LOGGER.info("Usage: find <user>");
			} else {
				StickClient client = Main.getLobbyServer().getClientRegistry().getClientfromName(args[1]);
				if (client == null) {
					PreparedStatement ps;
					try {
						ps = DatabaseTools.getDbConnection()
								.prepareStatement("SELECT username FROM users WHERE `username` = ?");
						ps.setString(1, args[1]);
						ResultSet rs = ps.executeQuery();
						if (rs.next()) {
							LOGGER.info("Player " + args[1] + " is currently offline.");
						} else {
							LOGGER.info("Player " + args[1] + " does not exist.");
						}
					} catch (SQLException e) {
						LOGGER.warn("Error retrieving /find data.");
						return;
					}

				} else {
					if (client.getLobbyStatus()) {
						LOGGER.info("Player " + args[1] + " is in lobby.");
					} else {
						StickRoom r = client.getRoom();
						String location = "Player " + args[1] + " is in Room " + r.getName();
						if (r.getPrivacy()) {
							location += " (private)";
						}
						LOGGER.info(location);
					}
				}
			}
			return;
		} else if (args[0].equalsIgnoreCase("warn")) {
			if (args.length < 2) {
				LOGGER.info("Usage: warn <user> <Message>");
			} else {
				StickClient client = Main.getLobbyServer().getClientRegistry().getClientfromName(args[1]);
				if (client != null) {
					StringJoiner sj = new StringJoiner(" ");
					if (args.length >= 3) {
						for (int index = 3; index < args.length; index++) {
							sj.add(args[index]);
						}
					}
					Main.getLobbyServer().sendToUID(client.getUID(), StickPacketMaker.getModWarn(sj.toString()));
				} else {
					LOGGER.info("User not found.");
				}
			}
			return;
		} else if (args[0].equalsIgnoreCase("ban")) {
			if (args.length < 3) {
				LOGGER.info("Usage: ban <user> <timeMinutes> <reason>");
			} else {
				StringJoiner sj = new StringJoiner(" ");
				if (args.length >= 3) {
					for (int index = 3; index < args.length; index++) {
						sj.add(args[index]);
					}
				}
				ModCommandHandler.banPlayer(args[1], consoleClient, args[2], sj.toString(), true, true);
			}
			return;
		} else if (args[0].equalsIgnoreCase("banname")) {
			if (args.length < 3) {
				LOGGER.info("Usage: banname <user> <timeMinutes> <reason>");
			} else {
				StringJoiner sj = new StringJoiner(" ");
				if (args.length >= 3) {
					for (int index = 3; index < args.length; index++) {
						sj.add(args[index]);
					}
				}
				ModCommandHandler.banPlayer(args[1], consoleClient, args[2], sj.toString(), false, true);
			}
			return;
		} else if (args[0].equalsIgnoreCase("stats")) {
			Collection<StickRoom> rooms = Main.getLobbyServer().getRoomRegistry().getRoomList().values();
			Collection<StickClient> players = Main.getLobbyServer().getClientRegistry().getAllClients();
			int numberLabpassPlayers = 0;
			int numberModerators = 0;
			int numberLabpassGames = 0;
			int numberPrivateMatches = 0;
			for (StickRoom room : rooms) {
				if (room.getPrivacy()) {
					numberPrivateMatches++;
				}
				if (room.getNeedsPass()) {
					numberLabpassGames++;
				}
			}
			for (StickClient player : players) {
				if (player.getModStatus()) {
					numberModerators++;
				}
				if (player.getPass()) {
					numberLabpassPlayers++;
				}
			}
			LOGGER.info("Statistics for Server:");
			LOGGER.info(rooms.size() + " Rooms (" + numberPrivateMatches + " private, " + numberLabpassGames
					+ " labpass games)");
			LOGGER.info(players.size() + " Players (" + numberLabpassPlayers + " with Labpass, " + numberModerators
					+ " moderators)");
			return;
		} else if (args[0].equalsIgnoreCase("global")) {
			StringJoiner sj = new StringJoiner(" ");
			if (args.length >= 2) {
				for (int index = 3; index < args.length; index++) {
					sj.add(args[index]);
				}
			}
			Main.getLobbyServer().BroadcastAnnouncement2(sj.toString());
			return;
		} else if (args[0].equalsIgnoreCase("ip")) {
			if (args.length != 2) {
				LOGGER.info("Usage: ip <user>");
				return;
			}
			StickClient clientForIP = Main.getLobbyServer().getClientRegistry().getClientfromUID(args[1]);
			if (clientForIP == null) {
				try {
					PreparedStatement ps = DatabaseTools.getDbConnection()
							.prepareStatement("SELECT ip FROM `users` WHERE username=?");
					ps.setString(1, args[1]);
					ResultSet rs1 = ps.executeQuery();
					if (rs1.next()) {
						String IP = rs1.getString("ip");
						if (!IP.isEmpty()) {
							LOGGER.info("IP for user " + args[1] + " is " + IP);
							return;
						}

					}
					LOGGER.info("No IP for requested user found.");
				} catch (SQLException sqle) {
					LOGGER.warn("Error retrieving data.", sqle);
				}
			} else {
				String ip = clientForIP.getIoSession().getRemoteAddress().toString().substring(1).split(":")[0];
				LOGGER.info("IP Adress for " + args[1] + " is " + ip);
			}
			return;
		} else if (args[0].equalsIgnoreCase("showgames")) {
			Collection<StickRoom> rooms = Main.getLobbyServer().getRoomRegistry().getRoomList().values();
			LOGGER.info("List of games:");
			for (StickRoom room : rooms) {
				String s = "(";
				if (room.getPrivacy()) {
					s += "private ";
				}
				if (room.getNeedsPass()) {
					s += " labpass";
				}
				s += ")";
				LOGGER.info(s);
			}
			return;
		} else if (args[0].equalsIgnoreCase("killroom") || args[0].equalsIgnoreCase("deleteroom")) {
			if (args.length != 2) {
				LOGGER.info("Usage: killroom|deleteroom <roomname>");
				return;
			}
			StickRoom Room = Main.getLobbyServer().getRoomRegistry().GetRoomFromName(args[1]);
			if (Room != null) {
				Room.killRoom();
			} else {
				LOGGER.info("Room " + args[1] + " was not found.");
			}
			return;
		} else if (args[0].equalsIgnoreCase("listplayers")) {
			StringBuilder sb = new StringBuilder();
			LOGGER.info("User list: ");
			for (StickClient SC : Main.getLobbyServer().getClientRegistry().getAllClients()) {
				if (SC.getName() != null)
					sb.append(" ").append(SC.getName()).append(",");
			}
			return;
		} else if (args[0].equalsIgnoreCase("disconnect")) {
			if (args.length != 2) {
				LOGGER.info("Usage: disconnect <user>");
				return;
			}
			if (!ModCommandHandler.disconnectPlayer(args[1])) {
				LOGGER.info("User " + args[1] + " was not found.");
			}
			return;
		} else if (args[0].equalsIgnoreCase("creator")) {
			if (args.length != 2) {
				LOGGER.info("Usage: creator <roomname>");
				return;
			}
			if (Main.getLobbyServer().getRoomRegistry().GetRoomFromName(args[1]) != null) {
				LOGGER.info("Creator of Room " + args[1] + " is "
						+ Main.getLobbyServer().getRoomRegistry().GetRoomFromName(args[1]).getCreatorName());
			} else {
				LOGGER.info("Room " + args[1] + " not found");
			}
			return;
		} else if (args[0].equalsIgnoreCase("lastlogin")) {
			if (args.length != 2) {
				LOGGER.info("Usage: lastlogin <user>");
				return;
			}
			try {
				PreparedStatement ps = DatabaseTools.getDbConnection()
						.prepareStatement("SELECT lastlogindate FROM `users` WHERE `username` = ?");
				ps.setString(1, args[1]);
				ResultSet rs = ps.executeQuery();
				if (rs.next()) {
					LOGGER.info("User " + args[1] + " last logged in "
							+ new SimpleDateFormat().format(rs.getBigDecimal("lastlogindate")));
				} else {
					LOGGER.info("User " + args[1] + " not found.");
					return;
				}
			} catch (SQLException e) {
				LOGGER.warn("Error retrieving last login date for player {}", args[1]);
			}
			return;
		} else if (args[0].equalsIgnoreCase("killserver")) {
			LOGGER.info("Server terminated at {} by console.", Calendar.getInstance().getTime().toString());
			System.exit(0);
		} else if (args[0].equalsIgnoreCase("ipban")) {
			if (args.length < 2) {
				LOGGER.info("Usage: ipban <user> <minutes>");
				return;
			}
			String minutes = null;
			if (args.length >= 3) {
				minutes = args[2];
			}
			ModCommandHandler.banPlayer(args[1], consoleClient, minutes, null, true, false);
			return;
		} else if (args[0].equalsIgnoreCase("ipunban")) {
			if (args.length != 2) {
				LOGGER.info("Usage: ipunban <user>");
				return;
			}
			PreparedStatement ps;
			DatabaseTools.lock.lock();
			try {
				ps = DatabaseTools.getDbConnection().prepareStatement("DELETE FROM `ipbans` WHERE `playername` = ?");
				ps.setString(1, args[1]);
				ps.executeUpdate();
			} catch (SQLException e) {
				LOGGER.warn("Exception whilst removing IP ban: ", e);
			} finally {
				DatabaseTools.lock.unlock();
			}
			return;
		} else if (args[0].equalsIgnoreCase("mute")) {
			if (args.length == 2) {
				StickClient SC = Main.getLobbyServer().getClientRegistry().getClientfromName(args[1]);
				if (SC != null) {
					SC.setMuteStatus(true);
					LOGGER.info("User " + args[1] + " successfully muted.");
				} else {
					LOGGER.info("User " + args[1] + " was not found.");
				}
			} else {
				LOGGER.info("Usage: mute username");
			}
			return;
		} else if (args[0].equalsIgnoreCase("unmute")) {
			if (args.length == 2) {
				StickClient SC = Main.getLobbyServer().getClientRegistry().getClientfromName(args[1]);
				if (SC != null) {
					SC.setMuteStatus(false);
					LOGGER.info("User " + args[1] + " successfully unmuted.");
				} else {
					LOGGER.info("User " + args[1] + " was not found.");
				}
			} else {
				LOGGER.info("Usage: unmute username");
			}
			return;
		} else if (args[0].equalsIgnoreCase("banrecord")) {
			if (args.length == 2) {
				try {
					PreparedStatement ps = DatabaseTools.getDbConnection()
							.prepareStatement("SELECT * FROM `bans` WHERE `playername` = ? ORDER BY id DESC");
					ps.setString(1, args[1]);
					ResultSet rs = ps.executeQuery();
					boolean hasBanrecords = false;
					LOGGER.info("Banrecord for " + args[1]);
					while (rs.next()) {
						hasBanrecords = true;
						String record = new SimpleDateFormat().format(rs.getBigDecimal("issuedate")) + " banned by "
								+ rs.getString("mod_responsible") + " for " + rs.getString("reason") + " for "
								+ ModCommandHandler
										.getDurationBreakdown(rs.getLong("enddate") - rs.getLong("issuedate"));
						LOGGER.info(record);
					}
					if (!hasBanrecords)
						LOGGER.info("User " + args[1] + " has no ban record.");

				} catch (SQLException e) {
					LOGGER.warn("There was an exception retrieving banrecord: ", e);
				}
			} else {
				LOGGER.info("Usage: banrecord <username>");
			}
			return;
		} else if (args[0].equalsIgnoreCase("alts")) {
			if (args.length != 2) {
				LOGGER.info("Usage: alts <username>");
				return;
			}
			String ip = null;
			StickClient clientForIP = Main.getLobbyServer().getClientRegistry().getClientfromUID(args[1]);
			if (clientForIP == null) {
				PreparedStatement ps;
				try {
					ps = DatabaseTools.getDbConnection()
							.prepareStatement("SELECT ip FROM `users` WHERE `username` = ?");
					ps.setString(1, args[1]);
					ResultSet rs = ps.executeQuery();
					if (rs.next()) {
						ip = rs.getString("ip");
					}
				} catch (SQLException e) {
					LOGGER.warn("There was an exception retrieving alts ", e);
				}
			} else {
				ip = clientForIP.getIoSession().getRemoteAddress().toString().substring(1).split(":")[0];
			}
			if (ip != null && !ip.isEmpty()) {
				try {
					String alts = "";
					PreparedStatement ps1 = DatabaseTools.getDbConnection()
							.prepareStatement("SELECT username FROM `users` WHERE `ip` = ?");
					ps1.setString(1, ip);
					ResultSet rs1 = ps1.executeQuery();
					while (rs1.next()) {
						if (!rs1.getString("username").equalsIgnoreCase(args[1])) {
							alts += rs1.getString("username");
							alts += " ";
						}
					}
					LOGGER.info("alts of User " + args[1] + ": " + alts);
				} catch (SQLException e) {
					LOGGER.warn("There was an exception retrieving alts ", e);
				}
			} else {
				LOGGER.info("No alts for account found");
			}
			return;
		} else if (args[0].equalsIgnoreCase("userinfo")) {
			if (args.length != 2) {
				LOGGER.info("Usage: userinfo <username>");
				return;
			}
			PreparedStatement ps1;
			try {
				ps1 = DatabaseTools.getDbConnection().prepareStatement(
						"SELECT uid,user_level,lastlogindate,ip,email_address,verified,wins,losses,kills,deaths,red,green,blue,ban FROM `users` WHERE `username` = ?");
				ps1.setString(1, args[1]);
				ResultSet rs1 = ps1.executeQuery();
				if (rs1.next()) {
					LOGGER.info("User Info for player " + args[1]);
					LOGGER.info("User Level     " + rs1.getString("user_level"));
					LOGGER.info("IP             " + rs1.getString("ip"));
					LOGGER.info("Email          " + rs1.getString("email_address"));
					LOGGER.info("Email Verified " + rs1.getString("verified"));
					if ("1".equals(rs1.getString("ban"))) {
						PreparedStatement ps = DatabaseTools.getDbConnection().prepareStatement(
								"SELECT id, enddate FROM `bans` WHERE `userid` = ? ORDER BY id DESC LIMIT 1");
						ps.setString(1, rs1.getString("uid"));
						ResultSet rs = ps.executeQuery();
						if (rs.next()) {
							LOGGER.info("Banned Until  " + new SimpleDateFormat().format(rs.getLong("enddate")));
						}
					}
					LOGGER.info("Wins           " + rs1.getString("wins"));
					LOGGER.info("Losses         " + rs1.getString("losses"));
					LOGGER.info("Kills          " + rs1.getString("kills"));
					LOGGER.info("Deaths         " + rs1.getString("deaths"));
					LOGGER.info("Color          " + rs1.getString("red") + " " + rs1.getString("green") + " "
							+ rs1.getString("blue"));
				} else {
					LOGGER.info("User " + args[1] + " not found.");
				}
			} catch (SQLException e) {
				LOGGER.warn("There was an exception retrieving userinfo: ", e);
			}
			return;
		} else if (args[0].equalsIgnoreCase("help")) {
			LOGGER.info("modpromote          Promotes user to moderator.");
			LOGGER.info("moddemote           Demotes user from moderator.");
			LOGGER.info("find                Prints the location of the player.");
			LOGGER.info("warn                Warns the player with a popup.");
			LOGGER.info("ban                 Bans the account and ip for given time.");
			LOGGER.info("banname             Bans the account for given time.");
			LOGGER.info("stats               Displays server statistics.");
			LOGGER.info("global              Displays a global message popup.");
			LOGGER.info("ip                  Displays the ip of given player.");
			LOGGER.info("showgames           Shows the current games.");
			LOGGER.info("killroom|deleteroom Closes the given game.");
			LOGGER.info("listplayers         Lists the players.");
			LOGGER.info("disconnect          Disconnects given player.");
			LOGGER.info("creator             Shows the creator of the game.");
			LOGGER.info("lastlogin           Show the last login of given player.");
			LOGGER.info("killserver          Shutdown the server.");
			LOGGER.info("ipban               Bans the given players ip.");
			LOGGER.info("ipunban             Unbans the given players ip.");
			LOGGER.info("mute                Mutes given player.");
			LOGGER.info("unmute              Unmutes given player.");
			LOGGER.info("banrecord           Shows the banrecord of given player.");
			LOGGER.info("alts                Shows the accounts with same ip.");
			LOGGER.info("userinfo            Displays player specific info.");
			LOGGER.info("labpass             Modifies labpass time of given player.");
			LOGGER.info("credits             Modifies credits of given player.");
			LOGGER.info("changename          Changes the player of given moderator.");
			LOGGER.info("chatlog             Enables/Disables the logging of chat.");
			return;
		} else if (args[0].equalsIgnoreCase("labpass")) {
			if (args.length != 4) {
				LOGGER.info("Usage: labpass <user> <add|set|subtract> <days>");
				return;
			}
			int amount = 0;
			int labpass = 0;
			int days = 0;
			try {
				amount = Integer.parseInt(args[3]);
				try {
					PreparedStatement ps = DatabaseTools.getDbConnection()
							.prepareStatement("SELECT labpass,passexpiry FROM `users` WHERE `username` = ?");
					ps.setString(1, args[1]);
					ResultSet rs = ps.executeQuery();
					if (rs.next()) {
						labpass = rs.getInt("labpass");
						days = rs.getInt("passexpiry");
					} else {
						LOGGER.info("User " + args[1] + " not found.");
						return;
					}
				} catch (SQLException e) {
					LOGGER.warn("There was an exception setting labpass: ", e);
				}

			} catch (NumberFormatException nfe) {
				LOGGER.info("Usage: labpass <user> <add|set|subtract> <days>");
				return;
			}
			if (args[2].equalsIgnoreCase("add")) {
				days += amount;
			} else if (args[2].equalsIgnoreCase("set")) {
				days = amount;
			} else if (args[2].equalsIgnoreCase("substract")) {
				days = Math.min(0, days - amount);
			} else {
				LOGGER.info("Usage: labpass <user> <add|set|subtract> <days>");
				return;
			}
			if (days > 0) {
				labpass = 1;
			}
			try {
				PreparedStatement ps = DatabaseTools.getDbConnection()
						.prepareStatement("UPDATE users SET labpass=?,passexpiry=? WHERE `username` = ?");
				ps.setInt(1, labpass);
				ps.setInt(2, days);
				ps.setString(3, args[1]);
				ps.executeUpdate();
				StickClient client = Main.getLobbyServer().getClientRegistry().getClientfromName(args[2]);
				if (client != null) {
					if (labpass == 1)
						client.setPass(true);
					client.setPassExpiry(days);
					PlayerCommandHandler.updatePlayer(client);
				}
			} catch (SQLException e) {
				LOGGER.warn("There was an exception setting labpass: ", e);
			}
			return;
		} else if (args[0].equalsIgnoreCase("credits")) {
			if (args.length != 4) {
				LOGGER.info("Usage: credits <user> <add|set|subtract> <amount>");
				return;
			}
			int amount = 0;
			int credits = 0;
			try {
				amount = Integer.parseInt(args[3]);
				try {
					PreparedStatement ps = DatabaseTools.getDbConnection()
							.prepareStatement("SELECT cash FROM `users` WHERE `username` = ?");
					ps.setString(1, args[1]);
					ResultSet rs = ps.executeQuery();
					if (rs.next()) {
						credits = rs.getInt("cash");
					} else {
						LOGGER.info("User " + args[1] + " not found.");
						return;
					}
				} catch (SQLException e) {
					LOGGER.warn("There was an exception setting credits: ", e);
				}
			} catch (NumberFormatException nfe) {
				LOGGER.info("Usage: credits <user> <add|set|subtract> <amount>");
				return;
			}
			if (args[2].equalsIgnoreCase("add")) {
				credits += amount;
			} else if (args[2].equalsIgnoreCase("set")) {
				credits = amount;
			} else if (args[2].equalsIgnoreCase("substract")) {
				credits = Math.min(0, credits - amount);
			} else {
				LOGGER.info("Usage: credits <user> <add|set|subtract> <amount>");
				return;
			}
			try {
				PreparedStatement ps = DatabaseTools.getDbConnection()
						.prepareStatement("UPDATE users SET cash=? WHERE `username` = ?");
				ps.setInt(1, credits);

				ps.setString(2, args[1]);
				ps.executeUpdate();
				StickClient client = Main.getLobbyServer().getClientRegistry().getClientfromName(args[2]);
				if (client != null) {
					client.setCash(credits);
					PlayerCommandHandler.updatePlayer(client);
				}
			} catch (SQLException e) {
				LOGGER.warn("There was an exception setting credits: ", e);
			}
			return;
		} else if (args[0].equalsIgnoreCase("changename")) {
			if (args.length != 3) {
				LOGGER.info("Usage: changename <accountname> <new name>");
				return;
			}
			if (!args[2].matches("[A-Za-z0-9.,]{3,20}")) {
				LOGGER.info("Can't change to given name because it is invalid.");
				return;
			}
			try {
				PreparedStatement ps = DatabaseTools.getDbConnection()
						.prepareStatement("SELECT username,user_level FROM users WHERE `username` = ?");
				ps.setString(1, args[1]);
				ResultSet rs = ps.executeQuery();
				if (rs.next()) {
					if (rs.getInt("user_level") > 0) {
						PreparedStatement ps1 = DatabaseTools.getDbConnection()
								.prepareStatement("SELECT username FROM users WHERE `username` = ?");
						ps1.setString(1, args[2]);
						ResultSet rs1 = ps1.executeQuery();
						if (rs1.next()) {
							LOGGER.info(
									"Can't rename to " + args[1] + " because there is already a user with that name.");
						} else {
							PreparedStatement ps3 = DatabaseTools.getDbConnection()
									.prepareStatement("UPDATE users SET username=? WHERE `username` = ?");
							ps3.setString(1, args[2]);
							ps3.setString(1, args[1]);
							StickClient client = Main.getLobbyServer().getClientRegistry().getClientfromName(args[1]);
							if (client != null) {
								client.setName(args[2]);
								PlayerCommandHandler.updatePlayer(client);
							}
						}
					} else {
						LOGGER.info("Only moderators can change names.");
					}
				} else {
					LOGGER.info("User " + args[1] + " does not exist");
				}
			} catch (SQLException e) {
				LOGGER.warn("There was an exception changing name: ", e);
			}
			return;
		} else if(args[0].equalsIgnoreCase("chatlog")) {
			LOGGER.info("Chat log setting changed.");
			Main.setChatLogEnabled(!Main.isChatLogEnabled());
			return;
		} else {
			LOGGER.info("Command not recognized. Type \"help\" for a list of commands.");
		}
	}

	public static class ConsoleClient extends StickClient {
		@Override
		public String getName() {
			return "<CONSOLE>";
		}

		public void setUID(String _UID) {
			throw new UnsupportedOperationException("Not allowed on console user.");
		}

		public void setDbID(int newID) {
			throw new UnsupportedOperationException("Not allowed on console user.");
		}

		public void setUserLevel(int _user_level) {
			throw new UnsupportedOperationException("Not allowed on console user.");
		}

		public void setRequiresUpdate(Boolean FT) {
			throw new UnsupportedOperationException("Not allowed on console user.");
		}

		public void setName(String _name) {
			throw new UnsupportedOperationException("Not allowed on console user.");
		}

		public void setDeaths(int _deaths) {
			throw new UnsupportedOperationException("Not allowed on console user.");
		}

		public void setWins(int _wins) {
			throw new UnsupportedOperationException("Not allowed on console user.");
		}

		public void setLosses(int _losses) {
			throw new UnsupportedOperationException("Not allowed on console user.");
		}

		public void setPassExpiry(int newPE) {
			throw new UnsupportedOperationException("Not allowed on console user.");
		}

		public void setRounds(int _rounds) {
			throw new UnsupportedOperationException("Not allowed on console user.");
		}

		public void setTicket(int _ticket) {
			throw new UnsupportedOperationException("Not allowed on console user.");
		}

		public void setCash(int _cash) {
			throw new UnsupportedOperationException("Not allowed on console user.");
		}

		public void setPass(Boolean pass) {
			throw new UnsupportedOperationException("Not allowed on console user.");
		}

		public void setModStatus(Boolean Mod) {
			throw new UnsupportedOperationException("Not allowed on console user.");
		}

		public void setBlendedStatus(Boolean Blended) {
			throw new UnsupportedOperationException("Not allowed on console user.");
		}

		public void setColour1(String _colour1) {
			throw new UnsupportedOperationException("Not allowed on console user.");
		}

		public void setColour2(String _colour2) {
			throw new UnsupportedOperationException("Not allowed on console user.");
		}

		public void setKills(int _kills) {
			throw new UnsupportedOperationException("Not allowed on console user.");
		}

		public void setGameKills(int _gamekills) {
			throw new UnsupportedOperationException("Not allowed on console user.");
		}

		public void setGameDeaths(int _gamedeaths) {
			throw new UnsupportedOperationException("Not allowed on console user.");
		}

		public void setLobbyStatus(Boolean AtLobby) {
			throw new UnsupportedOperationException("Not allowed on console user.");
		}

		public void setGameWins(int GameWins) {
			throw new UnsupportedOperationException("Not allowed on console user.");
		}

		public void incrementGameWins() {
			return;
		}

		public void incrementRounds() {
			return;
		}

		public void setIsReal(Boolean Real) {
			throw new UnsupportedOperationException("Not allowed on console user.");
		}

		public void setMuteStatus(Boolean Mute) {
			throw new UnsupportedOperationException("Not allowed on console user.");
		}

		public void setRoom(StickRoom room) {
			throw new UnsupportedOperationException("Not allowed on console user.");
		}

		public void setQuickplayStatus(Boolean IsQP) {
			throw new UnsupportedOperationException("Not allowed on console user.");
		}

		public String getUID() {
			return null;
		}

		public String getColour() {
			return null;
		}

		public String getColour2() {
			return null;
		}

		public void setColour(StickColour colour) {
			throw new UnsupportedOperationException("Not allowed on console user.");
		}

		public int getKills() {
			return 0;
		}

		public int getGameKills() {
			return 0;
		}

		public int getGameDeaths() {
			return 0;
		}

		public boolean getLobbyStatus() {
			return false;
		}

		public boolean getRequiresUpdate() {
			return false;
		}

		public IoSession getIoSession() {
			return null;
		}

		public boolean getReal() {
			return false;
		}

		public boolean getModStatus() {
			return true;
		}

		public boolean isBlended() {
			return true;
		}

		public boolean getPass() {
			return true;
		}

		public boolean getMuteStatus() {
			return false;
		}

		public boolean getQuickplayStatus() {
			return false;
		}

		public StickRoom getRoom() {
			return null;
		}

		public int getDeaths() {
			return 0;
		}

		public int getWins() {
			return 0;
		}

		public int getLosses() {
			return 0;
		}

		public int getPassExpiry() {
			return 1;
		}

		public int getRounds() {
			return 0;
		}

		public int getGameWins() {
			return 0;
		}

		public int getTicket() {
			return 0;
		}

		public int getCash() {
			return 0;
		}

		public int getUserLevel() {
			return 2;
		}

		public int getDbID() {
			return -1;
		}

		public LinkedHashMap<Integer, StickItem> getInventory() {
			return null;
		}

		public void write(StickPacket Packet) {
			LOGGER.info("Packet: " + Packet);

		}

		public void writePolicyFile() {
			LOGGER.info("<cross-domain-policy><allow-access-from domain=\"" + Main.IP
					+ "\" to-ports=\"3724,47624,1138,1139,443,110,80\" /></cross-domain-policy>");
		}

		public void writeMessage(String Message) {
			LOGGER.info("Message: " + Message);
		}

		public void getCredsTicket(String Message) {
			throw new UnsupportedOperationException("Not allowed on console user.");
		}

		public void getAnnounce(String Message) {
			LOGGER.info("Announce: " + Message);
		}

		public void getSecondaryLoginPacket() {
			throw new UnsupportedOperationException("Not allowed on console user.");
		}

		public void getBanned() {
			throw new UnsupportedOperationException("Not allowed on console user.");
		}

		public void getBanned(int time, String message) {
			throw new UnsupportedOperationException("Not allowed on console user.");
		}

		public void writeCallbackMessage(String Message) {
			LOGGER.info(Message);
		}

		public void addItemToInventory(int ItemDBID, StickItem item) {
			throw new UnsupportedOperationException("Not allowed on console user.");
		}

		public StickItem getItemByID(int ItemDBID) {
			return null;
		}

		public void setSelectedItem(int iType, int ItemID) {
			throw new UnsupportedOperationException("Not allowed on console user.");
		}

		public StickItem getFirstSpinner() {
			return null;
		}

		public StickItem getFirstPet() {
			return null;
		}

		public StickItem getSelectedSpinner() {
			return null;
		}

		public StickItem getSelectedPet() {
			return null;
		}

		public String getFormattedInventoryData() {
			return null;
		}

		public StickColour getStickColour() {
			return null;
		}

		@SuppressWarnings("unused")
		private void setSelectedInDB(StickItem toChange) {
			throw new UnsupportedOperationException("Not allowed on console user.");
		}

		public void setUpAsQuickplay() {
			throw new UnsupportedOperationException("Not allowed on console user.");
		}
	}
}
