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
using System.Data.Linq;
using System.Linq;
using Ufc.MpOS.Net.Endpoint;
using Ufc.MpOS.Net.Profile.Model;

namespace Ufc.MpOS.Persistence
{
	sealed class ProfileNetworkDao : Dao
	{
		private Table<Network> tableNetwork;

		public ProfileNetworkDao()
			: base("mpos.sdf")
		{
			tableNetwork = GetTable<Network>();
		}

		public void Add(Network network)
		{
			network.PingArraysToString();
			tableNetwork.InsertOnSubmit(network);
			SubmitChanges();//commit!
		}

		public List<Network> Last15Results()
		{
			IEnumerable<Network> networks = (from net in tableNetwork
									orderby net.Id descending
									select net).Take(16);

			List<Network> lists = new List<Network>(networks);
			if (lists.Count != 0)
			{
				lists.RemoveAt(0);
			}

			foreach (Network net in lists)
			{
				net.PingStringToArrays();
				net.GeneratingPingTcpStats();
				net.GeneratingPingUdpStats();
			}

			return lists;
		}

		public List<Network> Last15Results(EndpointType type)
		{
			IEnumerable<Network> networks = (from net in tableNetwork
									where net.EndpointType.Equals(type.ToString())
									orderby net.Id descending
									select net).Take(16);

			List<Network> lists = new List<Network>(networks);
			if (lists.Count != 0)
			{
				lists.RemoveAt(0);
			}

			foreach (Network net in lists)
			{
				net.PingStringToArrays();
				net.GeneratingPingTcpStats();
				net.GeneratingPingUdpStats();
			}

			return lists;
		}
	}
}