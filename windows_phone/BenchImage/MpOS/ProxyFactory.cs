

using System;
using Ufc.MpOS.Proxy;
using System.Reflection;
using System.Collections.Generic;

	using BenchImage.Core.Image;
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
			
			/*
			* This interfaces method support offloading technic by data type attribute.
			*
			* @author Philipp B. Costa
			*/
			namespace Ufc.MpOS.Proxy.Temp
			{
			public sealed class Proxy_CloudletFilter : CloudletFilter
			{
				private CloudletFilter instanceObject;
				private InvocationHandler instanceHandler;

				private Dictionary<string,MethodInfo> methodCache = new Dictionary<string,MethodInfo>(13);
				private Dictionary<string,object[]> attributeCache = new Dictionary<string,object[]>(13);

			public Proxy_CloudletFilter(object instanceObject, InvocationHandler instanceHandler)
			{
				this.instanceObject = (CloudletFilter)instanceObject;
				this.instanceHandler = instanceHandler;
			}

			public byte[] MapTone(byte[] source, byte[] map)
			{
				MethodInfo methodInfo = null;
				object[] attributes = null;

				if (!methodCache.TryGetValue("0", out methodInfo)){
					methodInfo = instanceObject.GetType().GetMethod("MapTone", new Type[]{ source.GetType(), map.GetType()});
					methodCache.Add("0", methodInfo);
				}

				if (!attributeCache.TryGetValue("0", out attributes)){
					MethodInfo method = typeof(CloudletFilter).GetMethod("MapTone", new Type[]{ source.GetType(), map.GetType()});
					if (method != null){
						attributes = method.GetCustomAttributes(true);
					}else{
						attributes = new object[0];
					}
					attributeCache.Add("0", attributes);
				}

				return (byte[])instanceHandler.Invoke(instanceObject, methodInfo, attributes, new object[] {source, map});
			}

			
			public byte[] FilterApply(byte[] source, double[][] filter, double factor, double offset)
			{
				MethodInfo methodInfo = null;
				object[] attributes = null;

				if (!methodCache.TryGetValue("1", out methodInfo)){
					methodInfo = instanceObject.GetType().GetMethod("FilterApply", new Type[]{ source.GetType(), filter.GetType(), factor.GetType(), offset.GetType()});
					methodCache.Add("1", methodInfo);
				}

				if (!attributeCache.TryGetValue("1", out attributes)){
					MethodInfo method = typeof(CloudletFilter).GetMethod("FilterApply", new Type[]{ source.GetType(), filter.GetType(), factor.GetType(), offset.GetType()});
					if (method != null){
						attributes = method.GetCustomAttributes(true);
					}else{
						attributes = new object[0];
					}
					attributeCache.Add("1", attributes);
				}

				return (byte[])instanceHandler.Invoke(instanceObject, methodInfo, attributes, new object[] {source, filter, factor, offset});
			}

			
			public byte[] CartoonizerImage(byte[] source)
			{
				MethodInfo methodInfo = null;
				object[] attributes = null;

				if (!methodCache.TryGetValue("2", out methodInfo)){
					methodInfo = instanceObject.GetType().GetMethod("CartoonizerImage", new Type[]{ source.GetType()});
					methodCache.Add("2", methodInfo);
				}

				if (!attributeCache.TryGetValue("2", out attributes)){
					MethodInfo method = typeof(CloudletFilter).GetMethod("CartoonizerImage", new Type[]{ source.GetType()});
					if (method != null){
						attributes = method.GetCustomAttributes(true);
					}else{
						attributes = new object[0];
					}
					attributeCache.Add("2", attributes);
				}

				return (byte[])instanceHandler.Invoke(instanceObject, methodInfo, attributes, new object[] {source});
			}

			public int[][] FilterApply(int[][] source, double[][] filter, double factor, double offset)
			{
				MethodInfo methodInfo = null;
				object[] attributes = null;

				if (!methodCache.TryGetValue("3", out methodInfo)){
					methodInfo = instanceObject.GetType().GetMethod("FilterApply", new Type[]{ source.GetType(), filter.GetType(), factor.GetType(), offset.GetType()});
					methodCache.Add("3", methodInfo);
				}

				if (!attributeCache.TryGetValue("3", out attributes)){
					MethodInfo method = typeof(CloudletFilter).GetMethod("FilterApply", new Type[]{ source.GetType(), filter.GetType(), factor.GetType(), offset.GetType()});
					if (method != null){
						attributes = method.GetCustomAttributes(true);
					}else{
						attributes = new object[0];
					}
					attributeCache.Add("3", attributes);
				}

				return (int[][])instanceHandler.Invoke(instanceObject, methodInfo, attributes, new object[] {source, filter, factor, offset});
			}

public int[][] MapTone(int[][] source, int[][] map)
			{
				MethodInfo methodInfo = null;
				object[] attributes = null;

				if (!methodCache.TryGetValue("4", out methodInfo)){
					methodInfo = instanceObject.GetType().GetMethod("MapTone", new Type[]{ source.GetType(), map.GetType()});
					methodCache.Add("4", methodInfo);
				}

				if (!attributeCache.TryGetValue("4", out attributes)){
					MethodInfo method = typeof(CloudletFilter).GetMethod("MapTone", new Type[]{ source.GetType(), map.GetType()});
					if (method != null){
						attributes = method.GetCustomAttributes(true);
					}else{
						attributes = new object[0];
					}
					attributeCache.Add("4", attributes);
				}

				return (int[][])instanceHandler.Invoke(instanceObject, methodInfo, attributes, new object[] {source, map});
			}

public int[][] CartoonizerImage(int[][] source)
			{
				MethodInfo methodInfo = null;
				object[] attributes = null;

				if (!methodCache.TryGetValue("5", out methodInfo)){
					methodInfo = instanceObject.GetType().GetMethod("CartoonizerImage", new Type[]{ source.GetType()});
					methodCache.Add("5", methodInfo);
				}

				if (!attributeCache.TryGetValue("5", out attributes)){
					MethodInfo method = typeof(CloudletFilter).GetMethod("CartoonizerImage", new Type[]{ source.GetType()});
					if (method != null){
						attributes = method.GetCustomAttributes(true);
					}else{
						attributes = new object[0];
					}
					attributeCache.Add("5", attributes);
				}

				return (int[][])instanceHandler.Invoke(instanceObject, methodInfo, attributes, new object[] {source});
			}


				}
			
			public sealed class Proxy_InternetFilter : InternetFilter
			{
				private InternetFilter instanceObject;
				private InvocationHandler instanceHandler;

				private Dictionary<string,MethodInfo> methodCache = new Dictionary<string,MethodInfo>(13);
				private Dictionary<string,object[]> attributeCache = new Dictionary<string,object[]>(13);

			public Proxy_InternetFilter(object instanceObject, InvocationHandler instanceHandler)
			{
				this.instanceObject = (InternetFilter)instanceObject;
				this.instanceHandler = instanceHandler;
			}

			public byte[] MapTone(byte[] source, byte[] map)
			{
				MethodInfo methodInfo = null;
				object[] attributes = null;

				if (!methodCache.TryGetValue("3", out methodInfo)){
					methodInfo = instanceObject.GetType().GetMethod("MapTone", new Type[]{ source.GetType(), map.GetType()});
					methodCache.Add("3", methodInfo);
				}

				if (!attributeCache.TryGetValue("3", out attributes)){
					MethodInfo method = typeof(InternetFilter).GetMethod("MapTone", new Type[]{ source.GetType(), map.GetType()});
					if (method != null){
						attributes = method.GetCustomAttributes(true);
					}else{
						attributes = new object[0];
					}
					attributeCache.Add("3", attributes);
				}

				return (byte[])instanceHandler.Invoke(instanceObject, methodInfo, attributes, new object[] {source, map});
			}

			
			public byte[] FilterApply(byte[] source, double[][] filter, double factor, double offset)
			{
				MethodInfo methodInfo = null;
				object[] attributes = null;

				if (!methodCache.TryGetValue("4", out methodInfo)){
					methodInfo = instanceObject.GetType().GetMethod("FilterApply", new Type[]{ source.GetType(), filter.GetType(), factor.GetType(), offset.GetType()});
					methodCache.Add("4", methodInfo);
				}

				if (!attributeCache.TryGetValue("4", out attributes)){
					MethodInfo method = typeof(InternetFilter).GetMethod("FilterApply", new Type[]{ source.GetType(), filter.GetType(), factor.GetType(), offset.GetType()});
					if (method != null){
						attributes = method.GetCustomAttributes(true);
					}else{
						attributes = new object[0];
					}
					attributeCache.Add("4", attributes);
				}

				return (byte[])instanceHandler.Invoke(instanceObject, methodInfo, attributes, new object[] {source, filter, factor, offset});
			}

			
			public byte[] CartoonizerImage(byte[] source)
			{
				MethodInfo methodInfo = null;
				object[] attributes = null;

				if (!methodCache.TryGetValue("5", out methodInfo)){
					methodInfo = instanceObject.GetType().GetMethod("CartoonizerImage", new Type[]{ source.GetType()});
					methodCache.Add("5", methodInfo);
				}

				if (!attributeCache.TryGetValue("5", out attributes)){
					MethodInfo method = typeof(InternetFilter).GetMethod("CartoonizerImage", new Type[]{ source.GetType()});
					if (method != null){
						attributes = method.GetCustomAttributes(true);
					}else{
						attributes = new object[0];
					}
					attributeCache.Add("5", attributes);
				}

				return (byte[])instanceHandler.Invoke(instanceObject, methodInfo, attributes, new object[] {source});
			}

			public int[][] FilterApply(int[][] source, double[][] filter, double factor, double offset)
			{
				MethodInfo methodInfo = null;
				object[] attributes = null;

				if (!methodCache.TryGetValue("6", out methodInfo)){
					methodInfo = instanceObject.GetType().GetMethod("FilterApply", new Type[]{ source.GetType(), filter.GetType(), factor.GetType(), offset.GetType()});
					methodCache.Add("6", methodInfo);
				}

				if (!attributeCache.TryGetValue("6", out attributes)){
					MethodInfo method = typeof(InternetFilter).GetMethod("FilterApply", new Type[]{ source.GetType(), filter.GetType(), factor.GetType(), offset.GetType()});
					if (method != null){
						attributes = method.GetCustomAttributes(true);
					}else{
						attributes = new object[0];
					}
					attributeCache.Add("6", attributes);
				}

				return (int[][])instanceHandler.Invoke(instanceObject, methodInfo, attributes, new object[] {source, filter, factor, offset});
			}

public int[][] MapTone(int[][] source, int[][] map)
			{
				MethodInfo methodInfo = null;
				object[] attributes = null;

				if (!methodCache.TryGetValue("7", out methodInfo)){
					methodInfo = instanceObject.GetType().GetMethod("MapTone", new Type[]{ source.GetType(), map.GetType()});
					methodCache.Add("7", methodInfo);
				}

				if (!attributeCache.TryGetValue("7", out attributes)){
					MethodInfo method = typeof(InternetFilter).GetMethod("MapTone", new Type[]{ source.GetType(), map.GetType()});
					if (method != null){
						attributes = method.GetCustomAttributes(true);
					}else{
						attributes = new object[0];
					}
					attributeCache.Add("7", attributes);
				}

				return (int[][])instanceHandler.Invoke(instanceObject, methodInfo, attributes, new object[] {source, map});
			}

public int[][] CartoonizerImage(int[][] source)
			{
				MethodInfo methodInfo = null;
				object[] attributes = null;

				if (!methodCache.TryGetValue("8", out methodInfo)){
					methodInfo = instanceObject.GetType().GetMethod("CartoonizerImage", new Type[]{ source.GetType()});
					methodCache.Add("8", methodInfo);
				}

				if (!attributeCache.TryGetValue("8", out attributes)){
					MethodInfo method = typeof(InternetFilter).GetMethod("CartoonizerImage", new Type[]{ source.GetType()});
					if (method != null){
						attributes = method.GetCustomAttributes(true);
					}else{
						attributes = new object[0];
					}
					attributeCache.Add("8", attributes);
				}

				return (int[][])instanceHandler.Invoke(instanceObject, methodInfo, attributes, new object[] {source});
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
            
  				case "CloudletFilter":
					return new Ufc.MpOS.Proxy.Temp.Proxy_CloudletFilter(instanceObject,handler);
  				case "InternetFilter":
					return new Ufc.MpOS.Proxy.Temp.Proxy_InternetFilter(instanceObject,handler);
 					}
							return null;
		}
	 }
}

