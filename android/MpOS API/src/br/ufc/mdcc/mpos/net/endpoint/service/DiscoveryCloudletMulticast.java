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
package br.ufc.mdcc.mpos.net.endpoint.service;

import java.io.IOException;
import java.net.ConnectException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.MulticastLock;
import android.util.Log;
import br.ufc.mdcc.mpos.MposFramework;
import br.ufc.mdcc.mpos.net.endpoint.EndpointController;

/**
 * This is a client implementation used for discovery cloudlet, under 
 * UDP Multicast protocol in port 31000. The server response is the unicast 
 * response using the default port (31001) on UDP protocol.
 * 
 * @author Philipp B. Costa
 */
public final class DiscoveryCloudletMulticast extends Thread {
	private final String clsName = DiscoveryCloudletMulticast.class.getName();

	private Context context;

	private WifiManager wifiMan = null;
	private MulticastLock mLock = null;

	private final int multicastPort = 31000;
	private final int replyCloudletPort = 31001;
	private final String ip = "230.230.230.230";

	private final int BUFFER = 32;

	private volatile boolean stop = false;

	public DiscoveryCloudletMulticast(Context context) {
		this.context = context;
	}

	private void multicastWifiContext() {
		wifiMan = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
		mLock = wifiMan.createMulticastLock("descServLock");
		mLock.setReferenceCounted(true);
		mLock.acquire();
	}

	@Override
	public void run() {
		int repeatTime = EndpointController.REPEAT_DISCOVERY_TASK / 2;
		
		while(!stop){
			Log.i(clsName, "# Started Cloudlet Discovery using Multicast UDP");
			multicastWifiContext();
			
			MulticastSocket socket = null;
			ReceiveReplyCloudlet receive = null;

			try {
				if (!MposFramework.getInstance().getDeviceController().connectionStatus(ConnectivityManager.TYPE_WIFI)) {
					throw new ConnectException("Wifi is offline!");
				}

				InetAddress address = InetAddress.getByName(ip);
				socket = listen(address);
				socket.setTimeToLive(16);

				String requestData = "mpos_cloudlet_req";
				DatagramPacket cloudletRequestPacket = new DatagramPacket(requestData.getBytes(), requestData.length(), address, multicastPort);

				receive = new ReceiveReplyCloudlet();
				receive.start();

				// sending request for 30s...
				int retry = 0;
				while (retry < 60 && !stop) {
					socket.send(cloudletRequestPacket);
					Thread.sleep(500);
					retry++;
				}

				// wait more 5s before kill this thread!
				if (retry == 60 && receive.isAlive()) {
					receive.join(5000);
					receive.interrupt();
				}
			} catch (IOException e) {
				Log.w(clsName, "Any problem with I/O in Multicast System!", e);
				if (receive != null) {
					receive.interrupt();
				}
			} catch (InterruptedException e) {
				Log.i(clsName, "External Interrupted Thread!", e);
			} finally {
				if (socket != null) {
					socket.close();
				}
			}

			releaseWifiLock();
			
			if (!stop) {
				Log.i(clsName, ">> Retry Discovery Cloudlet, in " + repeatTime + " ms");
				try {
					Thread.sleep(repeatTime);
				} catch (InterruptedException e) {
					// sent canceled timer because app and mpos api finished!
				}
			} else {
				Log.i(clsName, ">> Finished Discovery Cloudlet");
			}
		}
	}

	private MulticastSocket listen(InetAddress address) throws IOException {
		MulticastSocket socket = new MulticastSocket(multicastPort);
		socket.joinGroup(address);

		return socket;
	}

	private void releaseWifiLock() {
		if (mLock != null && mLock.isHeld()) {
			mLock.release();
			mLock = null;
		}
	}

	// serve para parar esse serviço!
	private void stopService() {
		stop = true;
	}

	private final class ReceiveReplyCloudlet extends Thread {
		private DatagramSocket socket;

		@Override
		public void run() {
			try {
				socket = new DatagramSocket(replyCloudletPort);
				DatagramPacket cloudletReplyPacket = new DatagramPacket(new byte[BUFFER], BUFFER);

				// PS: receive block
				socket.receive(cloudletReplyPacket);
				String message = new String(cloudletReplyPacket.getData(), 0, cloudletReplyPacket.getLength());
				if (message.contains("=")) {
					String extractMessage[] = message.split("=");

					stopService();
					MposFramework.getInstance().getEndpointController().foundCloudlet(extractMessage[1]);
				}
			} catch (IOException e) {
				Log.w(ReceiveReplyCloudlet.class.getName(), e);
			} finally {
				close();
				Log.i(clsName, "Finished Reply Cloudlet listen thread");
			}
		}

		private void close() {
			if (socket != null && !socket.isClosed()) {
				socket.close();
			}
		}

		@Override
		public void interrupt() {
			close();
			super.interrupt();
		}
	}
}