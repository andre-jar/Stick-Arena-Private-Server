/*
 *     THIS FILE AND PROJECT IS SUPPLIED FOR EDUCATIONAL PURPOSES ONLY.
 *
 *     This program is free software; you can redistribute it
 *     and/or modify it under the terms of the GNU General
 *     Public License as published by the Free Software
 *     Foundation; either version 2 of the License, or (at your
 *     option) any later version.
 *
 *     This program is distributed in the hope that it will be
 *     useful, but WITHOUT ANY WARRANTY; without even the
 *     implied warranty of MERCHANTABILITY or FITNESS FOR A
 *     PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General
 *     Public License along with this program; if not, write to
 *     the Free Software Foundation, Inc., 59 Temple Place,
 */
package ballistickemu.Lobby.handlers;

import ballistickemu.Types.StickClient;
import ballistickemu.Types.StickRoom;
import ballistickemu.Types.StickPacket;
import ballistickemu.Tools.DatabaseTools;
import ballistickemu.Tools.StickPacketMaker;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.StringJoiner;

import ballistickemu.Main;

/**
 *
 * @author Simon
 */
public class ModCommandHandler {
	public static void ProcessModCommand(StickClient client, String ModCommand) {
		if (client.getUserLevel() < 1) // <=== two bloody important lines of code there :P
			return;

		String[] ModCommandParsed = parseArgs(ModCommand);

		if (ModCommandParsed[0].equalsIgnoreCase("::ban")) {
			if (ModCommandParsed.length >= 3) {
				StringJoiner reason = new StringJoiner(" ");
				for (int index = 3; index < ModCommandParsed.length; index++) {
					reason.add(ModCommandParsed[index]);
				}
				banPlayer(ModCommandParsed[1], client, ModCommandParsed[2], reason.toString(), false, true);
			} else {
				client.writeMessage("Usage: ::ban username minutes reason");
			}
		}
		else if (ModCommandParsed[0].equalsIgnoreCase("::unban")) {
			PreparedStatement ps;
			DatabaseTools.lock.lock();
			try {
				ps = DatabaseTools.getDbConnection().prepareStatement("UPDATE `users` SET ban=0 WHERE `username` = ?");
				ps.setString(1, ModCommandParsed[1]);
				ps.executeUpdate();
			} catch (SQLException e) {
				System.out.println("Exception whilst removing ban: " + e.toString());
			} finally {
				DatabaseTools.lock.unlock();
			}
		}
		else if (ModCommandParsed[0].equalsIgnoreCase("::mute")) {
			if (ModCommandParsed.length == 2) {
				StickClient SC = Main.getLobbyServer().getClientRegistry().getClientfromName(ModCommandParsed[1]);
				if (SC != null) {
					SC.setMuteStatus(true);
					client.writeMessage("User " + ModCommandParsed[1] + " successfully muted.");
				} else {
					client.writeMessage("User " + ModCommandParsed[1] + " was not found.");
				}
			} else {
				client.writeMessage("Usage: ::mute username");
			}
		}

		else if (ModCommandParsed[0].equalsIgnoreCase("::unmute")) {
			if (ModCommandParsed.length == 2) {
				StickClient SC = Main.getLobbyServer().getClientRegistry().getClientfromName(ModCommandParsed[1]);
				if (SC != null) {
					SC.setMuteStatus(false);
					client.writeMessage("User " + ModCommandParsed[1] + " successfully unmuted.");
				} else {
					client.writeMessage("User " + ModCommandParsed[1] + " was not found.");
				}
			} else {
				client.writeMessage("Usage: ::unmute username");
			}
		}

		else if (ModCommandParsed[0].equalsIgnoreCase("::deleteroom")) {
			if (ModCommandParsed.length >= 2) {
				StickRoom Room = Main.getLobbyServer().getRoomRegistry()
						.GetRoomFromName(ModCommand.substring(13).replaceAll("\0", ""));
				if (Room != null) {
					Room.killRoom();
				} else {
					client.writeMessage("Room " + ModCommand.replaceAll("\0", "").substring(13) + " was not found.");
				}
			}
		}

		else if (ModCommandParsed[0].equalsIgnoreCase("::disconnect")) {
			if (ModCommandParsed.length == 2) {
				/*
				 * (StickClient SC =
				 * Main.getLobbyServer().getClientRegistry().getClientfromName(ModCommandParsed[
				 * 1]); if(SC != null) { SC.getIoSession().close(false); //the deregisterclient
				 * stuff will take care of this so we don't have to }
				 */
				if (!disconnectPlayer(ModCommandParsed[1])) {
					client.writeMessage("User " + ModCommandParsed[1] + " was not found.");
				}
			} else {
				client.writeMessage("Usage: ::disconnect username");
			}
		}

		else if (ModCommandParsed[0].equalsIgnoreCase("::ipban")) {
			if (ModCommandParsed.length >= 2) {
				String minutes = null;
				if (ModCommandParsed.length >= 3) {
					minutes = ModCommandParsed[2];
				}
				banPlayer(ModCommandParsed[1], client, minutes, null, true, false);
			} else {
				client.writeMessage("Usage: ::ipban username minutes");
			}
		} else if (ModCommandParsed[0].equalsIgnoreCase("::ipunban")) {
			PreparedStatement ps;
			DatabaseTools.lock.lock();
			try {
				ps = DatabaseTools.getDbConnection().prepareStatement("DELETE FROM `ipbans` WHERE `playername` = ?");
				ps.setString(1, ModCommandParsed[1]);
				ps.executeUpdate();
			} catch (SQLException e) {
				System.out.println("Exception whilst removing IP ban: " + e.toString());
			} finally {
				DatabaseTools.lock.unlock();
			}
		} else if (ModCommandParsed[0].equalsIgnoreCase("::announce")) {
			if (ModCommand.length() > 10)
				Main.getLobbyServer().BroadcastAnnouncement(ModCommand.substring(11).replaceAll("\0", ""));
		}

		else if (ModCommandParsed[0].equalsIgnoreCase("::announce2")) {
			if (ModCommand.length() > 10)
				Main.getLobbyServer().BroadcastAnnouncement2(ModCommand.substring(11).replaceAll("\0", ""));
		}

		else if (ModCommandParsed[0].equalsIgnoreCase("::getAllPlayers")) {
			StringBuilder Result = new StringBuilder();
			Result.append("User list: ");
			for (StickClient SC : Main.getLobbyServer().getClientRegistry().getAllClients()) {
				if (SC.getName() != null)
					Result.append(" ").append(SC.getName()).append(",");
			}
			client.writeMessage(Result.toString());
		}

		else if (ModCommandParsed[0].equalsIgnoreCase("::resetgametime") && client.getRoom() != null) {
			StickRoom Room = client.getRoom();
			Room.setRoundTime(300);
			StickPacket update = StickPacketMaker.getSendRoundDetail(Room.getMapID(), Room.getCycleMode(),
					Room.GetCR().getAllClients().size(), Room.getCurrentRoundTime());
			Room.BroadcastToRoom(update);
			client.writeMessage("Round time successfully reset.");
		}

		else if (ModCommandParsed[0].equalsIgnoreCase("::setgametime") && client.getRoom() != null) {
			int i = -1;
			if (ModCommandParsed.length < 2) {
				client.writeMessage("Syntax - ::setgametime <time in seconds>");
				return;
			}
			try {
				i = Integer.parseInt(ModCommandParsed[1]);
			} catch (NumberFormatException nfe) {
				client.writeMessage("Please ensure you entered a correct numerical value.");
				return;
			}
			if (i > -1) {
				StickRoom Room = client.getRoom();
				Room.setRoundTime(i);
				StickPacket update = StickPacketMaker.getSendRoundDetail(Room.getMapID(), Room.getCycleMode(),
						Room.GetCR().getAllClients().size(), Room.getCurrentRoundTime());
				Room.BroadcastToRoom(update);
				client.writeMessage("Round time successfully set as " + i + " seconds remaining.");
			} else {
				client.writeMessage("There was an error in resetting the round time.");
			}
		}

		else if (ModCommandParsed[0].equalsIgnoreCase("::killserver")) {
			System.out.printf("Server terminated at %s by moderator %s", Calendar.getInstance().getTime().toString(),
					client.getName());
			System.exit(0);
		}

		else if (ModCommandParsed[0].equalsIgnoreCase("::blend")) {
			if (client.getLobbyStatus()) {
				client.setBlendedStatus(true);
				Main.getLobbyServer()
						.BroadcastPacket(StickPacketMaker.getClientInfo(client.getUID(), client.getName(),
								client.getSelectedSpinner().getColour().getColour1AsString(), client.getKills(),
								client.getDeaths(), client.getWins(), client.getLosses(), client.getRounds(),
								client.getPass() ? 1 : 0, 0));
				client.writeCallbackMessage(
						"And you blend, like Altaiir, into the crowds... (what a pointless feature :P)");
			}
		} else if (ModCommandParsed[0].equalsIgnoreCase("::unblend")) {
			if (client.getLobbyStatus()) {
				client.setBlendedStatus(false);
				Main.getLobbyServer()
						.BroadcastPacket(StickPacketMaker.getClientInfo(client.getUID(), client.getName(),
								client.getSelectedSpinner().getColour().getColour1AsString(), client.getKills(),
								client.getDeaths(), client.getWins(), client.getLosses(), client.getRounds(),
								client.getPass() ? 1 : 0, 1));
				client.writeCallbackMessage("Unblended!");
			}
		}

		// ban, mute, deleteroom, disconnect, ipban, announce

	}

