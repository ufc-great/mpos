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
	sealed class BsonFormatter
	{
		private readonly static string[] genericPattern = { "[[" };
		private readonly static char[] extension = { ',' };

		private BsonFormatter() { }

		public static byte[] Serialize(object objReturn)
		{
			object[] payload = new object[2];

			payload[0] = objReturn.GetType().FullName;
			payload[1] = objReturn;

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

		public static object[] Deserialize(byte[] bsonData, AssemblyLoader assembly)
		{
			using (MemoryStream memoryStream = new MemoryStream(bsonData))
			{
				object[] methodParams = null;
				using (BsonReader reader = new BsonReader(memoryStream))
				{
					JsonSerializer serializer = new JsonSerializer();
					BsonObject bsonObject = serializer.Deserialize<BsonObject>(reader);

					List<string> types = JsonConvert.DeserializeObject<List<string>>(bsonObject.Payload[0].ToString());

					methodParams = new object[types.Count];
					for (int i = 1; i < bsonObject.Payload.Length; i++)
					{
						Type objectType = bsonObject.Payload[i].GetType();
						if (objectType == typeof(JArray))
						{
							string type = types[i - 1];
							if (type.Contains("List"))
							{
								Type listType = typeof(List<>);
								Type genericType = assembly.ResolveClass(ExtractGenerics(type));
								Type constructedListType = listType.MakeGenericType(genericType);

								methodParams[i - 1] = JsonConvert.DeserializeObject(bsonObject.Payload[i].ToString(), constructedListType);
							}
							else
							{
								//TODO: map and others collections!
							}
						}
						else if (objectType != Type.GetType(types[i - 1]))
						{
							switch (types[i - 1])
							{
								case "System.Int32":
									methodParams[i - 1] = Convert.ToInt32(bsonObject.Payload[i]);
									break;
								case "System.UInt32":
									methodParams[i - 1] = Convert.ToUInt32(bsonObject.Payload[i]);
									break;
								case "System.Int16":
									methodParams[i - 1] = Convert.ToUInt16(bsonObject.Payload[i]);
									break;
								case "System.UInt16":
									methodParams[i - 1] = Convert.ToUInt16(bsonObject.Payload[i]);
									break;
								case "System.Double":
									methodParams[i - 1] = Convert.ToDouble(bsonObject.Payload[i]);
									break;
								case "System.Single":
									methodParams[i - 1] = Convert.ToSingle(bsonObject.Payload[i]);
									break;
							}
						}
						else
						{
							methodParams[i - 1] = bsonObject.Payload[i];
						}
					}
				}
				return methodParams;
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