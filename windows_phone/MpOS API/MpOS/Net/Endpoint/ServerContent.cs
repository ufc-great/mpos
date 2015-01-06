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
using Newtonsoft.Json;
using System;
using System.Collections.Generic;
using System.Net;
using System.Reflection;
using System.Text;

namespace Ufc.MpOS.Net.Endpoint
{
	public class ServerContent
	{
		private readonly object mutexLock = new object();

		public ServerContent(EndpointType type)
		{
			Type = type;
		}

		public EndpointType Type { get; set; }
		public int PingTcpPort { get; set; }
		public int PingUdpPort { get; set; }
		public int JitterTestPort { get; set; }
		public int JitterRetrieveResultPort { get; set; }
		public int BandwidthPort { get; set; }
		public int SaveProfilePort { get; set; }
		public int DeployAppPort { get; set; }
		public int RpcServicePort { get; set; }

		private IPAddress ip;
		public IPAddress Ip
		{
			get
			{
				lock (mutexLock)
				{
					return ip;
				}
			}
			set
			{
				lock (mutexLock)
				{
					ip = value;
				}
			}
		}

		public bool IsReady()
		{
			lock (mutexLock)
			{
				return Ip != null && RpcServicePort > -1;
			}
		}

		//need equivalent synchronized method
		public void Clean()
		{
			lock (mutexLock)
			{
				PingTcpPort = 0;
				PingUdpPort = 0;
				JitterTestPort = 0;
				JitterRetrieveResultPort = 0;
				BandwidthPort = 0;
				SaveProfilePort = 0;
				DeployAppPort = 0;
				RpcServicePort = -1;
			}
		}

		// http://james.newtonking.com/json/help/index.html
		// string json = @"..."
		public bool JsonToPorts(string jsonData)
		{
			lock (mutexLock)
			{
				Dictionary<string, int> servicePorts = JsonConvert.DeserializeObject<Dictionary<string, int>>(jsonData);

				PingTcpPort = servicePorts["ping_tcp"];
				PingUdpPort = servicePorts["ping_udp"];
				JitterTestPort = servicePorts["jitter_test"];
				JitterRetrieveResultPort = servicePorts["jitter_retrieve_results"];
				BandwidthPort = servicePorts["bandwidth"];
				SaveProfilePort = servicePorts["save_profile_results"];
				DeployAppPort = servicePorts["deploy_wp_app"];

				RpcServicePort = servicePorts["rpc_serv"];

				return RpcServicePort == -1;
			}
		}

		public ServerContent NewInstance()
		{
			ServerContent instance = new ServerContent(Type);
			instance.BandwidthPort = BandwidthPort;
			instance.DeployAppPort = DeployAppPort;
			instance.Ip = Ip;
			instance.JitterRetrieveResultPort = JitterRetrieveResultPort;
			instance.JitterTestPort = JitterTestPort;
			instance.PingTcpPort = PingTcpPort;
			instance.PingUdpPort = PingUdpPort;
			instance.RpcServicePort = RpcServicePort;
			instance.SaveProfilePort = SaveProfilePort;

			return instance;
		}

		public override string ToString()
		{
			Type type = this.GetType();
			StringBuilder builder = new StringBuilder(type.Name + "=[");

			foreach (PropertyInfo property in type.GetProperties())
			{
				builder.Append(string.Format("{0}={1}, ", property.Name, property.GetValue(this, null)));
			}

			builder.Remove(builder.Length - 2, 2);
			builder.Append("]");

			return builder.ToString();
		}
	}
}