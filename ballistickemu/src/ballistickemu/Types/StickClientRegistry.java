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
			if(!client.isReceivingPolicy()) {
				// do not send disconnect package after the connection for the policy file gets closed
				Main.getLobbyServer().BroadcastPacket(StickPacketMaker.Disconnected(client.getUID()));
			}
		} else if (client.getRoom() != null) {
			StickRoom room = client.getRoom();
			// only allow access if another thread isn't reading the resource
			room.GetCR().ClientsLock.writeLock().lock();
			try {
				room.GetCR().Clients.remove(client.getUID());
			} finally {
				room.GetCR().ClientsLock.writeLock().unlock();
			}
			client.setLobbyStatus(true);
			room.BroadcastToRoom(StickPacketMaker.Disconnected(client.getUID()));
			
			if (room.usesCustomMap() && this.getClientfromName(room.getCreatorName())==null) {
					this.ClientsLock.writeLock().lock();
					// if the host of the custom map room leaves, kick all players
					for(StickClient c:this.getAllClients()) {
						if (!c.getLobbyStatus() && !room.getCreatorName().equals(c.getName())) {
							// Kick them one by one to avoid lobby conflicts with multiple players leaving at the same time
							c.setRequiresUpdate(true);
							c.write(StickPacketMaker.getErrorPacket("2"));
							break;
						}
					}
					this.ClientsLock.writeLock().unlock();
				} else if (room.getNeedsPass()) {
					if(client.getName().equals(room.getCreatorName())) {
						// if the creator of a vip room leaves, reset vips to initiate kicking all vips
						room.getVIPs().clear();
					}
					if(room.getVIPs().isEmpty()) {
					// if it has no vips or creator of vip match left kick remaining non lab pass players	
					this.ClientsLock.writeLock().lock();
					for(StickClient c:this.getAllClients()) {
						if (!c.getPass() && !c.getLobbyStatus()) {
							// Kick them one by one to avoid lobby conflicts with multiple players leaving at the same time
							c.setRequiresUpdate(true);
							c.write(StickPacketMaker.getErrorPacket("2"));
							break;
						}
					}	
					this.ClientsLock.writeLock().unlock();
					}
				} else if(room.isMarkedForKill()) {
					// killing of room was triggered by killroom command
					this.ClientsLock.writeLock().lock();
					for(StickClient c:this.getAllClients()) {
						if(!c.getLobbyStatus()) {
							// Kick them one by one to avoid lobby conflicts with multiple players leaving at the same time
							c.setRequiresUpdate(true);
							c.write(StickPacketMaker.getErrorPacket("5"));
							break;
						}
					}
					this.ClientsLock.writeLock().unlock();
				}
			
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
