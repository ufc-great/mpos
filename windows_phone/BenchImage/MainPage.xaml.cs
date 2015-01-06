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
using BenchImage.Core.Image;
using BenchImage.Core.Persistence;
using BenchImage.Core.Persistence.Model;
using BenchImage.Core.Util;
using Microsoft.Phone.Controls;
using System;
using System.Diagnostics;
using System.IO;
using System.Windows;
using System.Windows.Media.Imaging;
using Ufc.MpOS.Util.TaskOp;

namespace BenchImage
{
	public partial class MainPage : PhoneApplicationPage
	{
		private string photoName;
		private FileLoaderTask fileLoader;
		private ImageTask task;

		private Stopwatch stopWatch;

		public static AppConfiguration AppConfig { set; get; }
		public static CloudletFilter CloudletFilter { set; private get; }
		public static InternetFilter InternetFilter { set; private get; }
		private Filter FilterLocal = new ImageFilter();

		private BitmapImage blankScreen = new BitmapImage();

		public MainPage()
		{
			InitializeComponent();

			blankScreen.DecodePixelWidth = 640;
			blankScreen.DecodePixelHeight = 480;

			ConfigureListPickers();
			GetConfigFromListPicker();
			ConfigureStatusView();

			LoadSelectedImage();

			task = new ImageTask();
		}

		private void ConfigureListPickers()
		{
			string[] photo = { "FAB Show", "Cidade", "SkyLine" };
			this.ComboImage.ItemsSource = photo;
			this.ComboImage.SelectedIndex = 1;

			string[] filter = { "Original", "Benchmark", "Cartoonizer", "Sharpen", "Red Ton" };
			this.ComboFilter.ItemsSource = filter;
			this.ComboFilter.SelectedIndex = 0;

			string[] size = { "0.3MP", "1MP", "2MP", "4MP", "8MP" };
			this.ComboSize.ItemsSource = size;
			this.ComboSize.SelectedIndex = 4;

			string[] local = { "Local", "Cloudlet", "Internet" };
			this.ComboLocal.ItemsSource = local;
			this.ComboLocal.SelectedIndex = 0;
		}

		private void GetConfigFromListPicker()
		{
			photoName = (this.ComboImage.SelectedItem as string);
			AppConfig.Local = (this.ComboLocal.SelectedItem as string);
			AppConfig.Filter = (this.ComboFilter.SelectedItem as string);
			if (AppConfig.Filter.Equals("Benchmark"))
			{
				AppConfig.Size = "All";
			}
			else
			{
				AppConfig.Size = (this.ComboSize.SelectedItem as string);
			}
		}

		private void ConfigureStatusViewOnTaskStart()
		{
			ConfigureStatusView();
			this.ImageView.Source = blankScreen;
		}

		private void ConfigureStatusView()
		{
			Dispatcher.BeginInvoke(() =>
			{
				this.TextExec.Text = "Tempo de Execução: 0,000s";
				this.TextSize.Text = "Tamanho/Foto: " + AppConfig.Size + "/" + photoName;
			});
		}

		private void ConfigureStatusView(long totalTime)
		{
			Dispatcher.BeginInvoke(() =>
			{
				this.TextExec.Text = string.Format("Tempo de Execução: {0:F3}s", (totalTime / 1000.0));
				this.TextSize.Text = "Tamanho/Foto: " + AppConfig.Size + "/" + photoName;
			});
		}

		private void OnButtonExecuteClick(object sender, RoutedEventArgs e)
		{
			GetConfigFromListPicker();
			ConfigureStatusViewOnTaskStart();

			Dispatcher.BeginInvoke(() =>
			{
				ButtonExecute.Content = "Processando";
				ButtonExecute.IsEnabled = false;

				LoadSelectedImage();
			});
		}

		private void OnButtonExportDataClick(object sender, EventArgs e)
		{
			Dispatcher.BeginInvoke(() =>
			{
				new DataExportTask(new ResultDao().RetrieveAll()).Start();
			});
		}

		private void LoadSelectedImage()
		{
			fileLoader = new FileLoaderTask();
			fileLoader.RunCompleted += new RunCompletedEventHandler<ImageJpeg>(LoadCompleteImage);

			this.TextStatus.Text = "Status: Aplicando " + AppConfig.Filter;

			stopWatch = Stopwatch.StartNew();

			AppConfig.Image = PhotoUtilities.PhotoNameToFileName(photoName);
			if (AppConfig.Filter.Equals("Benchmark"))
			{
				AppConfig.Size = "0.3MP";
			}

			fileLoader.Config = AppConfig;
			fileLoader.Execute();
		}

		private void LoadCompleteImage(ImageJpeg file)
		{
			ResultImage result = null;
			switch (AppConfig.Local)
			{
				case "Local":
					result = task.ExecuteTask(file, FilterLocal, AppConfig, photoName, stopWatch);
					break;

				case "Cloudlet":
					result = task.ExecuteTask(file, CloudletFilter, AppConfig, photoName, stopWatch);
					break;

				case "Internet":
					result = task.ExecuteTask(file, InternetFilter, AppConfig, photoName, stopWatch);
					break;
			}

			SetImageView(result.Image);

			ConfigureStatusView(result.TotalTime);

			ButtonExecute.Content = "Inicia";
			ButtonExecute.IsEnabled = true;

			Dispatcher.BeginInvoke(() =>
			{
				this.TextStatus.Text = "Status: Terminou Processamento!";
			});

			//clean
			fileLoader.RunCompleted -= new RunCompletedEventHandler<ImageJpeg>(LoadCompleteImage);
			result = null;
			GC.Collect();
		}

		private void SetImageView(byte[] image)
		{
			BitmapImage bitmapImage = new BitmapImage();
			bitmapImage.SetSource(new MemoryStream(image));
			ImageView.Source = bitmapImage;
		}
	}
}