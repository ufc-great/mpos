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
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Semaphore;

import android.util.Log;
import br.ufc.mdcc.mpos.net.Protocol;
import br.ufc.mdcc.mpos.net.core.ClientAbstract;
import br.ufc.mdcc.mpos.net.core.FactoryClient;
import br.ufc.mdcc.mpos.net.core.ReceiveDataEvent;
import br.ufc.mdcc.mpos.net.endpoint.ServerContent;
import br.ufc.mdcc.mpos.net.exceptions.MissedEventException;
import br.ufc.mdcc.mpos.net.profile.model.Network;
import br.ufc.mdcc.mpos.util.TaskResult;
import br.ufc.mdcc.mpos.util.Util;

/**
 * This implement a full profile client.
 * 
 * @author Philipp B. Costa
 */
public final class ProfileNetworkFull extends ProfileNetworkTask {
	private byte data[] = new byte[32 * 1024];

	private Network network;
	private boolean bandwidthDone = false;

	public ProfileNetworkFull(TaskResult<Network> result, ServerContent server) throws MissedEventException {
		super(server, result, ProfileNetworkFull.class, "ProfileFull Started on endpoint: " + server.getIp());

		// randomize os dados que serão enviados
		new Random().nextBytes(data);
	}
	/**
     * Feedback code:
     * 15 -> Finished Ping TCP Test
     * 30 -> Finished Ping UDP Test
     * 35 -> Finished Ping Test with packet loss
     * 50 -> Finished Jitter Calculation
     * 55 -> Start Donwload Test
     * 75 -> Start Upload Test
     * 100 -> Finished Conection Test
     */
	@Override
	protected Network doInBackground(Void... params) {
		network = new Network();

		try {
			Log.i(clsName, "ping tcp");
			long[] pings = pingService(Protocol.TCP_EVENT);
			network.setResultPingTcp(pings);
			publishProgress(15);

			Log.i(clsName, "ping udp");
			pings = pingService(Protocol.UDP_EVENT);
			network.setResultPingUdp(pings);
			publishProgress(30);

			Log.i(clsName, "loss packet udp");
			// conta os pacotes perdidos UDP

			if (halted) {
				return null;
			}

			network.setLossPacket(lossPacketCalculation(network));
			publishProgress(35);

			Log.i(clsName, "jitter calculation");
			jitterCalculation();
			if (halted) {
				return null;
			}
			retrieveJitterResult();
			publishProgress(50);

			Log.i(clsName, "bandwidth calculation");
			boolean finish = bandwidthCalculation();
			publishProgress(100);

			// a task foi cancelada ou foi parado por um timer
			if (halted || !finish) {
				return null;
			}

			Log.d(clsName, "ProfileFull Finished");

			return network;
		} catch (InterruptedException e) {
			Log.w(clsName, e);
		} catch (IOException e) {
			Log.e(clsName, e.getMessage(), e);
		} catch (MissedEventException e) {
			Log.e(clsName, e.getMessage(), e);
		}

		publishProgress(100);
		return null;
	}

	/**
	 * Definição: RFC 4689 - defines jitter as “the absolute value of the difference between the Forwarding Delay of two consecutive received packets
	 * belonging to the same stream”. The jitter is important in real-time communications when the variation between delays can cause a negative
	 * impact to the server quality, such voice over IP services. Referencia: http://tools.ietf.org/html/rfc4689#section-3.2.5 Em resumo o jitter
	 * calcula os intervalos tempos entre o intervalo de tempo (corrente) e intervalo de tempo (anterior) e deve ser enviado num fluxo de taxa
	 * constante. #formula no servidor Intervalo de tempo (It) = Tempo_chegada - Tempo_anterior Jitter = it_atual - it_anterior (voce pode pegar a
	 * média, maximo e minimo) Sobre os resultados: Um jitter de 15ms é regular, abaixo de 5ms é excelente e acima de 15ms é ruim para o padrão VoIP.
	 * Seja esse site: http://www.onsip.com/tools/voip-test
	 * 
	 * @throws MissedEventException
	 * @throws IOException
	 * @throws InterruptedException
	 */
	private void jitterCalculation() throws IOException, MissedEventException, InterruptedException {
		ClientAbstract client = FactoryClient.getInstance(Protocol.UDP_EVENT);
		client.setReceiveDataEvent(new ReceiveDataEvent() {
			@Override
			public void receive(byte[] data, int offset, int read) {
				Log.d(clsName, "Jitter Finish");
			}
		});

		client.connect(server.getIp(), server.getJitterTestPort());

		for (int i = 0; i < 21; i++) {
			client.send(("jitter").getBytes());

			// bota 250ms para retorno
			// por causa do UDP que não tem controle de fluxo
			Thread.sleep(250);
		}

		client.close();
	}

