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
package br.ufc.mdcc.mpos.net;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;

import br.ufc.mdcc.mpos.net.util.Service;

/**
 * This is a multithreaded general TCP server implementation.
 * 
 * @author Philipp B. Costa
 */
public abstract class TcpServer extends AbstractServer {
	private ServerSocket serverSocket;
	
	public TcpServer(String cloudletIp, Service service, Class<?> cls) {
		super(cloudletIp, service, cls);
	}

	@Override
	public void run() {
		logger.info(startMessage);
		try {
			//bind port with 50 queue waiting
			serverSocket = new ServerSocket(getService().getPort(), 50, InetAddress.getByName(ip));
			while (true) {
				final Socket connection = serverSocket.accept();
				if (connection != null) {
					// processa cada cliente em paralelo
					new Thread() {
						@Override
						public void run() {
							try {
								clientRequest(connection);

							} catch (SocketTimeoutException e) {
								logger.info("Socket timeout!", e);
							} catch (SocketException e) {
								logger.info("Any problem with I/O socket", e);
							} catch (IOException e) {
								logger.info("The generic I/O problem", e);
							} finally {
								close(connection);
							}
						}
					}.start();
				}
			}
		} catch (IOException e) {
			logger.info("Any I/O Exception on server socket level!", e);
		} finally {
			try {
				serverSocket.close();
			} catch (IOException e) {
			}
		}
	}

	/**
	 * Each client request is processed on exclusive thread at close the socket.
	 * 
	 * @param socket client accept from <ServerSocket> bind.
	 */
	public abstract void clientRequest(Socket connection) throws IOException;
}