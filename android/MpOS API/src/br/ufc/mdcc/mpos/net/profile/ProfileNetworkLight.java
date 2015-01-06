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
 * This implement a light profile client.
 * 
 * @author Philipp B. Costa
 */
public final class ProfileNetworkLight extends ProfileNetworkTask {

	protected ProfileNetworkLight(TaskResult<Network> result, ServerContent server) throws MissedEventException {
		super(server, result, ProfileNetworkLight.class, "ProfileLight Started on endpoint: " + server.getIp());
	}

	/**
     * Feedback code:
     * 0 -> Begin Ping TCP Test
     * 100 -> Finished Ping TCP Test
     */
	@Override
	protected Network doInBackground(Void... params) {
		Network network = new Network();

		try {
		    long[] pings = pingService(Protocol.TCP_EVENT, 7);
			network.setResultPingTcp(pings);
			
			Log.d(clsName, "ProfileLight Finished");

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