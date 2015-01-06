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
using BenchImage.Core.Persistence;
using BenchImage.Core.Persistence.Model;
using BenchImage.Core.Util;
using System;
using System.Diagnostics;
using System.Threading;
using System.Threading.Tasks;
using Ufc.MpOS.Net.Endpoint;
using Ufc.MpOS.Util.TaskOp;

namespace BenchImage.Core.Image
{
	/*
	 * This "WP TRASH API" can't allow to use a background worker thread 
	 * for decode jpeg using BitmapImage [on new instance got exception]. This decode 
	 * only must be made in UI Thread by Dispatcher... ¬¬
	 * 
	 * Explain: http://msdn.microsoft.com/en-us/library/system.windows.media.imaging.writeablebitmap.aspx
	 */
	public sealed class ImageTask
	{
		private string photoName;
		private Stopwatch stopWatch;

		private Filter filter;
		private AppConfiguration appConfig;

		private ResultDao dao;

		public ImageTask()
		{
			dao = new ResultDao();
		}

		public ResultImage ExecuteTask(ImageJpeg imageFile, Filter filter, AppConfiguration appConfig, string photoName, Stopwatch stopWatch)
		{
			this.filter = filter;
			this.appConfig = appConfig;
			this.photoName = photoName;
			this.stopWatch = stopWatch;

			Debug.WriteLine("[BenchImage_DEBUG]: Iniciando aplicação de filtro");
			ResultImage result = null;
			switch (appConfig.Filter)
			{
				case "Original":
					result = OriginalTask(imageFile); break;
				case "Cartoonizer":
					result = CartoonizerTask(imageFile, filter); break;
				case "Benchmark":
					result = BenchmarkTask(filter); break;
				case "Sharpen":
					result = SharpenTask(imageFile, filter); break;
				default:
					result = FilterMapTask(imageFile, filter); break;
			}

			PhotoUtilities.ImageJpg = null;
			PhotoUtilities.FilterJpg = null;
			GC.Collect();

			Debug.WriteLine("[BenchImage_DEBUG]: Finalizou aplicação de filtro");
			return result;
		}

		private ResultImage OriginalTask(ImageJpeg image)
		{
			stopWatch.Stop();
			ResultImage result = new ResultImage(appConfig, EndpointController.rpcProfile, stopWatch.ElapsedMilliseconds);
			result.Image = image.ImageJpg;
			return result;
		}

		private ResultImage BenchmarkTask(Filter filter)
		{
			ResultImage result = null;

			int count = 1;
			string[] sizes = { "8MP", "4MP", "2MP", "1MP", "0.3MP" };
			foreach (string size in sizes)
			{
				appConfig.Size = size;
				new ImageLoadBenchmark(size, appConfig.Image).RunSynchronous();
				for (int i = 0; i < 3; i++)
				{
					Debug.WriteLine("Status: Benchmark [" + (count) + "/15]");
					count++;

					result = CartoonizerTask(PhotoUtilities.ImageJpg, filter);

					if (count != 16)
					{
						result = null;
						GC.Collect();
						Thread.Sleep(750);
					}
				}
				PhotoUtilities.ImageJpg = null;
				GC.Collect();
			}

			stopWatch.Stop();
			appConfig.Size = "Todos";
			ResultImage aggregateResult = new ResultImage(appConfig, EndpointController.rpcProfile, stopWatch.ElapsedMilliseconds);
			aggregateResult.Image = result.Image;

			dao.Add(aggregateResult);

			return aggregateResult;
		}

		private ResultImage CartoonizerTask(ImageJpeg file, Filter filter)
		{
			return CartoonizerTask(file.ImageJpg, filter);
		}

		private ResultImage CartoonizerTask(byte[] image, Filter filter)
		{
			if (appConfig.Filter.Equals("Benchmark"))
			{
				Stopwatch local = Stopwatch.StartNew();
				byte[] imageResult = filter.CartoonizerImage(image);

				new FileSaveTask(GeneratePhotoFileName(), imageResult).RunSynchronous();

				local.Stop();
				ResultImage result = new ResultImage(appConfig, EndpointController.rpcProfile, local.ElapsedMilliseconds);
				result.Image = imageResult;
				Debug.WriteLine("[BenchImage_DEBUG]: Adiciona no banco de dados local");
				dao.Add(result);

				return result;
			}
			else
			{
				byte[] imageResult = filter.CartoonizerImage(image);

				new FileSaveTask(GeneratePhotoFileName(), imageResult).RunSynchronous();

				stopWatch.Stop();
				ResultImage result = new ResultImage(appConfig, EndpointController.rpcProfile, stopWatch.ElapsedMilliseconds);
				result.Image = imageResult;
				Debug.WriteLine("[BenchImage_DEBUG]: Adiciona no banco de dados local");
				dao.Add(result);

				return result;
			}
		}

		private ResultImage SharpenTask(ImageJpeg file, Filter filter)
		{
			double[][] mask = new double[3][];
			mask[0] = new double[3] { -1.0, -1.0, -1.0 };
			mask[1] = new double[3] { -1.0, 9.0, -1.0 };
			mask[2] = new double[3] { -1.0, -1.0, -1.0 };

			double factor = 1.0;
			double bias = 0.0;

			byte[] imageResult = filter.FilterApply(file.ImageJpg, mask, factor, bias);

			new FileSaveTask(GeneratePhotoFileName(), imageResult).RunSynchronous();

			stopWatch.Stop();
			ResultImage result = new ResultImage(appConfig, EndpointController.rpcProfile, stopWatch.ElapsedMilliseconds);
			result.Image = imageResult;
			Debug.WriteLine("[BenchImage_DEBUG]: Adiciona no banco de dados local");
			dao.Add(result);

			return result;
		}

		private ResultImage FilterMapTask(ImageJpeg file, Filter filter)
		{
			byte[] imageResult = filter.MapTone(file.ImageJpg, file.FilterJpg);

			new FileSaveTask(GeneratePhotoFileName(), imageResult).RunSynchronous();

			stopWatch.Stop();
			ResultImage result = new ResultImage(appConfig, EndpointController.rpcProfile, stopWatch.ElapsedMilliseconds);
			result.Image = imageResult;
			Debug.WriteLine("[BenchImage_DEBUG]: Adiciona no banco de dados local");
			dao.Add(result);

			return result;
		}

		private string GeneratePhotoFileName()
		{
			return appConfig.Size.Equals("0.3MP") ? photoName + "_" + appConfig.Filter + "_0_3mp.jpg" : photoName + "_" + appConfig.Filter + "_" + appConfig.Size + ".jpg";
		}

		private sealed class ImageLoadBenchmark : Runnable
		{
			private string size, name;

			public ImageLoadBenchmark(string size, string name)
			{
				this.size = size;
				this.name = name;
			}

			protected override void Run()
			{
				var loadTask = new Task[] { PhotoUtilities.LoadPhotoTask(size, name) };
				Task.WaitAll(loadTask);
			}
		}
	}
}