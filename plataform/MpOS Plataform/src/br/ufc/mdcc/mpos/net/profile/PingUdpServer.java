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
package br.ufc.mdcc.mpos.net.profile;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Arrays;

import br.ufc.mdcc.mpos.net.AbstractServer;
import br.ufc.mdcc.mpos.net.util.Service;

/**
 * This server implementation was used for ping profile, under
 * UDP protocol. Working in single-thread execution!
 * 
 * @author Philipp B. Costa
 */
public final class PingUdpServer extends AbstractServer {
	private DatagramSocket requestClientSocket;
	private byte tempBuffer[] = new byte[1024 * 4];
	private DatagramPacket pacote = null;

	public PingUdpServer(String cloudletIp,Service service) {
		super(cloudletIp, service, PingUdpServer.class);
		
		startMessage = "Echo UDP started on port: " + service.getPort();
		pacote = new DatagramPacket(tempBuffer, tempBuffer.length);
	}

	@Override
	public void run() {
		logger.info(startMessage);
		try {
			requestClientSocket = new DatagramSocket(getService().getPort(), InetAddress.getByName(ip));
			while (true) {
				pacoteClean();
				requestClientSocket.receive(pacote);
				requestClientSocket.send(pacote);
			}
		} catch (IOException e) {
			logger.info("Error de Conexão", e);
		}
	}

	private void pacoteClean() {
		Arrays.fill(tempBuffer, (byte) 0);
		pacote.setAddress(null);
		pacote.setData(tempBuffer);
		pacote.setPort(0);
	}
}