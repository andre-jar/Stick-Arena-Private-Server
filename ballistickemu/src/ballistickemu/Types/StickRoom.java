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
package ballistickemu.Types;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.LinkedHashMap;
import ballistickemu.Main;
import ballistickemu.Lobby.handlers.PlayerCommandHandler;
import ballistickemu.Tools.DatabaseTools;
import ballistickemu.Tools.StickPacketMaker;

import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 *
 * @author Simon
 */
public class StickRoom {
	private StickClientRegistry CR;
	private String Name;
	private String MapID;
	private int CycleMode;
	private Boolean isPrivate;
	// private Timer RoomTimer;
	private int RoundTime;
	private int StorageKey;
	private String MapCycleList;
	private Boolean requiresPass;
	private LinkedHashMap<String, StickClient> VIPs;
	public ReentrantReadWriteLock VIPLock;
	private String CreatorName;
	private List<Integer> blacklist;
	private StickClient lastKickTarget;
	Set<StickClient> kickVoters;
	Set<String> totalJoinedClients;

	public StickRoom() {
		this.CR = new StickClientRegistry(false);
	}

	public StickRoom(String _Name, String _MapID, int CM, Boolean Priv, LinkedHashMap<String, StickClient> VIPs,
			Boolean needsPass, String _CreatorName) {
		this.Name = _Name;
		this.MapID = _MapID;
		this.CycleMode = CM;
		this.isPrivate = Priv;
		this.RoundTime = 300;
		// this.RoomTimer = new Timer();
		this.CR = new StickClientRegistry(false);
		// this.RoomTimer.scheduleAtFixedRate(new OnTimedEvent(), 1000, 1000);
		this.VIPs = VIPs;
		this.requiresPass = needsPass;
		this.VIPLock = new ReentrantReadWriteLock();
		this.CreatorName = _CreatorName;
		Main.getLobbyServer().getRoomRegistry().scheduleRoomTimer(_Name, new OnTimedEvent());
		blacklist = new ArrayList<>();
		kickVoters = new HashSet<>();
		totalJoinedClients = new HashSet<>();
	}

	public void BroadcastToRoom(StickPacket packet) {
		ArrayList<StickClient> ToDC = new ArrayList<>();
		this.CR.ClientsLock.readLock().lock();
		try {
			for (StickClient c : this.CR.getAllClients()) {
				try {
					if (!c.getLobbyStatus())
						c.write(packet);
				} catch (Exception e) {
					ToDC.add(c);
				}
			}
		} finally {
			this.CR.ClientsLock.readLock().unlock();
		}

		for (StickClient c : ToDC) {
			this.CR.deregisterClient(c);
		}
		ToDC.removeAll(ToDC);
	}

	public StickClientRegistry GetCR() {
		return CR;
	}

	public void setName(String NewName) {
		this.Name = NewName;
	}

	public void setCreatorName(String _CN) {
		this.Name = _CN;
	}

	public void setMapID(String NewMapID) {
		this.MapID = NewMapID;
	}

	public void setCycleMode(int newCM) {
		this.CycleMode = newCM;
	}

	public void setPrivacy(Boolean priv) {
		this.isPrivate = priv;
	}

	public void setRoundTime(int time) {
		this.RoundTime = time;
	}

	public void setMapCycleList(String MCL) {
		this.MapCycleList = MCL;
	}

	public String getName() {
		return this.Name;
	}

	public String getCreatorName() {
		return this.CreatorName;
	}

	public String getMapID() {
		return this.MapID;
	}

	public int getCycleMode() {
		return this.CycleMode;
	}

	public Boolean getPrivacy() {
		return this.isPrivate;
	}

	public Boolean getNeedsPass() {
		return this.requiresPass;
	}

	public int getCurrentRoundTime() {
		return this.RoundTime;
	}

	public int getStorageKey() {
		return this.StorageKey;
	}

	public String getMapCycleList() {
		if (this.MapCycleList != null)
			return this.MapCycleList;
		else
			return null;
	}

	public Set<String> getTotalJoinedClients() {
		return totalJoinedClients;
	}

	public void setStorageKey(int key) {
		this.StorageKey = key;
	}

	public void setNeedsPass(Boolean NeedsPass) {
		this.requiresPass = NeedsPass;
	}

	public void killRoom() {
		this.CR.ClientsLock.writeLock().lock();
		try {
			for (StickClient SC : this.CR.getAllClients()) {
				SC.write(StickPacketMaker.getErrorPacket("5"));
				CR.deregisterClient(SC);
			}
			Main.getLobbyServer().getRoomRegistry()
					.deRegisterRoom(Main.getLobbyServer().getRoomRegistry().GetRoomFromName(Name));
		} finally {
			this.CR.ClientsLock.writeLock().unlock();
		}
	}

	public LinkedHashMap<String, StickClient> getVIPs() {
		return this.VIPs;
	}

	public List<Integer> getBlackList() {
		return blacklist;
	}

	public void moderatorKick(StickClient victim) {
		if (!victim.getModStatus()) {
			blacklist.add(victim.getDbID());
		}
		this.CR.deregisterClient(victim);
		victim.write(StickPacketMaker.getKickedPacket());
	}

