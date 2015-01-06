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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import br.ufc.mdcc.mpos.net.TcpServer;
import br.ufc.mdcc.mpos.net.util.Service;
import br.ufc.mdcc.mpos.util.TimeClientManage;

/**
 * This server implementation was used for retrieve jitter results using
 * TCP protocol.
 * 
 * @author Philipp B. Costa
 */
public final class JitterRetrieveTcpServer extends TcpServer {
	public JitterRetrieveTcpServer(String cloudletIp, Service service) {
		super(cloudletIp, service, JitterRetrieveTcpServer.class);
		startMessage = "Jitter Retrieve TCP started on port: " + service.getPort();
	}

	@Override
	public void clientRequest(Socket connection) throws IOException {
		OutputStream output = connection.getOutputStream();
		InputStream input = connection.getInputStream();

		byte tempBuffer[] = new byte[1024 * 4];
		String remoteIp = connection.getInetAddress().toString();

		int read = 0;
		while ((read = input.read(tempBuffer)) != -1) {
			String data = new String(tempBuffer, 0, read);
			if (data.contains("get")) {
				List<Long> times = TimeClientManage.getInstance().getTimeResults(remoteIp);

				if (times != null) {
					byte resp[] = calculeJitter(times);
					output.write(resp, 0, resp.length);
				} else {
					String resp = "bug";
					output.write(resp.getBytes(), 0, resp.length());
				}
				output.flush();
				break;
			}
			Arrays.fill(tempBuffer, (byte) 0);
		}
		close(output);
		close(input);
	}

	private byte[] calculeJitter(List<Long> times) {
	    List<Long> timeInterval = new ArrayList<Long>(times.size() - 1);

		long timeReference = times.get(0);
		int tam = times.size();

		for (int i = 1; i < tam; i++) {
			timeInterval.add(times.get(i) - timeReference);
			timeReference = times.get(i);
		}

		List<Long> jitterInterval = new ArrayList<Long>(timeInterval.size() - 1);
		timeReference = timeInterval.get(0);
		tam = timeInterval.size();

		long jitterTotal = 0L;
		for (int i = 1; i < tam; i++) {
			long jitter = timeInterval.get(i) - timeReference;

			jitter = (jitter < 0) ? jitter *= (-1) : jitter;

			jitterTotal += jitter;
			jitterInterval.add(jitter);
			timeReference = timeInterval.get(i);
		}

		// TODO: Only average is implemented!
		// Collections.sort(jitterInterval);
		// long jitterMin = Collections.min(jitterInterval);
		// long jitterMax = Collections.max(jitterInterval);
		long jitterMed = jitterTotal / (long) jitterInterval.size();

		StringBuilder result = new StringBuilder();
		result.append(jitterMed);

		return result.toString().getBytes();
	}
}