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
package br.ufc.mdcc.mpos.controller;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import br.ufc.mdcc.mpos.net.AbstractServer;
import br.ufc.mdcc.mpos.net.profile.BandwidthTcpServer;
import br.ufc.mdcc.mpos.net.profile.JitterRetrieveTcpServer;
import br.ufc.mdcc.mpos.net.profile.JitterUdpServer;
import br.ufc.mdcc.mpos.net.profile.PersistenceTcpServer;
import br.ufc.mdcc.mpos.net.profile.PingTcpServer;
import br.ufc.mdcc.mpos.net.profile.PingUdpServer;
import br.ufc.mdcc.mpos.net.rpc.RpcTcpServer;
import br.ufc.mdcc.mpos.net.rpc.deploy.DeployAppTcpServer;
import br.ufc.mdcc.mpos.net.rpc.deploy.model.RpcService;
import br.ufc.mdcc.mpos.net.util.DiscoveryMulticastService;
import br.ufc.mdcc.mpos.net.util.DiscoveryServiceTcpServer;
import br.ufc.mdcc.mpos.net.util.Service;
import br.ufc.mdcc.mpos.persistence.FachadeDao;

/**
 * Control and Manege all network services (deploy, rpc, profile and etc.)
 * of MpOS System.
 * 
 * @author Philipp B. Costa
 */
public final class ServiceController {
    private Logger logger;

    private List<AbstractServer> networkServices;
    private List<RpcTcpServer> rpcServices;
    private DiscoveryServiceTcpServer discoveryService;

    private String ip;
    private Properties properties = new Properties();

    private ServiceController() {
        logger = Logger.getLogger(ServiceController.class);
        networkServices = new ArrayList<AbstractServer>(7);
        rpcServices = new ArrayList<RpcTcpServer>();
    }

    private static class SingletonHolder {
        public static final ServiceController instance = new ServiceController();
    }

    public static ServiceController getInstance() {
        return SingletonHolder.instance;
    }

    public void start() throws IOException {
        properties.load(new FileInputStream("config.properties"));
        ip = properties.getProperty("cloudlet.interface.ip");

        loadNetworkServices(loadingAndroidProperties());
        loadRpcServices();
        configureDiscoveryService();

        startServices(properties.getProperty("service.portable.multicastdiscovery").trim().equalsIgnoreCase("on"));
    }

    private void loadNetworkServices(Map<String, Service> services) {
        networkServices.add(new PingTcpServer(ip, services.get("ping_tcp")));
        networkServices.add(new PingUdpServer(ip, services.get("ping_udp")));
        networkServices.add(new JitterUdpServer(ip, services.get("jitter_test")));
        networkServices.add(new JitterRetrieveTcpServer(ip, services.get("jitter_retrieve_results")));
        networkServices.add(new BandwidthTcpServer(ip, services.get("bandwidth")));
        networkServices.add(new PersistenceTcpServer(ip, services.get("save_profile_results")));
        networkServices.add(new DeployAppTcpServer(ip, services.get("deploy_app")));
    }

    private void loadRpcServices() {
        List<RpcService> registeredRpcServices = FachadeDao.getInstance().getRpcServiceDao().getAll();
        for (RpcService rpcService : registeredRpcServices) {
            rpcServices.add(new RpcTcpServer(ip, rpcService));
        }
    }

    private void configureDiscoveryService() {
        discoveryService = new DiscoveryServiceTcpServer(ip, new Service("discovery_service", 30015));

        try {
            JSONObject androidAppServices = new JSONObject();
            JSONObject wpAppServices = new JSONObject();

            for (AbstractServer server : networkServices) {
                androidAppServices.put(server.getService().getName(), server.getService().getPort());

                //only for wp!
                if (server instanceof DeployAppTcpServer) {
                    wpAppServices.put(properties.getProperty("service.windowsphone.deploy.name"), Integer.parseInt(properties.getProperty("service.windowsphone.deploy.port")));
                } else {
                    wpAppServices.put(server.getService().getName(), server.getService().getPort());
                }
            }

            discoveryService.saveDefaultResponse(androidAppServices, wpAppServices);
        } catch (JSONException e) {
            logger.error("JSON Malformed!", e);
        }
    }

    private void startServices(boolean discoveryMulticastSupport) {
        for (AbstractServer server : networkServices) {
            server.start();
        }
        for (RpcTcpServer rpcServer : rpcServices) {
            rpcServer.start();
        }

        if (discoveryMulticastSupport) {
            new DiscoveryMulticastService(ip, new Service("discovery_cloudlet", 31000)).start();
        }
        discoveryService.start();
    }

    public synchronized void startServiceRpc(RpcService service) {
        RpcTcpServer server = new RpcTcpServer(ip, service);
        server.start();
        rpcServices.add(server);
    }

    private Map<String, Service> loadingAndroidProperties() {
        Map<String, Service> serviceMap = new HashMap<String, Service>(10);
        serviceMap.put("ping_tcp", new Service(properties.getProperty("service.portable.pingtcp.name"), Integer.parseInt(properties.getProperty("service.portable.pingtcp.port"))));
        serviceMap.put("ping_udp", new Service(properties.getProperty("service.portable.pingudp.name"), Integer.parseInt(properties.getProperty("service.portable.pingudp.port"))));
        serviceMap.put("jitter_test", new Service(properties.getProperty("service.portable.jitter.name"), Integer.parseInt(properties.getProperty("service.portable.jitter.port"))));
        serviceMap.put("jitter_retrieve_results", new Service(properties.getProperty("service.portable.jitter.retrieve.name"), Integer.parseInt(properties.getProperty("service.portable.jitter.retrieve.port"))));
        serviceMap.put("bandwidth", new Service(properties.getProperty("service.portable.bandwidth.name"), Integer.parseInt(properties.getProperty("service.portable.bandwidth.port"))));
        serviceMap.put("save_profile_results", new Service(properties.getProperty("service.portable.saveprofile.name"), Integer.parseInt(properties.getProperty("service.portable.saveprofile.port"))));
        serviceMap.put("deploy_app", new Service(properties.getProperty("service.android.deploy.name"), Integer.parseInt(properties.getProperty("service.android.deploy.port"))));

        return serviceMap;
    }

    public List<RpcTcpServer> getRpcServices() {
        return rpcServices;
    }
}