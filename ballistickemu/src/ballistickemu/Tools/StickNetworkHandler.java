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

import java.util.Calendar;
import java.util.TimeZone;

import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ballistickemu.Main;
import ballistickemu.Game.PacketHandlerGame;
import ballistickemu.Lobby.PacketHandlerLobby;
import ballistickemu.Types.StickClient;

public class StickNetworkHandler extends IoHandlerAdapter {
	private static final Logger LOGGER = LoggerFactory.getLogger(StickNetworkHandler.class);

	public StickNetworkHandler() {
	}

	@Override
	public void sessionOpened(IoSession session) throws Exception {
		int rowCount = -1;
		LOGGER.info("IoSession with {} opened at {}", session.getRemoteAddress(),
				Calendar.getInstance(TimeZone.getDefault()).getTime().toString());
		if (session.getRemoteAddress() == null)
			return;
		if (rowCount > 0) {
			session.close(false);
			return;
		}

		StickClient newClient = new StickClient(session,
				UIDTool.GenerateUID(Main.getLobbyServer().getClientRegistry()));
		session.setAttribute(StickClient.CLIENT_KEY, newClient);
	}

	@Override
	public void exceptionCaught(IoSession session, Throwable cause) throws Exception {
	}

	@Override
	public void messageReceived(IoSession session, Object message) throws Exception {
		// System.out.println("Message received");
		String S = message.toString().trim();

		StickClient c_Client = (StickClient) session.getAttribute(StickClient.CLIENT_KEY);
		if (c_Client == null)
			return;
		if (S.equalsIgnoreCase("<policy-file-request/>")) {
			c_Client.writePolicyFile();
			return;
		}

		String fix = S + "\0";
		if (S.length() > 1) {
			if (c_Client.getLobbyStatus() || (S.substring(0, 2).equalsIgnoreCase("03")
					|| (c_Client.getName() == null && c_Client.getLobbyStatus()))) {
				// Program.LS.GetLobbyThreadPool().QueueWorkItem(new
				// Amib.Threading.WorkItemCallback(PacketHandlerLobby.HandlePacket), new
				// PacketData(fix, c_Client));
				PacketHandlerLobby.HandlePacket(fix, c_Client);
			} else if (!c_Client.getLobbyStatus()) {
				// Program.LS.GetLobbyThreadPool().QueueWorkItem(new
				// Amib.Threading.WorkItemCallback(PacketHandlerGame.HandlePacket), new
				// PacketData(fix, c_Client));
				// handle in game
				PacketHandlerGame.HandlePacket(fix, c_Client);
			}
		}
	}

	@Override
	public void sessionIdle(IoSession session, IdleStatus status) throws Exception {
		if (session.getIdleCount(status) > 50)
			session.close(true);
	}

	@Override
	public void sessionClosed(IoSession session) throws Exception {
		synchronized (session) {
			StickClient c_Client = (StickClient) session.getAttribute(StickClient.CLIENT_KEY);
			if (c_Client == null)
				return;

			if (c_Client.getLobbyStatus()) {
				Main.getLobbyServer().getClientRegistry().deregisterClient(c_Client);
			} else if (c_Client.getRoom() != null) {
				c_Client.getRoom().GetCR().deregisterClient(c_Client);
				Main.getLobbyServer().getClientRegistry().deregisterClient(c_Client);

			}

			/*
			 * try { c_Client.finalize(); } catch (Throwable t) {}
			 */
		}
	}
}
