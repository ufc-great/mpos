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
package br.ufc.mdcc.mpos.net.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import org.json.JSONException;
import org.json.JSONObject;

import br.ufc.mdcc.mpos.net.TcpServer;
import br.ufc.mdcc.mpos.net.rpc.deploy.model.RpcService;
import br.ufc.mdcc.mpos.persistence.FachadeDao;

/**
 * This server implementation was used for discovery services in remote machine,
 * under TCP protocol.
 * 
 * @author Philipp B. Costa
 */
public final class DiscoveryServiceTcpServer extends TcpServer {
	private JSONObject androidAppServices;
	private JSONObject wpAppServices;
	private final int BUFFER = 256;

	public DiscoveryServiceTcpServer(String cloudletIp, Service service) {
		super(cloudletIp, service, DiscoveryServiceTcpServer.class);
		startMessage = "Discovery Service TCP started on port: " + service.getPort();
	}

	@Override
	public void clientRequest(Socket connection) throws IOException {
		OutputStream output = connection.getOutputStream();
		InputStream input = connection.getInputStream();

		byte dataBuffer[] = new byte[BUFFER];

		// PS: in this case "-1" mean a close socket in client side
		int read = 0;
		while ((read = input.read(dataBuffer)) != -1) {
			// e.g.: mpos_serv_req_droid:Calculator:5.0.1
			String mposServiceResquest = new String(dataBuffer, 0, read).trim().toLowerCase();
			if (mposServiceResquest.startsWith("mpos_serv_req_droid")) {
				mposServiceReply(output, mposServiceResquest, "android");
			} else if (mposServiceResquest.startsWith("mpos_serv_req_wp")) {
				mposServiceReply(output, mposServiceResquest, "wp");
			}
		}

		close(input);
		close(output);
	}

	private void mposServiceReply(OutputStream output, String mposServiceResquest, String plataform) throws IOException {
		String request[] = mposServiceResquest.split(":");
		RpcService rpcService = FachadeDao.getInstance().getRpcServiceDao().get(request[1], request[2], plataform);

		JSONObject jsonData = null;
		try {
			if(plataform.equals("android")){
				jsonData = new JSONObject(androidAppServices.toString());
			}else if(plataform.equals("wp")){
				jsonData = new JSONObject(wpAppServices.toString());
			}
			jsonData.put("rpc_serv", rpcService.getPort());
			
			byte[] data = jsonData.toString().getBytes();
			
			output.write(data);
			output.flush();

		} catch (JSONException e) {
			logger.error("JSON Malformed!", e);
		}
	}

	public void saveDefaultResponse(JSONObject androidAppServices, JSONObject wpAppServices) {
		this.androidAppServices = androidAppServices;
		this.wpAppServices = wpAppServices;
	}
}