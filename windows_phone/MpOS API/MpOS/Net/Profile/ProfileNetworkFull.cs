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
using System.IO;
using System.Text;
using System.Threading.Tasks;
using Ufc.MpOS.Net.Endpoint;
using Ufc.MpOS.Net.Profile.Model;
using Ufc.MpOS.Util;
using Ufc.MpOS.Util.Exceptions;
using Windows.Networking.Sockets;
using Windows.Storage.Streams;

namespace Ufc.MpOS.Net.Profile
{
	sealed class ProfileNetworkFull : ProfileNetworkTask
	{
		private byte[] data;//used on uploading...

		public ProfileNetworkFull(ServerContent server) : base(server, "ProfileFull Started on endpoint: " + server.Ip) {
			data = new byte[32 * 1024];
			new Random().NextBytes(data);
		}

		/**
		 * Feedback code:
		 * 15 -> Finished Ping TCP Test
		 * 30 -> Finished Ping UDP Test
		 * 35 -> Finished Ping Test with packet loss
		 * 50 -> Finished Jitter Calculation
		 * 55 -> Start Donwload Test
		 * 75 -> Start Upload Test
		 * 100 -> Finished Conection Test
		 */
		protected override Network DoInBackground()
		{
			System.Diagnostics.Debug.WriteLine("[ProfileNetworkFull]: " + startMessage);
			PublishProgress(0);

			try
			{
				var pingTask = new Task<long[]>[] { PingService(true) };
				Task<long[]>.WaitAll(pingTask, 15000);
				PublishProgress(15);
				network.ResultPingTcp = pingTask[0].Result;

				pingTask = new Task<long[]>[] { PingService(false) };
				Task<long[]>.WaitAll(pingTask, 15000);
				PublishProgress(30);
				network.ResultPingUdp = pingTask[0].Result;

				pingTask = null;
				if (halted)
				{
					return null;
				}

				network.LossPacket = LossPacketCalculation(network);
				PublishProgress(35);

				var jitterTask = new Task []{ JitterCalculation() };
				Task.WaitAll(jitterTask, 15000);
				if (halted)
				{
					return null;
				}
				jitterTask = new Task[] { RetrieveJitterResult() };
				Task.WaitAll(jitterTask, 15000);
				
				jitterTask = null;
				PublishProgress(50);

				var bandwidthTask = new Task<bool>[] { BandwidthCalculation() };
				Task<bool>.WaitAll(bandwidthTask, 120000);
				bool finished = bandwidthTask[0].Result;
				if (halted || !finished)
				{
					return null;
				}
				bandwidthTask = null;

				var sent = SentNetworkProfile(network);

				PublishProgress(100);
				return network;
			}
			catch (AggregateException e)
			{
				e.Handle((ex) =>
				{
					if (ex is IOException)
					{
						System.Diagnostics.Debug.WriteLine("[ProfileNetworkFull]: I/O Error -> " + ex.ToString());
						return true;
					}
					else if (ex is InterruptedException)
					{
						System.Diagnostics.Debug.WriteLine("[ProfileNetworkFull]: InterruptedException -> " + ex.Message);
						return true;
					}
					else if (ex is Exception)
					{
						System.Diagnostics.Debug.WriteLine("[ProfileNetworkFull]: Exception -> " + ex.Message);
						return true;
					}

					return false;
				});
			}
			finally
			{
				System.Diagnostics.Debug.WriteLine("[ProfileNetworkFull]: Finished Profile Task");
			}

			PublishProgress(100);
			return null;
		}

		private async Task JitterCalculation()
		{
			datagramSocket = new DatagramSocket();

			datagramSocket.MessageReceived += delegate(DatagramSocket socket, DatagramSocketMessageReceivedEventArgs args)
			{
				System.Diagnostics.Debug.WriteLine("[ProfileNetworkFull]: Jitter Finish");
			};

			string port = server.JitterTestPort.ToString();
			await datagramSocket.BindServiceNameAsync(port);
			using (DataWriter writer = new DataWriter(await datagramSocket.GetOutputStreamAsync(hostName, port)))
			{
				for (int i = 0; i < 21; i++)
				{
					writer.WriteString("jitter");
					await writer.StoreAsync();
					await writer.FlushAsync();

					await Task.Delay(250);
				}
			}

			Close(ref datagramSocket);
		}

