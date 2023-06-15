package ballistickemu.Lobby.handlers;

import ballistickemu.Main;
import ballistickemu.Types.StickClient;

public class ModGlobalHandler {
	public static void HandlePacket(StickClient client, String Packet) {
		if (Packet.length() < 2 || (!client.getModStatus())) {
			return;
		}
		Main.getLobbyServer().BroadcastAnnouncement2(Packet.substring(2));
	}
}
