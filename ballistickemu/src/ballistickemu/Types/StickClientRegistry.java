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

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ballistickemu.Main;
import ballistickemu.Tools.StickPacketMaker;

/**
 *
 * @author Simon
 */
public class StickClientRegistry {
	private static final Logger LOGGER = LoggerFactory.getLogger(StickClientRegistry.class);

	private LinkedHashMap<String, StickClient> Clients;
	private Boolean isLobby;
	public ReentrantReadWriteLock ClientsLock = new ReentrantReadWriteLock(true);
	private static Set<String> spyList = new HashSet<>();;

	public StickClientRegistry(Boolean lobby) {

		Clients = new LinkedHashMap<String, StickClient>();
		isLobby = lobby;
	}

	public LinkedHashMap<String, StickClient> getClientsList() {
		return Clients;
	}

	public void registerClient(StickClient client) {
		this.ClientsLock.writeLock().lock();
		try {
			Clients.put(client.getUID(), client);
		} finally {
			this.ClientsLock.writeLock().unlock();
		}
	}

	public void deregisterClient(StickClient client) {
		if (client.getLobbyStatus() && this.isLobby) {
			// only allow access if another thread isn't reading the resource
			this.ClientsLock.writeLock().lock();
			try {
				this.Clients.remove(client.getUID());
			} finally {
				this.ClientsLock.writeLock().unlock();
			}
			LOGGER.info("User " + client.getName() + " being deregistered from main registry.");
			Main.getLobbyServer().BroadcastPacket(StickPacketMaker.Disconnected(client.getUID()));
		} else if (client.getRoom() != null) {
			StickRoom room = client.getRoom();
			if (room.getCreatorName().equals(client.getName())) {
				if (room.usesCustomMap()) {
					for (StickClient c : room.GetCR().Clients.values()) {
						if (!c.getName().equals(client.getName())) {
							c.write(StickPacketMaker.getErrorPacket("2"));
							room.GetCR().deregisterClient(c);
							try {
								Thread.sleep(100); // Clients don't properly update without waiting somehow
							} catch (InterruptedException e) {
								LOGGER.warn("Failed to wait to kick remaining players in a custom map room.");
							}
						}
					}
				} else if (room.getNeedsPass()) {
					for (StickClient c : room.GetCR().Clients.values()) {
						if (!c.getPass()) {
							c.write(StickPacketMaker.getErrorPacket("2"));
							room.GetCR().deregisterClient(c);
							try {
								Thread.sleep(100); // Clients don't properly update without waiting somehow
							} catch (InterruptedException e) {
								LOGGER.warn("Failed to wait to kick remaining non-VIP players.");
							}
						}
					}
				}

			}

			// only allow access if another thread isn't reading the resource
			room.GetCR().ClientsLock.writeLock().lock();
			try {
				room.GetCR().Clients.remove(client.getUID());
			} finally {
				room.GetCR().ClientsLock.writeLock().unlock();
			}
			client.setLobbyStatus(true);
			room.BroadcastToRoom(StickPacketMaker.Disconnected(client.getUID()));
		}
	}

	public StickClient getClientfromUID(String UID) {
		this.ClientsLock.readLock().lock();
		try {
			return Clients.get(UID);
		} catch (Exception e) {
			return null;
		} finally {
			this.ClientsLock.readLock().unlock();
		}
	}

	public StickClient getClientfromName(String Name) {
		this.ClientsLock.readLock().lock();
		try {
			for (StickClient SC : this.getAllClients()) {

				if ((SC.getName() != null) && SC.getName().equalsIgnoreCase(Name)) {
					return SC;
				}
			}
		} finally {
			this.ClientsLock.readLock().unlock();
		}
		return null;
	}

	public Collection<StickClient> getAllClients() {
		return Clients.values();
	}

	public boolean UIDExists(String UID) {
		this.ClientsLock.readLock().lock();
		try {
			return Clients.containsKey(UID);
		} finally {
			this.ClientsLock.readLock().unlock();
		}
	}

	public StickClient getFirstClient() {
		this.ClientsLock.readLock().lock();
		try {
			for (StickClient c : this.getAllClients()) {
				if (this.isLobby) {
					if (c.getLobbyStatus())
						return c;
				} else {
					return c;
				}

			}
		} finally {
			this.ClientsLock.readLock().unlock();
		}
		return null;
	}

	public static Set<String> getSpyList() {
		return spyList;
	}

}
