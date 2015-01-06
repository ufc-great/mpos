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
using System.ComponentModel;
using System.Data.Linq;
using System.Data.Linq.Mapping;
using Ufc.MpOS.Net.Rpc.Model;

namespace BenchImage.Core.Persistence.Model
{
	[Table(Name = "ResultImage")]
	public sealed class ResultImage : INotifyPropertyChanged, INotifyPropertyChanging
	{
		public AppConfiguration AppConfig { get; private set; }
		public RpcProfile RpcProfile { get; private set; }

		public byte[] Image { get; set; }

		public ResultImage()
		{
			AppConfig = new AppConfiguration();
			RpcProfile = new RpcProfile();
			Date = DateTime.Now;
		}

		public ResultImage(AppConfiguration appConfig)
			: this()
		{
			PhotoName = appConfig.Image;
			FilterName = appConfig.Filter;
			Local = appConfig.Local;
			Size = appConfig.Size;
		}

		public ResultImage(AppConfiguration appConfig, RpcProfile profile)
			: this(appConfig)
		{
			ExecCpuTime = profile.ExecutionCpuTime;
			DownloadTime = profile.DonwloadTime;
			UploadTime = profile.UploadTime;
			DownloadSize = profile.DownloadSize;
			UploadSize = profile.UploadSize;
		}

		public ResultImage(AppConfiguration appConfig, RpcProfile profile, long totalTime)
			: this(appConfig, profile)
		{
			TotalTime = totalTime;
		}

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

		[Column(CanBeNull = false)]
		public string PhotoName
		{
			get
			{
				return AppConfig.Image;
			}
			set
			{
				NotifyPropertyChanging("PhotoName");
				AppConfig.Image = value;
				NotifyPropertyChanged("PhotoName");
			}
		}

		[Column(CanBeNull = false)]
		public string FilterName
		{
			get
			{
				return AppConfig.Filter;
			}
			set
			{
				NotifyPropertyChanging("FilterName");
				AppConfig.Filter = value;
				NotifyPropertyChanged("FilterName");
			}
		}

		[Column(CanBeNull = false)]
		public string Local
		{
			get
			{
				return AppConfig.Local;
			}
			set
			{
				NotifyPropertyChanging("Local");
				AppConfig.Local = value;
				NotifyPropertyChanged("Local");
			}
		}

		[Column(CanBeNull = false)]
		public string Size
		{
			get
			{
				return AppConfig.Size;
			}
			set
			{
				NotifyPropertyChanging("Size");
				AppConfig.Size = value;
				NotifyPropertyChanged("Size");
			}
		}

		[Column(CanBeNull = false)]
		public long ExecCpuTime
		{
			get
			{
				return RpcProfile.ExecutionCpuTime;
			}
			set
			{
				NotifyPropertyChanging("ExecCpuTime");
				RpcProfile.ExecutionCpuTime = value;
				NotifyPropertyChanged("ExecCpuTime");
			}
		}

		[Column(CanBeNull = false)]
		public long DownloadTime
		{
			get
			{
				return RpcProfile.DonwloadTime;
			}
			set
			{
				NotifyPropertyChanging("DownloadTime");
				RpcProfile.DonwloadTime = value;
				NotifyPropertyChanged("DownloadTime");
			}
		}

		[Column(CanBeNull = false)]
		public long UploadTime
		{
			get
			{
				return RpcProfile.UploadTime;
			}
			set
			{
				NotifyPropertyChanging("UploadTime");
				RpcProfile.UploadTime = value;
				NotifyPropertyChanged("UploadTime");
			}
		}

		private long _totalTime;
		[Column(CanBeNull = false)]
		public long TotalTime
		{
			get
			{
				return _totalTime;
			}
			set
			{
				NotifyPropertyChanging("TotalTime");
				_totalTime = value;
				NotifyPropertyChanged("TotalTime");
			}
		}

		[Column(CanBeNull = false)]
		public int DownloadSize
		{
			get
			{
				return RpcProfile.DownloadSize;
			}
			set
			{
				NotifyPropertyChanging("DownloadSize");
				RpcProfile.DownloadSize = value;
				NotifyPropertyChanged("DownloadSize");
			}
		}

		[Column(CanBeNull = false)]
		public int UploadSize
		{
			get
			{
				return RpcProfile.UploadSize;
			}
			set
			{
				NotifyPropertyChanging("UploadSize");
				RpcProfile.UploadSize = value;
				NotifyPropertyChanged("UploadSize");
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

		public override string ToString()
		{
			return Id + "," + PhotoName + "," + FilterName+ "," +Local+ "," +Size+ "," +ExecCpuTime+ "," +UploadTime+ "," +DownloadTime+ "," +TotalTime+ "," +UploadSize+ "," +DownloadSize+ "," + Date;
		}

		#region INotifyPropertyChanged Members

		public event PropertyChangedEventHandler PropertyChanged;

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