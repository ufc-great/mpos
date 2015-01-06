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
package br.ufc.mdcc.mpos.offload;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.ConnectException;
import java.util.HashMap;
import java.util.Map;

import android.util.Log;
import br.ufc.mdcc.mpos.MposFramework;
import br.ufc.mdcc.mpos.net.endpoint.EndpointType;
import br.ufc.mdcc.mpos.net.endpoint.ServerContent;
import br.ufc.mdcc.mpos.net.rpc.RpcClient;
import br.ufc.mdcc.mpos.net.rpc.model.RpcProfile;
import br.ufc.mdcc.mpos.net.rpc.util.RpcException;
import br.ufc.mdcc.mpos.net.rpc.util.RpcSerializable;
import br.ufc.mdcc.mpos.offload.Remotable.Offload;

/**
 * Proxy Handler, decides execution with is local or remote using rpc!
 * 
 * @author Philipp B. Costa
 */
public final class ProxyHandler implements InvocationHandler {
	private static final String clsName = ProxyHandler.class.getName();
	private final Map<String, Remotable> methodCache;
	private final RpcClient rpc;

	private Object objOriginal;
	private boolean manualSerialization = false;

	private ProxyHandler(Object objProxy, Class<?> interfaceType) {
		this.objOriginal = objProxy;

		manualSerialization = objProxy instanceof RpcSerializable;
		rpc = new RpcClient();

		methodCache = new HashMap<String, Remotable>(5);
		buildMethodCache(interfaceType);
	}

	public static Object newInstance(Class<?> cls, Class<?> interfaceType) throws InstantiationException, IllegalAccessException, ClassNotFoundException {
		Log.i(clsName, "Creating proxy with interface: " + interfaceType.getSimpleName() + ", for class: " + cls.getSimpleName());

		Object objectInstance = Class.forName(cls.getName()).newInstance();
		return Proxy.newProxyInstance(cls.getClassLoader(), new Class<?>[] { interfaceType }, new ProxyHandler(objectInstance, interfaceType));
	}

	@Override
	public Object invoke(Object original, Method method, Object[] params) throws Throwable {
		Remotable remotable = methodCache.get(generateKeyMethod(method));

		if (remotable != null) {
			ServerContent server = MposFramework.getInstance().getEndpointController().selectPriorityServer(remotable.cloudletPrority());
			if (remotable.value() == Offload.STATIC) {
				if (server != null) {
					try {
						return invokeRemotable(server, remotable.status(), method, params);
					} catch (ConnectException e) {
						Log.w(clsName, e);
						MposFramework.getInstance().getEndpointController().rediscoveryServices(server);
					} catch (RpcException e) {
						Log.w(clsName, e);
					}
				}
			} else {
				try {
					if (server != null) {
						MposFramework.getInstance().getEndpointController().updateDynamicDecisionSystemEndpoint(server);
						if (MposFramework.getInstance().getEndpointController().isRemoteAdvantage()) {
							return invokeRemotable(server, remotable.status(), method, params);
						}
					}
				} catch (ConnectException e) {
					Log.w(clsName, e);
					MposFramework.getInstance().getEndpointController().rediscoveryServices(server);

					if (server.getType() == EndpointType.CLOUDLET) {
						try {
							server = MposFramework.getInstance().getEndpointController().checkSecondaryServer();
							if (server != null) {
								return invokeRemotable(server, remotable.status(), method, params);
							}
						} catch (ConnectException eIntern) {
							Log.w(clsName, eIntern);
							MposFramework.getInstance().getEndpointController().rediscoveryServices(server);
						} catch (RpcException eIntern) {
							Log.w(clsName, eIntern);
						}
					}
				} catch (RpcException e) {
					Log.w(clsName, e);
				}
			}

			//not remotable avaliable and need debug, get cpu time
			if (remotable.status()) {
				long initCpu = System.currentTimeMillis();
				Object object = method.invoke(objOriginal, params);
				MposFramework.getInstance().getEndpointController().rpcProfile.setExecutionCpuTimeLocal(System.currentTimeMillis() - initCpu);
				Log.i(clsName, MposFramework.getInstance().getEndpointController().rpcProfile.toString());
				return object;
			}
		}

		return method.invoke(objOriginal, params);
	}

	private Object invokeRemotable(ServerContent server, boolean needProfile, Method method, Object params[]) throws RpcException, ConnectException {
		rpc.setupServer(server);
		Object returnMethod = rpc.call(needProfile, manualSerialization, objOriginal, method.getName(), params);

		if (needProfile) {
			RpcProfile profile = rpc.getProfile();
			Log.i(clsName, profile.toString());
			MposFramework.getInstance().getEndpointController().rpcProfile = profile;
		}

		if (returnMethod != null) {
			return returnMethod;
		} else {
			throw new RpcException("Method (failed): " + method.getName() + ", return 'null' value from remotable method");
		}
	}

	private String generateKeyMethod(Method method) {
		StringBuilder key = new StringBuilder();
		key.append(method.getName()).append("-");
		for (Class<?> cls : method.getParameterTypes()) {
			key.append(":").append(cls.getName());
		}

		return key.toString();
	}

	private void buildMethodCache(Class<?> interfaceType) {
		Method methods[] = interfaceType.getDeclaredMethods();
		for (Method method : methods) {
			Remotable remotable = method.getAnnotation(Remotable.class);
			if (remotable != null) {
				methodCache.put(generateKeyMethod(method), remotable);
			}
		}
	}
}