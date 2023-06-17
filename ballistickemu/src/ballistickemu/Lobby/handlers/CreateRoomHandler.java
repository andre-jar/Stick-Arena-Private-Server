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

import java.util.LinkedHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ballistickemu.Main;
import ballistickemu.Tools.StickPacketMaker;
import ballistickemu.Tools.StringTool;
import ballistickemu.Types.StickClient;
import ballistickemu.Types.StickRoom;

/**
 *
 * @author Simon
 */
public class CreateRoomHandler {
	private static final Logger LOGGER = LoggerFactory.getLogger(CreateRoomHandler.class);

	public static void HandlePacket(StickClient client, String Packet) {
		try {

			String RoomData = "";
			String[] VIPList = new String[0];
			Boolean IsLabPass = false;
			if (Packet.indexOf(";", 0) > 0) {
				RoomData = StringTool.GetStringBetween(Packet, ";");
				VIPList = StringTool.GetStringBetween(Packet, ";", "\0").split(";");
				IsLabPass = true;

			} else {
				RoomData = Packet;
			}
			String mapID = RoomData.substring(2, 3);
			int cycleMode = Integer.parseInt(RoomData.substring(3, 4));
			Boolean isPrivate = (RoomData.substring(4, 5).equalsIgnoreCase("1"));
			Boolean requiresLabPass = (RoomData.substring(5, 6).equalsIgnoreCase("1"));
			String RoomName = RoomData.substring(6).replace("\0", "");

			LinkedHashMap<String, StickClient> VIPMap = new LinkedHashMap<String, StickClient>();

			if ((IsLabPass) && (VIPList.length > 0)) {
				for (String s : VIPList) {
					if (VIPList.length > 0) {
						StickClient vipClient = Main.getLobbyServer().getClientRegistry().getClientfromName(s);
						if (vipClient != null)
							VIPMap.put(s, vipClient);
					}
				}
			}

			// System.out.println(Packet);
			// System.out.println("Room made with properties: " + mapID + cycleMode +
			// isPrivate + RoomName);
			if (client.getName() == null) // TODO: quickplay char generation here
				client.setUpAsQuickplay();
			else
				client.setQuickplayStatus(false);

			StickRoom newRoom = new StickRoom(RoomName, mapID, cycleMode, isPrivate, VIPMap, requiresLabPass,
					client.getName());

			Main.getLobbyServer().getRoomRegistry().RegisterRoom(newRoom);
			newRoom.GetCR().registerClient(client);
			newRoom.getTotalJoinedClients().add(client.getName());
			client.setRoom(newRoom);
			client.setLobbyStatus(false);
			Main.getLobbyServer().BroadcastPacket(StickPacketMaker.Disconnected(client.getUID()));
			client.setRequiresUpdate(true);
			client.write(StickPacketMaker.getNewPlayerUID(client.getUID()));

		} catch (Exception e) {
			LOGGER.warn("Exception parsing create room packet. Packet: " + Packet, e);
		}
	}
}