	private void retrieveJitterResult() throws IOException, MissedEventException, InterruptedException {
		Thread.sleep(1500);
		final Semaphore mutex = new Semaphore(0);

		ClientAbstract client = FactoryClient.getInstance(Protocol.TCP_EVENT);
		client.setReceiveDataEvent(new ReceiveDataEvent() {
			@Override
			public void receive(byte[] data, int offset, int read) {
				Log.d(clsName, "Retrieve data from server for Jitter calcule");

				network.setJitter(Integer.parseInt(new String(data, offset, read)));
				// System.out.println(results.getJitter());

				mutex.release();
			}
		});

		client.connect(server.getIp(), server.getJitterRetrieveResultPort());
		client.send("get".getBytes());

		mutex.acquire();
		client.close();
	}

	private boolean bandwidthCalculation() throws IOException, MissedEventException, InterruptedException {
		final Semaphore mutex = new Semaphore(0);
		
		//begin download
		publishProgress(55);
		
		ClientAbstract client = FactoryClient.getInstance(Protocol.TCP_EVENT);
		client.setReceiveDataEvent(new ReceiveDataEvent() {
			private long countBytes = 0L;

			private byte endDown[] = "end_down".getBytes();
			private byte endSession[] = "end_session".getBytes();

			@Override
			public void receive(byte[] data, int offset, int read) {
				countBytes += (long) read;

				if (Util.containsArrays(data, endDown)) {
					// System.out.println("Bytes: "+countBytes);
					// bytes * 8bits / 7s * 1E+6 = X Mbits
					double bandwidth = ((double) (countBytes * 8L) / (double) (7.0 * 1E+6));
					network.setBandwidthDownload(String.valueOf(bandwidth));
					countBytes = 0L;
					mutex.release();
				} else if (Util.containsArrays(data, endSession)) {
					bandwidthDone = true;
					String dataBlock = new String(data, offset, read);
					String res[] = dataBlock.split(":");
					network.setBandwidthUpload(res[1]);

					mutex.release();
				}
			}
		});

		// timer for finish!
		Timer timeout = new Timer("Timeout Bandwidth");
		timeout.schedule(new TimerTask() {
			@Override
			public void run() {
				if (!bandwidthDone) {
					// para garantir que não vai travar nenhum semaphoro!
					mutex.release();
					mutex.release();
					Log.i(clsName, "Bandwidth Timeout...");
				}
			}
		}, 120000);// 120s de timeout

		Log.i(clsName, "bandwidth (download)");
		client.connect(server.getIp(), server.getBandwidthPort());
		client.send("down".getBytes());
		
		//wait finish the down...
		mutex.acquire();
		
		//begin upload
		publishProgress(75);

		if (halted) {
			timeout.cancel();
			return false;
		}

		Log.i(clsName, "bandwidth (upload)");
		client.send("up".getBytes());

		// faz upload! - 11s
		long timeExit = System.currentTimeMillis() + 11000;
		while (System.currentTimeMillis() < timeExit) {
			client.send(data);
		}
		client.send("end_up".getBytes());

		Log.i(clsName, "bandwidth (ended upload)");
		mutex.acquire();
		client.close();

		// cancela o timer
		timeout.cancel();

		return bandwidthDone;
	}
}