		private async Task RetrieveJitterResult()
		{	
			streamSocket = new StreamSocket();
			await streamSocket.ConnectAsync(hostName, server.JitterRetrieveResultPort.ToString());

			await Task.Delay(1500);

			DataWriter writer = new DataWriter(streamSocket.OutputStream);
			DataReader reader = new DataReader(streamSocket.InputStream);

			writer.WriteString("get");
			await writer.StoreAsync();
			await writer.FlushAsync();

			reader.InputStreamOptions = InputStreamOptions.Partial;
			var read = await reader.LoadAsync(16);

			string message = reader.ReadString(read);
			network.Jitter = Convert.ToInt32(message);

			Close(ref writer);
			Close(ref reader);
			Close(ref streamSocket);
		}

		private async Task<bool> BandwidthCalculation()
		{
			System.Diagnostics.Debug.WriteLine("[ProfileNetworkFull]: Download");
			PublishProgress(55);

			streamSocket = new StreamSocket();
			await streamSocket.ConnectAsync(hostName, server.BandwidthPort.ToString());

			DataWriter writer = new DataWriter(streamSocket.OutputStream);
			DataReader reader = new DataReader(streamSocket.InputStream);

			byte[] endDown = Encoding.UTF8.GetBytes("end_down");
			byte[] endSession = Encoding.UTF8.GetBytes("end_session");

			writer.WriteString("down");
			await writer.StoreAsync();
			await writer.FlushAsync();

			uint bufferSize = 8192;
			reader.InputStreamOptions = InputStreamOptions.Partial;

			long countBytes = 0L;
			byte[] downloadData = new byte[bufferSize];
			while (true)
			{
				uint readTransmittedData = await reader.LoadAsync(bufferSize);
				countBytes += readTransmittedData;

				if (readTransmittedData == bufferSize)
				{
					reader.ReadBytes(downloadData);
					if (GetDonwloadBandwidth(countBytes, ref downloadData, endDown))
					{
						break;
					}
				}
				else
				{
					byte[] tempData = new byte[readTransmittedData];
					reader.ReadBytes(tempData);
					//System.Diagnostics.Debug.WriteLine("[ProfileNetworkFull]: " + readTransmittedData + "/" + countBytes);
					if (GetDonwloadBandwidth(countBytes, ref tempData, endDown))
					{
						break;
					}
				}
			}

			if (halted)
			{
				return false;
			}

			System.Diagnostics.Debug.WriteLine("[ProfileNetworkFull]: Upload");
			PublishProgress(75);
			writer.WriteString("up");
			await writer.StoreAsync();
			await writer.FlushAsync();

			System.Diagnostics.Stopwatch stopWatch = System.Diagnostics.Stopwatch.StartNew();
			while (stopWatch.ElapsedMilliseconds < 11000)
			{
				writer.WriteBytes(data);
				await writer.StoreAsync();
				await writer.FlushAsync();
			}
			stopWatch.Stop();

			writer.WriteString("end_up");
			await writer.StoreAsync();
			await writer.FlushAsync();

			var readFinish = await reader.LoadAsync(64);
			var endSessionBuffer = new byte[readFinish];
			reader.ReadBytes(endSessionBuffer);

			if (FunctionUtil.ContainsArrays(endSessionBuffer, endSession))
			{
				string dataBlock = Encoding.UTF8.GetString(endSessionBuffer, 0, endSessionBuffer.Length);
				string[] res = dataBlock.Split(new char[]{':'});
				network.BandwidthUpload = res[1];
			}

			Close(ref writer);
			Close(ref reader);
			Close(ref streamSocket);

			endSessionBuffer = null;

			return true;
		}

		private bool GetDonwloadBandwidth(long countBytes, ref byte[] downloadData, byte[] endDown)
		{
			if (FunctionUtil.ContainsArrays(downloadData, endDown))
			{
				// bytes * 8bits / 7s * 1E+6 = X Mbits
				double bandwidth = ((double)(countBytes * 8L) / (double)(7.0 * 1E+6));
				network.BandwidthDownload = Convert.ToString(bandwidth);
				downloadData = null;

				return true;
			}

			return false;
		}
	}
}