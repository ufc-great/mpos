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
using Newtonsoft.Json;
using Newtonsoft.Json.Linq;
using System;
using System.Collections.Generic;
using Ufc.MpOS.Util.Device;

namespace Ufc.MpOS.Net.Profile.Model
{
	sealed class ProfileResult
	{
		public Device Device { get; private set; }
		public Network Network { get; private set; }

		public ProfileResult(Device device, Network network)
		{
			Device = device;
			Network = network;
		}

		public string ToJson()
		{
			var dic = new Dictionary<string, object>();

			dic.Add("mobileId", Device.MobileId);
			dic.Add("deviceName", Device.DeviceName);
			dic.Add("carrier", Device.Carrier);
			dic.Add("lat", Device.Latitude);
			dic.Add("lon", Device.Longitude);
			
			dic.Add("date", (long)(Network.Date - new DateTime(1970, 1, 1)).TotalMilliseconds);
			if (Network.ResultPingTcp != null)
			{
				dic.Add("tcp", new JArray(Network.ResultPingTcp));
			}
			else
			{
				dic.Add("tcp", new JArray());
			}

			if (Network.ResultPingUdp != null)
			{
				dic.Add("udp", new JArray(Network.ResultPingUdp));
			}
			else
			{
				dic.Add("udp", new JArray());
			}
			dic.Add("loss", Network.LossPacket);
			dic.Add("jitter", Network.Jitter);
			dic.Add("down", Network.BandwidthDownload);
			dic.Add("up", Network.BandwidthUpload);
			dic.Add("net", Network.NetworkType);
			dic.Add("appName", Network.AppName);

			return JsonConvert.SerializeObject(dic);
		}

		public override string ToString()
		{
			return ToJson();
		}
	}
}
