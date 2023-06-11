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
package ballistickemu.Game.handlers;

import ballistickemu.Types.StickClient;
import ballistickemu.Tools.StickPacketMaker;

/**
 *
 * @author Simon
 */
public class VoteKickHandler {
	public static void HandlePacket(StickClient client, String Packet) {
		if (Packet.length() > 3) {
			String v_UID = Packet.substring(1, 4);
			StickClient victim = client.getRoom().GetCR().getClientfromUID(v_UID);
			if (victim != null) {
				if (client.getModStatus()) {
					client.getRoom().moderatorKick(victim);
				} else {
					client.getRoom()
							.BroadcastToRoom(StickPacketMaker.getKickVotePacket(client.getUID(), victim.getUID()));
					client.getRoom().addVoteKick(victim, client);
				}
			} else {
				GamePacketBroadcastHandler.HandlePacket(client, Packet);
			}
		}
	}
}
