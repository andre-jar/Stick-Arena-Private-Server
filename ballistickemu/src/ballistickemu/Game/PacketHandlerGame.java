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

import ballistickemu.Types.StickClient;
import ballistickemu.Lobby.handlers.*;
import ballistickemu.Game.handlers.*;

/**
 *
 * @author Simon
 */
public class PacketHandlerGame {

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
			System.out.printf("Unhandled packet received by GamePacketHandler: %s", Packet);
			System.out.println();

		}

	}

}
