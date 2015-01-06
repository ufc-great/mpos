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
using System.Reflection;
using System.Windows;
using Ufc.MpOS.Config;
using Ufc.MpOS.Net.Endpoint;
using Ufc.MpOS.Net.Profile;
using Ufc.MpOS.Offload;
using Ufc.MpOS.Proxy;
using Ufc.MpOS.Util.Device;
using Ufc.MpOS.Util.Exceptions;

namespace Ufc.MpOS
{
	public sealed class MposFramework
	{
		private static readonly MposFramework instance = new MposFramework();
		public static MposFramework Instance { get { return instance; } }

		private DeviceController deviceController;
		public DeviceController DeviceController { get { return deviceController; } }

		private EndpointController endpointController;
		public EndpointController EndpointController { get { return endpointController; } }

		private ProfileController profileController;
		public ProfileController ProfileController { get { return profileController; } }

		//avoid start twice times...
		private bool start = false;

		private MposFramework() { }

		public void Start(Application app, IProxy proxy)
		{
			try
			{
				if (!start)
				{
					start = true;
					ConfigureControllers(app);
				}
				InjectObjects(app, proxy);
			}
			catch (Exception e)
			{
				Debug.WriteLine("[ERROR]: " + e.StackTrace);
			}
		}

		private void ConfigureControllers(Application application)
		{
			Type localClass = application.GetType();

			deviceController = new DeviceController();

			Debug.WriteLine("## AppName: " + deviceController.AppName);
			Debug.WriteLine("## Version: " + deviceController.Version);

			foreach (object attribute in localClass.GetCustomAttributes(true))
			{
				if (attribute is MposConfig)
				{
					MposConfig appConfig = (MposConfig)attribute;
					Debug.WriteLine(appConfig);

					if (appConfig.DeviceDetails)
					{
						deviceController.CollectDeviceConfig();
					}
					if (appConfig.LocationCollect)
					{
						deviceController.CollectLocation();
					}

					if (appConfig.EndpointSecondary == null && !appConfig.DiscoveryCloudlet)
					{
						throw new NetworkException("You must define an internet server IP or allow the service discovery!");
					}

					profileController = new ProfileController(appConfig.Profile);
					endpointController = new EndpointController(appConfig.EndpointSecondary, appConfig.DecisionMakerActive, appConfig.DiscoveryCloudlet);

					break;
				}
			}
		}

		private void InjectObjects(Application app, IProxy proxy)
		{
			Type localClass = app.GetType();

			BindingFlags flags = BindingFlags.Public | BindingFlags.NonPublic
							   | BindingFlags.Instance | BindingFlags.DeclaredOnly;

			foreach (FieldInfo field in localClass.GetFields(flags))
			{
				foreach (object injectAttribute in field.GetCustomAttributes(true))
				{
					if (injectAttribute is Inject)
					{
						if (field.FieldType.IsInterface)
						{
							Inject inject = injectAttribute as Inject;
							Debug.WriteLine(">> Interface: '" + field.FieldType.Name + "' request a injecting class: " + inject.Type.Name);

							bool remoteSupport = false;
							foreach (MethodInfo method in field.FieldType.GetMethods())
							{
								foreach (object remotableAttribute in method.GetCustomAttributes(true))
								{
									if (remotableAttribute is Remotable)
									{
										remoteSupport = true;
										goto FoundSupport;
									}
								}
							}

						FoundSupport:
							if (remoteSupport)
							{
								field.SetValue(app, ProxyHandler.NewInstance(proxy, inject.Type, field.FieldType));
							}
							else
							{
								throw new InstantiationException("The injection required a interface with remotable annotation!");
							}
						}
						else
						{
							throw new InstantiationException("The injection annotation required a object interface, not a concrete class or primitive type!");
						}
					}
				}
			}
		}
	}
}