	private static String[] parseArgs(String toParse) {
		return toParse.split(" ");
	}

	public static int banPlayer(String playerName, StickClient client, String timeMinutes, String reason, boolean ipban,
			boolean playerban) {
		int banResult = -1;
		long endDate = timeMinutes != null ? System.currentTimeMillis() + (Long.valueOf(timeMinutes) * 60 * 1000)
				: Long.MAX_VALUE;
		StickClient SC = Main.getLobbyServer().getClientRegistry().getClientfromName(playerName);
		if (playerban) {
			if (SC != null)
				SC.getBanned(Integer.valueOf(timeMinutes), reason);
			DatabaseTools.lock.lock();
			try {
				PreparedStatement ps = DatabaseTools.getDbConnection()
						.prepareStatement("UPDATE `users` set `ban` = '1' where `username` = ?");
				ps.setString(1, playerName);
				banResult = ps.executeUpdate();

				if (banResult == -1) {
					client.writeMessage("There was an error banning " + playerName + ".");
					return banResult;
				} else if (banResult == 0) {
					client.writeMessage("User " + playerName + " does not exist.");
					return banResult;
				} else if (banResult >= 1) {
					client.writeMessage("User " + playerName + " was banned successfully.");
				}
				PreparedStatement ps4 = DatabaseTools.getDbConnection()
						.prepareStatement("SELECT UID FROM `users` WHERE `username` = ?");
				ps4.setString(1, playerName);
				ResultSet rs4 = ps4.executeQuery();
				rs4.next();
				PreparedStatement ps2 = DatabaseTools.getDbConnection().prepareStatement(
						"INSERT INTO `bans` (userid, playername, mod_responsible, issuedate, enddate, reason) VALUES (?, ?, ?, ?, ?, ?)");
				ps2.setInt(1, rs4.getInt("UID"));
				ps2.setString(2, playerName);

				ps2.setString(3, client.getName());
				ps2.setLong(4, System.currentTimeMillis());
				ps2.setLong(5, endDate);
				ps2.setString(6, reason);
				ps2.executeUpdate();
			} catch (SQLException e) {
				System.out.println("Exception during ban command: " + e.toString());
			} finally {
				DatabaseTools.lock.unlock();
			}
		}
		if (ipban) {
			try {
				String IP = "";
				if (SC != null) {
					IP = SC.getIoSession().getRemoteAddress().toString().substring(1).split(":")[0];
				} else {
					PreparedStatement ps4 = DatabaseTools.getDbConnection()
							.prepareStatement("SELECT ip FROM `users` WHERE `username` = ?");
					ps4.setString(1, playerName);
					ResultSet rs4 = ps4.executeQuery();
					rs4.next();
					IP = rs4.getString("ip");
				}
				if(IP==null || IP.isEmpty()) {
					System.out.println("No IP for User " +playerName + " could be found.");
					return -1;
				}
				PreparedStatement ps5 = DatabaseTools.getDbConnection().prepareStatement(
						"INSERT INTO `ipbans` (`ip`, `playername`, `mod_responsible`, `issuedate`, `enddate`) VALUES (?, ?, ?, ?, ?)");
				ps5.setString(1, IP);
				ps5.setString(2, playerName);
				ps5.setString(3, client.getName());
				ps5.setLong(4, System.currentTimeMillis());
				ps5.setLong(5, endDate);
				ps5.executeUpdate();
				for (StickClient c : Main.getLobbyServer().getClientRegistry().getAllClients()) {
					if (c.getIoSession().getRemoteAddress().toString().substring(1).split(":")[0].equals(IP)) {
						if (playerban) {
							c.getBanned(Integer.valueOf(timeMinutes), reason);
						} else {
							c.getIoSession().close(false);
						}
					}
				}
			} catch (SQLException e) {
				System.out.println("Exception during ban command: " + e.toString());
			}
		}
		return banResult;
	}

	private static Boolean disconnectPlayer(String playerName) // returns true if player found and dc'ed
	{
		StickClient SC = null;
		int count = 0;
		do {
			SC = Main.getLobbyServer().getClientRegistry().getClientfromName(playerName);
			if (SC != null) {
				SC.getIoSession().close(false); // the deregisterclient stuff will take care of this so we don't have to
			}
			count++;
		} while (SC != null);
		return (count > 0);
	}

}
