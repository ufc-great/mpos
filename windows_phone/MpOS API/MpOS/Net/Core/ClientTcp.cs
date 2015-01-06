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
using System.Net;
using System.Net.Sockets;
using System.Threading;

namespace Ufc.MpOS.Net.Core
{
	public sealed class ClientTcp
	{
		//Explain about ManualResetEvent
		//http://www.albahari.com/threading/part2.aspx#_Signaling_with_Event_WaitOne_Handles
		private ManualResetEvent blockClientTcp = new ManualResetEvent(false);

		private Socket socket = null;
		private int bufferSize;
		private int bufferSizeSmall;
		private readonly byte[] buffer = null;
		private readonly int timeout = 10000;//in ms

		private readonly SocketAsyncEventArgs socketEventConnect;
		private readonly SocketAsyncEventArgs socketEventSent;
		private readonly SocketAsyncEventArgs socketEventReceive;
		private readonly SocketAsyncEventArgs socketEventReceiveWithHeader;

		//receive stream
		private MemoryStream receiveStream;
		private bool headerProcess;
		private bool needHeader;
		private int totalStreamSize;
		private int totalRead;

		//for debugtime
		private Stopwatch stopWatch;
		private bool startDebug = false, finishDebug = true;

		private IPAddress ip;
		private int port;

		public ClientTcp(int bufferSize, bool needHeader)
		{
			this.needHeader = needHeader;
			this.bufferSize = bufferSize;
			this.bufferSizeSmall = 256;

			socketEventConnect = new SocketAsyncEventArgs();
			socketEventSent = new SocketAsyncEventArgs();

			socketEventConnect.Completed += new EventHandler<SocketAsyncEventArgs>(EventResult);
			socketEventSent.Completed += new EventHandler<SocketAsyncEventArgs>(EventResult);

			if (needHeader)
			{
				socketEventReceiveWithHeader = new SocketAsyncEventArgs();
				socketEventReceiveWithHeader.Completed += new EventHandler<SocketAsyncEventArgs>(EventReceiveWithHeaderSize);
			}
			else
			{
				socketEventReceive = new SocketAsyncEventArgs();
				socketEventReceive.Completed += new EventHandler<SocketAsyncEventArgs>(EventReceive);
			}

			buffer = new byte[bufferSize];
		}

		public ClientTcp(bool needHeader) : this(4096, needHeader) { }

		public ClientTcp() : this(4096, true) { }

		public void Connect(IPAddress ip, int port)
		{
			this.ip = ip;
			this.port = port;

			IPEndPoint host = new IPEndPoint(ip, port);
			socket = new Socket(AddressFamily.InterNetwork, SocketType.Stream, ProtocolType.Tcp);

			socketEventConnect.RemoteEndPoint = host;
			socketEventSent.RemoteEndPoint = host;

			if (needHeader)
			{
				socketEventReceiveWithHeader.RemoteEndPoint = host;
			}
			else
			{
				socketEventReceive.RemoteEndPoint = host;
			}

			blockClientTcp.Reset();
			socket.ConnectAsync(socketEventConnect);
			blockClientTcp.WaitOne(timeout);
		}

		public void Sent(byte[] data)
		{
			socketEventSent.SetBuffer(data, 0, data.Length);

			blockClientTcp.Reset();
			//socket.SendBufferSize = bufferSize;
			socket.SendAsync(socketEventSent);
			blockClientTcp.WaitOne(timeout);
		}

		public MemoryStream Receive()
		{
			receiveStream = new MemoryStream(bufferSizeSmall);

			Array.Clear(buffer, 0, bufferSizeSmall);
			socketEventReceive.SetBuffer(buffer, 0, bufferSizeSmall);

			blockClientTcp.Reset();
			socket.ReceiveAsync(socketEventReceive);
			blockClientTcp.WaitOne(timeout);

			return receiveStream;
		}

		private void EventReceive(object obj, SocketAsyncEventArgs e)
		{
			Socket socket = (Socket)obj;

			if (e.SocketError == SocketError.Success)
			{
				receiveStream.Write(e.Buffer, e.Offset, e.BytesTransferred);

				if (e.BytesTransferred == bufferSizeSmall)
				{
					Array.Clear(buffer, 0, bufferSizeSmall);
					socketEventReceive.SetBuffer(buffer, 0, bufferSizeSmall);
					socket.ReceiveAsync(socketEventReceive);
				}
				else
				{
					blockClientTcp.Set();
				}
			}
		}

