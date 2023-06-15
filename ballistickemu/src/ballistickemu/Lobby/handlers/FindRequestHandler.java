/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ballistickemu.Lobby.handlers;

import ballistickemu.Types.StickClient;
import ballistickemu.Main;

/**
 *
 * @author Simon
 */
public class FindRequestHandler {
	public static void HandlePacket(StickClient client, String packet) {
		String playerName = packet.substring(2).replaceAll("\0", "");
		StickClient target = Main.getLobbyServer().getClientRegistry().getClientfromName(playerName);
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
