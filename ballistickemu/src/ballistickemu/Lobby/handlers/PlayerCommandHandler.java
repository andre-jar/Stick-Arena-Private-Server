/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ballistickemu.Lobby.handlers;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ballistickemu.Main;
import ballistickemu.Tools.DatabaseTools;
import ballistickemu.Tools.StickPacketMaker;
import ballistickemu.Tools.StringTool;
import ballistickemu.Types.StickClient;
import ballistickemu.Types.StickColour;
import ballistickemu.Types.StickItem;

/**
 *
 * @author Simon
 */
public class PlayerCommandHandler {
	private static final Logger LOGGER = LoggerFactory.getLogger(PlayerCommandHandler.class);

	public static void HandlePacket(StickClient client, String CommandStr) // so for example CommandStr would be
																			// "!setcolor 255 255 255 255 255 255"
	{
		String[] C_Splitted = CommandStr.replaceAll("\0", "").split(" ");

		if (C_Splitted[0].equalsIgnoreCase("!setcolor") && client.getModStatus()) {
			setNewColor(client, getArgs(CommandStr, "!setcolor"), false);
			return;
		}
		if (C_Splitted[0].equalsIgnoreCase("!setpetcolor") && client.getModStatus()) {
			setNewColor(client, getArgs(CommandStr, "!setpetcolor"), true);
			return;
		}
		if (C_Splitted[0].equalsIgnoreCase("!builder") && client.getPass()) {
			addSpinner(client, getArgs(CommandStr, "!builder"), 181);
			return;
		}
		if (C_Splitted[0].equalsIgnoreCase("!fuzzy") && client.getPass()) {
			addSpinner(client, getArgs(CommandStr, "!fuzzy"), 182);
			return;
		}
		if (C_Splitted[0].equalsIgnoreCase("!canes") && client.getPass()) {
			addSpinner(client, getArgs(CommandStr, "!canes"), 184);
			return;
		}
		if (C_Splitted[0].equalsIgnoreCase("!hearts") && client.getPass()) {
			addSpinner(client, getArgs(CommandStr, "!hearts"), 185);
			return;
		}
		if (C_Splitted[0].equalsIgnoreCase("!bluehead") && client.getPass()) {
			addSpinner(client, getArgs(CommandStr, "!bluehead"), 186);
			return;
		}
		if ((C_Splitted[0].equalsIgnoreCase("!modspinner")) && (client.getModStatus())) {
			addSpinner(client, getArgs(CommandStr, "!modspinner"), 183);
			return;
		}
		if (C_Splitted[0].equalsIgnoreCase("!setkills") && C_Splitted.length > 1 && client.getModStatus()) {
			setKills(client, C_Splitted[1]);
		}
	}

	private static String[] getArgs(String CommandStr, String command) { // !setcolor 255 255 255 255
		String[] splitted = CommandStr.substring(command.length() + 1).replaceAll("\0", "").split(" ");
		return splitted;
	}

	private static void setNewColor(StickClient client, String[] colour, Boolean Pet) // <--- true for pet, false for
																						// spinner
	{
		if (colour.length != 6) {
			client.writeCallbackMessage("!setcolor syntax: !setcolor red 1 green 1 blue 1 red 2 green 2 blue 2");
			return;
		}

		if (!verifyColourInput(client, colour)) {
			client.writeCallbackMessage("There was an error setting new colour - no changes have been made.");
			return;
		}

		String red = colour[0];
		String green = colour[1];
		String blue = colour[2];
		String red2 = colour[3];
		String green2 = colour[4];
		String blue2 = colour[5];

		StickItem ToUpdate;
		ToUpdate = null;

		if (Pet) {
			ToUpdate = client.getSelectedPet();
		} else {
			ToUpdate = client.getSelectedSpinner();
		}
		int itemDBID = ToUpdate.getItemDBID();
		try {
			PreparedStatement ps = DatabaseTools.getDbConnection().prepareStatement(
					"UPDATE inventory SET `red1` = ?, `green1` = ?, `blue1` = ?, `red2` = ?, `green2` = ?, `blue2` = ? "
							+ "WHERE `userid` = (SELECT `UID` from `users` WHERE `username` = ?) AND `id` = ?");
			ps.setString(1, red);
			ps.setString(2, green);
			ps.setString(3, blue);
			ps.setString(4, red2);
			ps.setString(5, green2);
			ps.setString(6, blue2);
			ps.setString(7, client.getName());
			ps.setInt(8, itemDBID);
			if (ps.executeUpdate() == 1) {
				client.writeCallbackMessage("Color successfully changed.");
				StickColour newColour = new StickColour(Integer.valueOf(red), Integer.valueOf(green),
						Integer.valueOf(blue), Integer.valueOf(red2), Integer.valueOf(green2), Integer.valueOf(blue2));
				ToUpdate.setColour(newColour);
				if (!Pet) {
					client.setColour(newColour);
					PreparedStatement ps2 = DatabaseTools.getDbConnection()
							.prepareStatement("UPDATE `users` SET `red` = ?, `green` = ?, `blue` = ? WHERE `UID` = ?");
					ps2.setInt(1, Integer.valueOf(red));
					ps2.setInt(2, Integer.valueOf(green));
					ps2.setInt(3, Integer.valueOf(blue));
					ps2.setInt(4, client.getDbID());
					ps2.executeUpdate();
				}
				updatePlayer(client);
			} else {
				LOGGER.warn("Updating color failed.");
			}
		} catch (SQLException e) {
			LOGGER.warn("Exception changing colour of user " + client.getName() + ". Exception thrown: ", e);
		}

	}

