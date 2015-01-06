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
using Microsoft.Phone.Info;
using Microsoft.Phone.Net.NetworkInformation;
using System;
using System.Diagnostics;
using System.Xml;

namespace Ufc.MpOS.Util.Device
{
	public sealed class DeviceController
	{
		public DeviceController()
		{
			AppStatus();
		}

		public Device Device { get; set; }
		public string AppName { get; private set; }
		public string Version { get; private set; }

		public bool IsOnline()
		{
			return ConnectionStatus(ConnectivityManager.TYPE_WIFI) || ConnectionStatus(ConnectivityManager.TYPE_MOBILE);
		}

		public bool ConnectionStatus(ConnectivityManager type)
		{
			switch (type)
			{
				case ConnectivityManager.TYPE_MOBILE:
					return DeviceNetworkInformation.IsCellularDataEnabled;

				case ConnectivityManager.TYPE_WIFI:
					return DeviceNetworkInformation.IsWiFiEnabled;

				default:
					return false;
			}
		}

		public string GetNetworkConnectedType()
		{
			NetworkInterfaceList networkInterface = new NetworkInterfaceList();
			//iterate only connected interface
			foreach (NetworkInterfaceInfo netInfo in networkInterface)
			{
				if (netInfo.InterfaceType == NetworkInterfaceType.Wireless80211)
				{
					return "WiFi";
				}
				else if (netInfo.InterfaceType == NetworkInterfaceType.MobileBroadbandGsm || netInfo.InterfaceType == NetworkInterfaceType.MobileBroadbandCdma)
				{
					switch (netInfo.InterfaceSubtype)
					{
						case NetworkInterfaceSubType.Cellular_3G:
							return "3G";
						case NetworkInterfaceSubType.Cellular_EDGE:
							return "EDGE";
						case NetworkInterfaceSubType.Cellular_EHRPD:
							return "EHRPD_4G";
						case NetworkInterfaceSubType.Cellular_EVDO:
							return "EVDO";
						case NetworkInterfaceSubType.Cellular_EVDV:
							return "EVDV";
						case NetworkInterfaceSubType.Cellular_GPRS:
							return "GPRS";
						case NetworkInterfaceSubType.Cellular_HSPA:
							return "HSPA";
						case NetworkInterfaceSubType.Cellular_LTE:
							return "LTE";
						case NetworkInterfaceSubType.Cellular_1XRTT:
							return "1XRTT";
						default:
							return "Unknown";
					}
				}
			}
			return "Offline";
		}

		public void CollectDeviceConfig()
		{
			Device = new Device();
			Device.MobileId = GetDeviceID();
			Device.Carrier = DeviceNetworkInformation.CellularMobileOperator;
			Device.DeviceName = DeviceProperties("DeviceManufacturer") + " " + DeviceProperties("DeviceName");
		}

		public void CollectLocation()
		{
			GpsTrack task = new GpsTrack();
			task.Start();
		}

		//OBS: requires ID_CAP_IDENTITY_DEVICE
		private string DeviceProperties(string key)
		{
			string valueProperties = string.Empty;
			object obj = null;
			if (DeviceExtendedProperties.TryGetValue(key, out obj))
			{
				valueProperties = obj as string;
			}

			return valueProperties;
		}

		//OBS: requires ID_CAP_IDENTITY_DEVICE 
		//OBS2: this will then warn users in marketplace!
		private string GetDeviceID()
		{
			byte[] id = new byte[1];
			object uniqueId;
			if (DeviceExtendedProperties.TryGetValue("DeviceUniqueId", out uniqueId))
			{
				id = (byte[])uniqueId;
			}

			string encode = "ID_EMPTY";

			try
			{
				return Convert.ToBase64String(id, 0, id.Length);
			}
			catch (ArgumentNullException e)
			{
				Debug.WriteLine("Binary data array is null.\n" + e.Message);
			}

			return encode;
		}

		private void AppStatus()
		{
			var xmlReaderSettings = new XmlReaderSettings
			{
				XmlResolver = new XmlXapResolver()
			};

			//others atributtes: ProductID, RuntimeType, Author, Description, Publisher, PublisherID
			using (var xmlReader = XmlReader.Create("WMAppManifest.xml", xmlReaderSettings))
			{
				xmlReader.ReadToDescendant("App");

				Version = xmlReader.GetAttribute("Version");
				AppName = xmlReader.GetAttribute("Title");
			}
		}

		
	}

	public enum ConnectivityManager
	{
		TYPE_WIFI, TYPE_MOBILE
	}
}