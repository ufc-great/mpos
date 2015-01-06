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
using Ufc.MpOS.Net.Endpoint;
using Ufc.MpOS.Net.Profile.Model;
using Ufc.MpOS.Util.Device;
using Ufc.MpOS.Util.Exceptions;
using Ufc.MpOS.Util.TaskOp;
using Windows.Networking;
using Windows.Networking.Sockets;
using Windows.Storage.Streams;

namespace Ufc.MpOS.Net.Profile
{
	abstract class ProfileNetworkTask : AsyncTask<Network>
	{
		protected Network network;
		
		protected StreamSocket streamSocket;
		protected DatagramSocket datagramSocket;

		protected ServerContent server;
		protected HostName hostName;

		protected string startMessage;
		protected volatile bool halted = false;

		private Stopwatch watch = null;

		public ProfileNetworkTask(ServerContent server, string startMessage)
		{
			this.server = server;
			this.startMessage = startMessage;

			network = new Network();
			watch = new Stopwatch();
			hostName = new HostName(server.Ip.ToString());
		}

		protected async Task<long[]> PingService(bool isTcp)
		{
			return await PingService(isTcp, 7);
		}

		protected async Task<long[]> PingService(bool isTcp, int pingRounds)
		{
			long[] pings = new long[pingRounds];
			watch.Reset();

			if (isTcp)
			{
				streamSocket = new StreamSocket();
				await streamSocket.ConnectAsync(hostName, server.PingTcpPort.ToString());

				DataWriter writer = new DataWriter(streamSocket.OutputStream);
				DataReader reader = new DataReader(streamSocket.InputStream);

				int count = 0;
				for (int i = 0; i < pingRounds; i++)
				{
					watch.Start();
					writer.WriteString("pigado" + i);
					await writer.StoreAsync();
					await writer.FlushAsync();

					reader.InputStreamOptions = InputStreamOptions.Partial;
					await reader.LoadAsync(256);

					watch.Stop();
					pings[count++] = watch.ElapsedMilliseconds;
					watch.Reset();

					if (halted)
					{
						throw new InterruptedException("PingService TCP was aborted externally!");
					}

					await Task.Delay(500);
				}

				Array.Sort<long>(pings);
				Close(ref writer);
				Close(ref reader);
				Close(ref streamSocket);
			}
			else
			{
				datagramSocket = new DatagramSocket();

				int count = 0;
				datagramSocket.MessageReceived += delegate(DatagramSocket socket, DatagramSocketMessageReceivedEventArgs args)
				{
					watch.Stop();
					pings[count++] = watch.ElapsedMilliseconds;
					watch.Reset();
				};

				//for receive packets
				string port = server.PingUdpPort.ToString();
				await datagramSocket.BindServiceNameAsync(port);
				using (DataWriter writer = new DataWriter(await datagramSocket.GetOutputStreamAsync(hostName, port)))
				{
					for (int i = 0; i < pingRounds; i++)
					{
						watch.Start();

						writer.WriteString("pigado" + i);
						await writer.StoreAsync();
						await writer.FlushAsync();

						if (halted)
						{
							throw new InterruptedException("PingService UDP was aborted externally!");
						}

						await Task.Delay(500);
					}
				}

				Array.Sort<long>(pings);
				Close(ref datagramSocket);
			}

			return pings;
		}

		protected int LossPacketCalculation(Network results)
		{
			int count = 0;
			long[] pingResults = results.ResultPingUdp;
			if (pingResults != null)
			{
				foreach (long udpPing in pingResults)
				{
					if (udpPing == 0 || udpPing > 1500)
					{
						count++;
					}
				}
			}

			pingResults = results.ResultPingTcp;
			if (pingResults != null)
			{
				foreach (long tcpPing in pingResults)
				{
					if (tcpPing > 1500)
					{
						count++;
					}
				}
			}
			return count;
		}

		protected async Task SentNetworkProfile(Network network)
		{
			string jsonData = "";
			Device device = MposFramework.Instance.DeviceController.Device;

			try
			{
				if (device != null)
				{
					jsonData = new ProfileResult(device, network).ToJson();
				}
				else
				{
					jsonData = network.ToJson();
				}
				
				streamSocket = new StreamSocket();
				await streamSocket.ConnectAsync(hostName, server.SaveProfilePort.ToString());

				using (DataWriter writer = new DataWriter(streamSocket.OutputStream))
				{
					writer.WriteString(jsonData);
					await writer.StoreAsync();
					await writer.FlushAsync();

					//wait ok comeback...
					using (DataReader reader = new DataReader(streamSocket.InputStream))
					{
						reader.InputStreamOptions = InputStreamOptions.Partial;
						await reader.LoadAsync(256);
					}
				}

				System.Diagnostics.Debug.WriteLine("[ProfileNetworkTask]: Sent profile to server with success");
			}
			catch (AggregateException e)
			{
				e.Handle((ex) =>
				{
					if (ex is IOException)
					{
						System.Diagnostics.Debug.WriteLine("[ProfileNetworkTask]: Error on transmition to remote data -> " + ex.ToString());
						return true;
					}

					return false;
				});
			}
			finally
			{
				Close(ref streamSocket);
			}
		}

		// stop profile running...
		public void halt()
		{
			halted = true;
		}

		protected void Close(ref StreamSocket socket)
		{
			if (socket != null)
			{
				socket.Dispose();
				//ref keyword made this null works in outside...
				socket = null;
			}
		}

		protected void Close(ref DatagramSocket socket)
		{
			if (socket != null)
			{
				socket.Dispose();
				socket = null;
			}
		}

		protected void Close(ref DataWriter writer)
		{
			if (writer != null)
			{
				writer.DetachStream();
				writer.Dispose();
				writer = null;
			}
		}

		protected void Close(ref DataReader reader)
		{
			if (reader != null)
			{
				reader.DetachStream();
				reader.Dispose();
				reader = null;
			}
		}
	}
}