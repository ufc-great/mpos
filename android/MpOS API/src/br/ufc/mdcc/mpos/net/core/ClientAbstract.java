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

import br.ufc.mdcc.mpos.net.exceptions.MissedEventException;

/**
 * Abstract Socket Client for TCP and UDP Protocols
 * 
 * @author Philipp B. Costa
 */
public abstract class ClientAbstract {
	protected final int BUFFER = 4 * 1024;
	protected ReceiveDataEvent event;
	protected Thread thread;
	protected String threadName;

	public abstract void connect(String ip, int porta) throws IOException, MissedEventException;

	public abstract void close() throws IOException;

	protected abstract void receive() throws IOException, ClassNotFoundException;

	public abstract void send(byte data[]) throws IOException;

	public ClientAbstract() {
	}

	protected void startThreadListening() throws IOException {
		if (thread != null) {
			close();
		}

		thread = new Thread() {
			@Override
			public void run() {
				try {
					receive();
				} catch (Exception e) {
					// quando o servidor ou cliente fechar o socket
				}
			}
		};

		thread.setName(threadName);
		thread.start();
	}

	public void setReceiveDataEvent(ReceiveDataEvent event) {
		if (event == null) {
			throw new NullPointerException();
		}
		this.event = event;
	}
}