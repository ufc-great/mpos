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
using BenchImage.Core.Persistence.Model;
using BenchImage.Core.Util;
using System;
using System.Collections.Generic;
using System.IO;
using System.Threading.Tasks;
using Ufc.MpOS.Util.TaskOp;
using Windows.Storage;

namespace BenchImage.Core.Image
{
	//Limitations: http://msdn.microsoft.com/en-us/library/windowsphone/develop/ff402541(v=vs.105).aspx
	public sealed class FileLoaderTask : AsyncTask<ImageJpeg>
	{
		public AppConfiguration Config { get; set; }

		protected override ImageJpeg DoInBackground()
		{
			ImageJpeg image = new ImageJpeg();
			if (Config.Filter.Equals("Red Ton"))
			{
				var task = new Task[] { PhotoUtilities.LoadPhotoTask(Config.Size, Config.Image) };
				Task.WaitAll(task);
				task = new Task[] { PhotoUtilities.LoadFilterTask(Config.Filter) };
				Task.WaitAll(task);

				image.ImageJpg = PhotoUtilities.ImageJpg;
				image.FilterJpg = PhotoUtilities.FilterJpg;
			}
			else
			{
				var task = new Task[] { PhotoUtilities.LoadPhotoTask(Config.Size, Config.Image) };
				Task.WaitAll(task);
				image.ImageJpg = PhotoUtilities.ImageJpg;
			}
			return image;
		}
	}

	public sealed class FileSaveTask : Runnable
	{
		private byte[] image;
		private string name;

		public FileSaveTask(string name, byte[] image)
		{
			this.name = name;
			this.image = image;
		}

		protected override void Run()
		{
			var task = new Task[] { PhotoUtilities.SavePhotoTask(image, name) };
			Task.WaitAll(task);
		}
	}

	public sealed class DataExportTask : Runnable
	{
		private List<ResultImage> resultList;

		public DataExportTask(List<ResultImage> data)
		{
			this.resultList = data;
		}

		protected override void Run()
		{
			var task = new Task[] { SaveData() };
			Task.WaitAll(task);
		}

		private async Task SaveData()
		{
			StorageFolder local = ApplicationData.Current.LocalFolder;
			StorageFile file = await local.CreateFileAsync("benchimage_results.csv", CreationCollisionOption.ReplaceExisting);

			using (var stream = new StreamWriter(await file.OpenStreamForWriteAsync()))
			{
				stream.WriteLine("id,photo_name,filter_name,local,size,exec_cpu_time,upload_time,down_time,total_time,up_size,down_size,date");
				foreach (ResultImage result in resultList)
				{
					stream.WriteLine(result.ToString());
				}
			}
		}
	}
}