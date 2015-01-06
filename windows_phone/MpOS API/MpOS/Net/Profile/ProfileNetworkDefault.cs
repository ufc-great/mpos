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
using System.Threading.Tasks;
using Ufc.MpOS.Net.Endpoint;
using Ufc.MpOS.Net.Profile.Model;
using Ufc.MpOS.Util.Exceptions;

namespace Ufc.MpOS.Net.Profile
{
	sealed class ProfileNetworkDefault : ProfileNetworkTask
	{
		public ProfileNetworkDefault(ServerContent server) : base(server, "ProfileDefault Started on endpoint: " + server.Ip) { }

		/**
		 * Feedback code:
         * 33 -> Finished Ping TCP Test
         * 66 -> Finished Ping UDP Test
         * 100 -> Finished Ping Test with packet loss
		 */
		protected override Network DoInBackground()
		{
			System.Diagnostics.Debug.WriteLine("[ProfileNetworkDefault]: " + startMessage);
			PublishProgress(0);

			try
			{
				var task = new Task<long[]>[] { PingService(true) };
				Task<long[]>.WaitAll(task, 15000);
				PublishProgress(33);
				network.ResultPingTcp = task[0].Result;

				task = new Task<long[]>[] { PingService(false) };
				Task<long[]>.WaitAll(task, 15000);
				PublishProgress(66);
				network.ResultPingUdp = task[0].Result;

				network.LossPacket = LossPacketCalculation(network);

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
						System.Diagnostics.Debug.WriteLine("[ProfileNetworkDefault]: I/O Error -> " + ex.ToString());
						return true;
					}
					else if (ex is InterruptedException)
					{
						System.Diagnostics.Debug.WriteLine("[ProfileNetworkDefault]: InterruptedException -> " + ex.Message);
						return true;
					}
					else if (ex is Exception)
					{
						System.Diagnostics.Debug.WriteLine("[ProfileNetworkDefault]: Exception -> " + ex.Message);
						return true;
					}

					return false;
				});
			}
			finally
			{
				System.Diagnostics.Debug.WriteLine("[ProfileNetworkDefault]: Finished Profile Task");
			}

			PublishProgress(100);
			return null;
		}
	}
}