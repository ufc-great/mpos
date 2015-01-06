/*******************************************************************************
 * Copyright (C) 2014 Philipp B. Costa
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package br.ufc.mdcc.mpos.net.util;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketException;

import br.ufc.mdcc.mpos.net.AbstractServer;

/**
 * This server implementation was used for discovery cloudlet, under 
 * UDP Multicast protocol. The server response is the unicast response
 * using the default port (31001) on UDP protocol.
 * 
 * @author Philipp B. Costa
 */
public final class DiscoveryMulticastService extends AbstractServer {
	// used for unicast reply message
	private final int replyPort = 31001;
	private final String multicastIP = "230.230.230.230";
	
	private final int BUFFER = 32;

	public DiscoveryMulticastService(String ip, Service service) {
		super(ip, service, DiscoveryMulticastService.class);
		startMessage = "Discovery Cloudlet Multicast started on port: " + service.getPort();
	}

	@Override
	public void run() {
		logger.info(startMessage);
		
		MulticastSocket socket = null;
		try {
			socket = listen(InetAddress.getByName(multicastIP));
			socket.setTimeToLive(16);

			String cloudletEndpoint = "mpos_cloudlet_ip=" + ip;
			DatagramPacket cloudletRequestPacket = new DatagramPacket(new byte[BUFFER], BUFFER);
			while (true) {

				// PS: receive blocks waiting for client
				socket.receive(cloudletRequestPacket);
				String message = new String(cloudletRequestPacket.getData(), 0, cloudletRequestPacket.getLength());
				if (message.equals("mpos_cloudlet_req")) {
					logger.info("Receive request from: " + cloudletRequestPacket.getAddress() + ":" + cloudletRequestPacket.getPort());
					// sent unicast for client!
					DatagramPacket cloudletReplyPacket = new DatagramPacket(cloudletEndpoint.getBytes(), cloudletEndpoint.length(), cloudletRequestPacket.getAddress(), replyPort);
					new SentReplyMobile(socket, cloudletReplyPacket).start();
				}
			}
		} catch (SocketException e) {
			logger.info("Socket Error!", e);
		} catch (IOException e) {
			logger.info("Create a property file: 'config.properties' with follow entry (cloudlet.interface.ip=<address>)", e);
		} finally {
			if (socket != null) {
				socket.close();
			}
		}
	}

	private MulticastSocket listen(InetAddress address) throws IOException {
		MulticastSocket socket = new MulticastSocket(getService().getPort());
		socket.setInterface(InetAddress.getByName(ip));
		socket.joinGroup(address);
		return socket;
	}

	/**
	 * Send the reply message in this format: <ip>:<ask_service_port>
	 * 
	 * @author Philipp B. Costa
	 */
	private class SentReplyMobile extends Thread {
		private MulticastSocket socket;
		private DatagramPacket cloudletReplyPacket;

		public SentReplyMobile(MulticastSocket socket, DatagramPacket cloudletReplyPacket) {
			this.socket = socket;
			this.cloudletReplyPacket = cloudletReplyPacket;
		}

		@Override
		public void run() {
			try {
				// send 7 times because is UDP ;)
				for (int i = 0; i < 7; i++) {
					socket.send(cloudletReplyPacket);
					Thread.sleep(500);
				}
			} catch (InterruptedException e) {
				logger.info("Reply Thread was interrupted!", e);
			} catch (IOException e) {
				logger.info("Multicast socket send problems", e);
			}
		}
	}
}