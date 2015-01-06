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
using System.Net;
using System.Net.Sockets;
using System.Threading;
using Ufc.MpOS.Net.Util;

namespace Ufc.MpOS.Net
{
	public abstract class TcpServerPool : AbstractServer
	{
		private TcpListener serverSocket;

		public TcpServerPool(string ip, Service service, Type typeCls) : base(ip, service, typeCls) { }

		public abstract void ClientRequest(object connection);

		public override void Run()
		{
			//ThreadPool.SetMinThreads(4, 4);
			ThreadPool.SetMaxThreads(100, 100);

			//read this IP from properties file!
			serverSocket = new TcpListener(IPAddress.Parse(ip), Service.Port);
			serverSocket.Start();
			logger.Info(startMessage);
			try
			{
				while (true)
				{
					TcpClient clientSocket = serverSocket.AcceptTcpClient();

					ThreadPool.QueueUserWorkItem(new WaitCallback(ClientRequest), clientSocket);
				}
			}
			catch (Exception e)
			{
				logger.Error("Any I/O Exception on server socket level!", e);
			}
			finally
			{
				serverSocket.Stop();
			}
		}
	}
}