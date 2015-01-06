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

import java.util.Timer;

import android.content.Context;
import android.net.ConnectivityManager;
import android.util.Log;
import br.ufc.mdcc.mpos.MposFramework;
import br.ufc.mdcc.mpos.net.endpoint.service.DiscoveryCloudletMulticast;
import br.ufc.mdcc.mpos.net.endpoint.service.DiscoveryService;
import br.ufc.mdcc.mpos.net.exceptions.NetworkException;
import br.ufc.mdcc.mpos.net.rpc.deploy.DeployService;
import br.ufc.mdcc.mpos.net.rpc.model.RpcProfile;
import br.ufc.mdcc.mpos.offload.DynamicDecisionSystem;
import br.ufc.mdcc.mpos.util.Util;

/**
 * This class control many cycle-life services: 
 * - Discovery Service in local or remotable servers on internet or intranet.
 * - Deploy mobile app dependences in remote service.
 * - Decision Maker select.
 * - Etc.   
 * 
 * @author Philipp B. Costa
 */
public final class EndpointController {
	private final String clsName = EndpointController.class.getName();

	public static final int REPEAT_DISCOVERY_TASK = 20 * 1000;
	public static final int REPEAT_DECISION_MAKER = 35 * 1000;

	private Context context;

	private ServerContent secondaryServer;
	private ServerContent cloudletServer;

	// used for rediscovery services before discovery and using
	// help on mobility user!
	private DiscoveryService discoverySecondaryServer = null;
	private DiscoveryService discoveryCloudletServer = null;
	private DiscoveryCloudletMulticast discoveryCloudletMulticast = null;

	private Timer decisionMakerTimer;
	private DynamicDecisionSystem dynamicDecisionSystem;
	private boolean decisionMakerActive;
	private boolean remoteAdvantageExecution;

	public RpcProfile rpcProfile = new RpcProfile();

	public EndpointController(Context context, String internetIp, boolean decisionMakerActive, boolean discoveryCloudlet) throws NetworkException {
	    this.context = context;
        this.decisionMakerActive = decisionMakerActive;
	    
	    secondaryServer = new ServerContent(EndpointType.SECONDARY_SERVER);
		cloudletServer = new ServerContent(EndpointType.CLOUDLET);
		remoteAdvantageExecution = false;
        
        if(internetIp != null){
            setSecondaryServerIp(internetIp);
            discoveryServiceSecondary();
        }
        if(discoveryCloudlet){
            discoveryCloudletMulticast();
        }
	}

	private void discoveryServiceSecondary() {
		if(discoverySecondaryServer == null){
			secondaryServer.clean();
			discoverySecondaryServer = new DiscoveryService(secondaryServer);
			discoverySecondaryServer.setName("Discovery Internet Server");
			discoverySecondaryServer.start();
		}
	}

	private void setSecondaryServerIp(String ip) throws NetworkException {
		if (!Util.validateIpAddress(ip)) {
			throw new NetworkException("Invalid IP Address");
		}
		secondaryServer.setIp(ip);
	}

	public ServerContent getSecondaryServer() {
		return secondaryServer;
	}

	public ServerContent checkSecondaryServer() {
		if (MposFramework.getInstance().getDeviceController().isOnline() && secondaryServer.isReady()) {
			return secondaryServer;
		}
		return null;
	}

	private void discoveryCloudletMulticast() {
		if (context != null) {
			if (discoveryCloudletMulticast == null) {
			    cloudletServer.clean();
				discoveryCloudletMulticast = new DiscoveryCloudletMulticast(context);
				discoveryCloudletMulticast.setName("Discovery Cloudlet Multicast");
				discoveryCloudletMulticast.start();
			}
		}
	}

	public void foundCloudlet(String cloudletIp) {
		Log.i(clsName, "Cloudlet address: " + cloudletIp);

		cloudletServer.setIp(cloudletIp);
		discoveryServiceCloudlet();
        shutdownRediscoveryCloudletMulticast();
	}

	private void discoveryServiceCloudlet() {
		if (discoveryCloudletServer == null) {
			discoveryCloudletServer = new DiscoveryService(cloudletServer);
			discoveryCloudletServer.setName("Discovery Cloudlet Server");
			discoveryCloudletServer.start();
		}
	}

	public ServerContent getCloudletServer() {
		return cloudletServer;
	}

	private ServerContent checkCloudletServer() {
		if (MposFramework.getInstance().getDeviceController().connectionStatus(ConnectivityManager.TYPE_WIFI) && cloudletServer.isReady()) {
			return cloudletServer;
		}
		return null;
	}

	public void rediscoveryServices(ServerContent server) {
		if (server.getType() == EndpointType.SECONDARY_SERVER) {
			discoveryServiceSecondary();
		} else if (server.getType() == EndpointType.CLOUDLET) {
			discoveryCloudletMulticast();
		}
	}

	public ServerContent selectPriorityServer(boolean cloudletPriority) {
		if (cloudletPriority) {
			ServerContent server = checkCloudletServer();
			if (server != null) {
				return server;
			}
			server = checkSecondaryServer();
			if (server != null) {
				return server;
			}
		} else {
			ServerContent server = checkSecondaryServer();
			if (server != null) {
				return server;
			}
			server = checkCloudletServer();
			if (server != null) {
				return server;
			}
		}
		return null;
	}

	public void deployService(ServerContent server) {
		new DeployService(context, server).start();
	}

	public void shutdownRediscoveryInternetServer() {
		if(discoverySecondaryServer != null){
			discoverySecondaryServer.stopTask();
			discoverySecondaryServer.interrupt();
			discoverySecondaryServer = null;
		}
	}

	public void shutdownRediscoveryCloudletServer() {
		if (discoveryCloudletServer != null) {
			discoveryCloudletServer.stopTask();
			discoveryCloudletServer.interrupt();
			discoveryCloudletServer = null;
		}
	}

	private void shutdownRediscoveryCloudletMulticast() {
		if (discoveryCloudletMulticast != null) {
			discoveryCloudletMulticast.interrupt();
			discoveryCloudletMulticast = null;
		}
	}

	private void shutdownDecisionMaker() {
		if (decisionMakerTimer != null) {
			decisionMakerTimer.cancel();
			decisionMakerTimer.purge();
			decisionMakerTimer = null;
		}
	}

	//TODO: transf decision maker in thread...
	public synchronized void startDecisionMaker(ServerContent server) {
		if (decisionMakerTimer == null && decisionMakerActive) {
			decisionMakerTimer = new Timer("Decision Maker Watch");
			dynamicDecisionSystem = new DynamicDecisionSystem(context, server);
			decisionMakerTimer.schedule(dynamicDecisionSystem, 0, REPEAT_DECISION_MAKER);
		}
	}

	public synchronized void setRemoteAdvantageExecution(boolean remoteAdvantageExecution) {
		this.remoteAdvantageExecution = remoteAdvantageExecution;
	}

	public synchronized boolean isRemoteAdvantage() {
		return remoteAdvantageExecution;
	}

	//on UML -> updateDdsEndpoint 
	public void updateDynamicDecisionSystemEndpoint(ServerContent server) {
		dynamicDecisionSystem.setServer(server);
	}

	public void destroy() {
		shutdownRediscoveryCloudletMulticast();
		shutdownRediscoveryCloudletServer();
		shutdownRediscoveryInternetServer();
		shutdownDecisionMaker();

		secondaryServer.setIp(null);
		secondaryServer.clean();
		cloudletServer.setIp(null);
		cloudletServer.clean();
	}
}