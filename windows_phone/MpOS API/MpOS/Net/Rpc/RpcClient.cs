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
using System.IO;
using System.Net.Sockets;
using System.Reflection;
using Ufc.MpOS.Net.Core;
using Ufc.MpOS.Net.Endpoint;
using Ufc.MpOS.Net.Rpc.Model;
using Ufc.MpOS.Net.Rpc.Util;
using Ufc.MpOS.Util.Exceptions;

namespace Ufc.MpOS.Net.Rpc
{
	sealed class RpcClient
	{
		private ServerContent server;
		private byte dataFlag = 0;

		private RpcProfile profile;
		public RpcProfile Profile
		{
			get
			{
				return profile;
			}
		}

		public RpcClient()
		{
			profile = new RpcProfile();
			this.server = null;
		}

		public void SetupServer(ServerContent server)
		{
			this.server = server;
		}

		public object Call(Object objOriginal, MethodInfo method, object[] methodParams)
		{
			return Call(false, objOriginal, method.Name, methodParams);
		}

		public object Call(bool manualSerialization, object objOriginal, string methodName, object[] methodParams)
		{
			return Call(false, manualSerialization, objOriginal, methodName, methodParams);
		}

		public object Call(bool needProfile, bool manualSerialization, Object objOriginal, String methodName, object[] methodParams)
		{
			ClientTcp socket = null;

			if (server == null)
			{
				throw new NetworkException("Need to setup any server for use the RPC Client");
			}

			try
			{
				socket = new ClientTcp();
				socket.Connect(server.Ip, server.RpcServicePort);

				Sent(socket, manualSerialization, needProfile, objOriginal, methodName, methodParams);
				ResponseRemotable response = Receive(socket, objOriginal, methodName);

				if (response.Code == Code.OK)
				{
					return response.MethodReturn;
				}
				else if (response.Code == Code.METHOD_THROW_ERROR)
				{
					throw new RpcException("[Server]: Remote method thrown some errors\n" + response.Except);
				}
				else
				{
					throw new RpcException("[Server]: RPC call terminated with some errors!\n" + response.Except);
				}
			}
			catch (SocketException e)
			{
				throw new ConnectException("Failed to connect to server: " + server.Ip + ":" + server.RpcServicePort, e);
			}
			finally
			{
				socket.Close();
			}
		}

		private void Sent(ClientTcp socket, bool manualSerialization, bool debug, object objOriginal, string methodName, object[] methodParams)
		{
			using (MemoryStream memoryStream = new MemoryStream(1024))
			{
				using (BinaryWriter writer = new BinaryWriter(memoryStream))
				{
					if (manualSerialization)
					{
						dataFlag = debug ? Code.CUSTOMSTREAMDEBUG : Code.CUSTOMSTREAM;

						writer.Write(objOriginal.GetType().FullName);
						writer.Write(methodName);
						((RpcSerializable)objOriginal).WriteMethodParams(writer, methodName, methodParams);
					}
					else
					{
						dataFlag = debug ? Code.BSONSTREAMDEBUG : Code.BSONSTREAM;

						writer.Write(objOriginal.GetType().FullName);
						writer.Write(methodName);

						byte[] bsonData = BsonFormatter.Serialize(methodParams);
						writer.Write(bsonData.Length);
						writer.Write(bsonData);
					}

					writer.Flush();

					byte[] headerSize = new byte[5];
					headerSize[4] = dataFlag;
					Array.Copy(BitConverter.GetBytes(memoryStream.Length), headerSize, 4);


					byte[] sentStream = new byte[(int)memoryStream.Length + 5];
					headerSize.CopyTo(sentStream, 0);
					memoryStream.ToArray().CopyTo(sentStream, headerSize.Length);

					socket.Sent(sentStream);
				}
			}
			//Debug.WriteLine(">> RpcClient Sent Data");
		}

		//async method can't receive out params!
		private ResponseRemotable Receive(ClientTcp socket, object objOriginal, string methodName)
		{
			//Debug.WriteLine(">> start RpcClient Receive Data");

			ResponseRemotable response = new ResponseRemotable();
			BinaryReader reader = null;

			long downloadTime = 0L;
			int downloadSize = 0;
			if (dataFlag == Code.CUSTOMSTREAM || dataFlag == Code.BSONSTREAM)
			{
				reader = new BinaryReader(socket.ReceiveWithHeaderSize());
			}
			else
			{
				reader = new BinaryReader(socket.ReceiveWithHeaderDebug(ref downloadTime, ref downloadSize));
			}

			//Debug.WriteLine(">> RpcClient Receive wait first flag");
			dataFlag = reader.ReadByte();
			//Debug.WriteLine(">> RpcClient Receive end first flag");

			if (dataFlag == Code.CUSTOMSTREAM)
			{
				response.Code = Code.OK;
				response.MethodReturn = ((RpcSerializable)objOriginal).ReadMethodReturn(reader, methodName);
			}
			else if (dataFlag == Code.BSONSTREAM)
			{
				response.Code = Code.OK;
				response.MethodReturn = GetMethodReturn(reader);
			}
			else if (dataFlag == Code.CUSTOMSTREAMDEBUG)
			{
				response.Code = Code.OK;

				profile.UploadSize = reader.ReadInt32();
				profile.UploadTime = reader.ReadInt64();
				profile.ExecutionCpuTime = reader.ReadInt64();
				response.MethodReturn = ((RpcSerializable)objOriginal).ReadMethodReturn(reader, methodName);

				profile.DonwloadTime = downloadTime;
				profile.DownloadSize = downloadSize;
			}
			else if (dataFlag == Code.BSONSTREAMDEBUG)
			{
				response.Code = Code.OK;

				profile.UploadSize = reader.ReadInt32();
				profile.UploadTime = reader.ReadInt64();
				profile.ExecutionCpuTime = reader.ReadInt64();
				response.MethodReturn = GetMethodReturn(reader);

				profile.DonwloadTime = downloadTime;
				profile.DownloadSize = downloadSize;
			}
			else if (dataFlag == Code.METHOD_THROW_ERROR)
			{
				response.Code = dataFlag;
				response.Except = reader.ReadString();
			}
			else
			{
				response.Code = Code.SERVER_ERROR;
				response.Except = "Code different from expected: " + dataFlag;
			}

			//Debug.WriteLine(">> end RpcClient Receive Data");

			reader.Dispose();
			return response;
		}

		private object GetMethodReturn(BinaryReader streamReader)
		{
			int bsonLength = streamReader.ReadInt32();
			byte[] bsonData = new byte[bsonLength];
			streamReader.Read(bsonData, 0, bsonLength);
			return BsonFormatter.Deserialize(bsonData);
		}

		//only internal use!
		private sealed class ResponseRemotable
		{
			public byte Code { get; set; }
			public object MethodReturn { get; set; }
			public string Except { get; set; }
		}
	}
}