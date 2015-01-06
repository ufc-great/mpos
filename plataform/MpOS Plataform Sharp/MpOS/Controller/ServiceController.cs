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
using System;
using System.Collections.Generic;
using Ufc.MpOS.Net.Rpc;
using Ufc.MpOS.Net.Rpc.Deploy;
using Ufc.MpOS.Net.Rpc.Deploy.Model;
using Ufc.MpOS.Net.Util;
using Ufc.MpOS.Persistence;
using Ufc.MpOS.Util;

namespace Ufc.MpOS.Controller
{
	/*
	 * Only Specifics services in on c# server...
	 */ 
	public sealed class ServiceController
	{
		private static readonly ServiceController instance = new ServiceController();

		public static ServiceController Instance { get { return instance; } }

		public List<RpcTcpServer> RpcServices { get; private set; }
		private string ip = null;

		private ServiceController() {
			RpcServices = new List<RpcTcpServer>();
		}

		public void Start(Properties properties)
		{
			this.ip = properties.Data["cloudlet.interface.ip"];

			new DeployAppTcpServer(ip, new Service(properties.Data["service.windowsphone.deploy.name"], Convert.ToInt32(properties.Data["service.windowsphone.deploy.port"]))).Start();

			LoadRpcServices();
			StartServices();
		}

		private void LoadRpcServices()
		{
			List<RpcService> registeredRpcServices = FachadeDao.Instance.RpcServiceDao.GetAll();
			foreach (RpcService rpcService in registeredRpcServices)
			{
				RpcServices.Add(new RpcTcpServer(ip, rpcService));
				Console.WriteLine("Service: " + rpcService);
			}
		}

		private void StartServices()
		{
			foreach (RpcTcpServer server in RpcServices)
			{
				server.Start();
			}
		}

		public void StartServiceRpc(RpcService service) {
			lock (instance)
			{
				RpcTcpServer server = new RpcTcpServer(ip, service);
				server.Start();
				RpcServices.Add(server);
			}
		}
	}
}