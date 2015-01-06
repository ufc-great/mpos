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

import android.util.Log;
import br.ufc.mdcc.mpos.net.Protocol;
import br.ufc.mdcc.mpos.net.endpoint.ServerContent;
import br.ufc.mdcc.mpos.net.exceptions.MissedEventException;
import br.ufc.mdcc.mpos.net.profile.model.Network;
import br.ufc.mdcc.mpos.util.TaskResult;

/**
 * This implement a default profile client.
 * 
 * @author Philipp B. Costa
 */
public final class ProfileNetworkDefault extends ProfileNetworkTask {

	protected ProfileNetworkDefault(TaskResult<Network> result, ServerContent server) throws MissedEventException {
		super(server, result, ProfileNetworkDefault.class, "ProfileDefault Started on endpoint: " + server.getIp());
	}

	/**
     * Feedback code:
     * 33 -> Finished Ping TCP Test
     * 66 -> Finished Ping UDP Test
     * 100 -> Finished Ping Test with packet loss
     */
	@Override
	protected Network doInBackground(Void... params) {
		Network network = new Network();

		try {
		    long[] pings = pingService(Protocol.TCP_EVENT);
			network.setResultPingTcp(pings);
			publishProgress(33);

			pings = pingService(Protocol.UDP_EVENT);
			network.setResultPingUdp(pings);
			publishProgress(66);

			network.setLossPacket(lossPacketCalculation(network));

			Log.d(clsName, "ProfileDefault Finished");

			return network;
		} catch (InterruptedException e) {
			Log.w(clsName, e);
		} catch (IOException e) {
			Log.e(clsName, e.getMessage(), e);
		} catch (MissedEventException e) {
			Log.e(clsName, e.getMessage(), e);
		}finally{
		    publishProgress(100);
		}

		return null;
	}
}