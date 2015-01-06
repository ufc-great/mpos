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

import br.ufc.mdcc.mpos.net.AbstractServer;
import br.ufc.mdcc.mpos.net.util.Service;
import br.ufc.mdcc.mpos.util.TimeClientManage;

/**
 * This server implementation was used for network jitter profile, under
 * UDP protocol.
 * 
 * @author Philipp B. Costa
 */
public final class JitterUdpServer extends AbstractServer {
	private DatagramSocket requestClientSocket;

	public JitterUdpServer(String cloudletIp,Service service) {
		super(cloudletIp, service, JitterUdpServer.class);
		startMessage = "Jitter UDP started on port: " + service.getPort();
	}

	@Override
	public void run() {
		logger.info(startMessage);
		try {
			requestClientSocket = new DatagramSocket(getService().getPort(), InetAddress.getByName(ip));
			byte tempBuffer[] = new byte[1024 * 4];

			while (true) {

				// limpa pacote para receber o dado
				DatagramPacket pacote = new DatagramPacket(tempBuffer, tempBuffer.length);
				requestClientSocket.receive(pacote);
				TimeClientManage.getInstance().addTime(pacote.getAddress().toString());
			}
		} catch (IOException e) {
			logger.info("Error de Conexão", e);
		}
	}
}