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
using Microsoft.Phone.Controls;
using Microsoft.Phone.Shell;
using ParticleCollision.Core;
using ParticleCollision.Core.Model;
using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Diagnostics;
using System.IO.IsolatedStorage;
using System.Threading;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Media;
using System.Windows.Navigation;
using System.Windows.Shapes;

namespace ParticleCollision
{
	public partial class MainPage : PhoneApplicationPage
	{
		private Configuration config = new Configuration();

		public static BallUpdater ControllerDefault { set; private get; }
		public static BallUpdaterCustom ControllerCustom { set; private get; }

		private List<Ball> balls;
		private readonly List<Ellipse> ellipses = new List<Ellipse>(500);

		private volatile bool running = false;
		private bool startAnimation = false;

		private int width, height;

		private readonly SolidColorBrush red = new SolidColorBrush();
		private readonly SolidColorBrush yellow = new SolidColorBrush();
		private ApplicationBarIconButton StartButton;

		private Thread render = null;
		private readonly EventWaitHandle waitThreadUi = new AutoResetEvent(false);

		private int countFps = 0;
		private float fps = 60.0f;
		private long cycleTime = 1000L;
		private readonly int FRAME_PER_SEC = 15;
		private Stopwatch timer;

		private readonly Random random = new Random();

		public MainPage()
		{
			InitializeComponent();

			BackKeyPress += OnBackKeyPress;

			config.Load(IsolatedStorageSettings.ApplicationSettings);

			red.Color = Colors.Red;
			yellow.Color = Colors.Yellow;
		}

		//post canvas load!
		private void OnLoadCanvas(object sender, RoutedEventArgs e)
		{
			StartButton = ApplicationBar.Buttons[0] as ApplicationBarIconButton;

			width = Convert.ToInt32(Canvas.RenderSize.Width) - 30;
			height = Convert.ToInt32(Canvas.RenderSize.Height) - 20;

			Debug.WriteLine("[DEBUG]: WindowSize -> " + width + "x" + height);

			ClearCanvas();
		}

		private void OnButtonStartClick(object sender, EventArgs e)
		{
			config.Load(IsolatedStorageSettings.ApplicationSettings);
			Debug.WriteLine("[DEBUG]: Configuration -> " + config.Quantity);

			if (StartButton.Text.Equals("Iniciar"))
			{
				StartButton.Text = "Parar";
				StartButton.IconUri = new Uri("/Assets/AppBar/transport.pause.png", UriKind.Relative);

				startAnimation = true;
				running = true;

				render = new Thread(RenderCanvas);
				render.Name = "RenderCanvas";
				render.Start();
			}
			else if (StartButton.Text.Equals("Parar"))
			{
				StartButton.Text = "Iniciar";
				StartButton.IconUri = new Uri("/Assets/AppBar/transport.play.png", UriKind.Relative);

				running = false;
				startAnimation = false;

				ClearCanvas();

				while (!render.IsAlive) ;
				render = null;
			}
		}

		private void OnButtonConfigurationClick(object sender, EventArgs e)
		{
			NavigationService.Navigate(new Uri("/SettingPage.xaml", UriKind.Relative));
			ClearCanvas();
		}

		//retrieve information from SettingPage.xaml
		protected override void OnNavigatedTo(NavigationEventArgs e)
		{
			base.OnNavigatedTo(e);
			if (PhoneApplicationService.Current.State.ContainsKey("Reload"))
			{
				config.Load(IsolatedStorageSettings.ApplicationSettings);
				ClearCanvas();
			}
		}

		private void OnBackKeyPress(object sender, CancelEventArgs e)
		{
			MessageBoxResult result = MessageBox.Show("Deseja encerrar aplicação?", "Sair", MessageBoxButton.OKCancel);
			switch (result)
			{
				case MessageBoxResult.Cancel:
					e.Cancel = true; //cancel default nav.
					break;

				default:
					break;
			}
		}

		private void ClearCanvas()
		{
			GenerateBalls();
			Dispatcher.BeginInvoke(() => DrawFirstScreen());

			fps = 60.0f;
			cycleTime = 1000L;
			Dispatcher.BeginInvoke(() => DrawStringFps());
		}

