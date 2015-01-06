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
using Ufc.MpOS.Net.Rpc.Deploy.Model;
using Ufc.MpOS.Net.Rpc.Model;
using Ufc.MpOS.Net.Rpc.Util;
using Ufc.MpOS.Net.Util;
using Ufc.MpOS.Util.Exceptions;

namespace Ufc.MpOS.Net.Rpc
{
	public sealed class RpcTcpServer : TcpServer
	{
		private AssemblyLoader assembly;
		private readonly int bufferSize = 1460; //NOT change!!! this trash API didn't support packet...

		private readonly Type[] ReadMethodParamsTypes = { typeof(BinaryReader), typeof(string) };
		private readonly Type[] WriteMethodReturnTypes = { typeof(BinaryWriter), typeof(string), typeof(object) };

		public RpcTcpServer(string ip, RpcService service)
			: base(ip, service, typeof(RpcTcpServer))
		{
			startMessage = "Service: " + service.Name + "_" + service.VersionApp + "v, was started on port: " + service.Port;
			assembly = new AssemblyLoader(Service.Dependencies);
		}

		public override void ClientRequest(object connection)
		{
			TcpClient clientSocket = (TcpClient)connection;

			CallRemotable call = null;

			NetworkStream networkStream = clientSocket.GetStream();

			try
			{
				object objReturn = null;
				call = Receive(new BufferedStream(networkStream, bufferSize));

				if (call.Debug == null)
				{
					objReturn = InvokeMethod(call);
				}
				else
				{
					Stopwatch stopWatch = Stopwatch.StartNew();
					objReturn = InvokeMethod(call);
					stopWatch.Stop();

					call.Debug.ExecutionCpuTime = stopWatch.ElapsedMilliseconds;
				}

				Sent(new BufferedStream(networkStream, bufferSize), objReturn, call);
			}
			catch (Exception e)
			{
				if (call != null)
				{
					logger.Error("Method call: " + call.MethodName + ", from object: " + call.ObjLocal.GetType().Name + " failed!", e);
				}
				else
				{
					logger.Error("CallRemotable have some params which didn't loading correctly.", e);
				}

				SentError(new BufferedStream(networkStream, bufferSize), e);
			}
			finally
			{
				networkStream.ReadByte();

				Close(ref clientSocket, ref networkStream);
			}
		}

		private Object InvokeMethod(CallRemotable call)
		{
			int size = call.MethodParams.Length;
			Type[] typeParam = new Type[size];
			for (int i = 0; i < size; i++)
			{
				typeParam[i] = call.MethodParams[i].GetType();
			}

			MethodInfo method = call.ObjLocal.GetType().GetMethod(call.MethodName, typeParam);
			return method.Invoke(call.ObjLocal, call.MethodParams);
		}

		private CallRemotable Receive(BufferedStream stream)
		{
			//logger.Info(">> RpcTcpServer start Receive Data");
			CallRemotable call = new CallRemotable();

			byte[] headerData = new byte[5];
			stream.Read(headerData, 0, headerData.Length);
			int totalStreamSize = BitConverter.ToInt32(headerData, 0);

			if (headerData[4] == Code.CUSTOMSTREAM)
			{
				using (BinaryReader reader = new BinaryReader(ReadStream(stream, bufferSize, totalStreamSize)))
				{
					//logger.Info(">> RpcTcpServer Receive: build stream");
					string clsName = reader.ReadString();

					object objLocal = assembly.NewInstance(clsName);
					if (objLocal != null)
					{
						call.ObjLocal = objLocal;
						call.CustomSerialization = true;
						call.MethodName = reader.ReadString();

						call.MethodParams = (object[])objLocal.GetType().GetMethod("ReadMethodParams", ReadMethodParamsTypes).Invoke(objLocal, new object[] { reader, call.MethodName });
					}
					else
					{
						throw new ClassNotFoundException("Update your dependence in side client, not found this class:" + clsName);
					}
				}

			}
			else if (headerData[4] == Code.CUSTOMSTREAMDEBUG)
			{
				long totalDownloadTime = 0L;
				using (MemoryStream memoryStream = ReadStreamDebug(stream, bufferSize, totalStreamSize, ref totalDownloadTime))
				{
					call.Debug = new RpcProfile();
					using (BinaryReader reader = new BinaryReader(memoryStream))
					{
						string clsName = reader.ReadString();

						object objLocal = assembly.NewInstance(clsName);
						if (objLocal != null)
						{
							call.ObjLocal = objLocal;
							call.CustomSerialization = true;
							call.MethodName = reader.ReadString();
							call.MethodParams = (object[])objLocal.GetType().GetMethod("ReadMethodParams", ReadMethodParamsTypes).Invoke(objLocal, new object[] { reader, call.MethodName });

							call.Debug.DownloadSize = (int)memoryStream.Length;
							call.Debug.DonwloadTime = totalDownloadTime;
						}
						else
						{
							throw new ClassNotFoundException("Update your dependence in side client, not found this class:" + clsName);
						}
					}
				}
			}
			else if (headerData[4] == Code.BSONSTREAM)
			{
				using (BinaryReader reader = new BinaryReader(ReadStream(stream, 4096, totalStreamSize)))
				{
					string clsName = reader.ReadString();

					object objLocal = assembly.NewInstance(clsName);
					if (objLocal != null)
					{
						call.ObjLocal = objLocal;
						call.MethodName = reader.ReadString();
						call.MethodParams = GetParams(reader);
					}
					else
					{
						throw new ClassNotFoundException("Update your dependence in side client, not found this class:" + clsName);
					}
				}
			}
			else if (headerData[4] == Code.BSONSTREAMDEBUG)
			{
				long totalDownloadTime = 0L;
				using (MemoryStream memoryStream = ReadStreamDebug(stream, 4096, totalStreamSize, ref totalDownloadTime))
				{
					call.Debug = new RpcProfile();
					using (BinaryReader reader = new BinaryReader(memoryStream))
					{
						string clsName = reader.ReadString();

						object objLocal = assembly.NewInstance(clsName);
						if (objLocal != null)
						{
							call.ObjLocal = objLocal;
							call.MethodName = reader.ReadString();
							call.MethodParams = GetParams(reader);
							call.Debug.DownloadSize = (int)memoryStream.Length;
							call.Debug.DonwloadTime = totalDownloadTime;
						}
						else
						{
							throw new ClassNotFoundException("Update your dependence in side client, not found this class:" + clsName);
						}
					}
				}
			}

			//logger.Info(">> RpcTcpServer end Receive Data");
			return call;
		}

