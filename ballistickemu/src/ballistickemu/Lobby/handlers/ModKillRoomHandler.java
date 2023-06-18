package ballistickemu.Lobby.handlers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ballistickemu.Main;
import ballistickemu.Types.StickClient;
import ballistickemu.Types.StickRoom;

public class ModKillRoomHandler {
	private static final Logger LOGGER = LoggerFactory.getLogger(ModKillRoomHandler.class);
	public static void HandlePacket(StickClient client, String Packet) {
		if (Packet.length() < 5 || (!client.getModStatus())) {
			return;
		}
		String room = Packet.substring(2).replaceAll("\0", "");
		StickRoom Room = Main.getLobbyServer().getRoomRegistry().GetRoomFromName(room);
		if (Main.isChatLogEnabled()) {
			LOGGER.info("Player {} issued command /killroom {}", client.getName(), room);
		}
		if (Room != null) {
			Room.killRoom();
			client.writeCallbackMessage("Room " + room + " was closed.");
		} else {
			client.writeCallbackMessage("Room " + room + " was not found.");
		}
	}
}
