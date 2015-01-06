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
using Ufc.MpOS.Net.Endpoint;
using Ufc.MpOS.Net.Rpc;
using Ufc.MpOS.Net.Rpc.Util;
using Ufc.MpOS.Proxy;
using Ufc.MpOS.Util.Exceptions; 

namespace Ufc.MpOS.Offload
{
	public class ProxyHandler : InvocationHandler
	{
		private Object objOriginal = null;
		private bool manualSerialization = false;
		
		private RpcClient rpc = null;
		private Stopwatch watch = null;

		private ProxyHandler(Object objOriginal)
		{
			this.objOriginal = objOriginal;
			this.manualSerialization = objOriginal is RpcSerializable;
			rpc = new RpcClient();
			watch = new Stopwatch();
		}

		public static Object NewInstance(IProxy proxy, Type clsType, Type objInterface)
		{
			Object objectInstance = Activator.CreateInstance(clsType);
			return proxy.NewProxyInstance(objectInstance, objInterface, new ProxyHandler(objectInstance));
		}

		public object Invoke(object original, MethodInfo method, object[] methodAttributes, object[] methodParams)
		{
			foreach (object remotableAttribute in methodAttributes)
			{
				if (remotableAttribute is Remotable)
				{
					//Debug.WriteLine(">> ProxyHandler '" + proxy.GetType().Name + "' call Remotable method: " + method + " with params: " + methodParams);

					Remotable remotable = remotableAttribute as Remotable;

					ServerContent server = MposFramework.Instance.EndpointController.SelectPriorityServer(remotable.CloudletPriority);
					if (remotable.Type == Offload.STATIC)
					{
						if (server != null)
						{
							try
							{
								return InvokeRemotable(server, remotable.Status, method, methodParams);
							}
							catch (ConnectException e)
							{
								Debug.WriteLine("[DEBUG]: " + e.Message);
								MposFramework.Instance.EndpointController.RediscoveryServices(server);
							}
							catch (Exception e)
							{
								Debug.WriteLine("[DEBUG]: " + e.Message);
							}
						}
					}
					else
					{
						try
						{
							if (server != null)
							{
								MposFramework.Instance.EndpointController.UpdateDynamicDecisionSystemEndpoint(server);
								if (MposFramework.Instance.EndpointController.RemoteAdvantage)
								{
									return InvokeRemotable(server, remotable.Status, method, methodParams);
								}
							}

						}
						catch (ConnectException e)
						{
							Debug.WriteLine("[DEBUG]: " + e.Message);
							MposFramework.Instance.EndpointController.RediscoveryServices(server);

							//cloudlet fails try using the internet service
							if (server.Type == EndpointType.CLOUDLET)
							{
								try
								{
									//NOTICE: only on real device, can check connection, however use this line
									//server = MposFramework.Instance.EndpointController.InternetServer;
									server = MposFramework.Instance.EndpointController.CheckSecondaryServer();
									if (server != null)
									{
										return InvokeRemotable(server, remotable.Status, method, methodParams);
									}
								}
								catch (ConnectException eIntern)
								{
									Debug.WriteLine("[DEBUG]: Inner try -> " + eIntern.Message);
									MposFramework.Instance.EndpointController.RediscoveryServices(server);
								}
								catch (Exception eIntern)
								{
									Debug.WriteLine("[DEBUG]: Inner try -> " + eIntern.Message);
								}
							}
						}
						catch (Exception e)
						{
							Debug.WriteLine("[DEBUG]: " + e.Message);
						}
					}

					if (remotable.Status) {
						watch.Reset();
						watch.Start();
						object objReturn = method.Invoke(objOriginal, methodParams);
						watch.Stop();

						EndpointController.rpcProfile.ExecutionCpuTime = watch.ElapsedMilliseconds;
						//Debug.WriteLine("[DEBUG]: Results -> " + EndpointController.rpcProfile.ToString());
						return objReturn;
					}
				}
			}

			//Debug.WriteLine("#> ProxyHandler '" + proxy.GetType().Name + "' call Local method: " + method + " with params: " + methodParams);
			return method.Invoke(objOriginal, methodParams);
		}

		private object InvokeRemotable(ServerContent server, bool needProfile, MethodInfo method, object[] methodParams)
		{
			rpc.SetupServer(server);
			object returnMethod = rpc.Call(needProfile, manualSerialization, objOriginal, method.Name, methodParams);

			if (needProfile)
			{
				var profile = rpc.Profile;
				//Debug.WriteLine("[DEBUG]: Results -> " + profile.ToString());
				EndpointController.rpcProfile = profile;
			}

			if (returnMethod != null)
			{
				return returnMethod;
			}
			else
			{
				throw new RpcException("Method (failed): " + method.Name + ", return 'null' value from remotable method");
			}
		}
	}
}