		private void Sent(BufferedStream stream, Object objReturn, CallRemotable call)
		{
			if (call.CustomSerialization)
			{
				if (call.Debug == null)
				{
					using (MemoryStream memoryStream = new MemoryStream(bufferSize))
					{
						BinaryWriter writer = new BinaryWriter(memoryStream);
						writer.Write(Code.CUSTOMSTREAM);

						call.ObjLocal.GetType().GetMethod("WriteMethodReturn", WriteMethodReturnTypes).Invoke(call.ObjLocal, new object[] { writer, call.MethodName, objReturn });
						writer.Flush();

						Sent(stream, memoryStream);
					}
				}
				else
				{
					using (MemoryStream memoryStream = new MemoryStream(bufferSize))
					{
						BinaryWriter writer = new BinaryWriter(memoryStream);
						writer.Write(Code.CUSTOMSTREAMDEBUG);
						writer.Write(call.Debug.DownloadSize);
						writer.Write(call.Debug.DonwloadTime);
						writer.Write(call.Debug.ExecutionCpuTime);
						call.ObjLocal.GetType().GetMethod("WriteMethodReturn", WriteMethodReturnTypes).Invoke(call.ObjLocal, new object[] { writer, call.MethodName, objReturn });
						writer.Flush();

						Sent(stream, memoryStream);
					}
				}
			}
			else
			{
				if (call.Debug == null)
				{
					using (MemoryStream memoryStream = new MemoryStream(bufferSize))
					{
						BinaryWriter writer = new BinaryWriter(memoryStream);
						writer.Write(Code.BSONSTREAM);

						byte[] bsonData = BsonFormatter.Serialize(objReturn);
						writer.Write(bsonData.Length);
						writer.Write(bsonData);
						writer.Flush();

						Sent(stream, memoryStream);
					}
				}
				else
				{
					using (MemoryStream memoryStream = new MemoryStream(bufferSize))
					{
						BinaryWriter writer = new BinaryWriter(memoryStream);
						writer.Write(Code.BSONSTREAMDEBUG);
						writer.Write(call.Debug.DownloadSize);
						writer.Write(call.Debug.DonwloadTime);
						writer.Write(call.Debug.ExecutionCpuTime);

						byte[] bsonData = BsonFormatter.Serialize(objReturn);
						writer.Write(bsonData.Length);
						writer.Write(bsonData);
						writer.Flush();

						Sent(stream, memoryStream);
					}
				}
			}
			//logger.Info(">> RpcTcpServer Sent Data");
		}

		private void Sent(BufferedStream stream, MemoryStream memoryStream)
		{
			byte[] data = memoryStream.ToArray();
			byte[] streamSize = BitConverter.GetBytes(data.Length);
			stream.Write(streamSize, 0, streamSize.Length);
			stream.Write(data, 0, data.Length);

			//logger.Info(">> RpcTcpServer Sent (data size): " + data.Length);
		}

		private void SentError(BufferedStream stream, Exception e)
		{
			using (MemoryStream memoryStream = new MemoryStream(512))
			{
				BinaryWriter writer = new BinaryWriter(memoryStream);

				writer.Write(Code.METHOD_THROW_ERROR);
				writer.Write(e.ToString());
				writer.Flush();

				Sent(stream, memoryStream);
			}
		}

		private object[] GetParams(BinaryReader streamReader)
		{
			int bsonLength = streamReader.ReadInt32();
			byte[] bsonData = new byte[bsonLength];
			streamReader.Read(bsonData, 0, bsonLength);
			return BsonFormatter.Deserialize(bsonData, assembly);
		}

		public new RpcService Service
		{
			get { return (RpcService)base.Service; }
		}
	}

	sealed class CallRemotable
	{
		public bool CustomSerialization { get; set; }
		public string MethodName { get; set; }
		public object[] MethodParams { get; set; }
		public object ObjLocal { get; set; }
		public RpcProfile Debug { get; set; }
	}
}