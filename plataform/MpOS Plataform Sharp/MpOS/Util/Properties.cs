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
using System.Collections.Generic;
using System.IO;

namespace Ufc.MpOS.Util
{
	public sealed class Properties
	{
		public Dictionary<string, string> Data { get; private set; }

		public Properties()
		{
			Data = new Dictionary<string, string>();
		}

		public void Load(string path)
		{
			foreach (var row in File.ReadAllLines(path))
			{
				string line = row.Trim();
				if (!line.StartsWith("#"))
				{
					string[] itens = line.Split('=');
					if (itens.Length == 2)
					{
						Data.Add(itens[0], itens[1]);
					}
				}
			}
		}
	}
}