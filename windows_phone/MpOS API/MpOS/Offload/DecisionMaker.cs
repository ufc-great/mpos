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
using System.Collections.Generic;
using System.Diagnostics;
using System.Threading;
using Ufc.MpOS.Net.Endpoint;
using Ufc.MpOS.Net.Profile;
using Ufc.MpOS.Net.Profile.Model;
using Ufc.MpOS.Persistence;
using Ufc.MpOS.Util.Exceptions;
using Ufc.MpOS.Util.TaskOp;

namespace Ufc.MpOS.Offload
{
	class DynamicDecisionSystem : Runnable
	{
		private readonly object mutex = new object();

		private readonly long PingTolerance = 70;//ms

		private ProfileNetworkDao profileDao;

		private ServerContent server;
		public ServerContent Server
		{
			get
			{
				lock (mutex)
				{
					return server.NewInstance();//immutable operation
				}
			}
			set
			{
				lock (mutex)
				{
					server = value;
				}
			}
		}

		public DynamicDecisionSystem(ServerContent server)
		{
			Server = server;
			profileDao = new ProfileNetworkDao();
		}

		protected override void Run()
		{
			//setting the method for processing the Profile Result
			MposFramework.Instance.ProfileController.FinishProfile += new FinishProfileEventHandler(ExecutionCompleted);

			while (running)
			{
				try
				{
					if (Server != null)
					{
						MposFramework.Instance.ProfileController.NetworkAnalysis(Server);
					}
					else
					{
						Debug.WriteLine("[DecisionMaker]: Waiting for new endpoint...");
					}
				}
				catch (NetworkException e)
				{
					Debug.WriteLine("[DecisionMaker]: Error -> " + e.ToString());
				}

				Thread.Sleep(EndpointController.REPEAT_DECISION_MAKER);
			}
		}

		private void ExecutionCompleted(Network network)
		{
			if (network != null)
			{
				network.GeneratingPingTcpStats();
				Debug.WriteLine("[DecisionMaker]:  -> Ping max: " + network.PingMaxTCP + ", med: " + network.PingMedTCP + ", min: " + network.PingMinTCP);

				MposFramework.Instance.EndpointController.RemoteAdvantage = network.PingMedTCP < PingTolerance;

				//List<Network> lists = profileDao.Last15Results();
				//System.Diagnostics.Debug.WriteLine("[DEBUG]: Network Results!!!");
				//foreach (Network net in lists)
				//{
				//	System.Diagnostics.Debug.WriteLine(net);
				//}
			}
			else
			{
				Server = null;
				MposFramework.Instance.EndpointController.RemoteAdvantage = false;
				Debug.WriteLine("[DecisionMaker]: Any problem in ping test!");
			}
		}
	}
}