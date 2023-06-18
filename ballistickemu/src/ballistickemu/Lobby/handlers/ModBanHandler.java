package ballistickemu.Lobby.handlers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ballistickemu.Main;
import ballistickemu.Types.StickClient;

public class ModBanHandler {
	private static final Logger LOGGER = LoggerFactory.getLogger(ModBanHandler.class);

	public static void HandlePacket(StickClient client, String Packet) {
		if (Packet.length() < 8 || (!client.getModStatus())) {
			return;
		}
		String message = Packet.substring(2);
		String[] args = message.split(";");
		String username = args[0];
		String periodMinutes = args[1];
		String reason = null;
		if (args.length == 3) {
			reason = args[2];
		}
		if (Main.isChatLogEnabled()) {
			LOGGER.info("Player {} issued command /ban {}", client.getName(), username+" "
					+getSeverity(periodMinutes)+" "+reason);
		}

		ModCommandHandler.banPlayer(username, client, periodMinutes, reason, true, true);
	}

	public static final String getSeverity(String periodMinutes) {
		switch (periodMinutes) {
		case "5":
			return "1";
		case "30":
			return "2";
		case "1440":
			return "3";
		case "10080":
			return "4";
		case "26208000":
			return "5";
		}
		return "-1";
	}
}
