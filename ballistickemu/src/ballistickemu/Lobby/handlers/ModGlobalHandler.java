package ballistickemu.Lobby.handlers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ballistickemu.Main;
import ballistickemu.Types.StickClient;

public class ModGlobalHandler {
	private static final Logger LOGGER = LoggerFactory.getLogger(ModGlobalHandler.class);
	public static void HandlePacket(StickClient client, String Packet) {
		if (Packet.length() < 2 || (!client.getModStatus())) {
			return;
		}
		if (Main.isChatLogEnabled()) {
			LOGGER.info("Player {} issued command /global {}", client.getName(), Packet.substring(2));
		}
		Main.getLobbyServer().BroadcastAnnouncement2(Packet.substring(2));
	}
}
