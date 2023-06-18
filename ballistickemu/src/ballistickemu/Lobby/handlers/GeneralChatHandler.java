/*
 *     THIS FILE AND PROJECT IS SUPPLIED FOR EDUCATIONAL PURPOSES ONLY.
 *
 *     This program is free software; you can redistribute it
 *     and/or modify it under the terms of the GNU General
 *     Public License as published by the Free Software
 *     Foundation; either version 2 of the License, or (at your
 *     option) any later version.
 *
 *     This program is distributed in the hope that it will be
 *     useful, but WITHOUT ANY WARRANTY; without even the
 *     implied warranty of MERCHANTABILITY or FITNESS FOR A
 *     PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General
 *     Public License along with this program; if not, write to
 *     the Free Software Foundation, Inc., 59 Temple Place,
 */
package ballistickemu.Lobby.handlers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ballistickemu.Main;
import ballistickemu.Tools.StickPacketMaker;
import ballistickemu.Types.StickClient;

/**
 *
 * @author Simon
 */
public class GeneralChatHandler {
	private static final Logger LOGGER = LoggerFactory.getLogger(GeneralChatHandler.class);

	public static void HandlePacket(StickClient client, String Packet) {
		if (client != null) {
			if (Packet.substring(1).startsWith("!")) {
				if (Main.isChatLogEnabled()) {
					LOGGER.info("Player {} issued command {}", client.getName(), Packet.substring(1));
				}
				PlayerCommandHandler.HandlePacket(client, Packet.substring(1));
				return;
			}

			if (Packet.substring(1, 3).equalsIgnoreCase("::")) {
				if (Main.isChatLogEnabled()) {
					LOGGER.info("Player {} issued command {}", client.getName(), Packet.substring(1));
				}
				ModCommandHandler.ProcessModCommand(client, Packet.substring(1).replaceAll("\0", ""));
				return;
			}

			if (client.getMuteStatus()) {
				client.writeCallbackMessage("SERVER MESSAGE: Unable to send chat message as you have been muted.");
				return;
			}
			String UIDFrom = client.getUID();
			String Text = Packet.substring(1);
			if (client.getLobbyStatus()) {
				if (Main.isChatLogEnabled()) {
					if (client.getModStatus()) {
						LOGGER.info("<<{}>> {}", client.getName(), Text);
					} else {
						LOGGER.info("<{}> {}", client.getName(), Text);
					}
				}
				Main.getLobbyServer().BroadcastPacket(StickPacketMaker.GeneralChat(UIDFrom, Text));
			} else
				client.getRoom().BroadcastToRoom(StickPacketMaker.GeneralChat(UIDFrom, Text));
		}

	}
}
