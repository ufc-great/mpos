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
package br.ufc.mdcc.mpos.net.core;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import br.ufc.mdcc.mpos.net.exceptions.FlowUdpException;
import br.ufc.mdcc.mpos.net.exceptions.MissedEventException;

/**
 * Socket Client for UDP Protocols
 * 
 * @author Philipp B. Costa
 */
public final class ClientUdp extends ClientAbstract {
	private DatagramSocket socket;
	private InetAddress address;

	// maximum payload is 64k
	private final byte payload[] = new byte[BUFFER];

	private DatagramPacket sentPacket = null, receivePacket = null;

	ClientUdp() {
		super.threadName = "Thread_Cliente_UDP";

		sentPacket = new DatagramPacket(new byte[BUFFER], BUFFER);
		receivePacket = new DatagramPacket(new byte[BUFFER], BUFFER);
	}

	@Override
	public void connect(String ip, int porta) throws IOException, MissedEventException {
		address = InetAddress.getByName(ip);

		socket = new DatagramSocket();
		socket.connect(address, porta);

		sentPacket.setSocketAddress(socket.getRemoteSocketAddress());

		if (event == null)
			throw new MissedEventException();

		startThreadListening();
	}

	protected void receive() throws IOException, ClassNotFoundException {
		while(true){
			socket.receive(receivePacket);
			event.receive(receivePacket.getData(), receivePacket.getOffset(), receivePacket.getLength());
			clean(receivePacket);
		}
	}

	public void send(byte data[]) throws IOException {
		InputStream bis = new ByteArrayInputStream(data);

		int size = 0;
		while ((size = bis.read(payload)) > 0) {
			sentPacket.setData(payload, 0, size);

			socket.send(sentPacket);
			clean(sentPacket);

			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				throw new FlowUdpException();
			}
		}
	}

	public void close() throws IOException {
		if (socket != null) {
			socket.close();
		}
		if (thread != null) {
			thread.interrupt();
		}
	}

	private void clean(DatagramPacket packet) {
		packet.setData(new byte[BUFFER]);
	}
}