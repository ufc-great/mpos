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
using Newtonsoft.Json;
using Newtonsoft.Json.Bson;
using Newtonsoft.Json.Linq;
using System;
using System.Collections.Generic;
using System.IO;

namespace Ufc.MpOS.Net.Rpc.Util
{
	public sealed class BsonFormatter
	{
		private readonly static string[] genericPattern = { "[[" };
		private readonly static char[] extension = { ',' };

		private BsonFormatter() { }

		public static byte[] Serialize(object[] methodParams)
		{
			List<String> complexParams = new List<String>();
			object[] payload = new object[methodParams.Length + 1];

			//elements
			int count = 1;
			foreach (object param in methodParams)
			{
				payload[count] = param;
				complexParams.Add(param.GetType().FullName);
				count++;
			}

			//header type
			payload[0] = complexParams;

			using (MemoryStream memoryStream = new MemoryStream(4096))
			{
				using (BsonWriter bsonWriter = new BsonWriter(memoryStream))
				{
					BsonObject bson = new BsonObject();
					bson.Payload = payload;

					JsonSerializer serializer = new JsonSerializer();
					serializer.Serialize(bsonWriter, bson);
				}
				return memoryStream.ToArray();
			}
		}

		public static object Deserialize(byte[] bsonData)
		{
			using (MemoryStream memoryStream = new MemoryStream(bsonData))
			{
				object objReturn = null;
				using (BsonReader reader = new BsonReader(memoryStream))
				{
					JsonSerializer serializer = new JsonSerializer();
					BsonObject bsonObject = serializer.Deserialize<BsonObject>(reader);

					string type = bsonObject.Payload[0] as string;
					Type objectType = bsonObject.Payload[1].GetType();
					if (objectType == typeof(JArray))
					{
						if (type.Contains("List"))
						{
							Type genericType = Type.GetType(type);
							objReturn = JsonConvert.DeserializeObject(bsonObject.Payload[1].ToString(), genericType);
						}
						else
						{
							//TODO: map and others collections!
						}
					}
					else if (objectType != Type.GetType(type))
					{
						switch (type)
						{
							case "System.Int32":
								objReturn = Convert.ToInt32(bsonObject.Payload[1]);
								break;
							case "System.UInt32":
								objReturn = Convert.ToUInt32(bsonObject.Payload[1]);
								break;
							case "System.Int16":
								objReturn = Convert.ToUInt16(bsonObject.Payload[1]);
								break;
							case "System.UInt16":
								objReturn = Convert.ToUInt16(bsonObject.Payload[1]);
								break;
							case "System.Double":
								objReturn = Convert.ToDouble(bsonObject.Payload[1]);
								break;
							case "System.Single":
								objReturn = Convert.ToSingle(bsonObject.Payload[1]);
								break;
						}
					}
					else
					{
						objReturn = bsonObject.Payload[1];
					}
				}
				return objReturn;
			}
		}

		private static string ExtractGenerics(string type)
		{
			string generic = type.Split(genericPattern, StringSplitOptions.None)[1];
			string[] discard = generic.Split(extension);
			return discard[0];
		}

		private sealed class BsonObject
		{
			public object[] Payload { get; set; }
		}
	}
}