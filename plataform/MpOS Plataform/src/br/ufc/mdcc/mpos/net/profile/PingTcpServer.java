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
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Arrays;

import br.ufc.mdcc.mpos.net.TcpServer;
import br.ufc.mdcc.mpos.net.util.Service;

/**
 * This server implementation was used for ping profile, under
 * TCP protocol.
 * 
 * @author Philipp B. Costa
 */
public final class PingTcpServer extends TcpServer {
	public PingTcpServer(String cloudletIp, Service service) {
		super(cloudletIp, service, PingTcpServer.class);
		startMessage = "Echo TCP started on port: " + service.getPort();
	}

	@Override
	public void clientRequest(Socket connection) throws IOException {
		OutputStream output = connection.getOutputStream();
		InputStream input = connection.getInputStream();

		byte tempBuffer[] = new byte[1024 * 4];
		int read = 0;
		while ((read = input.read(tempBuffer)) != -1) {
			String data = new String(tempBuffer, 0, read);
			if (data.contains("pigado")) {
				// echo
				output.write(data.getBytes(), 0, data.length());
				output.flush();
			}
			Arrays.fill(tempBuffer, (byte) 0);
		}
		close(input);
		close(output);
	}
}