	public void addVoteKick(StickClient target, StickClient voter) {
		if (!target.equals(lastKickTarget)) {
			if (target.getModStatus()) {
				return; // Mods should not be kicked
			}
			kickVoters.clear();
			lastKickTarget = target;
		}
		kickVoters.add(voter);
		for (StickClient kickVoter : kickVoters) {
			if (!CR.getAllClients().contains(kickVoter)) {
				kickVoters.remove(kickVoter);
			}
		}
		if (kickVoters.size() > 1 && kickVoters.size() >= (CR.getAllClients().size() - 1)) {
			blacklist.add(target.getDbID());
			target.write(StickPacketMaker.getKickedPacket());
			this.CR.deregisterClient(target);
			kickVoters.clear();
		}
	}

	/*
	 * public RoomTimer getStickRoomTimer() { return this.Timer; }
	 */

	class OnTimedEvent implements Runnable {
		public void run() {
			if (CR.getAllClients().isEmpty()) {
				Main.getLobbyServer().getRoomRegistry()
						.deRegisterRoom(Main.getLobbyServer().getRoomRegistry().GetRoomFromName(Name));
				updateJoinedClients();
				Thread.currentThread().interrupt();
				return;
			}

			if (RoundTime > 0)
				RoundTime = (RoundTime - 1);
			else
				RoundTime = 270;
			if (RoundTime == 1) {
				RoundTime = 300;
				updateStats(getWinner());
			}

		}

		private void updateJoinedClients() {
			try {
				for (String s : totalJoinedClients) {
					PreparedStatement ps = DatabaseTools.getDbConnection()
							.prepareStatement("UPDATE `users` SET `rounds` = `rounds` + 1 WHERE `username` = ?");
					ps.setString(1, s);
					ps.executeUpdate();
					StickClient c = Main.getLobbyServer().getClientRegistry().getClientfromName(s);
					if(c!=null) {
						c.incrementRounds();
						PlayerCommandHandler.updatePlayer(c);
					}
				}
			} catch (SQLException e) {
				System.out.println("Problem updating user rounds stat.");
			}

		}

		private StickClient getWinner() {
			blacklist.clear();
			int mostKills = -1;
			StickClient tempWinner = null;
			CR.ClientsLock.readLock().lock();

			try {
				for (StickClient c : CR.getAllClients()) {
					System.out.println(c.getName() + " : " + c.getGameKills());
					if (mostKills == -1) {
						tempWinner = c;
						mostKills = c.getGameKills();
						continue;
					}
					if ((c != null) && (c.getGameKills() > mostKills)) {
						tempWinner = c;
						mostKills = c.getGameKills();
					}
				}
			} finally {
				CR.ClientsLock.readLock().unlock();
			}
			if (tempWinner == null) {
				System.out.println("Winner was null - setting winner as blank client");
				return new StickClient();
			}
			tempWinner.incrementGameWins();
			return tempWinner;
		}

		private void updateStats(StickClient winner) {
			CR.ClientsLock.readLock().lock();
			try {
				List<StickClient> winners = new ArrayList<StickClient>();
				List<StickClient> realWinners = winners;
				for (StickClient c : CR.getAllClients()) {
					try {
						if (!c.getQuickplayStatus() && (c.getDbID() != -1)) {
							if (c.equals(winner)) {
								winners.add(winner);
								// win = 1;
							} else {
								// if somebody is loser but has same kills with equal or lesser deaths make them
								// winner too
								if (c.getGameKills() >= winner.getGameKills()
										&& c.getGameDeaths() <= winner.getGameDeaths()) {
									winners.add(c);
									// win = 1;
								}
							}
						}

					} catch (Exception e) {
						System.out.println("There was an error updating winner status for user " + c.getName());
					}
				}
				for (StickClient w : winners) {
					try {
						for (StickClient x : winners) {
							if (w.getGameDeaths() > x.getGameDeaths()) {
								if (realWinners.contains(w)) {
									realWinners.remove(w);
									;
								}
							}
						}
					} catch (Exception e) {

					}
				}

				for (StickClient c : CR.getAllClients()) {
					try {
						int win = 0;
						int loss = 0;
						if (realWinners.contains(c)) {
							win = 1;
						} else {
							loss = 1;
						}
						PreparedStatement ps = DatabaseTools.getDbConnection()
								.prepareStatement("UPDATE `users` SET `kills` = `kills` + ?, `deaths` = `deaths` + ?, "
										+ "`wins` = `wins` + ?, `losses` = `losses` + ? WHERE `UID` = ?");
						ps.setInt(1, c.getGameKills());
						ps.setInt(2, c.getGameDeaths());
						ps.setInt(3, win);
						ps.setInt(4, loss);
						ps.setInt(5, c.getDbID());
						ps.executeUpdate();

						System.out.println("Updated stats for user: " + c.getName());

					} catch (Exception e) {
						System.out.println("There was an error updating round stats for user " + c.getName());
						System.out.println(e);
					}
				}
				updateJoinedClients();
				totalJoinedClients.clear();
				for (StickClient c : CR.getAllClients()) {
					try {
						totalJoinedClients.add(c.getName());
						c.setGameKills(0);
						c.setGameDeaths(0);
					} catch (Exception e) {
						System.out.println("There was an error resetting everybodys game stats");
					}
				}

			} finally {
				CR.ClientsLock.readLock().unlock();
			}
		}
	}
}
