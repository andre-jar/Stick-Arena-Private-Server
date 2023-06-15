package ballistickemu.Lobby.handlers;

import ballistickemu.Types.StickClient;

public class ModBanHandler {
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

		ModCommandHandler.banPlayer(username, client, periodMinutes, reason, true, true);
	}
}
