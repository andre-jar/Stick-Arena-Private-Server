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
package ballistickemu.Game;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ballistickemu.Game.handlers.GamePacketBroadcastHandler;
import ballistickemu.Game.handlers.KillHandler;
import ballistickemu.Game.handlers.MapRatingHandler;
import ballistickemu.Game.handlers.SetMapCycleListHandler;
import ballistickemu.Game.handlers.VoteKickHandler;
import ballistickemu.Lobby.handlers.FindRequestHandler;
import ballistickemu.Lobby.handlers.GeneralChatHandler;
import ballistickemu.Lobby.handlers.GenericSendDataHandler;
import ballistickemu.Lobby.handlers.MapCycleRequestHandler;
import ballistickemu.Lobby.handlers.ModBanHandler;
import ballistickemu.Lobby.handlers.ModBanNameHandler;
import ballistickemu.Lobby.handlers.ModGlobalHandler;
import ballistickemu.Lobby.handlers.ModRequestIPHandler;
import ballistickemu.Lobby.handlers.ModWarnHandler;
import ballistickemu.Lobby.handlers.NewClientHandler;
import ballistickemu.Lobby.handlers.RoomDetailRequestHandler;
import ballistickemu.Lobby.handlers.RoomRequestHandler;
import ballistickemu.Types.StickClient;

/**
 *
 * @author Simon
 */
public class PacketHandlerGame {
	private static final Logger LOGGER = LoggerFactory.getLogger(PacketHandlerGame.class);

	public static void HandlePacket(String Packet, StickClient client)
	// public static object HandlePacket(object state)
	{
		// PacketData PD = (PacketData)state;
		// string Packet = PD.getData();
		// StickClient client = PD.getClient();
		if (Packet.length() < 2) {
			return;
		}
		// Console.WriteLine("Packet being handled from " + client.getName() + " : " +
		// Packet);

		if (Packet.substring(0, 1).equalsIgnoreCase("0")) {
			if (Packet.substring(0, 2).equalsIgnoreCase("0\0")) {

			} else if (Packet.substring(0, 2).equalsIgnoreCase("01")) {
				RoomRequestHandler.handlePacket(client);

			}

			else if (Packet.substring(0, 2).equalsIgnoreCase("03")) {
				NewClientHandler.HandlePacket(client, Packet);

			}

			else if (Packet.substring(0, 2).equalsIgnoreCase("00")) // Send specified data to specified UID
			{
				GenericSendDataHandler.HandlePacket(client, Packet);

			}

			else if (Packet.substring(0, 2).equalsIgnoreCase("04")) {
				RoomDetailRequestHandler.HandlePacket(client, Packet);

			}

			else if (Packet.substring(0, 2).equalsIgnoreCase("05")) {
				SetMapCycleListHandler.HandlePacket(client, Packet);

			}

			else if (Packet.substring(0, 2).equalsIgnoreCase("06")) {
				MapCycleRequestHandler.HandlePacket(client, Packet);
			} else if (Packet.substring(0, 2).equalsIgnoreCase("0g")) {
				ModWarnHandler.HandlePacket(client, Packet);
				return;

			} else if (Packet.substring(0, 2).equalsIgnoreCase("0f")) {
				ModBanHandler.HandlePacket(client, Packet);
			} else if (Packet.substring(0, 2).equalsIgnoreCase("0j")) {
				ModGlobalHandler.HandlePacket(client, Packet);
			} else if (Packet.substring(0, 2).equalsIgnoreCase("07")) {
				ModRequestIPHandler.HandlePacket(client, Packet);
			} else if (Packet.substring(0, 2).equalsIgnoreCase("0l")) {
				ModBanNameHandler.HandlePacket(client, Packet);
			} else if (Packet.substring(0, 2).equalsIgnoreCase("0h")) {
				FindRequestHandler.HandlePacket(client, Packet);
			} else if (Packet.substring(0, 2).equalsIgnoreCase("0i")) {
				MapRatingHandler.HandlePacket(client, Packet);
			}
		}

		else if (Packet.substring(0, 1).equalsIgnoreCase("9")) {
			GeneralChatHandler.HandlePacket(client, Packet);

		} else if (Packet.substring(0, 1).equalsIgnoreCase("1") || Packet.substring(0, 1).equalsIgnoreCase("2")
				|| Packet.substring(0, 1).equalsIgnoreCase("4") || Packet.substring(0, 1).equalsIgnoreCase("6")
				|| Packet.substring(0, 1).equalsIgnoreCase("5") || Packet.substring(0, 1).equalsIgnoreCase("8")) {
			GamePacketBroadcastHandler.HandlePacket(client, Packet);
		}

		else if (Packet.substring(0, 1).equalsIgnoreCase("K")) {
			VoteKickHandler.HandlePacket(client, Packet);
		}

		else if (Packet.substring(0, 1).equalsIgnoreCase("7")) {
			KillHandler.HandlePacket(client, Packet);
		}

		else {
			// Console.WriteLine("Unhandled packet from " +
			// client.getClient().Client.RemoteEndPoint + ":");
			LOGGER.warn("Unhandled packet received by GamePacketHandler: {}", Packet);
		}

	}

}
