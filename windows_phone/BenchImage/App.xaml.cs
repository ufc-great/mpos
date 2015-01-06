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
using BenchImage.Core.Persistence.Model;
using Microsoft.Phone.Controls;
using Microsoft.Phone.Shell;
using System;
using System.ComponentModel;
using System.Diagnostics;
using System.Threading.Tasks;
using System.Windows;
using System.Windows.Navigation;
using Ufc.MpOS;
using Ufc.MpOS.Config;
using Ufc.MpOS.Proxy;
using Ufc.MpOS.Util.TaskOp;
using Windows.ApplicationModel;
using Windows.Storage;

/**
 * Good Practices: http://msdn.microsoft.com/en-us/library/windowsphone/develop/ff967560(v=vs.105).aspx
 */
namespace BenchImage
{
	[MposConfig("54.94.157.178")]
	public partial class App : Application
	{
		[Inject(typeof(ImageFilter))]
		private CloudletFilter cloudletFilter = null;

		[Inject(typeof(ImageFilter))]
		private InternetFilter internetFilter = null;

		/// <summary>
		/// Provides easy access to the root frame of the Phone Application.
		/// </summary>
		/// <returns>The root frame of the Phone Application.</returns>
		public static PhoneApplicationFrame RootFrame { get; private set; }

		/// <summary>
		/// Constructor for the Application object.
		/// </summary>
		public App()
		{
			MposFramework.Instance.Start(this, new ProxyFactory());

			MainPage.CloudletFilter = cloudletFilter;
			MainPage.InternetFilter = internetFilter;

			// Global handler for uncaught exceptions.
			UnhandledException += Application_UnhandledException;

			// Standard XAML initialization
			InitializeComponent();
			// Phone-specific initialization
			InitializePhoneApplication();

			//create output folder
			CreateDirOutput();

			// Show graphics profiling information while debugging.
			if (Debugger.IsAttached)
			{
				// Display the current frame rate counters.
				Application.Current.Host.Settings.EnableFrameRateCounter = true;

				PhoneApplicationService.Current.UserIdleDetectionMode = IdleDetectionMode.Disabled;
			}

			//event for back key
			RootFrame.BackKeyPress += OnBackKeyPress;

			Debug.WriteLine("[App]: Iniciou PicFilter");
		}

		// Code to execute when the application is launching (eg, from Start)
		// This code will not execute when the application is reactivated
		private void Application_Launching(object sender, LaunchingEventArgs e)
		{
		}

		// Code to execute when the application is activated (brought to foreground)
		// This code will not execute when the application is first launched
		private void Application_Activated(object sender, ActivatedEventArgs e)
		{
		}

		// Code to execute when the application is deactivated (sent to background)
		// This code will not execute when the application is closing
		private void Application_Deactivated(object sender, DeactivatedEventArgs e)
		{
		}

		// Code to execute when the application is closing (eg, user hit Back)
		// This code will not execute when the application is deactivated
		private void Application_Closing(object sender, ClosingEventArgs e)
		{
			MessageBoxResult result = MessageBox.Show("Encerrar aplicação?", "Sair", MessageBoxButton.OKCancel);
			switch (result)
			{
				case MessageBoxResult.OK:

					break;
				case MessageBoxResult.Cancel:
					
					break;
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

		// Code to execute if a navigation fails
		private void RootFrame_NavigationFailed(object sender, NavigationFailedEventArgs e)
		{
			if (Debugger.IsAttached)
			{
				// A navigation has failed; break into the debugger
				Debugger.Break();
			}
		}

		// Code to execute on Unhandled Exceptions
		private void Application_UnhandledException(object sender, ApplicationUnhandledExceptionEventArgs e)
		{
			if (Debugger.IsAttached)
			{
				// An unhandled exception has occurred; break into the debugger
				Debugger.Break();
			}
		}

		private void CreateDirOutput()
		{
			CreateFolderTask task = new CreateFolderTask();
			task.Start();
		}

		private sealed class CreateFolderTask : Runnable
		{
			protected override void Run()
			{
				try
				{
					var task = new Task[] { ExecuteTask() };
					Task.WaitAll(task);
				}
				catch (AggregateException e)
				{
					Debug.WriteLine("[DEBUG]: " + e.ToString());
				}
			}

			private async Task ExecuteTask()
			{
				StorageFolder InstallationFolder = Package.Current.InstalledLocation;

				// another files operations...
				// http://msdn.microsoft.com/en-us/library/windowsphone/develop/jj681698(v=vs.105).aspx
				MainPage.AppConfig = new AppConfiguration();
				MainPage.AppConfig.OutputDirectory = await InstallationFolder.CreateFolderAsync("BenchImageOutput", CreationCollisionOption.OpenIfExists);
			}
		}

		#region Phone application initialization

		// Avoid double-initialization
		private bool phoneApplicationInitialized = false;

		// Do not add any additional code to this method
		private void InitializePhoneApplication()
		{
			if (phoneApplicationInitialized)
				return;

			// Create the frame but don't set it as RootVisual yet; this allows the splash
			// screen to remain active until the application is ready to render.
			RootFrame = new PhoneApplicationFrame();
			RootFrame.Navigated += CompleteInitializePhoneApplication;

			// Handle navigation failures
			RootFrame.NavigationFailed += RootFrame_NavigationFailed;

			// Handle reset requests for clearing the backstack
			RootFrame.Navigated += CheckForResetNavigation;

			// Ensure we don't initialize again
			phoneApplicationInitialized = true;
		}

		// Do not add any additional code to this method
		private void CompleteInitializePhoneApplication(object sender, NavigationEventArgs e)
		{
			// Set the root visual to allow the application to render
			if (RootVisual != RootFrame)
				RootVisual = RootFrame;

			// Remove this handler since it is no longer needed
			RootFrame.Navigated -= CompleteInitializePhoneApplication;
		}

		private void CheckForResetNavigation(object sender, NavigationEventArgs e)
		{
			// If the app has received a 'reset' navigation, then we need to check
			// on the next navigation to see if the page stack should be reset
			if (e.NavigationMode == NavigationMode.Reset)
				RootFrame.Navigated += ClearBackStackAfterReset;
		}

		private void ClearBackStackAfterReset(object sender, NavigationEventArgs e)
		{
			// Unregister the event so it doesn't get called again
			RootFrame.Navigated -= ClearBackStackAfterReset;

			// Only clear the stack for 'new' (forward) and 'refresh' navigations
			if (e.NavigationMode != NavigationMode.New && e.NavigationMode != NavigationMode.Refresh)
				return;

			// For UI consistency, clear the entire page stack
			while (RootFrame.RemoveBackEntry() != null)
			{
				; // do nothing
			}
		}

		#endregion
	}
}