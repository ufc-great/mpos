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
using System.Threading;
using Ufc.MpOS.Net.Util;
using Ufc.MpOS.Util;

namespace Ufc.MpOS.Net
{
	public abstract class AbstractServer
	{
		protected Logger logger;
		private Thread serverThread;
		protected string startMessage;

		protected string ip;

		public Service Service { get; private set; }

		public AbstractServer(string ip, Service service, Type typeCls)
		{
			this.ip = ip;
			logger = Logger.GetLogger(typeCls);
			Service = service;
		}

		public abstract void Run();

		public void Start()
		{
			serverThread = new Thread(Run);
			serverThread.Start();
		}

		protected void Dispose(params IDisposable[] resources)
		{
			if (resources != null)
			{
				foreach (IDisposable res in resources)
				{
					res.Dispose();
				}
			}
		}

		protected MemoryStream ReadStream(NetworkStream stream)
		{
			return ReadStream(new BufferedStream(stream), 1024 * 32);
		}

		//client side on wp didn't has BufferedStream because this, I'm need made
		//the implementation style it...
		protected MemoryStream ReadStream(BufferedStream stream, int bufferSize)
		{
			byte[] streamSize = new byte[4];
			stream.Read(streamSize, 0, streamSize.Length);
			int totalStreamSize = BitConverter.ToInt32(streamSize, 0);

			return ReadStream(stream, bufferSize, totalStreamSize);
		}

		//the client need to sent a 4 byte of stream size!
		protected MemoryStream ReadStream(BufferedStream stream, int bufferSize, int totalStreamSize)
		{
			byte[] buffer = new byte[bufferSize-200];
			MemoryStream memoryStream = new MemoryStream(totalStreamSize);

			//logger.Info(">> RpcTcpServer Receive (totalStreamSize): " + totalStreamSize);
			int totalRead = 0;
			do
			{
				int read = stream.Read(buffer, 0, buffer.Length);
				memoryStream.Write(buffer, 0, read);
				totalRead += read;
				//logger.Info(">> RpcTcpServer Read (totalRead): " + totalRead);
			} while (totalRead < totalStreamSize);

			//start position on memory stream, maybe need discard the header size!
			memoryStream.Position = totalRead - totalStreamSize;
			return memoryStream;
		}

		protected MemoryStream ReadStreamDebug(BufferedStream stream, int bufferSize, int totalStreamSize, ref long totalDownloadTime)
		{
			Stopwatch stopWatch = Stopwatch.StartNew();
			MemoryStream ms = ReadStream(stream, bufferSize, totalStreamSize);
			stopWatch.Stop();

			totalDownloadTime = stopWatch.ElapsedMilliseconds;
			return ms;
		}
	}
}