		private void RenderCanvas()
		{
			countFps = 0;

			if (ControllerCustom != null)
			{
				ControllerCustom.CanvasDimensions(width, height);
			}

			while (running)
			{
				InitTimeFrame();
				EndTimeFrame();

				Dispatcher.BeginInvoke(() => DrawStringFps());
				DrawScreen();

				waitThreadUi.WaitOne();
				countFps++;
			}

			Debug.WriteLine("[DEBUG]: Finish render.");
		}

		private void DrawScreen()
		{
			if (startAnimation)
			{
				if (config.SerialNative)
				{
					if (config.ExecutitonType.Equals("Local"))
					{
						balls = ControllerDefault.UpdateOffline(balls, width, height);
					}
					else if (config.ExecutitonType.Equals("Static Offload"))
					{
						balls = ControllerDefault.UpdateStatic(balls, width, height);
					}
					else if (config.ExecutitonType.Equals("Dynamic Offload"))
					{
						balls = ControllerDefault.UpdateDynamic(balls, width, height);
					}
				}
				else
				{
					if (config.ExecutitonType.Equals("Local"))
					{
						balls = ControllerCustom.UpdateOffline(balls);
					}
					else if (config.ExecutitonType.Equals("Static Offload"))
					{
						balls = ControllerCustom.UpdateStatic(balls);
					}
					else if (config.ExecutitonType.Equals("Dynamic Offload"))
					{
						balls = ControllerCustom.UpdateDynamic(balls);
					}
				}
			}

			Dispatcher.BeginInvoke(() =>
			{
				DrawBalls();
				waitThreadUi.Set();
			});
		}

		//only update itens in canvas!
		private void DrawBalls()
		{
			int item = 0;
			foreach (Ball ball in balls)
			{
				ball.GenerateBounds();

				Ellipse pic = ellipses[item++];

				//speedup fill use by 2 times...
				//now is red
				//if (ball.CollisionColor)
				//{
				//	if (pic.Fill == yellow)
				//	{
				//		pic.Fill = red;
				//	}
				//}
				//else
				//{
				//	if (pic.Fill == red)
				//	{
				//		pic.Fill = yellow;
				//	}
				//}

				//same implementation used on Android by method onCanvas.
				if (ball.CollisionColor)
				{
					pic.Fill = red;
				}
				else
				{
					pic.Fill = yellow;
				}

				Canvas.SetLeft(pic, ball.BallX);
				Canvas.SetTop(pic, ball.BallY);
			}
		}

		private void DrawFirstScreen()
		{
			Canvas.Children.Clear();
			ellipses.Clear();

			foreach (Ball ball in balls)
			{
				Ellipse pic = new Ellipse();

				pic.IsHitTestVisible = false;
				pic.Fill = ball.CollisionColor ? red : yellow;

				float widthBall = ball.Left - ball.Right;
				pic.Width = Convert.ToDouble(widthBall < 0.0f ? widthBall * -1.0f : widthBall);
				float heightBall = ball.Top - ball.Bottom;
				pic.Height = Convert.ToDouble(heightBall < 0.0f ? heightBall * -1.0f : heightBall);

				Canvas.SetLeft(pic, ball.BallX);
				Canvas.SetTop(pic, ball.BallY);
				ellipses.Add(pic);
				Canvas.Children.Add(pic);
			}
		}

		private void InitTimeFrame()
		{
			if (countFps % FRAME_PER_SEC == 0)
			{
				timer = Stopwatch.StartNew();
			}
		}

		private void EndTimeFrame()
		{
			//capture the last frame from 15fps for calcule the cycle...
			//cycletime how many 'ms' spent for made 15fps
			if (countFps % FRAME_PER_SEC == 14)
			{
				timer.Stop();
				cycleTime = timer.ElapsedMilliseconds;
				fps = (FRAME_PER_SEC / ((float)cycleTime / 1000.0f));
			}
		}

		private void DrawStringFps()
		{
			FpsTextBlock.Text = string.Format("FPS: {0:0.00}; Cycle Process: {1}ms; Frame: {2}", fps, cycleTime, countFps);
		}

		private void GenerateBalls()
		{
			int size = config.Quantity;
			balls = new List<Ball>(size);

			for (int i = 0; i < size; i++)
			{
				balls.Add(new Ball(config.RadiusBall, random, config.PlaceStart, width, height));
			}
		}
	}
}