	private static void addSpinner(StickClient client, String[] colour, int itemID) // fyi "colour" is british spelling
																					// of "color"
	{
		if ((itemID == 184) || (itemID == 185)) // candy cane and heart spinners are not customisable.
		{ // set the colours to default so user does not have to enter a colour
			String[] defCol = { "255", "-99", "-99", "255", "255", "255" }; // red and white, doesn't matter as they
																			// would be same anyways but good for lobby
																			// cols etc
			colour = defCol;
		}
		if (itemID == 186) {
			String[] defCol = { "002", "101", "203", "002", "101", "203" }; // red and white, doesn't matter as they
																			// would be same anyways but good for lobby
																			// cols etc
			colour = defCol;
		}

		if (colour.length != 6) {
			client.writeCallbackMessage("!builder / !fuzzy syntax: !builder / !fuzzy 255 000 000 255 000 000");
			return;
		}

		if (!verifyColourInput(client, colour)) {
			client.writeCallbackMessage("There was an error adding new spinner  - no changes have been made.");
			return;
		}

		String red = colour[0];
		String green = colour[1];
		String blue = colour[2];
		String red2 = colour[3];
		String green2 = colour[4];
		String blue2 = colour[5];

		try {
			PreparedStatement ps = DatabaseTools.getDbConnection().prepareStatement(
					"INSERT INTO `inventory` (`userid`, `itemid`, `itemtype`, `red1`, `green1`, `blue1`, "
							+ "`red2`, `green2`, `blue2`) VALUES (?, ?, 1, ?, ?, ?, ?, ?, ?)");
			ps.setInt(1, client.getDbID());
			ps.setInt(2, itemID);
			ps.setString(3, red);
			ps.setString(4, green);
			ps.setString(5, blue);
			ps.setString(6, red2);
			ps.setString(7, green2);
			ps.setString(8, blue2);

			int newDBID = ps.executeUpdate();
			if (newDBID > 0) {
				PreparedStatement ps3 = DatabaseTools.getDbConnection().prepareStatement(
						"SELECT MAX(id) AS `max` FROM `inventory` WHERE `userid` = ? AND `itemtype` = ?");
				ps3.setInt(1, client.getDbID());
				ps3.setInt(2, 1);
				ResultSet result = ps3.executeQuery();
				if (result.next()) {
					newDBID = result.getInt("max");
				}
				client.writeCallbackMessage("Item successfully added. Check your profile tab!");
				StickColour newcol = new StickColour();
				newcol.setColour1FromString(red + green + blue);
				newcol.setColour2FromString(red2 + green2 + blue2);
				client.addItemToInventory(newDBID, new StickItem(itemID, newDBID, client.getDbID(), 1, false, newcol));
				client.write(StickPacketMaker.getInventoryPacket(client.getFormattedInventoryData()));
			} else {
				client.writeCallbackMessage("Updating color failed.");
			}
		} catch (SQLException e) {
			LOGGER.warn("Exception changing colour of user " + client.getName() + ". Exception thrown: ", e);
		}

	}

