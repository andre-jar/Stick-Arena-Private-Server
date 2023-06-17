/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ballistickemu.Lobby.handlers;
import java.sql.PreparedStatement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ballistickemu.Tools.DatabaseTools;
import ballistickemu.Types.StickClient;
import ballistickemu.Types.StickItem;
/**
 *
 * @author Simon
 */
public class SetActiveItemRequestHandler {
	private static final Logger LOGGER = LoggerFactory.getLogger(SetActiveItemRequestHandler.class);
	
    public static void HandlePacket(StickClient client, String packet)
    {
        try
        {
            //System.out.println("Packet: " + packet);
            //if (packet.replaceAll("\0", "").equalsIgnoreCase("0d1")) //for some reason this packet gets sent sometimes; discard it
                //return;
            if (packet.indexOf("undefined") != -1)
                return; //strange client thingy going on here, discard packet

            int itemDBID = Integer.valueOf(packet.substring(2, packet.length() - 1));
            StickItem toChange = client.getItemByID(itemDBID);
            if(toChange != null)
            {
                //toChange.setSelected(true);
                client.setSelectedItem(toChange.getitemType(), itemDBID);
                PreparedStatement ps = DatabaseTools.getDbConnection().prepareStatement("UPDATE `inventory` SET `selected` = 0 WHERE `itemtype` = ? AND `userid` = ?");
                ps.setInt(1, toChange.getitemType());
                ps.setInt(2, toChange.getUserDBID());
                ps.executeUpdate();
                ps = DatabaseTools.getDbConnection().prepareStatement("UPDATE `inventory` SET `selected` = 1 WHERE `id` = ? AND `userid` = ?");
                ps.setInt(1, toChange.getItemDBID());
                ps.setInt(2, toChange.getUserDBID());
                ps.executeUpdate();
				if (toChange.getitemType() == 1) {
					ps = DatabaseTools.getDbConnection()
							.prepareStatement("UPDATE `users` SET `red` = ?, `green` = ?, `blue` = ? WHERE `UID` = ?");
					ps.setInt(1, toChange.getColour().getRed1());
					ps.setInt(2, toChange.getColour().getGreen1());
					ps.setInt(3, toChange.getColour().getBlue1());
					ps.setInt(4, toChange.getUserDBID());
					ps.executeUpdate();
				}
            }
            else
            {
                LOGGER.warn("Error setting active item as it was null or something.");
            }
        }
        catch(Exception e)
        {
            LOGGER.warn("Query exception while setting active item",e);
        }
    }
}
