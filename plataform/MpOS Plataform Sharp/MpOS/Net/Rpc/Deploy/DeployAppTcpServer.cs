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
using System.IO;
using System.Net.Sockets;
using System.Text;
using Ufc.MpOS.Controller;
using Ufc.MpOS.Net.Rpc.Deploy.Model;
using Ufc.MpOS.Net.Util;
using Ufc.MpOS.Persistence;

namespace Ufc.MpOS.Net.Rpc.Deploy
{
	sealed class DeployAppTcpServer : TcpServer
	{
		public DeployAppTcpServer(string ip, Service service)
			: base(ip, service, typeof(DeployAppTcpServer))
		{
			startMessage = "Deploy App TCP started on port: " + service.Port;
		}

		public override void ClientRequest(object connection)
		{
			logger.Info("Starting deploy service!");
			RpcService service = new RpcService();

			TcpClient clientSocket = (TcpClient)connection;
			//tem que sinalizar quando chegar a mensagem...
			NetworkStream networkStream = clientSocket.GetStream();

			try
			{
				// e.g.: mpos_deploy_app:wp:<Name_App>:<Version_App>
				string clientMessage = null;
				if ((clientMessage = ReadMessage(networkStream, "mpos_deploy_app")) == null)
				{
					return;// finished a conex from side client
				}

				service.ProcessDeployMessage(clientMessage);
				string dir = "./app_dep/windowsphone/" + service.Name + "_" + service.VersionApp + "v";
				CheckDiretory(dir);

				if ((clientMessage = ReadMessage(networkStream, "mpos_dependence_size")) == null)
				{
					return;
				}

				service.Dependencies = ProcessDependence(networkStream, Convert.ToInt32(clientMessage.Split(':')[1]), dir);
				service.Port = GenerateServicePort();

				FachadeDao.Instance.RpcServiceDao.Add(service);
				SentMessage(networkStream, "mpos_dependence_port:" + service.Port);

				ServiceController.Instance.StartServiceRpc(service);
				logger.Info("Service: " + service.Name + ", deployed on port: " + service.Port);
			}
			finally
			{
				Close(ref clientSocket, ref networkStream);
			}
		}

		private string ReadMessage(NetworkStream networkStream, string header)
		{
			byte[] buffer = new byte[64];
			using (MemoryStream memoryStream = new MemoryStream(64))
			{
				do
				{
					int read = networkStream.Read(buffer, 0, buffer.Length);
					memoryStream.Write(buffer, 0, read);
				} while (networkStream.DataAvailable);

				byte[] data = memoryStream.ToArray();
				string message = Encoding.UTF8.GetString(data, 0, data.Length);

				if (message == null && !message.Contains(header))
				{
					return null;
				}

				SentMessage(networkStream, "ok");
				return message;
			}
		}

		private void SentMessage(NetworkStream networkStream, String message)
		{
			byte[] data = Encoding.UTF8.GetBytes(message);
			networkStream.Write(data, 0, data.Length);
		}

		private void CheckDiretory(String path)
		{
			if (!Directory.Exists(path))
			{
				Directory.CreateDirectory(path);
			}
		}

		private List<DependencePath> ProcessDependence(NetworkStream networkStream, int countFiles, string dir)
		{
			List<DependencePath> dependences = new List<DependencePath>(countFiles);

			int fileBufferSize = 256 * 1024;

			for (int i = 0; i < countFiles; i++)
			{
				using (MemoryStream memoryStream = ReadStream(networkStream))
				{
					using (BinaryReader reader = new BinaryReader(memoryStream))
					{
						string nameFile = reader.ReadString();
						int dataLength = reader.ReadInt32();
						byte[] data = new byte[dataLength];
						reader.Read(data, 0, dataLength);

						string path = dir + "/" + nameFile;

						try
						{
							if (File.Exists(path))
							{
								File.Delete(path);
							}

							using (FileStream file = File.Create(path, fileBufferSize, FileOptions.None))
							{
								using (BinaryWriter writer = new BinaryWriter(file))
								{
									writer.Write(data, 0, dataLength);
									writer.Flush();
								}
							}

							dependences.Add(new DependencePath(path));
						}
						catch (UnauthorizedAccessException)
						{
							//file exists and was linked with some process and can't be deleted.
						}

						SentMessage(networkStream, "ok");
					}
				}
			}
			return dependences;
		}

		private int GenerateServicePort()
		{
			int port = 0;
			Random random = new Random();
			List<int> registerPorts = FachadeDao.Instance.RpcServiceDao.GetAllPorts();

			while (true)
			{
				port = random.Next(1000) + 36000;
				if (registerPorts.BinarySearch(port) < 0)
				{
					break;
				}
			}
			return port;
		}
	}
}