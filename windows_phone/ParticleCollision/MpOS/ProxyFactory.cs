

using System;
using Ufc.MpOS.Proxy;
using System.Reflection;
using System.Collections.Generic;

	using ParticleCollision.Core.Model;
			using ParticleCollision.Core;
			using Ufc.MpOS.Net.Rpc.Util;
			using Ufc.MpOS.Offload;
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
			
			namespace Ufc.MpOS.Proxy.Temp
			{
			public sealed class Proxy_BallUpdater : BallUpdater
			{
				private BallUpdater instanceObject;
				private InvocationHandler instanceHandler;

				private Dictionary<string,MethodInfo> methodCache = new Dictionary<string,MethodInfo>(13);
				private Dictionary<string,object[]> attributeCache = new Dictionary<string,object[]>(13);

			public Proxy_BallUpdater(object instanceObject, InvocationHandler instanceHandler)
			{
				this.instanceObject = (BallUpdater)instanceObject;
				this.instanceHandler = instanceHandler;
			}

			public List<Ball> UpdateStatic(List<Ball> balls, int width, int height)
			{
				MethodInfo methodInfo = null;
				object[] attributes = null;

				if (!methodCache.TryGetValue("0", out methodInfo)){
					methodInfo = instanceObject.GetType().GetMethod("UpdateStatic", new Type[]{ balls.GetType(), width.GetType(), height.GetType()});
					methodCache.Add("0", methodInfo);
				}

				if (!attributeCache.TryGetValue("0", out attributes)){
					MethodInfo method = typeof(BallUpdater).GetMethod("UpdateStatic", new Type[]{ balls.GetType(), width.GetType(), height.GetType()});
					if (method != null){
						attributes = method.GetCustomAttributes(true);
					}else{
						attributes = new object[0];
					}
					attributeCache.Add("0", attributes);
				}

				return (List<Ball>)instanceHandler.Invoke(instanceObject, methodInfo, attributes, new object[] {balls, width, height});
			}

			
			public List<Ball> UpdateDynamic(List<Ball> balls, int width, int height)
			{
				MethodInfo methodInfo = null;
				object[] attributes = null;

				if (!methodCache.TryGetValue("1", out methodInfo)){
					methodInfo = instanceObject.GetType().GetMethod("UpdateDynamic", new Type[]{ balls.GetType(), width.GetType(), height.GetType()});
					methodCache.Add("1", methodInfo);
				}

				if (!attributeCache.TryGetValue("1", out attributes)){
					MethodInfo method = typeof(BallUpdater).GetMethod("UpdateDynamic", new Type[]{ balls.GetType(), width.GetType(), height.GetType()});
					if (method != null){
						attributes = method.GetCustomAttributes(true);
					}else{
						attributes = new object[0];
					}
					attributeCache.Add("1", attributes);
				}

				return (List<Ball>)instanceHandler.Invoke(instanceObject, methodInfo, attributes, new object[] {balls, width, height});
			}

			
			public List<Ball> UpdateOffline(List<Ball> balls, int width, int height)
			{
				MethodInfo methodInfo = null;
				object[] attributes = null;

				if (!methodCache.TryGetValue("2", out methodInfo)){
					methodInfo = instanceObject.GetType().GetMethod("UpdateOffline", new Type[]{ balls.GetType(), width.GetType(), height.GetType()});
					methodCache.Add("2", methodInfo);
				}

				if (!attributeCache.TryGetValue("2", out attributes)){
					MethodInfo method = typeof(BallUpdater).GetMethod("UpdateOffline", new Type[]{ balls.GetType(), width.GetType(), height.GetType()});
					if (method != null){
						attributes = method.GetCustomAttributes(true);
					}else{
						attributes = new object[0];
					}
					attributeCache.Add("2", attributes);
				}

				return (List<Ball>)instanceHandler.Invoke(instanceObject, methodInfo, attributes, new object[] {balls, width, height});
			}

			
				}
			
			public sealed class Proxy_BallUpdaterCustom : BallUpdaterCustom
			{
				private BallUpdaterCustom instanceObject;
				private InvocationHandler instanceHandler;

				private Dictionary<string,MethodInfo> methodCache = new Dictionary<string,MethodInfo>(13);
				private Dictionary<string,object[]> attributeCache = new Dictionary<string,object[]>(13);

			public Proxy_BallUpdaterCustom(object instanceObject, InvocationHandler instanceHandler)
			{
				this.instanceObject = (BallUpdaterCustom)instanceObject;
				this.instanceHandler = instanceHandler;
			}

			public List<Ball> UpdateStatic(List<Ball> balls)
			{
				MethodInfo methodInfo = null;
				object[] attributes = null;

				if (!methodCache.TryGetValue("3", out methodInfo)){
					methodInfo = instanceObject.GetType().GetMethod("UpdateStatic", new Type[]{ balls.GetType()});
					methodCache.Add("3", methodInfo);
				}

				if (!attributeCache.TryGetValue("3", out attributes)){
					MethodInfo method = typeof(BallUpdaterCustom).GetMethod("UpdateStatic", new Type[]{ balls.GetType()});
					if (method != null){
						attributes = method.GetCustomAttributes(true);
					}else{
						attributes = new object[0];
					}
					attributeCache.Add("3", attributes);
				}

				return (List<Ball>)instanceHandler.Invoke(instanceObject, methodInfo, attributes, new object[] {balls});
			}

			
			public List<Ball> UpdateDynamic(List<Ball> balls)
			{
				MethodInfo methodInfo = null;
				object[] attributes = null;

				if (!methodCache.TryGetValue("4", out methodInfo)){
					methodInfo = instanceObject.GetType().GetMethod("UpdateDynamic", new Type[]{ balls.GetType()});
					methodCache.Add("4", methodInfo);
				}

				if (!attributeCache.TryGetValue("4", out attributes)){
					MethodInfo method = typeof(BallUpdaterCustom).GetMethod("UpdateDynamic", new Type[]{ balls.GetType()});
					if (method != null){
						attributes = method.GetCustomAttributes(true);
					}else{
						attributes = new object[0];
					}
					attributeCache.Add("4", attributes);
				}

				return (List<Ball>)instanceHandler.Invoke(instanceObject, methodInfo, attributes, new object[] {balls});
			}

			
			public List<Ball> UpdateOffline(List<Ball> balls)
			{
				MethodInfo methodInfo = null;
				object[] attributes = null;

				if (!methodCache.TryGetValue("5", out methodInfo)){
					methodInfo = instanceObject.GetType().GetMethod("UpdateOffline", new Type[]{ balls.GetType()});
					methodCache.Add("5", methodInfo);
				}

				if (!attributeCache.TryGetValue("5", out attributes)){
					MethodInfo method = typeof(BallUpdaterCustom).GetMethod("UpdateOffline", new Type[]{ balls.GetType()});
					if (method != null){
						attributes = method.GetCustomAttributes(true);
					}else{
						attributes = new object[0];
					}
					attributeCache.Add("5", attributes);
				}

				return (List<Ball>)instanceHandler.Invoke(instanceObject, methodInfo, attributes, new object[] {balls});
			}

			
			public void CanvasDimensions(int width, int height)
			{
				MethodInfo methodInfo = null;
				object[] attributes = null;

				if (!methodCache.TryGetValue("6", out methodInfo)){
					methodInfo = instanceObject.GetType().GetMethod("CanvasDimensions", new Type[]{ width.GetType(), height.GetType()});
					methodCache.Add("6", methodInfo);
				}

				if (!attributeCache.TryGetValue("6", out attributes)){
					MethodInfo method = typeof(BallUpdaterCustom).GetMethod("CanvasDimensions", new Type[]{ width.GetType(), height.GetType()});
					if (method != null){
						attributes = method.GetCustomAttributes(true);
					}else{
						attributes = new object[0];
					}
					attributeCache.Add("6", attributes);
				}

				instanceHandler.Invoke(instanceObject, methodInfo, attributes, new object[] {width, height});
			}

			public void WriteMethodParams(System.IO.BinaryWriter writer, string methodName, System.Object[] methodParams)
			{
				MethodInfo methodInfo = null;
				object[] attributes = null;

				if (!methodCache.TryGetValue("7", out methodInfo)){
					methodInfo = instanceObject.GetType().GetMethod("WriteMethodParams", new Type[]{ writer.GetType(), methodName.GetType(), methodParams.GetType()});
					methodCache.Add("7", methodInfo);
				}

				if (!attributeCache.TryGetValue("7", out attributes)){
					MethodInfo method = typeof(BallUpdaterCustom).GetMethod("WriteMethodParams", new Type[]{ writer.GetType(), methodName.GetType(), methodParams.GetType()});
					if (method != null){
						attributes = method.GetCustomAttributes(true);
					}else{
						attributes = new object[0];
					}
					attributeCache.Add("7", attributes);
				}

				instanceHandler.Invoke(instanceObject, methodInfo, attributes, new object[] {writer, methodName, methodParams});
			}

public object[] ReadMethodParams(System.IO.BinaryReader reader, string methodName)
			{
				MethodInfo methodInfo = null;
				object[] attributes = null;

				if (!methodCache.TryGetValue("8", out methodInfo)){
					methodInfo = instanceObject.GetType().GetMethod("ReadMethodParams", new Type[]{ reader.GetType(), methodName.GetType()});
					methodCache.Add("8", methodInfo);
				}

				if (!attributeCache.TryGetValue("8", out attributes)){
					MethodInfo method = typeof(BallUpdaterCustom).GetMethod("ReadMethodParams", new Type[]{ reader.GetType(), methodName.GetType()});
					if (method != null){
						attributes = method.GetCustomAttributes(true);
					}else{
						attributes = new object[0];
					}
					attributeCache.Add("8", attributes);
				}

				return (object[])instanceHandler.Invoke(instanceObject, methodInfo, attributes, new object[] {reader, methodName});
			}

public void WriteMethodReturn(System.IO.BinaryWriter writer, string methodName, System.Object returnParam)
			{
				MethodInfo methodInfo = null;
				object[] attributes = null;

				if (!methodCache.TryGetValue("9", out methodInfo)){
					methodInfo = instanceObject.GetType().GetMethod("WriteMethodReturn", new Type[]{ writer.GetType(), methodName.GetType(), returnParam.GetType()});
					methodCache.Add("9", methodInfo);
				}

				if (!attributeCache.TryGetValue("9", out attributes)){
					MethodInfo method = typeof(BallUpdaterCustom).GetMethod("WriteMethodReturn", new Type[]{ writer.GetType(), methodName.GetType(), returnParam.GetType()});
					if (method != null){
						attributes = method.GetCustomAttributes(true);
					}else{
						attributes = new object[0];
					}
					attributeCache.Add("9", attributes);
				}

				instanceHandler.Invoke(instanceObject, methodInfo, attributes, new object[] {writer, methodName, returnParam});
			}

public object ReadMethodReturn(System.IO.BinaryReader reader, string methodName)
			{
				MethodInfo methodInfo = null;
				object[] attributes = null;

				if (!methodCache.TryGetValue("10", out methodInfo)){
					methodInfo = instanceObject.GetType().GetMethod("ReadMethodReturn", new Type[]{ reader.GetType(), methodName.GetType()});
					methodCache.Add("10", methodInfo);
				}

				if (!attributeCache.TryGetValue("10", out attributes)){
					MethodInfo method = typeof(BallUpdaterCustom).GetMethod("ReadMethodReturn", new Type[]{ reader.GetType(), methodName.GetType()});
					if (method != null){
						attributes = method.GetCustomAttributes(true);
					}else{
						attributes = new object[0];
					}
					attributeCache.Add("10", attributes);
				}

				return (object)instanceHandler.Invoke(instanceObject, methodInfo, attributes, new object[] {reader, methodName});
			}


				}
			}
			
namespace Ufc.MpOS.Proxy
{
     public class ProxyFactory : IProxy
     {
        public object NewProxyInstance(object instanceObject, Type objInterface, InvocationHandler handler)
        {
			
			
			switch (objInterface.Name)
            {
            
  				case "BallUpdater":
					return new Ufc.MpOS.Proxy.Temp.Proxy_BallUpdater(instanceObject,handler);
  				case "BallUpdaterCustom":
					return new Ufc.MpOS.Proxy.Temp.Proxy_BallUpdaterCustom(instanceObject,handler);
 					}
							return null;
		}
	 }
}