		public MemoryStream ReceiveWithHeaderDebug(ref long downloadTime, ref int downloadSize)
		{
			receiveStream = new MemoryStream(bufferSize);
			headerProcess = true;
			totalRead = 0;
			startDebug = true;
			finishDebug = false;

			Array.Clear(buffer, 0, bufferSize);
			socketEventReceiveWithHeader.SetBuffer(buffer, 0, bufferSize);

			blockClientTcp.Reset();
			socket.ReceiveAsync(socketEventReceiveWithHeader);
			blockClientTcp.WaitOne();

			downloadTime = stopWatch.ElapsedMilliseconds;
			downloadSize = (int)receiveStream.Length;

			return receiveStream;
		}

		public MemoryStream ReceiveWithHeaderSize()
		{
			receiveStream = new MemoryStream(bufferSize);
			headerProcess = true;
			totalRead = 0;

			Array.Clear(buffer, 0, bufferSize);
			socketEventReceiveWithHeader.SetBuffer(buffer, 0, bufferSize);

			blockClientTcp.Reset();
			socket.ReceiveAsync(socketEventReceiveWithHeader);
			blockClientTcp.WaitOne();

			return receiveStream;
		}

		private void EventReceiveWithHeaderSize(object obj, SocketAsyncEventArgs e)
		{

			Socket socket = (Socket)obj;
			if (e.SocketError == SocketError.Success)
			{
				if (startDebug)
				{
					startDebug = false;
					stopWatch = Stopwatch.StartNew();
				}

				try
				{
					if (headerProcess)
					{
						ProcessHeader(e.Buffer);
						if (e.BytesTransferred > 4)
						{
							receiveStream.Write(e.Buffer, 4, e.BytesTransferred-4);
							totalRead += e.BytesTransferred - 4;
						}
					}
					else
					{
						receiveStream.Write(e.Buffer, 0, e.BytesTransferred);
						totalRead += e.BytesTransferred;
					}

					if (totalRead < totalStreamSize)
					{
						Array.Clear(buffer, 0, bufferSize);
						socketEventReceiveWithHeader.SetBuffer(buffer, 0, bufferSize);
						socket.ReceiveAsync(socketEventReceiveWithHeader);//recursion event call
					}
					else
					{
						receiveStream.Position = 0;
						blockClientTcp.Set();
						if (!finishDebug)
						{
							stopWatch.Stop();
							finishDebug = true;
						}
					}
				}
				catch (Exception ex)
				{
					Debug.WriteLine(">> ClientTcp: "+ex.ToString());
				}	
			}
		}

		private void ProcessHeader(byte[] buffer)
		{
			headerProcess = false;
			byte[] headerData = new byte[4];
			Array.Copy(buffer, headerData, 4);
			totalStreamSize = BitConverter.ToInt32(headerData, 0);
		}

		private void EventResult(object s, SocketAsyncEventArgs e)
		{
			blockClientTcp.Set();

			if (e.SocketError != SocketError.Success)
			{
				switch (e.SocketError)
				{
					case SocketError.HostNotFound:
						Debug.WriteLine("## [ClientTcp]: Host not found: " + ip.ToString() + ":" + port);
						break;
					case SocketError.ConnectionRefused:
						Debug.WriteLine("## [ClientTcp]: Connection Refused to server: " + ip.ToString() + ":" + port);
						break;
					case SocketError.ConnectionReset:
						Debug.WriteLine("## [ClientTcp]: Server: " + ip.ToString() + ":" + port + " is reset by peer");
						break;
					case SocketError.TimedOut:
						Debug.WriteLine("## [ClientTcp]: Server: " + ip.ToString() + ":" + port + " TimedOut");
						break;
					default:
						Debug.WriteLine("## [ClientTcp]: Failed to connect to server: " + ip.ToString() + ":" + port + " -> " + e.SocketError.ToString());
						break;
				}
			}
		}

		public void Close()
		{
			if (socket != null)
			{
				if (socket.Connected)
				{
					socket.Shutdown(SocketShutdown.Send);
				}
				socket.Close();
			}
		}
	}
}