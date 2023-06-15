package ballistickemu.Lobby.handlers;

import ballistickemu.Main;
import ballistickemu.Types.StickClient;
import ballistickemu.Types.StickRoom;

public class ModKillRoomHandler {
	public static void HandlePacket(StickClient client, String Packet) {

		if (Packet.length() < 5 || (!client.getModStatus())) {
			return;
		}
		String room = Packet.substring(2).replaceAll("\0", "");
		StickRoom Room = Main.getLobbyServer().getRoomRegistry().GetRoomFromName(room);
		if (Room != null) {
			Room.killRoom();
			client.writeCallbackMessage("Room " + room + " was closed.");
		} else {
			client.writeCallbackMessage("Room " + room + " was not found.");
		}
	}
}
