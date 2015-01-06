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
package br.ufc.mdcc.mpos.net.profile.model;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import br.ufc.mdcc.mpos.util.device.Device;

/**
 * Model used for sent all data to server
 * 
 * @author Philipp B. Costa
 */
public final class ProfileResult {
	private Device device;
	private Network network;

	public ProfileResult(Device device, Network network) {
		this.device = device;
		this.network = network;
	}

	public Device getDevice() {
		return device;
	}

	public Network getNetwork() {
		return network;
	}

	public JSONObject toJSON() throws JSONException {
		JSONObject object = new JSONObject();

		object.put("mobileId", device.getMobileId());
		object.put("deviceName", device.getDeviceName());
		object.put("carrier", device.getCarrier());
		object.put("lat", device.getLatitude());
		object.put("lon", device.getLongitude());

		object.put("date", network.getDate().getTime());
		object.put("tcp", new JSONArray(Network.arrayToString(network.getResultPingTcp())));
		object.put("udp", new JSONArray(Network.arrayToString(network.getResultPingUdp())));
		object.put("loss", network.getLossPacket());
		object.put("jitter", network.getJitter());
		object.put("down", network.getBandwidthDownload());
		object.put("up", network.getBandwidthUpload());
		object.put("net", network.getNetworkType());
		object.put("appName", network.getAppName());

		return object;
	}

	public String toString() {
		try {
			return toJSON().toString();
		} catch (JSONException e) {
			return null;
		}
	}
}