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
using System.ComponentModel;
using System.Data.Linq;
using System.Data.Linq.Mapping;
using System.Reflection;
using System.Text;

namespace Ufc.MpOS.Net.Profile.Model
{
	[Table(Name = "Networks")]
	public sealed class Network : INotifyPropertyChanged, INotifyPropertyChanging
	{
		// Version column aids update performance. (linq use...)
		[Column(IsVersion = true)]
		private Binary _version;

		private int _id;
		[Column(IsPrimaryKey = true, IsDbGenerated = true, DbType = "INT NOT NULL Identity", CanBeNull = false, AutoSync = AutoSync.OnInsert)]
		public int Id
		{
			get
			{
				return _id;
			}
			set
			{
				NotifyPropertyChanging("Id");
				_id = value;
				NotifyPropertyChanged("Id");
			}
		}

		private DateTime _date;
		[Column(CanBeNull = false)]
		public DateTime Date
		{
			get
			{
				return _date;
			}
			set
			{
				NotifyPropertyChanging("Date");
				_date = value;
				NotifyPropertyChanged("Date");
			}
		}

		private int _lossPacket;
		[Column]
		public int LossPacket
		{
			get
			{
				return _lossPacket;
			}
			set
			{
				NotifyPropertyChanging("LossPacket");
				_lossPacket = value;
				NotifyPropertyChanged("LossPacket");
			}
		}

		private int _jitter;
		[Column]
		public int Jitter
		{
			get
			{
				return _jitter;
			}
			set
			{
				NotifyPropertyChanging("Jitter");
				_jitter = value;
				NotifyPropertyChanged("Jitter");
			}
		}

		private string _bandwidthDownload;
		[Column]
		public string BandwidthDownload
		{
			get
			{
				return _bandwidthDownload;
			}
			set
			{
				NotifyPropertyChanging("BandwidthDownload");
				_bandwidthDownload = value;
				NotifyPropertyChanged("BandwidthDownload");
			}
		}

		private string _bandwidthUpload;
		[Column]
		public string BandwidthUpload
		{
			get
			{
				return _bandwidthUpload;
			}
			set
			{
				NotifyPropertyChanging("BandwidthUpload");
				_bandwidthUpload = value;
				NotifyPropertyChanged("BandwidthUpload");
			}
		}

		private string _networkType;
		[Column]
		public string NetworkType
		{
			get
			{
				return _networkType;
			}
			set
			{
				NotifyPropertyChanging("NetworkType");
				_networkType = value;
				NotifyPropertyChanged("NetworkType");
			}
		}

		private string _endpointType;
		[Column]
		public string EndpointType
		{
			get
			{
				return _endpointType;
			}
			set
			{
				NotifyPropertyChanging("EndpointType");
				_endpointType = value;
				NotifyPropertyChanged("EndpointType");
			}
		}

		private string _resultPingUdpString;
		[Column(Name = "ResultPingUdp")]
		public string ResultPingUdpString
		{
			get
			{
				return _resultPingUdpString;
			}
			set
			{
				NotifyPropertyChanging("ResultPingUdpString");
				_resultPingUdpString = value;
				NotifyPropertyChanged("ResultPingUdpString");
			}
		}

		private string _resultPingTcpString;
		[Column(Name = "ResultPingTcp")]
		public string ResultPingTcpString
		{
			get
			{
				return _resultPingTcpString;
			}
			set
			{
				NotifyPropertyChanging("ResultPingTcpString");
				_resultPingTcpString = value;
				NotifyPropertyChanged("ResultPingTcpString");
			}
		}

		public string AppName { get; set; }

		public long[] ResultPingUdp { get; set; }
		public long[] ResultPingTcp { get; set; }

		public int PingEstimated { get; set; }
		public int PingMedUDP { get; set; }
		public int PingMedTCP { get; set; }
		public int PingMaxUDP { get; set; }
		public int PingMinUDP { get; set; }
		public int PingMaxTCP { get; set; }
		public int PingMinTCP { get; set; }

		public Network()
		{
			Date = DateTime.UtcNow;
			BandwidthDownload = "0";
			BandwidthUpload = "0";

			NetworkType = MposFramework.Instance.DeviceController.GetNetworkConnectedType();
			AppName = MposFramework.Instance.DeviceController.AppName;
		}

