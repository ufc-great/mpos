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
package br.ufc.mdcc.mpos.net.endpoint;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * The server content include a ip and all ports discovered in remotable endpoint.
 * 
 * @author Philipp B. Costa
 */
public final class ServerContent {
	private String ip;
	private final EndpointType type;

	private int pingTcpPort;
	private int pingUdpPort;
	private int jitterTestPort;
	private int jitterRetrieveResultPort;
	private int bandwidthPort;
	private int saveProfilePort;

	private int deployAppPort;
	private int rpcServicePort;
	
	public ServerContent(EndpointType type) {
		this.type = type;
		rpcServicePort = -1;
	}

	public int getPingTcpPort() {
		return pingTcpPort;
	}

	public void setPingTcpPort(int pingTcpPort) {
		this.pingTcpPort = pingTcpPort;
	}

	public int getPingUdpPort() {
		return pingUdpPort;
	}

	public void setPingUdpPort(int pingUdpPort) {
		this.pingUdpPort = pingUdpPort;
	}

	public int getJitterTestPort() {
		return jitterTestPort;
	}

	public void setJitterTestPort(int jitterTestPort) {
		this.jitterTestPort = jitterTestPort;
	}

	public int getJitterRetrieveResultPort() {
		return jitterRetrieveResultPort;
	}

	public void setJitterRetrieveResultPort(int jitterRetrieveResultPort) {
		this.jitterRetrieveResultPort = jitterRetrieveResultPort;
	}

	public int getBandwidthPort() {
		return bandwidthPort;
	}

	public void setBandwidthPort(int bandwidthPort) {
		this.bandwidthPort = bandwidthPort;
	}

	public int getSaveProfilePort() {
		return saveProfilePort;
	}

	public void setSaveProfilePort(int saveProfilePort) {
		this.saveProfilePort = saveProfilePort;
	}

	public int getRpcServicePort() {
		return rpcServicePort;
	}

	public void setRpcServicePort(int rpcServicePort) {
		this.rpcServicePort = rpcServicePort;
	}

	public int getDeployAppPort() {
		return deployAppPort;
	}

	public void setDeployAppPort(int deployAppPort) {
		this.deployAppPort = deployAppPort;
	}

	public EndpointType getType() {
		return type;
	}

	/**
	 * Transform JSON server response in ports attributes!
	 * 
	 * @param ports in json format
	 * @return true if need deploy rpc service
	 * @throws JSONException
	 */
	public synchronized boolean setJsonToPorts(JSONObject ports) throws JSONException {
		setPingTcpPort(ports.getInt("ping_tcp"));
		setPingUdpPort(ports.getInt("ping_udp"));
		setJitterTestPort(ports.getInt("jitter_test"));
		setJitterRetrieveResultPort(ports.getInt("jitter_retrieve_results"));
		setBandwidthPort(ports.getInt("bandwidth"));
		setSaveProfilePort(ports.getInt("save_profile_results"));
		setDeployAppPort(ports.getInt("deploy_android_app"));
		setRpcServicePort(ports.getInt("rpc_serv"));

		return getRpcServicePort() == -1;
	}

	public synchronized String getIp() {
		return ip;
	}

	public synchronized void setIp(String ip) {
		this.ip = ip;
	}

	public synchronized boolean isReady() {
		return ip != null && rpcServicePort > -1;
	}

	public synchronized void clean() {
		pingTcpPort = 0;
		pingUdpPort = 0;
		jitterTestPort = 0;
		jitterRetrieveResultPort = 0;
		bandwidthPort = 0;
		saveProfilePort = 0;
		deployAppPort = 0;
		rpcServicePort = -1;
	}
	
	public ServerContent newInstance(){
	    ServerContent instance = new ServerContent(getType());
	    instance.setBandwidthPort(getBandwidthPort());
	    instance.setDeployAppPort(getDeployAppPort());
	    instance.setIp(getIp());
	    instance.setJitterRetrieveResultPort(getJitterRetrieveResultPort());
	    instance.setJitterTestPort(getJitterTestPort());
	    instance.setPingTcpPort(getPingTcpPort());
	    instance.setPingUdpPort(getPingUdpPort());
	    instance.setRpcServicePort(getRpcServicePort());
	    instance.setSaveProfilePort(getSaveProfilePort());
	    
	    return instance;
	}

	@Override
	public String toString() {
		return "Type: " + type + ", ip=" + ip + ":" + rpcServicePort;
	}
}