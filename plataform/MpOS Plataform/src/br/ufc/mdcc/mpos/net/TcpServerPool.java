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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import br.ufc.mdcc.mpos.net.rpc.util.ClassLoaderException;
import br.ufc.mdcc.mpos.net.rpc.util.DynamicClassLoaderEvent;
import br.ufc.mdcc.mpos.net.util.Service;

/**
 * This is a general TCP server implementation, using thread pool 
 * with cache system. This implementation speedup short-live connections 
 * used by real-time application in rpc subsystem.
 * 
 * @author Philipp B. Costa
 */
public abstract class TcpServerPool extends AbstractServer {
	private ServerSocket requestClientSocket;
	private ExecutorService threadPool;

	private DynamicClassLoaderEvent dynamicClassLoaderEvent = null;
	
	public TcpServerPool(String cloudletIp, Service service, Class<?> cls) {
	    super(cloudletIp, service, cls);
        threadPool = Executors.newCachedThreadPool();
	}

	@Override
	public void run() {
		logger.info(startMessage);
		try {
		    if(dynamicClassLoaderEvent != null){
		        dynamicClassLoaderEvent.setCurrentClassLoader(currentThread().getContextClassLoader());
	        }
		    
			requestClientSocket = new ServerSocket(getService().getPort(), 50, InetAddress.getByName(ip));
			while (true) {
				final Socket connection = requestClientSocket.accept();
				if (connection != null) {
					threadPool.submit(new ClientThread(connection));
				}
			}
		} catch (IOException e) {
			logger.error("Any I/O Exception on server socket level!", e);
		} catch (ClassLoaderException e) {
		    logger.error("Some problem on thread ClassLoader!", e);
        } finally {
			try {
				requestClientSocket.close();
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
	
	/**
	 * Client Thread used on cache system.
	 * 
	 * @author Philipp B. Costa
	 */
	private final class ClientThread implements Runnable {
		private Socket connection;

		private ClientThread(Socket connection) {
			this.connection = connection;
		}

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
	}
	
	protected void setDynamicClassLoaderEvent(DynamicClassLoaderEvent dynamicClassLoaderEvent) {
        this.dynamicClassLoaderEvent = dynamicClassLoaderEvent;
    }
}