		public void GeneratingPingTcpStats()
		{
			if (ResultPingTcp != null)
			{
				Array.Sort<long>(ResultPingTcp);
				int tam = ResultPingTcp.Length;
				long soma = 0L;
				foreach (long res in ResultPingTcp)
				{
					soma += res;
				}

				PingMedTCP = (int)(soma / tam);
				PingMaxTCP = (int)ResultPingTcp[tam - 1];
				PingMinTCP = (int)ResultPingTcp[0];
			}
		}

		public void GeneratingPingUdpStats()
		{
			if (ResultPingUdp != null)
			{
				Array.Sort<long>(ResultPingUdp);
				int tam = ResultPingUdp.Length;
				long soma = 0L;
				foreach (long res in ResultPingUdp)
				{
					soma += res;
				}

				PingMedUDP = (int)(soma / tam);
				PingMaxUDP = (int)ResultPingUdp[tam - 1];
				PingMinUDP = (int)ResultPingUdp[0];
			}
		}

		private delegate long[] TransformStringToArray(string result);

		public void PingStringToArrays()
		{
			TransformStringToArray annoMethod = delegate(string result)
			{
				result = result.Replace("[", "");
				result = result.Replace("]", "");
				string[] splits = result.Split(new char[] { ',' });

				long[] values = new long[splits.Length];
				for (int i = 0; i < splits.Length; i++)
				{
					if (!splits[i].Equals(""))
					{
						values[i] = Int64.Parse(splits[i]);
					}
				}

				return values;
			};
			
			if (ResultPingTcpString != null)
			{
				ResultPingTcp = annoMethod(ResultPingTcpString);
			}
			else
			{
				ResultPingTcp = new long[7];
			}

			if (ResultPingUdpString != null)
			{
				ResultPingUdp = annoMethod(ResultPingUdpString);
			}
			else
			{
				ResultPingUdp = new long[7];
			}
		}

		public void PingArraysToString()
		{
			if (ResultPingTcp != null)
			{
				ResultPingTcpString = new JArray(ResultPingTcp).ToString().Replace("\r\n", string.Empty).Replace(" ", string.Empty);
			}
			else
			{
				ResultPingTcpString = "[]";
			}

			if (ResultPingUdp != null)
			{
				ResultPingUdpString = new JArray(ResultPingUdp).ToString().Replace("\r\n", string.Empty).Replace(" ", string.Empty);
			}
			else
			{
				ResultPingUdpString = "[]";
			}
		}

		public string ToJson()
		{
			var dic = new Dictionary<string, object>();
			dic.Add("date", (long)(Date - new DateTime(1970, 1, 1)).TotalMilliseconds);
			if (ResultPingTcp != null)
			{
				dic.Add("tcp", new JArray(ResultPingTcp));
			}
			else
			{
				dic.Add("tcp", "[]");
			}

			if (ResultPingUdp != null)
			{
				dic.Add("udp", new JArray(ResultPingUdp));
			}
			else
			{
				dic.Add("udp", "[]");
			}

			dic.Add("loss", LossPacket);
			dic.Add("jitter", Jitter);
			dic.Add("down", BandwidthDownload);
			dic.Add("up", BandwidthUpload);
			dic.Add("net", NetworkType);
			dic.Add("appName", AppName);

			return JsonConvert.SerializeObject(dic);
		}

		public override string ToString()
		{
			Type type = this.GetType();
			StringBuilder builder = new StringBuilder(type.Name + "=[");

			foreach (PropertyInfo property in type.GetProperties())
			{
				builder.Append(string.Format("{0}={1}, ", property.Name, property.GetValue(this, null)));
			}

			builder.Remove(builder.Length - 2, 2);
			builder.Append("]");

			return builder.ToString();
		}

		#region INotifyPropertyChanged Members

		public event PropertyChangedEventHandler PropertyChanged;

		// Used to notify the page that a data context property changed
		private void NotifyPropertyChanged(string propertyName)
		{
			if (PropertyChanged != null)
			{
				PropertyChanged(this, new PropertyChangedEventArgs(propertyName));
			}
		}

		#endregion

		#region INotifyPropertyChanging Members

		public event PropertyChangingEventHandler PropertyChanging;

		// Used to notify the data context that a data context property is about to change
		private void NotifyPropertyChanging(string propertyName)
		{
			if (PropertyChanging != null)
			{
				PropertyChanging(this, new PropertyChangingEventArgs(propertyName));
			}
		}

		#endregion
	}
}