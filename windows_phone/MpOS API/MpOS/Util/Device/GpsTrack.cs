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
using System.Diagnostics;
using System.Threading.Tasks;
using Ufc.MpOS.Util.Exceptions;
using Ufc.MpOS.Util.TaskOp;
using Windows.Devices.Geolocation;
using Windows.Foundation;

namespace Ufc.MpOS.Util.Device
{
	class GpsTrack : Runnable
	{
		protected override void Run()
		{
			Task task = ExecuteTask();
			task.Wait();
		}

		//http://blogs.windows.com/windows_phone/b/wpdev/archive/2012/11/30/acquiring-a-single-geoposition-in-windows-phone-8.aspx
		private async Task ExecuteTask()
		{
			//need to close Asynctask
			IAsyncOperation<Geoposition> locationTask = null;
			try
			{
				Geolocator geolocator = new Geolocator();
				geolocator.DesiredAccuracyInMeters = 200;

				locationTask = geolocator.GetGeopositionAsync(maximumAge: TimeSpan.FromMinutes(3), timeout: TimeSpan.FromSeconds(30));
				Geoposition geoposition = await locationTask;

				string latitude = Convert.ToString(geoposition.Coordinate.Latitude);
				string longitude = Convert.ToString(geoposition.Coordinate.Longitude);

				MposFramework.Instance.DeviceController.Device.Latitude = latitude;
				MposFramework.Instance.DeviceController.Device.Longitude = longitude;

				Debug.WriteLine("# Location collect completed");
				Debug.WriteLine("# Device: " + MposFramework.Instance.DeviceController.Device.ToString());
			}
			catch (Exception e)
			{
				if ((uint)e.HResult == 0x80004004)
				{
					throw new GeopositionException("Location is disabled in phone settings.");
				}
				else
				{
					throw new GeopositionException("Something else happened acquring the GPS location.");
				}
			}
			finally
			{
				if (locationTask != null)
				{
					if (locationTask.Status == AsyncStatus.Started) { locationTask.Cancel(); }
					locationTask.Close();
				}
			}
		}
	}
}