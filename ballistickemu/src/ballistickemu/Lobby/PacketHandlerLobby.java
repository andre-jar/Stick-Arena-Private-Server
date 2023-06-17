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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ballistickemu.Lobby.handlers.BuyItemRequestHandler;
import ballistickemu.Lobby.handlers.CheckCapacityHandler;
import ballistickemu.Lobby.handlers.CreateRoomHandler;
import ballistickemu.Lobby.handlers.FindRequestHandler;
import ballistickemu.Lobby.handlers.GeneralChatHandler;
import ballistickemu.Lobby.handlers.GenericSendDataHandler;
import ballistickemu.Lobby.handlers.GiveTicketHandler;
import ballistickemu.Lobby.handlers.InventoryRequestHandler;
import ballistickemu.Lobby.handlers.LoginHandler;
import ballistickemu.Lobby.handlers.MapCycleRequestHandler;
import ballistickemu.Lobby.handlers.ModBanHandler;
import ballistickemu.Lobby.handlers.ModBanNameHandler;
import ballistickemu.Lobby.handlers.ModGlobalHandler;
import ballistickemu.Lobby.handlers.ModKillRoomHandler;
import ballistickemu.Lobby.handlers.ModRequestIPHandler;
import ballistickemu.Lobby.handlers.ModWarnHandler;
import ballistickemu.Lobby.handlers.NewClientHandler;
import ballistickemu.Lobby.handlers.RoomDetailRequestHandler;
import ballistickemu.Lobby.handlers.RoomRequestHandler;
import ballistickemu.Lobby.handlers.SetActiveItemRequestHandler;
import ballistickemu.Types.StickClient;

/**
 *
 * @author Simon
 */
public class PacketHandlerLobby {
	private static final Logger LOGGER = LoggerFactory.getLogger(PacketHandlerLobby.class);
	
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
			LOGGER.warn("Unhandled packet received by LobbyPacketHandler: {}", Packet);
			// return;
		}

	}

}
