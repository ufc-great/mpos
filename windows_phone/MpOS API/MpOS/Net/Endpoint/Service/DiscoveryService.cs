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
using System.Diagnostics;
using System.IO;
using System.Text;
using System.Threading;
using Ufc.MpOS.Net.Core;
using Ufc.MpOS.Util.Exceptions;
using Ufc.MpOS.Util.TaskOp;

namespace Ufc.MpOS.Net.Endpoint.Service
{
	/**
	 * Only discovery services and discovery multicast has fixed port number.
	 */
	sealed class DiscoveryService : Runnable
	{
		private ServerContent server;
		private int servicePort;

		private string appVersion;
		private string appName;

		public DiscoveryService(ServerContent server)
		{
			this.server = server;

			servicePort = 30015;
			appName = MposFramework.Instance.DeviceController.AppName;
			appVersion = MposFramework.Instance.DeviceController.Version;
		}

		protected override void Run()
		{
			bool foundService = false;
			string requestMessage = "mpos_serv_req_wp:" + appName + ":" + appVersion;

			while (running && !foundService)
			{
				Debug.WriteLine("#> [DiscoveryService]: Started Discovery Service for endpoint: " + server.Type + " and app: " + appName + "/" + appVersion);
				foundService = true;

				ClientTcp socket = null;
				try
				{
					socket = new ClientTcp(256, false);
					if (server.Type == EndpointType.SECONDARY_SERVER && !MposFramework.Instance.DeviceController.IsOnline())
					{
						throw new ConnectException("The mobile is completly offline!");
					}

					socket.Connect(server.Ip, servicePort);
					Sent(socket, requestMessage);
					Receive(socket);
				}
				catch (Exception e)
				{
					Debug.WriteLine("## [DiscoveryService]: Error -> \n" + e.ToString());
					foundService = false;
				}
				finally
				{
					if (running && !foundService)
					{
						Debug.WriteLine(">> [DiscoveryService]: Retry Discovery Service for endpoint: " + server.Type + ", in " + EndpointController.REPEAT_DISCOVERY_TASK + " ms");
						Thread.Sleep(EndpointController.REPEAT_DISCOVERY_TASK);
					}
					else
					{
						Debug.WriteLine("#> [DiscoveryService]: Finished Discovery Service for endpoint: " + server.Type + " on " + server.Ip);
						MposFramework.Instance.EndpointController.StartDecisionMaker(server);
					}

					socket.Close();
				}
			}
		}

		private void Sent(ClientTcp socket, string message)
		{
			socket.Sent(Encoding.UTF8.GetBytes(message));
		}

		private void Receive(ClientTcp socket)
		{
			using (MemoryStream ms = socket.Receive())
			{
				//ignore 4 byte of header
				string jsonData = Encoding.UTF8.GetString(ms.GetBuffer(), 0, (int)ms.Length);
				if (jsonData != null && !jsonData.Equals(""))
				{
					if (server.JsonToPorts(jsonData))
					{
						MposFramework.Instance.EndpointController.DeployService(server);
					}

					Stop();
				}
				else
				{
					throw new ConnectException("Didn't received nothing about remote services in: " + server.Ip);
				}
			}
		}
	}
}