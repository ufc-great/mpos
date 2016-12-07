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
using System.IO.IsolatedStorage;

namespace ParticleCollision.Core.Model
{
	public sealed class Configuration
	{
		private string QuantityKey = "Quantity";
		private string RadiusBallKey = "RadiusBall";
		private string PlaceStartKey = "PlaceStart";
		private string SerialNativeKey = "SerialNative";
		private string ExecutitonTypeKey = "ExecutitonType";

		public Configuration()
		{
			Quantity = 250;
			RadiusBall = 6.5f;
			PlaceStart = true;
			SerialNative = false;
			ExecutitonType = "Local";
		}

		public int Quantity { get; set; }
		public float RadiusBall { get; set; }
		public bool PlaceStart { get; set; }
		public bool SerialNative { get; set; }
		public string ExecutitonType { get; set; }

		public void Save(IsolatedStorageSettings storageSettings)
		{
			AddOrUpdate(storageSettings, Quantity, QuantityKey);
			AddOrUpdate(storageSettings, RadiusBall, RadiusBallKey);
			AddOrUpdate(storageSettings, PlaceStart, PlaceStartKey);
			AddOrUpdate(storageSettings, SerialNative, SerialNativeKey);
			AddOrUpdate(storageSettings, ExecutitonType, ExecutitonTypeKey);

			storageSettings.Save();
		}

		public void Load(IsolatedStorageSettings storageSettings)
		{
			if (storageSettings.Contains(QuantityKey))
			{
				Quantity = (int)storageSettings[QuantityKey];
			}
			if (storageSettings.Contains(RadiusBallKey))
			{
				RadiusBall = Convert.ToSingle(storageSettings[RadiusBallKey]);
			}
			if (storageSettings.Contains(PlaceStartKey))
			{
				PlaceStart = (bool)storageSettings[PlaceStartKey];
			}
			if (storageSettings.Contains(SerialNativeKey))
			{
				SerialNative = (bool)storageSettings[SerialNativeKey];
			}
			if (storageSettings.Contains(ExecutitonTypeKey))
			{
				ExecutitonType = (string)storageSettings[ExecutitonTypeKey];
			}
		}

		private void AddOrUpdate(IsolatedStorageSettings storageSettings, object obj, string key)
		{
			if (!storageSettings.Contains(key))
			{
				storageSettings.Add(key, obj);
			}
			else
			{
				storageSettings[key] = obj;//update
			}
		}
	}
}