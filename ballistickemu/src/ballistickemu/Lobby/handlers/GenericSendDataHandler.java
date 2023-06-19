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
import ballistickemu.Types.StickClientRegistry;

/**
 *
 * @author Simon
 */
public class GenericSendDataHandler {
	private static final Logger LOGGER = LoggerFactory.getLogger(GenericSendDataHandler.class);

	public static void HandlePacket(StickClient client, String Packet) {
		if (Packet.length() < 5) {
			return;
		}
		String ToUID = Packet.substring(2, 5);
		if (Main.isChatLogEnabled() || !StickClientRegistry.getSpyList().isEmpty() && Packet.length() > 5) {
			String ID = Packet.substring(5, 6);
			if ("P".equals(ID)) {
				String fromName = client.getName();
				String toName = Main.getLobbyServer().getClientRegistry().getClientfromUID(ToUID).getName();
				String msg = "[" + fromName + "->" + toName + "]: " + Packet.substring(6);
				if (Main.isChatLogEnabled()) {
					LOGGER.info(msg);
				}
				for (String spy : StickClientRegistry.getSpyList()) {
					if (!spy.equals(fromName) && !spy.equals(toName))
						Main.getLobbyServer().getClientRegistry().getClientfromName(spy).writeCallbackMessage(msg);
				}
			}
		}
		Main.getLobbyServer().sendToUID(ToUID,
				StickPacketMaker.GenericSendPacket(client.getUID(), Packet.substring(5)));

	}

}
