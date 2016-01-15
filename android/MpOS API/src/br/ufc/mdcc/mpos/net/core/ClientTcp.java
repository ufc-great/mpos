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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Arrays;

import br.ufc.mdcc.mpos.net.exceptions.MissedEventException;

/**
 * Socket Client for TCP Protocols
 * 
 * @author Philipp B. Costa
 */
public final class ClientTcp extends ClientAbstract {
	private Socket socket;
	private boolean threadControl;

	ClientTcp(boolean threadControl) {
		super.threadName = "Thread_Cliente_TCP";
		this.threadControl = threadControl;
	}

	ClientTcp() {
		this(true);
	}

	/**
	 * Default is start listening
	 */
	@Override
	public void connect(String ip, int port) throws IOException, MissedEventException {
		socket = new Socket(ip, port);
		socket.setSoTimeout(Integer.MAX_VALUE);

		if (threadControl) {
			if (event == null) {
				throw new MissedEventException();
			}
			startThreadListening();
		}
	}

	protected void receive() throws IOException, ClassNotFoundException {
		byte tempBuffer[] = new byte[BUFFER];// 4k de buffer

		InputStream is = socket.getInputStream();

		int read = 0;
		byte zero = (byte) 0;
		while ((read = is.read(tempBuffer)) != -1) {
			event.receive(tempBuffer, 0, read);
			Arrays.fill(tempBuffer, zero);
		}
	}

	public void send(byte data[]) throws IOException {
		socket.getOutputStream().write(data, 0, data.length);
		socket.getOutputStream().flush();
	}

	public void close() throws IOException {
		if (socket != null) {
			socket.close();
		}
		if (thread != null) {
			thread.interrupt();
		}
	}

	public OutputStream getOutputStream() throws IOException {
		return socket.getOutputStream();
	}

	public InputStream getInputStream() throws IOException {
		return socket.getInputStream();
	}
}