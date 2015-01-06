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
package br.ufc.mdcc.mpos.net.endpoint.service;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ConnectException;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;
import br.ufc.mdcc.mpos.MposFramework;
import br.ufc.mdcc.mpos.net.Protocol;
import br.ufc.mdcc.mpos.net.core.ClientAbstract;
import br.ufc.mdcc.mpos.net.core.ClientTcp;
import br.ufc.mdcc.mpos.net.core.FactoryClient;
import br.ufc.mdcc.mpos.net.endpoint.EndpointController;
import br.ufc.mdcc.mpos.net.endpoint.EndpointType;
import br.ufc.mdcc.mpos.net.endpoint.ServerContent;
import br.ufc.mdcc.mpos.net.exceptions.MissedEventException;

/**
 * This is a client implementation was used for discovery services in remote machine,
 * under TCP protocol.
 * 
 * @author Philipp B. Costa
 */
public final class DiscoveryService extends Thread {
	private final String clsName = DiscoveryService.class.getName();

	private ServerContent server;
	private int servicePort;
	private String appVersion;
	private String appName;

	private volatile boolean stopTask;
	
	public DiscoveryService(ServerContent server) {
		this.server = server;
		
		servicePort = 30015;
		stopTask = false;
		appName = MposFramework.getInstance().getDeviceController().getAppName();
		appVersion = MposFramework.getInstance().getDeviceController().getAppVersion();
	}

	@Override
	public void run() {
		boolean foundService = false;
		String requestMessage = "mpos_serv_req_droid:" + appName + ":" + appVersion;
		
		while(!stopTask && !foundService){
			Log.i(clsName, "# Started Discovery Service for endpoint: " + server.getType() + " and app: " + appName + "/" + appVersion);
			foundService = true;

			ClientTcp client = (ClientTcp) FactoryClient.getInstance(Protocol.TCP_STREAM);
			try {
				if (server.getType() == EndpointType.SECONDARY_SERVER && !MposFramework.getInstance().getDeviceController().isOnline()) {
					throw new ConnectException("The mobile is completly offline!");
				}

				client.connect(server.getIp(), servicePort);

				send(client.getOutputStream(), requestMessage);
				receive(client.getInputStream());
				
			} catch (IOException e) {
				Log.w(clsName, "Any problem with I/O in Discovery System: " + server.getType(), e);
				foundService = false;
			} catch (MissedEventException e) {
				Log.e(clsName, "Didn't TCP Manual?", e);
				foundService = false;
			} catch (JSONException e) {
				Log.e(clsName, "Any problem with json processing!", e);
				foundService = false;
			} finally {
				if (!stopTask && !foundService) {
					Log.i(clsName, ">> Retry Discovery Service for endpoint: " + server.getType() + ", in " + EndpointController.REPEAT_DISCOVERY_TASK + " ms");
					try {
						Thread.sleep(EndpointController.REPEAT_DISCOVERY_TASK);
					} catch (InterruptedException e) {
						// sent canceled timer because app and mpos api finished!
					}
				} else {
					Log.i(clsName, ">> Finished Discovery Service for endpoint: " + server.getType() + " on " + server.getIp());
					MposFramework.getInstance().getEndpointController().startDecisionMaker(server);
				}
				close(client);
			}
		}
	}

	private void send(OutputStream os, String mposServiceResquest) throws IOException {
		os.write(mposServiceResquest.getBytes(), 0, mposServiceResquest.length());
		os.flush();
	}

	//receive with minimalist protocol!
	private void receive(InputStream is) throws IOException, JSONException {
	    byte[] dataBuffer = new byte[512];
	    int read = is.read(dataBuffer);
	    
		JSONObject resp = new JSONObject(new String(dataBuffer, 0, read));
		processJson(server, resp);
		stopTask();
	}

	private void processJson(ServerContent server, JSONObject data) throws JSONException {
		if (server.setJsonToPorts(data)) {
			MposFramework.getInstance().getEndpointController().deployService(server);
		}
	}

	private void close(ClientAbstract client) {
		try {
			client.close();
		} catch (IOException e) {
			Log.e(clsName, "Any problem with I/O close", e);
		}
	}
	
	public void stopTask(){
		stopTask = true;
	}
}