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
using System.Net;
using Ufc.MpOS.Net.Endpoint.Service;
using Ufc.MpOS.Net.Rpc.Deploy;
using Ufc.MpOS.Net.Rpc.Model;
using Ufc.MpOS.Offload;
using Ufc.MpOS.Util.Device;

namespace Ufc.MpOS.Net.Endpoint
{
	public sealed class EndpointController
	{
		private readonly object mutex = new object();

		public static readonly int REPEAT_DISCOVERY_TASK = 20 * 1000;
		public static readonly int REPEAT_DECISION_MAKER = 35 * 1000;

		public static RpcProfile rpcProfile = new RpcProfile();

		public EndpointController(string secondaryEndpoint, bool decisionMakerActive, bool discoveryCloudlet)
		{
			this.decisionMakerActive = decisionMakerActive;
			
			SecondaryServer = new ServerContent(EndpointType.SECONDARY_SERVER);
			CloudletServer = new ServerContent(EndpointType.CLOUDLET);
			remoteAdvantage = false;

			if (secondaryEndpoint != null)
			{
				SecondaryServerIp(secondaryEndpoint);
				DiscoveryServiceSecondary();
			}
			if (discoveryCloudlet)
			{
				DiscoveryCloudletMulticast();
			}
		}

		public ServerContent SecondaryServer { get; private set; }
		public ServerContent CloudletServer { get; private set; }

		private DiscoveryCloudletMulticast discoveryCloudletMulticast = null;
		private DiscoveryService discoveryCloudletServer = null;
		private DiscoveryService discoveryInternetServer = null;

		private DynamicDecisionSystem dynamicDecisionSystem = null;
		private bool decisionMakerActive;

		private bool remoteAdvantage;
		public bool RemoteAdvantage
		{
			get
			{
				lock (mutex)
				{
					return remoteAdvantage;
				}
			}
			set
			{
				lock (mutex)
				{
					remoteAdvantage = value;
				}
			}
		}

		private void DiscoveryServiceSecondary()
		{
			if (discoveryInternetServer == null)
			{
				SecondaryServer.Clean();
				discoveryInternetServer = new DiscoveryService(SecondaryServer);
				discoveryInternetServer.Start();
			}
		}

		public void SecondaryServerIp(string ip)
		{
			SecondaryServer.Ip = IPAddress.Parse(ip);
		}

		public ServerContent CheckSecondaryServer()
		{
			if (MposFramework.Instance.DeviceController.IsOnline() && SecondaryServer.IsReady())
			{
				return SecondaryServer;
			}
			return null;
		}

		private void DiscoveryCloudletMulticast()
		{
			if (discoveryCloudletMulticast == null)
			{
				CloudletServer.Clean();
				discoveryCloudletMulticast = new DiscoveryCloudletMulticast();
				discoveryCloudletMulticast.Start();
			}
		}

		public void FoundCloudlet(string cloudletIp)
		{
			System.Diagnostics.Debug.WriteLine("#> [EndpointController]: Cloudlet address: " + cloudletIp);
			CloudletServer.Ip = IPAddress.Parse(cloudletIp);
			DiscoveryServiceCloudlet();
			discoveryCloudletMulticast = null;
		}

		private void DiscoveryServiceCloudlet()
		{
			if (discoveryCloudletServer == null)
			{
				discoveryCloudletServer = new DiscoveryService(CloudletServer);
				discoveryCloudletServer.Start();
			}
		}

		private ServerContent CheckCloudletServer()
		{
			if (MposFramework.Instance.DeviceController.ConnectionStatus(ConnectivityManager.TYPE_WIFI) && CloudletServer.IsReady())
			{
				return CloudletServer;
			}
			return null;
		}

		public void RediscoveryServices(ServerContent server)
		{
			if (server.Type == EndpointType.SECONDARY_SERVER)
			{
				DiscoveryServiceSecondary();
			}
			else if (server.Type == EndpointType.CLOUDLET)
			{
				DiscoveryCloudletMulticast();
			}
		}

		public ServerContent SelectPriorityServer(bool cloudletPriority)
		{
			if (cloudletPriority)
			{
				ServerContent server = CheckCloudletServer();
				if (server != null)
				{
					return server;
				}
				server = CheckSecondaryServer();
				if (server != null)
				{
					return server;
				}
			}
			else
			{
				ServerContent server = CheckSecondaryServer();
				if (server != null)
				{
					return server;
				}
				server = CheckCloudletServer();
				if (server != null)
				{
					return server;
				}
			}
			return null;
		}

		public void DeployService(ServerContent server)
		{
			new DeployService(server).Start();
		}

		public void StartDecisionMaker(ServerContent server){
			lock (mutex)
			{
				if (dynamicDecisionSystem == null && decisionMakerActive)
				{
					dynamicDecisionSystem = new DynamicDecisionSystem(server);
					dynamicDecisionSystem.Start();
				}
			}
		}

		//on UML = updateDdsEndpoint 
		public void UpdateDynamicDecisionSystemEndpoint(ServerContent server)
		{
			dynamicDecisionSystem.Server = server;
		}
	}
}