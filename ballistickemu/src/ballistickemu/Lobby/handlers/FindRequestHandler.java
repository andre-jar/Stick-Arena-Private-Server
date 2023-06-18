/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ballistickemu.Lobby.handlers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ballistickemu.Main;
import ballistickemu.Types.StickClient;

/**
 *
 * @author Simon
 */
public class FindRequestHandler {
	private static final Logger LOGGER = LoggerFactory.getLogger(FindRequestHandler.class);

	public static void HandlePacket(StickClient client, String packet) {
		String playerName = packet.substring(2).replaceAll("\0", "");
		StickClient target = Main.getLobbyServer().getClientRegistry().getClientfromName(playerName);
		if (Main.isChatLogEnabled()) {
			LOGGER.info("Player {} issued command /find {}", client.getName(), playerName);
		}
		if (target == null) {
			client.writeCallbackMessage("Player " + playerName + " was not found.");
			return;
		} else if (target.getLobbyStatus()) {
			client.writeCallbackMessage("Player " + playerName + " is in the lobby.");
		} else if (target.getRoom() != null) {
			if (!target.getRoom().getPrivacy() || client.getModStatus()) {
				client.writeCallbackMessage(
						"Player " + playerName + " is in the game called '" + client.getRoom().getName() + "'.");
			} else {
				client.writeCallbackMessage("Player " + playerName + " was not found.");
			}
		}
	}

}
