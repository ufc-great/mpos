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
using System.Threading.Tasks;
using Ufc.MpOS.Util.TaskOp;
using Windows.Networking;
using Windows.Networking.Sockets;
using Windows.Storage.Streams;

namespace Ufc.MpOS.Net.Endpoint.Service
{
	sealed class DiscoveryCloudletMulticast : Runnable
	{
		private readonly int multicastPort = 31000;
		private readonly int replyCloudletPort = 31001;
		private readonly HostName ip = new HostName("230.230.230.230");

		private const int bufferSize = 64;

		private int retry;

		protected override void Run()
		{
			Task discoveryTask = ExecuteDiscoveryTask();
		}

		private async Task ExecuteDiscoveryTask()
		{
			int repeatTime = EndpointController.REPEAT_DISCOVERY_TASK / 2;

			while (running)
			{
				Debug.WriteLine("#> [DiscoveryCloudletMulticast]: Started Cloudlet Discovery using Multicast UDP");
				retry = 0;

				DatagramSocket socketSent = null;
				DatagramSocket socketReplay = null;
				try
				{
					socketSent = new DatagramSocket();
					await socketSent.BindEndpointAsync(null, string.Empty);
					socketSent.JoinMulticastGroup(ip);

					socketReplay = new DatagramSocket();
					socketReplay.MessageReceived += SocketOnMessageReceived;
					await socketReplay.BindServiceNameAsync(replyCloudletPort.ToString());

					using (DataWriter writer = new DataWriter(await socketSent.GetOutputStreamAsync(ip, multicastPort.ToString())))
					{
						while (retry < 60 && running)
						{
							writer.WriteString("mpos_cloudlet_req");
							await writer.StoreAsync();
							await writer.FlushAsync();

							await Task.Delay(500);
							retry++;
						}
					}
				}
				catch (IOException e)
				{
					Debug.WriteLine("## [DiscoveryCloudletMulticast]: Any problem with i/o in multicast system!\n" + e.ToString());
				}
				finally
				{
					socketSent.Dispose();
					socketReplay.Dispose();

					socketSent = null;
					socketReplay = null;
				}

				if (running)
				{
					Debug.WriteLine(">> [DiscoveryCloudletMulticast]: Retry Discovery Cloudlet, in " + repeatTime + " ms");
					await Task.Delay(repeatTime);
				}
				else
				{
					Debug.WriteLine("#> [DiscoveryCloudletMulticast]: Finished Discovery Cloudlet");
				}
			}
		}

		private async void SocketOnMessageReceived(DatagramSocket socket, DatagramSocketMessageReceivedEventArgs args)
		{
			using (var reader = new StreamReader(args.GetDataStream().AsStreamForRead(bufferSize)))
			{
				Debug.WriteLine("#> [DiscoveryCloudletMulticast]: Wait Reply...");
				string message = await reader.ReadToEndAsync();
				if (message.Contains("="))
				{
					string[] extractMessage = message.Split(new char[] { '=' });
					Stop();

					MposFramework.Instance.EndpointController.FoundCloudlet(extractMessage[1]);
					Debug.WriteLine("#> [ReceiveReplyCloudlet]: Finished Reply Cloudlet listen");
				}
			}
			socket.Dispose();
		}
	}
}