	private static Boolean verifyColourInput(StickClient client, String[] colour) {
		String red = colour[0];
		String green = colour[1];
		String blue = colour[2];
		String red2 = colour[3];
		String green2 = colour[4];
		String blue2 = colour[5];

		int i_red = 1000;
		int i_green = 1000;
		int i_blue = 1000;
		int i_red2 = 1000;
		int i_green2 = 1000;
		int i_blue2 = 1000;

		try {
			i_red = Integer.parseInt(red);
			i_green = Integer.parseInt(green);
			i_blue = Integer.parseInt(blue);
			i_red2 = Integer.parseInt(red2);
			i_green2 = Integer.parseInt(green2);
			i_blue2 = Integer.parseInt(blue2);
		} catch (NumberFormatException e) {
			client.writeCallbackMessage("Error changing color: One of the supplied arguments was invalid.");
			return false;
		}

		if ((i_red > 255) || (i_green > 255) || (i_blue > 255) || (i_red2 > 255) || (i_green2 > 255)
				|| (i_blue2 > 255)) {
			client.writeCallbackMessage("Error changing color: Max value for arguments is 255.");
			return false;
		}

		if ((i_red < -99) || (i_green < -99) || (i_blue < -99) || (i_red2 < -99) || (i_green2 < -99)
				|| (i_blue2 < -99)) {
			client.writeCallbackMessage("Error changing color: Minimum value for arguments is -99.");
			return false;
		}
		return true;
	}

	private static void setKills(StickClient client, String S_new_kills) {
		int new_kills = 0;

		try {
			new_kills = Integer.parseInt(S_new_kills);
		} catch (NumberFormatException nfe) {
			client.writeCallbackMessage("Error setting kills: Invalid number entered.");
			return;
		}

		if (new_kills > 9000000) {
			client.writeCallbackMessage("Error setting kills: 9000000 is max amount.");
			return;
		}

		if (new_kills < 0) {
			client.writeCallbackMessage("Error setting kills: 0 is minimum amount.");
			return;
		}

		try {
			PreparedStatement ps = DatabaseTools.getDbConnection()
					.prepareStatement("UPDATE users SET `kills` = ? WHERE `UID` = ?");
			ps.setInt(1, new_kills);
			ps.setInt(2, client.getDbID());
			ps.executeUpdate();
		} catch (SQLException e) {
			LOGGER.warn("Error setting kills; something went wrong with the DB query.");
			return;
		}

		client.setKills(new_kills);

		updatePlayer(client);
		client.writeCallbackMessage("Kills successfully set to + " + S_new_kills);
	}

	public static void updatePlayer(StickClient client) {
		if (client.getLobbyStatus()) {
			Main.getLobbyServer()
					.BroadcastPacket(StickPacketMaker.getClientInfo(client.getUID(), client.getName(),
							client.getSelectedSpinner().getColour().getColour1AsString(), client.getKills(),
							client.getDeaths(), client.getWins(), client.getLosses(), client.getRounds(),
							client.getPass() ? 1 : 0, client.getUserLevel()), true, client.getUID());
			client.write(StickPacketMaker.getClientInfo(client.getUID(), client.getName(),
					client.getSelectedSpinner().getColour().getColour1AsString(), client.getKills(), client.getDeaths(),
					client.getWins(), client.getLosses(), client.getRounds(), client.getPass() ? 1 : 0,
					client.getUserLevel()));
		} else if (client.getRoom() != null) {
			String petID = StringTool.PadStringLeft(String.valueOf(client.getSelectedPet().getItemID() - 200), "0", 2);
			String petColour1 = client.getSelectedPet().getColour().getColour1AsString();
			String petColour2 = client.getSelectedPet().getColour().getColour2AsString();

			String spinnerID = StringTool.PadStringLeft(String.valueOf(client.getSelectedSpinner().getItemID() - 100),
					"0", 2);
			String spinnerColour1 = client.getSelectedSpinner().getColour().getColour1AsString();
			String spinnerColour2 = client.getSelectedSpinner().getColour().getColour2AsString();

			client.getRoom().BroadcastToRoom(StickPacketMaker.getUserDataGame(client.getUID(), client.getGameWins(),
					client.getGameKills(), client.getGameDeaths(), StringTool.PadStringLeft(client.getName(), "#", 20),
					spinnerID, spinnerColour1, spinnerColour2, client.getKills(), petID, petColour1, petColour2));
		}
		String paddedUN = StringTool.PadStringLeft(client.getName(), "#", 20);
		int pass = (client.getPass() == true ? 1 : 0);
		// sending login success again seems to update on client side.
		client.write(
				StickPacketMaker.getLoginSuccess(client.getUID(), paddedUN, client.getColour(), client.getColour2(),
						client.getKills(), client.getDeaths(), client.getWins(), client.getLosses(), client.getRounds(),
						pass, client.getPassExpiry(), client.getTicket(), client.getCash(), client.getUserLevel()));
	}
}
