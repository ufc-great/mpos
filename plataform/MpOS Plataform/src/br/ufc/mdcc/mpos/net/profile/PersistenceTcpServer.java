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
import java.util.Date;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import br.ufc.mdcc.mpos.net.TcpServer;
import br.ufc.mdcc.mpos.net.profile.model.Network;
import br.ufc.mdcc.mpos.net.profile.model.ProfileResult;
import br.ufc.mdcc.mpos.net.util.Service;
import br.ufc.mdcc.mpos.persistence.FachadeDao;
import br.ufc.mdcc.mpos.util.Device;

/**
 * This server implementation was used for persistence remote mobile results,
 * under TCP protocol.
 * 
 * @author Philipp B. Costa
 */
public final class PersistenceTcpServer extends TcpServer {
	public PersistenceTcpServer(String cloudletIp, Service service) {
		super(cloudletIp, service, PersistenceTcpServer.class);
		startMessage = "Persistence TCP started on port: " + service.getPort();
	}

	@Override
	public void clientRequest(Socket connection) throws IOException {
		OutputStream output = connection.getOutputStream();
		InputStream input = connection.getInputStream();

		byte tempBuffer[] = new byte[1024 * 4];
		while (input.read(tempBuffer) != -1) {

			String data = new String(tempBuffer);
			if (data != null && data.contains("date")) {
				persistence(data, connection.getInetAddress().toString());

				String resp = "ok";
				output.write(resp.getBytes(), 0, resp.length());
				output.flush();
			}
			Arrays.fill(tempBuffer, (byte) 0);
		}
		close(input);
		close(output);
	}

	private void persistence(String data, String ip) {
		Device device = new Device();
		Network network = null;

		try {
			JSONObject resp = new JSONObject(data);
			device.setMobileId((String) resp.get("mobileId"));
			device.setDeviceName((String) resp.get("deviceName"));
			device.setCarrier((String) resp.getString("carrier"));
			device.setLatitude((String) resp.getString("lat"));
			device.setLongitude((String) resp.getString("lon"));

			network = getJSONToNetwork(data);
		} catch (JSONException e) {
			// When no device data is available, only process the network data.
			try {
				network = getJSONToNetwork(data);
			} catch (JSONException ex) {
				logger.info("Algum erro de parser do JSON", ex);
			}
		} finally {
			if (network != null) {
				asyncPersistence(ip, device, network);
			}
		}
	}

	private Network getJSONToNetwork(String data) throws JSONException {
		JSONObject resp = new JSONObject(data);
		Network network = new Network();
		network.setDate(new Date((Long) resp.get("date")));
		network.setResultPingTcp(Network.StringToLongArray(((JSONArray) resp.get("tcp")).toString()));
		network.setResultPingUdp(Network.StringToLongArray(((JSONArray) resp.get("udp")).toString()));
		network.setLossPacket((Integer) resp.get("loss"));
		network.setJitter((Integer) resp.get("jitter"));
		network.setBandwidthDownload((String) resp.get("down"));
		network.setBandwidthUpload((String) resp.get("up"));
		network.setType((String) resp.getString("net"));
		network.setAppName((String) resp.get("appName"));

		return network;
	}

	private void asyncPersistence(String ip, Device device, Network network) {
		final ProfileResult result = new ProfileResult(device, network);
		result.setIp(ip);
		new Thread() {
			public void run() {
				FachadeDao.getInstance().getNetProfileDao().add(result);
			}
		}.start();
	}
}