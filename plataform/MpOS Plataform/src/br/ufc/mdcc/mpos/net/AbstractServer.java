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
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import org.apache.log4j.Logger;

import br.ufc.mdcc.mpos.net.util.Service;

/**
 * This is a general server implementation.
 * 
 * @author Philipp B. Costa
 */
public abstract class AbstractServer extends Thread {
	protected Logger logger;
	
	private Service service;
	protected String ip;
	
	protected String startMessage;

	public AbstractServer(String ip, Service service, Class<?> cls) {
		super(service.getName());

		this.logger = Logger.getLogger(cls);
		this.service = service;
		this.ip = ip;
	}

	public Service getService() {
		return service;
	}

	protected void close(OutputStream os) {
		try {
			if (os != null) {
				os.close();
			}
		} catch (IOException e) {
			logger.info("IOException was thrown during close Stream");
		}
	}

	protected void close(Socket socket) {
		try {
			if (socket != null) {
				socket.close();
			}
		} catch (IOException e) {
			logger.info("IOException was thrown during close Socket");
		}
	}

	protected void close(InputStream is) {
		try {
			if (is != null) {
				is.close();
			}
		} catch (IOException e) {
			logger.info("IOException was thrown during close Stream");
		}
	}
}