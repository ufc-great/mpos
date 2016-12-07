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
using ParticleCollision.Core.Model;
using System;
using System.IO.IsolatedStorage;
using System.Windows;
using System.Windows.Navigation;

namespace ParticleCollision
{
	public partial class SettingPage : PhoneApplicationPage
	{
		private float[] ballRadius = { 6.5f, 5.0f, 3.5f, 3.5f, 3.0f};

		private string[] quantityBall = { "250", "500", "750", "1000", "1500" };
		private string[] executionType = { "Local", "Static Offload", "Dynamic Offload" };

		private Configuration config = new Configuration();

		public SettingPage()
		{
			InitializeComponent();
			ConfigureListPickers();

			LoadStorageSettings();
		}

		private void ConfigureListPickers()
		{			
			this.QuantidadeListPicker.ItemsSource = quantityBall;
			this.QuantidadeListPicker.SelectedIndex = 0;

			this.ExecutionListPicker.ItemsSource = executionType;
			this.ExecutionListPicker.SelectedIndex = 0;
		}

		private void LoadStorageSettings()
		{
			config.Load(IsolatedStorageSettings.ApplicationSettings);
			LugarInicialSwitch.IsChecked = config.PlaceStart;
			SerializacaoSwitch.IsChecked = config.SerialNative;

			int count = 0;
			foreach (string quantity in quantityBall)
			{
				if (config.Quantity == Convert.ToInt32(quantity))
				{
					QuantidadeListPicker.SelectedIndex = count;
					break;
				}
				count++;
			}

			count = 0;
			foreach (string execution in executionType)
			{
				if (config.ExecutitonType.Equals(execution))
				{
					ExecutionListPicker.SelectedIndex = count;
					break;
				}
				count++;
			}

			config.RadiusBall = ballRadius[QuantidadeListPicker.SelectedIndex];

			if (config.PlaceStart)
			{
				LugarInicialSwitch.Content = "Fixo";
			}
			else
			{
				LugarInicialSwitch.Content = "Aleatório";
			}

			if (config.SerialNative)
			{
				SerializacaoSwitch.Content = "BSON (Automática)";
			}
			else
			{
				SerializacaoSwitch.Content = "Desempenho";
			}
		}

		private void OnButtonSaveClick(object sender, EventArgs e)
		{
			config.Quantity = Convert.ToInt32(QuantidadeListPicker.SelectedItem as string);
			config.ExecutitonType = ExecutionListPicker.SelectedItem as string;
			config.PlaceStart = Convert.ToBoolean(LugarInicialSwitch.IsChecked);
			config.SerialNative = Convert.ToBoolean(SerializacaoSwitch.IsChecked);
			config.RadiusBall = ballRadius[QuantidadeListPicker.SelectedIndex];

			config.Save(IsolatedStorageSettings.ApplicationSettings);

			NavigationService.GoBack();
		}

		//sent the message for first page!
		protected override void OnNavigatedFrom(NavigationEventArgs e)
		{
			base.OnNavigatedFrom(e);
			PhoneApplicationService.Current.State["Reload"] = "Config";
		}

		private void OnButtonVoltarClick(object sender, EventArgs e)
		{
			NavigationService.GoBack();
		}

		private void LugarInicialSwitchChecked(object sender, RoutedEventArgs e)
		{
			LugarInicialSwitch.Content = "Fixo";
		}

		private void LugarInicialSwitchUnchecked(object sender, RoutedEventArgs e)
		{
			LugarInicialSwitch.Content = "Aleatório";
		}

		private void SerializacaoSwitchChecked(object sender, RoutedEventArgs e)
		{
			SerializacaoSwitch.Content = "BSON (Automática)";
		}

		private void SerializacaoSwitchUnchecked(object sender, RoutedEventArgs e)
		{
			SerializacaoSwitch.Content = "Desempenho";
		}
	}
}