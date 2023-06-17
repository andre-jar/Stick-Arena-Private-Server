package ballistickemu.Lobby.handlers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ballistickemu.Types.StickClient;

public class DoubleLoginHandler {
	private static final Logger LOGGER = LoggerFactory.getLogger(DoubleLoginHandler.class);

	public static void HandlePacket(StickClient client) {
		LOGGER.info("Secondary login");
		client.getSecondaryLoginPacket();
	}
}
