package ballistickemu.Lobby.handlers;

import ballistickemu.Types.StickClient;
import ballistickemu.Tools.DatabaseTools;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Random;

/**
 *
 * @author Michal
 */
public class GiveTicketHandler {
	public static void HandlePacket(StickClient client) {
		if (1 > client.getTicket()) // dont let anyone spam the cred ticket
		{
			System.out.println("WARNING: " + client.getName()
					+ " attempted to collect cred ticket despite not having it available for them");
		} else {
			int prize = 0;

			int random = new Random().nextInt(105);
			if (random < 14) {
				prize = 20;
				client.getCredsTicket("0");
			} else if (random < 27) {
				prize = 25;
				client.getCredsTicket("1");
			} else if (random < 39) {
				prize = 30;
				client.getCredsTicket("2");
			} else if (random < 50) {
				prize = 35;
				client.getCredsTicket("3");
			} else if (random < 60) {
				prize = 40;
				client.getCredsTicket("4");
			} else if (random < 69) {
				prize = 55;
				client.getCredsTicket("5");
			} else if (random < 77) {
				prize = 60;
				client.getCredsTicket("6");
			} else if (random < 84) {
				prize = 75;
				client.getCredsTicket("7");
			} else if (random < 90) {
				prize = 100;
				client.getCredsTicket("8");
			} else if (random < 95) {
				prize = 250;
				client.getCredsTicket("9");
			} else if (random < 99) {
				prize = 500;
				client.getCredsTicket("10");
			} else if (random < 102) {
				prize = 999;
				client.getCredsTicket("11");
			} else if (random < 104) {
				prize = 1500;
				client.getCredsTicket("12");
			} else if (random < 105) {
				prize = 5000;
				client.getCredsTicket("13");
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

			} catch (SQLException e) {
				client.getAnnounce("There was an error collecting creds ticket, try again later");
				System.out.println("There was an error accepting cred ticket for a user on database.");
			}
		}
	}
}
