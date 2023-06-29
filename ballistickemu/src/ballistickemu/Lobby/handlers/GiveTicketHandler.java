package ballistickemu.Lobby.handlers;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ballistickemu.Tools.DatabaseTools;
import ballistickemu.Types.StickClient;

/**
 *
 * @author Michal
 */
public class GiveTicketHandler {	
	private static final Logger LOGGER = LoggerFactory.getLogger(GiveTicketHandler.class);
	private static final int[] prizes = new int[] { 20, 25, 30, 35, 40, 55, 60, 75, 100, 250, 500, 999, 1500, 5000 };
	private static int randomNumber;

	static {
		randomNumber = prizes.length * (prizes.length + 1) / 2;
	}

	public static void HandlePacket(StickClient client) {
		if (1 > client.getTicket()) // dont let anyone spam the cred ticket
		{
			LOGGER.warn(
					client.getName() + " attempted to collect cred ticket despite not having it available for them");
		} else {
			int prize = 0;
			int number = 0;
			int random = new Random().nextInt(randomNumber);
			for (int index = 0; index < prizes.length; index++) {
				number += (prizes.length - index);
				if (random < number) {
					prize = prizes[index];
					client.getCredsTicket(String.valueOf(index));
					break;
				}
			}

			try {
				PreparedStatement ps = DatabaseTools.getDbConnection()
						.prepareStatement("UPDATE `users` SET `cash` = `cash` + ? WHERE `username` = ?");

				ps.setInt(1, prize);
				ps.setString(2, client.getName());
				ps.executeUpdate();

				PreparedStatement ps2 = DatabaseTools.getDbConnection()
						.prepareStatement("UPDATE `users` SET `ticket` = ? WHERE `username` = ?");

				ps2.setInt(1, 0);
				ps2.setString(2, client.getName());
				ps2.executeUpdate();

				PreparedStatement ps3 = DatabaseTools.getDbConnection()
						.prepareStatement("UPDATE `users` SET `lastticket` = ? WHERE `username` = ?");
				ps3.setLong(1, System.currentTimeMillis());
				ps3.setString(2, client.getName());
				ps3.executeUpdate();
				client.setTicket(0);
			} catch (SQLException e) {
				client.getAnnounce("There was an error collecting creds ticket, try again later");
				LOGGER.warn("There was an error accepting cred ticket for a user on database.");
			}
		}
	}
}
