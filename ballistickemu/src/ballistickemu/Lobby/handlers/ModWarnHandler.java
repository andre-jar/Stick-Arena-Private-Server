/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ballistickemu.Lobby.handlers;
import ballistickemu.Types.StickClient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ballistickemu.Main;
import ballistickemu.Tools.StickPacketMaker;
/**
 *
 * @author Simon
 */
public class ModWarnHandler {
	private static final Logger LOGGER = LoggerFactory.getLogger(ModWarnHandler.class);
	
    public static void HandlePacket(StickClient client, String Packet)
    {
        if (Packet.length() < 5 || (!client.getModStatus())) { return; }
            String ToUID = Packet.substring(2, 5);
            String WarnMSG = Packet.substring(5);
    		if (Main.isChatLogEnabled()) {
    			String name = Main.getLobbyServer().getClientRegistry().getClientfromUID(ToUID).getName();
    			LOGGER.info("Player {} issued command /warn {}", client.getName(), name);
    		}
        Main.getLobbyServer().sendToUID(ToUID, StickPacketMaker.getModWarn(WarnMSG));
    }
}
