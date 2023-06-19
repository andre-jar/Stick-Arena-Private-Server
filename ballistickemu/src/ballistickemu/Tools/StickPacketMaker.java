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
package ballistickemu.Tools;

import ballistickemu.Types.StickClient;
import ballistickemu.Types.StickClientRegistry;
import ballistickemu.Types.StickPacket;
import ballistickemu.Main;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Simon
 */
public class StickPacketMaker {
	private static final Logger LOGGER = LoggerFactory.getLogger(StickPacketMaker.class);
	
	public static StickPacket ServerHello() {
		StickPacket Result = new StickPacket();
		Result.Append("08\0");
		return Result;
	}

	public static StickPacket getNewPlayerUID(String UID) {
		StickPacket Result = new StickPacket();
		Result.Append("C");
		Result.Append(UID);
		Result.Append("\0");
		return Result;
	}

	public static StickPacket getRoomList(StickClient client) {
		StickPacket Result = new StickPacket();
		Result.Append("01");
		Result.Append(Main.getLobbyServer().getRoomRegistry().GetRoomPacketInfo(client));
		Result.Append("\0");
		return Result;
	}

	public static StickPacket GenericSendPacket(String MyUID, String PacketData) {
		StickPacket Result = new StickPacket();
		Result.Append("M");
		Result.Append(MyUID);
		Result.Append(PacketData);
		return Result;
	}

	public static void EchoPacket(byte[] bytes) {
		for (int i = 0; i < bytes.length; i++) {
			LOGGER.info("{0:X} ", bytes[i]);
		}
		LOGGER.info("\n");

	}

	public static StickPacket GeneralChat(String UID, String ChatText) {
		StickPacket Result = new StickPacket();
		Result.Append("M");
		Result.Append(UID);
		Result.Append("9");
		Result.Append(ChatText);
		return Result;
	}

	public static StickPacket Disconnected(String UID) {
		StickPacket Result = new StickPacket();
		Result.Append("D");
		Result.Append(UID);
		Result.Append("\0");
		return Result;
	}

	public static StickPacket getSendRoundDetail(String mapID, int cyclemode, int players, int RoundTime) {
		StickPacket Result = new StickPacket();
		Result.Append("04");
		Result.Append(mapID);
		Result.Append(Integer.toString(cyclemode));

		Result.Append(Integer.toString(players));
		Result.Append(Integer.toString(RoundTime + 31));
		Result.Append("\0");
		return Result;
	}

	public static StickPacket getMapCycleRequestResponse(String mapID) {
		StickPacket Result = new StickPacket();
		Result.Append("06mp=");
		Result.Append(mapID);
		Result.Append("\0");
		return Result;
	}

	public static StickPacket getSendRoundTimeTest(int RoundTime) {
		StickPacket Result = new StickPacket();
		Result.Append("04");
		Result.Append(Integer.toString(RoundTime));
		Result.Append("\0");
		return Result;
	}

	public static StickPacket getBroadcastPacket(String Packet, String UIDFrom) {
		StickPacket Result = new StickPacket();
		Result.Append("M");
		Result.Append(UIDFrom);
		Result.Append(Packet);
		return Result;
	}

	public static StickPacket getMessagePacket(String Message, String UID) {
		StickPacket Result = new StickPacket();
		Result.Append("M");
		Result.Append(UID);
		Result.Append("9");
		Result.Append(Message);
		Result.Append("\0");
		return Result;
	}

	public static StickPacket getLoginFailed() {
		StickPacket Result = new StickPacket();
		Result.Append("09");
		Result.Append("\0");
		return Result;
	}

	public static StickPacket getErrorPacket(String errorCode) {
		StickPacket Result = new StickPacket();
		Result.Append("09");
		Result.Append(errorCode);
		Result.Append("\0");
		return Result;
	}

	public static StickPacket getLoginSuccess(String UID, String paddedName, String colour1, String colour2, int kills,
			int deaths, int wins, int losses, int rounds, int pass, int expiryDays, int ticket, int cash,
			int user_level) {
		StickPacket Result = new StickPacket();
		Result.Append("A");
		Result.Append(UID);
		Result.Append(paddedName);
		Result.Append(colour1);
		Result.Append(colour2);
		Result.Append(kills);
		Result.Append(";");
		Result.Append(deaths);
		Result.Append(";");
		Result.Append(wins);
		Result.Append(";");
		Result.Append(losses);
		Result.Append(";");
		Result.Append(rounds);
		Result.Append(";");
		Result.Append(pass);
		Result.Append(";");
		Result.Append(expiryDays);
		Result.Append(";");
		Result.Append(ticket);
		Result.Append(";");
		Result.Append(cash);
		Result.Append(";");
		Result.Append(user_level);
		Result.Append("\0");
		return Result;
	}

