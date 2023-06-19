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
import ballistickemu.Tools.StringTool;
import ballistickemu.Types.StickClient;
import ballistickemu.Types.StickRoom;

/**
 *
 * @author Simon
 */
public class NewClientHandler {
	private static final Logger LOGGER = LoggerFactory.getLogger(NewClientHandler.class);

	public static void HandlePacket(StickClient client, String Packet) {
		if (Packet.substring(0, 3).equalsIgnoreCase("03_")) {
			if ((client.getQuickplayStatus()) || (client.getName() == null)) // no QP chars in lobby thanks
			{
				client.getIoSession().close(true);
				return;
			}

			if ((client.getRequiresUpdate())) {
				client.getPassDb();
				if (client.getRoom() != null) {
					client.getRoom().GetCR().deregisterClient(client);
					client.setRoom(null);
					client.setLobbyStatus(true);
				}

				Main.getLobbyServer().BroadcastPacket(StickPacketMaker.getNewPlayerUID(client.getUID()));
				client.write(StickPacketMaker.getUserList(Main.getLobbyServer().getClientRegistry(), client.getUID(),
						true, client));
				Main.getLobbyServer()
						.BroadcastPacket(StickPacketMaker.getClientInfo(client.getUID(), client.getName(),
								client.getSelectedSpinner().getColour().getColour1AsString(), client.getKills(),
								client.getDeaths(), client.getWins(), client.getLosses(), client.getRounds(),
								client.getPass() ? 1 : 0, client.getUserLevel()), true, client.getUID());

				client.setRequiresUpdate(false);
			} else { // this happens when someone's come back from the shop / profile page - update
						// with any changes made there
				int pass = 0;
				if (client.getPassDb())
					pass = 1;
				Main.getLobbyServer()
						.BroadcastPacket(StickPacketMaker.getClientInfo(client.getUID(), client.getName(),
								client.getSelectedSpinner().getColour().getColour1AsString(), client.getKills(),
								client.getDeaths(), client.getWins(), client.getLosses(), client.getRounds(), pass,
								client.getUserLevel()));
				client.write(StickPacketMaker.getLoginFailed());
			}
			client.setLobbyStatus(true);
		} else // joining a room
		{
			client.setIsReal(true);
			if (client.getName() == null) {
				client.setUpAsQuickplay();
			} else {
				client.setQuickplayStatus(false);
			}

			String RoomName = Packet.substring(2, (Packet.length() - 1));
			try {
				StickRoom Room = Main.getLobbyServer().getRoomRegistry().GetRoomFromName(RoomName);
				if (Room.getBlackList().contains(client.getDbID())) {
					client.write(StickPacketMaker.getErrorPacket("4"));
					return; // Disconnects the player if he tries to join a server he got kicked out of
				}
				if (Room.isFull(client)) {
					client.write(StickPacketMaker.getErrorPacket("42"));
					return; // Blocks user from joining full room. Fix when a better solutions is found
				}
				if (Room.getNeedsPass() && !client.getPass() && !Room.getVIPs().containsValue(client)) {
					client.write(StickPacketMaker.getErrorPacket("42"));
					return; // Disconnects if player tries to join lab pass match without pass. Fix when a
							// better solution is found
				}
				client.setLobbyStatus(false);
				client.setRequiresUpdate(true);
				Room.GetCR().registerClient(client);
				client.setRoom(Room);
				if (!client.getQuickplayStatus())
					Main.getLobbyServer().BroadcastPacket(StickPacketMaker.Disconnected(client.getUID()));
				Room.BroadcastToRoom(StickPacketMaker.getNewPlayerUID(client.getUID()));
				Room.getTotalJoinedClients().add(client.getName());

				String petID = StringTool.PadStringLeft(String.valueOf(client.getSelectedPet().getItemID() - 200), "0",
						2);
				String petColour1 = client.getSelectedPet().getColour().getColour1AsString();
				String petColour2 = client.getSelectedPet().getColour().getColour2AsString();

				String spinnerID = StringTool
						.PadStringLeft(String.valueOf(client.getSelectedSpinner().getItemID() - 100), "0", 2);
				String spinnerColour1 = client.getSelectedSpinner().getColour().getColour1AsString();
				String spinnerColour2 = client.getSelectedSpinner().getColour().getColour2AsString();

				Room.BroadcastToRoom(
						StickPacketMaker.getUserDataGame(client.getUID(), client.getGameWins(), client.getGameKills(),
								client.getGameDeaths(), StringTool.PadStringLeft(client.getName(), "#", 20), spinnerID,
								spinnerColour1, spinnerColour2, client.getKills(), petID, petColour1, petColour2));
				client.write(StickPacketMaker.getUserListGame(Room.GetCR(), client.getUID(), false, client));
			} catch (Exception e) {
				client.setLobbyStatus(true);
				Main.getLobbyServer().getRoomRegistry().GetRoomFromName(RoomName).GetCR().deregisterClient(client);
				LOGGER.warn("Exception when parsing join room packet: ", e);
			}
		}

	}
}
