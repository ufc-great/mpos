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
	sealed class ProfileNetworkLight : ProfileNetworkTask
	{
		public ProfileNetworkLight(ServerContent server) : base(server, "ProfileLight Started on endpoint: " + server.Ip) { }

		protected override Network DoInBackground()
		{
			System.Diagnostics.Debug.WriteLine("[ProfileNetworkLight]: " + startMessage);
			PublishProgress(0);

			network.EndpointType = server.Type.ToString();
			try
			{
				var task = new Task<long[]>[] { PingService(true) };
				//wait for 15s the executing
				Task<long[]>.WaitAll(task, 15000);

				network.ResultPingTcp = task[0].Result;

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
						System.Diagnostics.Debug.WriteLine("[ProfileNetworkLight]: I/O Error -> " + ex.ToString());
						return true;
					}
					else if (ex is InterruptedException)
					{
						System.Diagnostics.Debug.WriteLine("[ProfileNetworkLight]: InterruptedException -> " + ex.Message);
						return true;
					}
					else if (ex is Exception)
					{
						System.Diagnostics.Debug.WriteLine("[ProfileNetworkLight]: Exception -> " + ex.Message);
						return true;
					}

					return false;
				});
			}
			finally
			{
				System.Diagnostics.Debug.WriteLine("[ProfileNetworkLight]: Finished Profile Task");
			}

			PublishProgress(100);
			return null;
		}
	}
}