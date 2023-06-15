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
package ballistickemu.Lobby;

import ballistickemu.Types.StickClient;
import ballistickemu.Lobby.handlers.*;

/**
 *
 * @author Simon
 */
public class PacketHandlerLobby {

	public static void HandlePacket(String Packet, StickClient client)
	// public static object HandlePacket(object state)
	{
		if (Packet.length() < 2) {
			return;
		}
		// Console.WriteLine("Packet being handled from " + client.getName() + " : " +
		// Packet);

		if (Packet.substring(0, 1).equalsIgnoreCase("0")) {
			if (Packet.substring(0, 2).equalsIgnoreCase("08")) {
				CheckCapacityHandler.HandlePacket(client);
				return;
			}

			else if (Packet.substring(0, 2).equalsIgnoreCase("09")) {
				LoginHandler.HandlePacket(client, Packet);
				return;
			}

			else if (Packet.substring(0, 2).equalsIgnoreCase("0\0"))
				return;

			else if (Packet.substring(0, 2).equalsIgnoreCase("01")) {
				RoomRequestHandler.handlePacket(client);
				return;
			}

			else if (Packet.substring(0, 2).equalsIgnoreCase("03")) // || Packet.endsWith("_") &&
																	// Packet.startsWith("02"))
			{
				NewClientHandler.HandlePacket(client, Packet);
				return;
			}

			else if (Packet.substring(0, 2).equalsIgnoreCase("00")) // Send specified data to specified UID
			{
				GenericSendDataHandler.HandlePacket(client, Packet);
				return;
			}

			else if (Packet.substring(0, 2).equalsIgnoreCase("02")) // && !Packet.endsWith("_"))
			{
				CreateRoomHandler.HandlePacket(client, Packet);
				return;
			}

			else if (Packet.substring(0, 2).equalsIgnoreCase("0a")) {
				GiveTicketHandler.HandlePacket(client);
				return;
			}

			else if (Packet.substring(0, 2).equalsIgnoreCase("04")) {
				RoomDetailRequestHandler.HandlePacket(client, Packet);
				return;
			}

			else if (Packet.substring(0, 2).equalsIgnoreCase("06")) {
				MapCycleRequestHandler.HandlePacket(client, Packet);
				return;
			}

			else if (Packet.substring(0, 2).equalsIgnoreCase("0d")) {
				SetActiveItemRequestHandler.HandlePacket(client, Packet);
				return;
			} else if (Packet.substring(0, 2).equalsIgnoreCase("0b")) {
				BuyItemRequestHandler.HandlePacket(client, Packet);
				return;
			} else if (Packet.substring(0, 2).equalsIgnoreCase("0c")) {
				InventoryRequestHandler.HandlePacket(client, Packet);
				return;
			} else if (Packet.substring(0, 2).equalsIgnoreCase("0g")) {
				ModWarnHandler.HandlePacket(client, Packet);
				return;

			} else if (Packet.substring(0, 2).equalsIgnoreCase("0f")) {
				ModBanHandler.HandlePacket(client, Packet);
			} else if (Packet.substring(0, 2).equalsIgnoreCase("0m")) {
				ModKillRoomHandler.HandlePacket(client, Packet);
			} else if (Packet.substring(0, 2).equalsIgnoreCase("0j")) {
				ModGlobalHandler.HandlePacket(client, Packet);
			} else if (Packet.substring(0, 2).equalsIgnoreCase("07")) {
				ModRequestIPHandler.HandlePacket(client, Packet);
			} else if (Packet.substring(0, 2).equalsIgnoreCase("0l")) {
				ModBanNameHandler.HandlePacket(client, Packet);
			} else if (Packet.substring(0, 2).equalsIgnoreCase("0h")) {
				FindRequestHandler.HandlePacket(client, Packet);
				return;
			}
		}

		else if (Packet.substring(0, 1).equalsIgnoreCase("9")) {
			GeneralChatHandler.HandlePacket(client, Packet);
			return;
		} else {
			// Console.WriteLine("Unhandled packet from " +
			// client.getClient().Client.RemoteEndPoint + ":");
			System.out.printf("Unhandled packet received by LobbyPacketHandler: %s", Packet);
			// return;
		}

	}

}