	public static StickPacket getClientInfo(String UID, String Name, String Colour, int Kills, int Deaths, int Wins,
			int Losses, int Rounds, int pass, int UserLevel) {
		StickPacket Result = new StickPacket();

		Result.Append("U");
		Result.Append(UID);
		Result.Append(StringTool.PadStringLeft(Name, "#", 20));
		Result.Append(Colour);
		Result.Append(Integer.toString(Kills));
		Result.Append(";");
		Result.Append(Integer.toString(Deaths));
		Result.Append(";");
		Result.Append(Integer.toString(Wins));
		Result.Append(";");
		Result.Append(Integer.toString(Losses));
		Result.Append(";");
		Result.Append(Integer.toString(Rounds));
		Result.Append(";");
		Result.Append(Integer.toString(pass));
		Result.Append(";");
		Result.Append(Integer.toString(UserLevel));
		Result.Append("\0");

		return Result;
	}

	public static StickPacket getUserList(StickClientRegistry Clients, String UIDFrom, Boolean lobby,
			StickClient client) {
		client.setGameKills(0);
		client.setGameDeaths(0);
		StickPacket Result = new StickPacket();
		ArrayList<StickClient> ToDC = new ArrayList<>();
		Clients.ClientsLock.readLock().lock();
		try {
			for (StickClient SC : Clients.getAllClients()) {
				try {
					int pass = 0;
					if ((lobby && SC.getLobbyStatus()) && (SC.getIoSession() != null) && (SC.getReal())
							&& !(SC.getUID().equalsIgnoreCase(UIDFrom))) {

						if (SC.getPass())
							pass = 1;
						Result.Append("C");
						Result.Append(SC.getUID());
						Result.Append("\0");
						Result.Append("U");
						Result.Append(SC.getUID());
						Result.Append(StringTool.PadStringLeft(SC.getName(), "#", 20));
						Result.Append(SC.getSelectedSpinner().getColour().getColour1AsString().substring(0, 9));
						Result.Append(Integer.toString(SC.getKills()));
						Result.Append(";");
						Result.Append(Integer.toString(SC.getDeaths()));
						Result.Append(";");
						Result.Append(Integer.toString(SC.getWins()));
						Result.Append(";");
						Result.Append(Integer.toString(SC.getLosses()));
						Result.Append(";");
						Result.Append(Integer.toString(SC.getRounds()));
						Result.Append(";");
						Result.Append(pass);
						Result.Append(";");
						Result.Append(Integer.toString(SC.getUserLevel()));
						Result.Append("\0");
					}
				} catch (Exception e) {
					ToDC.add(SC);
				}
			}
		} finally {
			Clients.ClientsLock.readLock().unlock();
		}

		for (StickClient SC : ToDC) {
			Clients.deregisterClient(SC);
		}
		ToDC.removeAll(ToDC);
		return Result;
	}

	public static StickPacket getUpdateUserList(StickClientRegistry Clients, String UIDFrom, Boolean lobby,
			StickClient client) {
		StickPacket Result = new StickPacket();
		ArrayList<StickClient> ToDC = new ArrayList<>();
		Clients.ClientsLock.readLock().lock();
		try {
			for (StickClient SC : Clients.getAllClients()) {
				try {
					int pass = 0;
					if ((lobby && SC.getLobbyStatus()) && (SC.getIoSession() != null) && (SC.getReal())
							&& !(SC.getUID().equalsIgnoreCase(UIDFrom))) {

						if (SC.getPass())
							pass = 1;
						Result.Append("U");
						Result.Append(SC.getUID());
						Result.Append(StringTool.PadStringLeft(SC.getName(), "#", 20));
						Result.Append(SC.getSelectedSpinner().getColour().getColour1AsString().substring(0, 9));
						Result.Append(Integer.toString(SC.getKills()));
						Result.Append(";");
						Result.Append(Integer.toString(SC.getDeaths()));
						Result.Append(";");
						Result.Append(Integer.toString(SC.getWins()));
						Result.Append(";");
						Result.Append(Integer.toString(SC.getLosses()));
						Result.Append(";");
						Result.Append(Integer.toString(SC.getRounds()));
						Result.Append(";");
						Result.Append(pass);
						Result.Append(";");
						Result.Append(Integer.toString(SC.getUserLevel()));
						Result.Append("\0");
					}
				} catch (Exception e) {
					ToDC.add(SC);
				}
			}
		} finally {
			Clients.ClientsLock.readLock().unlock();
		}

		for (StickClient SC : ToDC) {
			Clients.deregisterClient(SC);
		}
		ToDC.removeAll(ToDC);
		return Result;
	}

