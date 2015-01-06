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
using System.Reflection;
using System.Text;

namespace Ufc.MpOS.Net.Rpc.Deploy.Model
{
	public class DependencePath
	{
		public int Id { get; set; }
		public int IdService { get; set; }
		public string Path { get; set; }

		public DependencePath() : this(null) { }

		public DependencePath(String path) : this(0, 0, path){ }

		public DependencePath(int idService, String path) : this(0, idService, path){ }

		public DependencePath(int id, int idService, string path)
		{
			Id = id;
			IdService = idService;
			Path = path;
		}

		public override string ToString()
		{
			Type type = this.GetType();
			StringBuilder builder = new StringBuilder(type.Name + "=[");

			foreach (PropertyInfo property in type.GetProperties())
			{
				builder.Append(string.Format("{0}={1}, ", property.Name, property.GetValue(this, null)));
			}

			builder.Remove(builder.Length - 2, 2);
			builder.Append("]");

			return builder.ToString();
		}
	}
}