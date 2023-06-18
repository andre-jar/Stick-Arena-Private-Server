package ballistickemu.Lobby.handlers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ballistickemu.Main;
import ballistickemu.Types.StickClient;
import ballistickemu.Types.StickPacket;

public class ModRequestIPHandler {
	private static final Logger LOGGER = LoggerFactory.getLogger(ModRequestIPHandler.class);
	
	public static void HandlePacket(StickClient client, String Packet) {
		if (Packet.length() < 5 || (!client.getModStatus())) {
			return;
		}
		String UID = Packet.substring(2).replaceAll("\0", "");
		StickClient clientForIP = Main.getLobbyServer().getClientRegistry().getClientfromUID(UID);
		if (clientForIP == null) {
			return;
		}
		if (Main.isChatLogEnabled()) {
			LOGGER.info("Player {} issued command /ip {}", client.getName(), clientForIP.getName());
		}
		String ip = clientForIP.getIoSession().getRemoteAddress().toString().substring(1).split(":")[0];
		StickPacket ipreturn = new StickPacket();
		ipreturn.Append("07");
		ipreturn.Append(ip);
		client.write(ipreturn);
	}
}