	public static StickPacket getUserListGame(StickClientRegistry Clients, String UIDFrom, Boolean lobby,
			StickClient client) {
		StickPacket Result = new StickPacket();
		String clientUID = client.getUID();
		ArrayList<StickClient> ToDC = new ArrayList<>();
		Clients.ClientsLock.readLock().lock();
		try {
			for (StickClient SC : Clients.getAllClients()) {
				if (SC.getName() == null)
					SC.setName("QuickplayChar");
				try {
					if (!clientUID.equalsIgnoreCase(SC.getUID())) {
						Result.Append("C");
						Result.Append(SC.getUID());
						Result.Append("\0");
						Result.Append("U");
						Result.Append(SC.getUID());
						Result.Append(SC.getGameWins());
						Result.Append(StringTool.PadStringLeft(String.valueOf(SC.getGameKills()), "0", 2));
						Result.Append(StringTool.PadStringLeft(String.valueOf(SC.getGameDeaths()), "0", 2));
						Result.Append(StringTool.PadStringLeft(SC.getName(), "#", 20));
						Result.Append(StringTool
								.PadStringLeft(String.valueOf(SC.getSelectedSpinner().getItemID() - 100), "0", 2));
						Result.Append(SC.getSelectedSpinner().getColour().getColour1AsString());
						Result.Append(SC.getSelectedSpinner().getColour().getColour2AsString());
						Result.Append(StringTool.PadStringLeft(String.valueOf(SC.getSelectedPet().getItemID() - 200),
								"0", 2));
						Result.Append(SC.getSelectedPet().getColour().getColour1AsString());
						Result.Append(SC.getSelectedPet().getColour().getColour2AsString());
						Result.Append(SC.getKills());
						Result.Append("\0");
					}
				}

				catch (Exception e) {
					ToDC.add(SC);
				}

			}
		} finally {
			Clients.ClientsLock.readLock().unlock();
		}

		// clearup
		for (StickClient SC : ToDC) {
			Clients.deregisterClient(SC);
		}
		ToDC.removeAll(ToDC);

		return Result;
	}

	public static StickPacket getUserDataGame(String UID, int GameWins, int GameKills, int GameDeaths,
			String paddedName, String spinnerID, String colour, String colour2, int kills, String petID,
			String petColour1, String petColour2) {
		StickPacket Result = new StickPacket();
		Result.Append("U");
		Result.Append(UID);
		Result.Append(GameWins);
		Result.Append(StringTool.PadStringLeft(String.valueOf(GameKills), "0", 2));
		Result.Append(StringTool.PadStringLeft(String.valueOf(GameDeaths), "0", 2));
		Result.Append(paddedName);
		Result.Append(spinnerID);
		Result.Append(colour);
		Result.Append(colour2);
		Result.Append(petID);
		Result.Append(petColour1);
		Result.Append(petColour2);
		Result.Append(kills);
		Result.Append("\0");
		return Result;
	}

	public static StickPacket getRoomCreatorResponse(String CreatorName) {
		StickPacket Result = new StickPacket();
		Result.Append("06rc=");
		Result.Append(CreatorName);
		Result.Append("\0");
		return Result;
	}

	public static StickPacket getDefaultInventoryPacket() // temp
	{
		StickPacket Result = new StickPacket();

		Result.Append("0c10010430262280430262282598819;20000000000000000000002598820");
		Result.Append("\0");
		return Result;
	}

	public static StickPacket getInventoryPacket(String formattedInventory) {
		StickPacket Result = new StickPacket();

		Result.Append("0c");
		Result.Append(formattedInventory);
		Result.Append("\0");
		return Result;
	}

	public static StickPacket getAnnouncePacket(String message) {
		StickPacket Result = new StickPacket();
		Result.Append("0j");
		Result.Append(message);
		Result.Append("\0");
		return Result;
	}

	public static StickPacket getModWarn(String message) {
		StickPacket Result = new StickPacket();
		Result.Append("0g");
		Result.Append(message);
		return Result;
	}

	public static StickPacket getCallbackPacket(String message) // used "officially" for find player response, but can
																// be used for nicer server messages :)
	{
		StickPacket Result = new StickPacket();
		Result.Append("0h");
		Result.Append(message);
		Result.Append("\0");
		return Result;
	}

	public static StickPacket getSecondaryLoginPacket() {
		StickPacket Result = new StickPacket();
		Result.Append("093");
		Result.Append("\0");
		return Result;
	}

	public static StickPacket getCredsTicket(String message) {
		StickPacket Result = new StickPacket();
		Result.Append("0a");
		Result.Append(message);
		Result.Append("\0");
		return Result;
	}

	public static StickPacket getBanMsg(String message) {
		StickPacket Result = new StickPacket();
		Result.Append("0e");
		Result.Append("24000");
		Result.Append(";");
		Result.Append("Until admin unbans");
		Result.Append("\0");
		return Result;
	}

	public static StickPacket getBanMsg(int time, String message) {
		StickPacket Result = new StickPacket();
		Result.Append("0e");
		Result.Append(time);
		Result.Append(";");
		Result.Append(message);
		Result.Append("\0");
		return Result;
	}

	public static StickPacket getKickedPacket() {
		StickPacket Result = new StickPacket();
		Result.Append("094");
		Result.Append("\0");
		return Result;
	}

	public static StickPacket getKickVotePacket(String kickFromId, String toBeKickedId) {
		StickPacket Result = new StickPacket();
		Result.Append("M");
		Result.Append(kickFromId);
		Result.Append("K");
		Result.Append(toBeKickedId);
		Result.Append("\0");
		return Result;
	}
}
