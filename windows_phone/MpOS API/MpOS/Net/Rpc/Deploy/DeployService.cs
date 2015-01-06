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
using System.Diagnostics;
using System.IO;
using System.Text;
using System.Threading.Tasks;
using Ufc.MpOS.Net.Core;
using Ufc.MpOS.Net.Endpoint;
using Ufc.MpOS.Util.Exceptions;
using Ufc.MpOS.Util.TaskOp;
using Windows.ApplicationModel;
using Windows.Storage;
using Windows.Storage.Streams;

namespace Ufc.MpOS.Net.Rpc.Deploy
{
	public sealed class DeployService : Runnable
	{
		private ServerContent server;

		private string appVersion;
		private string appName;

		private DependenceFileLoadTask task = null;

		public DeployService(ServerContent server)
		{
			this.server = server;

			appName = MposFramework.Instance.DeviceController.AppName;
			appVersion = MposFramework.Instance.DeviceController.Version;
		}

		protected override void Run()
		{
			Debug.WriteLine("#> [DeployRpcService]: Started Deploy App " + appName + "/" + appVersion + " on Remote Server (" + server.Ip + ":" + server.DeployAppPort + ")");
			ClientTcp socket = null;

			//async programming, but run synch mode
			task = new DependenceFileLoadTask();
			task.RunSynchronous();

			try
			{
				socket = new ClientTcp(false);
				socket.Connect(server.Ip, server.DeployAppPort);

				string deployRequest = "mpos_deploy_app:" + appName + ":" + appVersion;
				//Debug.WriteLine("[DEBUG]: sent -> " + deployRequest);
				Sent(socket, deployRequest);
				if (!ReceiveFeedbackMessage(socket))
				{
					throw new NetworkException("Problems in deploy request");
				}

				string mposDependence = "mpos_dependence_size:" + task.dependenceFiles.Count;
				//Debug.WriteLine("[DEBUG]: sent -> " + mposDependence);
				Sent(socket, mposDependence);
				if (!ReceiveFeedbackMessage(socket))
				{
					throw new NetworkException("Problems in deploy request");
				}

				SentFiles(socket);
				server.RpcServicePort = ReceiveServicePort(socket);
			}
			catch (Exception e)
			{
				Debug.WriteLine("## [DeployRpcService]: Any problem in I/O processing!\n" + e.ToString());
			}
			finally
			{
				Debug.WriteLine("#> [DeployRpcService]: Finished Deploy App " + appName + "/" + appVersion + " on Remote Server port: " + server.RpcServicePort);
				socket.Close();
			}
		}

		private void Sent(ClientTcp socket, string message)
		{
			socket.Sent(Encoding.UTF8.GetBytes(message));
		}

		private void SentFiles(ClientTcp socket)
		{
			foreach (DependenceFile dep in task.dependenceFiles)
			{
				MemoryStream memoryStream = new MemoryStream(64 * 1024);
				BinaryWriter writer = new BinaryWriter(memoryStream);

				writer.Write(dep.name);
				writer.Write(dep.length);
				writer.Write(dep.data, 0, dep.data.Length);
				writer.Flush();

				byte[] streamSize = BitConverter.GetBytes(memoryStream.Length);
				socket.Sent(streamSize);
				socket.Sent(memoryStream.ToArray());

				writer.Close();
				memoryStream.Close();

				if (!ReceiveFeedbackMessage(socket))
				{
					throw new NetworkException("Problems in sent file to server");
				}
				Debug.WriteLine("#> [DeployRpcService]: server receive file -> " + dep.name);
			}
		}

		private bool ReceiveFeedbackMessage(ClientTcp socket)
		{
			MemoryStream ms = socket.Receive();
			if (ms.Length > 0)
			{
				string message = Encoding.UTF8.GetString(ms.GetBuffer(), 0, (int)ms.Length);
				return message.Equals("ok");
			}

			return false;
		}

		private int ReceiveServicePort(ClientTcp socket)
		{
			MemoryStream ms = socket.Receive();
			if (ms.Length > 0)
			{
				string message = Encoding.UTF8.GetString(ms.GetBuffer(), 0, (int)ms.Length);
				return Convert.ToInt32(message.Split(new char[] { ':' })[1]);
			}

			return -1;
		}

		private sealed class DependenceFile
		{
			public string name;
			public int length;
			public byte[] data;
		}

		//Best Practices in Asynchronous Programming
		//http://msdn.microsoft.com/en-us/magazine/jj991977.aspx
		private sealed class DependenceFileLoadTask : Runnable
		{
			public List<DependenceFile> dependenceFiles;
			
			protected override void Run()
			{
				Task task = ExecuteTask();
				task.Wait();
			}

			//never return void with async keyword...
			private async Task ExecuteTask()
			{
				StorageFolder InstallationFolder = Package.Current.InstalledLocation;
				IReadOnlyList<StorageFolder> assets = await InstallationFolder.GetFoldersAsync();
				foreach (StorageFolder assetFolder in assets)
				{
					if (assetFolder.Name.ToLower().Equals("mpos"))
					{
						IReadOnlyList<StorageFolder> assetDepSearch = await assetFolder.GetFoldersAsync();
						foreach (StorageFolder depFolder in assetDepSearch)
						{
							if (depFolder.Name.ToLower().Equals("dep"))
							{
								IReadOnlyList<StorageFile> dlls = await depFolder.GetFilesAsync();
								dependenceFiles = new List<DependenceFile>(dlls.Count);

								foreach (StorageFile dll in dlls)
								{
									DependenceFile dependence = new DependenceFile();
									dependence.name = dll.Name;
									using (IRandomAccessStream fileStream = await dll.OpenReadAsync())
									{
										dependence.length = (int) fileStream.Size;
										dependence.data = new byte[fileStream.Size];
										using (DataReader reader = new DataReader(fileStream))
										{
											await reader.LoadAsync((uint)fileStream.Size);
											reader.ReadBytes(dependence.data);
										}
										dependenceFiles.Add(dependence);
									}
								}

								goto Exit;
							}
						}
					}
				}

			Exit:
				Debug.WriteLine("#> [DeployRpcService]: Loading " + dependenceFiles.Count + " file(s)");

			}
		}
	}
}