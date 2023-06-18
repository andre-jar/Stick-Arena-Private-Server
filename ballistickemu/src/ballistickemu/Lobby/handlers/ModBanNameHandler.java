package ballistickemu.Lobby.handlers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ballistickemu.Main;
import ballistickemu.Types.StickClient;

public class ModBanNameHandler {
	private static final Logger LOGGER = LoggerFactory.getLogger(ModBanNameHandler.class);
	
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
			LOGGER.info("Player {} issued command /banname {}", client.getName(), username+" "
					+ModBanHandler.getSeverity(periodMinutes)+" "+reason);
		}

		ModCommandHandler.banPlayer(username, client, periodMinutes, reason, false, true);
	}
}
