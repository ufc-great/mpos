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
package br.ufc.mdcc.mpos.net.profile;

import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.Semaphore;

import android.os.AsyncTask;
import android.util.Log;
import br.ufc.mdcc.mpos.MposFramework;
import br.ufc.mdcc.mpos.net.Protocol;
import br.ufc.mdcc.mpos.net.core.ClientAbstract;
import br.ufc.mdcc.mpos.net.core.FactoryClient;
import br.ufc.mdcc.mpos.net.core.ReceiveDataEvent;
import br.ufc.mdcc.mpos.net.endpoint.ServerContent;
import br.ufc.mdcc.mpos.net.exceptions.MissedEventException;
import br.ufc.mdcc.mpos.net.profile.model.Network;
import br.ufc.mdcc.mpos.net.profile.model.ProfileResult;
import br.ufc.mdcc.mpos.util.TaskResult;
import br.ufc.mdcc.mpos.util.device.Device;

/**
 * This is a general profile client implementation.
 * 
 * @author Philipp B. Costa
 */
public abstract class ProfileNetworkTask extends AsyncTask<Void, Integer, Network> {
	protected String clsName;
	protected ServerContent server;

	protected volatile boolean halted = false;

	private String startMessage;
	private TaskResult<Network> taskResult;
	private long initTime;

	/**
	 * @param sendDataToView - Os dados do resultado da tarefa é enviado para activity atraves de uma interface
	 * @param ip - IP do serviço que pode ser local ou remoto
	 * @throws MissedEventException
	 */
	protected ProfileNetworkTask(ServerContent server, TaskResult<Network> result, Class<?> cls, String startMessage) throws MissedEventException {
		if (result == null) {
			throw new MissedEventException("Need to set a TaskResult<Network> for get results from end task");
		}

		this.clsName = cls.getName();
		this.taskResult = result;
		this.server = server;
		this.startMessage = startMessage;
	}

	protected void onPreExecute() {
		taskResult.taskOnGoing(0);
		Log.d(clsName, startMessage);
	}

	@Override
	protected void onProgressUpdate(Integer... values) {
		taskResult.taskOnGoing(values[0]);
	}

	@Override
	protected void onPostExecute(Network result) {
		Log.i(clsName, "Finished Profile Task");
		// depois da execução vai ser primeiro enviado pela rede, para depois ser persistido localmente...
		if (result != null) {
			result.setEndpointType(server.getType().getValue());

			Device device = MposFramework.getInstance().getDeviceController().getDevice();
			if (device != null) {
				ProfileResult profileResult = new ProfileResult(device, result);
				sentNetworkProfile(profileResult.toString().getBytes());
			} else {
				sentNetworkProfile(result.toString().getBytes());
			}
		}
		taskResult.completedTask(result);
	}

	protected int lossPacketCalculation(Network results) {
		int count = 0;
		long[] pingResults = results.getResultPingUdp();
		if (pingResults != null) {
			for (long udpPing : pingResults) {
				if (udpPing == 0 || udpPing > 1500) {
					count++;
				}
			}
		}

		pingResults = results.getResultPingTcp();
		if (pingResults != null) {
			for (long tcpPing : pingResults) {
				if (tcpPing > 1500) {
					count++;
				}
			}
		}
		return count;
	}

	protected long[] pingService(Protocol prot) throws InterruptedException, IOException, MissedEventException {
		return pingService(prot, 7);
	}

	protected long[] pingService(Protocol prot, int pingRounds) throws InterruptedException, IOException, MissedEventException {
		final long pings[] = new long[pingRounds];

		ClientAbstract client = FactoryClient.getInstance(prot);
		client.setReceiveDataEvent(new ReceiveDataEvent() {
			int count = 0;

			@Override
			public void receive(byte[] data, int offset, int read) {
				long vlr = (System.currentTimeMillis() - initTime);
				pings[count++] = vlr;
			}
		});

		if (prot == Protocol.TCP_EVENT) {
			client.connect(server.getIp(), server.getPingTcpPort());
		} else if (prot == Protocol.UDP_EVENT) {
			client.connect(server.getIp(), server.getPingUdpPort());
		}

		for (int i = 0; i < pingRounds; i++) {
			initTime = System.currentTimeMillis();
			client.send(("pigado" + i).getBytes());

			if (halted) {
				throw new InterruptedException("PingService was aborted externally!");
			}
			
			Thread.sleep(500);//wait return
		}

		Arrays.sort(pings);

		client.close();
		return pings;
	}

	private void sentNetworkProfile(final byte jsonData[]) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				final Semaphore mutex = new Semaphore(0);

				ClientAbstract client = FactoryClient.getInstance(Protocol.TCP_EVENT);
				client.setReceiveDataEvent(new ReceiveDataEvent() {
					@Override
					public void receive(byte[] data, int offset, int read) {
						Log.d(clsName, "Sent profile to server with success");
						mutex.release();
					}
				});

				try {
					client.connect(server.getIp(), server.getSaveProfilePort());
					client.send(jsonData);

					mutex.acquire();
					client.close();
				} catch (IOException e) {
					Log.e(clsName, "Error on transmition to remote data", e);
				} catch (MissedEventException e) {
					Log.e(clsName, "Need to set a listening interface", e);
				} catch (InterruptedException e) {
					Log.e(clsName, "Error on thread interrupted", e);
				}
			}
		}).start();
	}

	// stop profile!
	public void halt() {
		halted = true;
	}
}