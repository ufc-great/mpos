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
using System.Collections.Generic;
using System.Reflection;
using Ufc.MpOS.Net.Rpc.Deploy.Model;
using Ufc.MpOS.Util;

namespace Ufc.MpOS.Net.Rpc.Util
{
	class AssemblyLoader
	{
		private Logger logger;
		private Dictionary<string, Type> cache = new Dictionary<string, Type>();
		private List<Assembly> assemblies = new List<Assembly>(3);
		private readonly object mutex = new object();

		public AssemblyLoader(List<DependencePath> dependencies)
		{
			logger = Logger.GetLogger(GetType());
			foreach (DependencePath dep in dependencies)
			{
				assemblies.Add(Assembly.LoadFrom(dep.Path));
			}
		}

		public object NewInstance(string clsName)
		{
			foreach (Assembly assembly in assemblies)
			{
				try
				{
					object instance = assembly.CreateInstance(clsName);
					if (instance != null)
					{
						return instance;
					}
				}
				catch (Exception e)
				{
					logger.Error("Assembly Error.", e);
				}
			}
			logger.Error("Not found any class on assembly!");
			return null;
		}

		public Type ResolveClass(string clsName)
		{
			foreach (Assembly assembly in assemblies)
			{
				try
				{
					Type type = ClassFromAssembly(clsName, assembly);
					if (type != null)
					{
						return type;
					}
				}
				catch (Exception)
				{

				}
			}

			logger.Error("Not found any class on assembly!");
			return null;
		}

		private Type ClassFromAssembly(string clsName, Assembly assembly)
		{
			lock (mutex)
			{
				Type type = null;
				if (!cache.TryGetValue(clsName, out type))
				{
					type = assembly.GetType(clsName);
					if (type != null)
					{
						cache.Add(clsName, type);
					}
				}
				return type;
			}
		}
	}
}