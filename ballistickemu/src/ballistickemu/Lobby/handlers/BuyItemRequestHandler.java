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
import ballistickemu.Tools.ItemType;
import ballistickemu.Types.StickClient;
import ballistickemu.Types.StickColour;
import ballistickemu.Types.StickItem;

/**
 *
 * @author Simon
 */
public class BuyItemRequestHandler {
	private static final Logger LOGGER = LoggerFactory.getLogger(BuyItemRequestHandler.class);

	public static void HandlePacket(StickClient client, String packet) { // 0b999123123123456456456.
		if (packet.length() < 24) {
			LOGGER.info("Invalid buy item packet length received. Packet length: " + packet.length() + ", Packet data: "
					+ packet);
			return;
		}
		int ItemID = Integer.parseInt(packet.substring(2, 5));
		int red1 = Integer.parseInt(packet.substring(5, 8));
		int green1 = Integer.parseInt(packet.substring(8, 11));
		int blue1 = Integer.parseInt(packet.substring(11, 14));
		int red2 = Integer.parseInt(packet.substring(14, 17));
		int green2 = Integer.parseInt(packet.substring(17, 20));
		int blue2 = Integer.parseInt(packet.substring(20, 23));
		int itemDBID = -1;
		int iType = ItemType.getItemType(ItemID);
		StickColour newColour = new StickColour(red1, green1, blue1, red2, green2, blue2);
		int price = Main.getLobbyServer().getShop().getPriceByItemID(client, ItemID);

		// some price checking stuff now
		if (price == -1) // ie, non-existant item
		{
			LOGGER.warn(client.getName() + " attempted to buy non-existant item with ID " + ItemID + ".");
			return;
		}

		if (price > client.getCash()) // trying to purchase something you can't afford, i see!
		{
			LOGGER.warn(client.getName() + " attempted to buy item costing more than held cash.");
			client.getIoSession().close(true);
			return;
		}

		try {
			PreparedStatement ps = DatabaseTools.getDbConnection().prepareStatement(
					"INSERT INTO `inventory` (`userid`, `itemid`, `itemtype`, `red1`, `green1`, `blue1`, `red2`, `green2`, `blue2`) VALUES"
							+ " (?, ?, ?, ?, ?, ?, ?, ?, ?)");
			ps.setInt(1, client.getDbID());
			ps.setInt(2, ItemID);
			ps.setInt(3, iType);
			ps.setInt(4, red1);
			ps.setInt(5, green1);
			ps.setInt(6, blue1);
			ps.setInt(7, red2);
			ps.setInt(8, green2);
			ps.setInt(9, blue2);

			itemDBID = DatabaseTools.executeQuery(ps);

			PreparedStatement ps2 = DatabaseTools.getDbConnection()
					.prepareStatement("UPDATE `users` SET `cash` = `cash` - ? WHERE `username` = ?");
			ps2.setInt(1, price);
			ps2.setString(2, client.getName());
			ps2.executeUpdate();

		} catch (SQLException e) {
			LOGGER.warn("There was an error updating the database with a new item.");
		}
		// int _ItemID, int _dbID, int _userDBID, int itemType, Boolean _selected,
		// StickColour _colour
		if (itemDBID != -1) {
			try {
				PreparedStatement ps3 = DatabaseTools.getDbConnection().prepareStatement(
						"SELECT MAX(id) AS `max` FROM `inventory` WHERE `userid` = ? AND `itemtype` = ?");
				ps3.setInt(1, client.getDbID());
				ps3.setInt(2, iType);
				ResultSet result = ps3.executeQuery();
				if (result.next()) {
					itemDBID = result.getInt("max");
				}
			} catch (SQLException e) {
				LOGGER.warn("There was an error retrieving correct item number for item");
			}
			client.addItemToInventory(itemDBID,
					new StickItem(ItemID, itemDBID, client.getDbID(), iType, false, newColour));
			client.setCash(client.getCash() - price);
		} else {
			LOGGER.warn("There was an error in updating the database with a new item - item DBID was returned as -1.");
		}
	}
}
