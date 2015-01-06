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
using System.Diagnostics;
using Ufc.MpOS.Config;
using Ufc.MpOS.Net.Endpoint;
using Ufc.MpOS.Net.Profile.Model;
using Ufc.MpOS.Persistence;
using Ufc.MpOS.Util.Exceptions;
using Ufc.MpOS.Util.TaskOp;

namespace Ufc.MpOS.Net.Profile
{
	public delegate void FinishProfileEventHandler(Network network);
	
	public sealed class ProfileController
	{
		public ProfileController(ProfileNetwork profile) {
			Debug.WriteLine("[ProfileController]: MpOS Profile SubSystem started!");
			profileDao = new ProfileNetworkDao();

			this.profileNetwork = profile;
		}

		private ProfileNetworkDao profileDao;
		private ProfileNetworkTask taskNetwork;
		private ProfileNetwork profileNetwork;

		public event ProgressEventHandler ProgressChanged;
		public event FinishProfileEventHandler FinishProfile;

		public void NetworkAnalysis(ServerContent server)
		{
			NetworkAnalysis(server, profileNetwork);
		}

		public void NetworkAnalysis(ServerContent server, ProfileNetwork profile)
		{
			if (server == null)
			{
				throw new NetworkException("The remote service isn't ready for profile network");
			}

			if (profile == ProfileNetwork.LIGHT)
			{
				taskNetwork = new ProfileNetworkLight(server);
			}
			else if (profile == ProfileNetwork.DEFAULT)
			{
				taskNetwork = new ProfileNetworkDefault(server);
			}
			else if (profile == ProfileNetwork.FULL)
			{
				taskNetwork = new ProfileNetworkFull(server);
			}

			EventIntercept();

			taskNetwork.Execute();
		}

		private void EventIntercept()
		{
			if (FinishProfile != null)
			{
				taskNetwork.RunCompleted += new RunCompletedEventHandler<Network>(PersistNetworkResults);
			}

			if (ProgressChanged != null)
			{
				taskNetwork.ProgressChanged += ProgressChanged;
			}
		}

		private void PersistNetworkResults(Network network)
		{
			if (network != null)
			{
				profileDao.Add(network);
				FinishProfile(network);
			}
		}

		public void Destroy()
		{
			if (taskNetwork != null)
			{
				taskNetwork.Cancel();
				taskNetwork = null;
			}
		}
	}
}