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
using Ufc.MpOS.Net.Util;

namespace Ufc.MpOS.Net.Rpc.Deploy.Model
{
	public class RpcService : Service, IComparable
	{
		public int Id { get; set; }
		public string VersionApp { get; set; }
		public List<DependencePath> Dependencies { get; set; }

		public RpcService() : this(0, null, -1, null, null) {
			Dependencies = new List<DependencePath>(4);
		}

		public RpcService(String name, int port, string versionApp, List<DependencePath> dependences)
			: this(0, name, port, versionApp, dependences) { }

		public RpcService(int id, String name, int port, string versionApp, List<DependencePath> dependences)
			: base(name, port)
		{
			Id = id;
			VersionApp = versionApp;
			Dependencies = dependences;
		}

		public void ProcessDeployMessage(String message)
		{
			string[] process = message.ToLower().Split(new char[] { ':' });
			Name = process[1];
			VersionApp = process[2];
		}

		public int CompareTo(object obj)
		{
			if (obj == null) return 1;

			RpcService rpcService = obj as RpcService;
			if (rpcService != null)
			{
				int strCmp = rpcService.Name.CompareTo(Name);
				if (strCmp == 0)
				{
					return rpcService.VersionApp.CompareTo(VersionApp);
				}
				else
				{
					return strCmp;
				}
			}
			else
			{
				throw new ArgumentException("Object is not a RpcService");
			}
		}
	}
}