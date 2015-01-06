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

import java.util.Arrays;
import java.util.Date;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * This object save all results from executed task used for persisted in 
 * local database.
 * 
 * @author Philipp B. Costa
 */
public final class Network {
	private int id;
	private int pingEstimated;
	private int pingMedUDP, pingMedTCP;
	private int pingMaxUDP, pingMinUDP;
	private int pingMaxTCP, pingMinTCP;

	// esses atributos serão persistidos remotamente e localmente!
	private Date date;
	private long resultPingUdp[];
	private long resultPingTcp[];
	private int lossPacket;
	private int jitter;
	private String bandwidthDownload, bandwidthUpload;
	private String type;
	private String appName;

	public Network() {
		date = new Date();
		bandwidthDownload = "0";
		bandwidthUpload = "0";
	}

	public final String getAppName() {
		return appName;
	}

	public final void setAppName(String appName) {
		this.appName = appName;
	}

	public final long[] getResultPingUdp() {
		return resultPingUdp;
	}

	public final void setResultPingUdp(long[] resultadoPingUdp) {
		this.resultPingUdp = resultadoPingUdp;
	}

	public final long[] getResultPingTcp() {
		return resultPingTcp;
	}

	public final void setResultPingTcp(long[] resultPingTcp) {
		this.resultPingTcp = resultPingTcp;
	}

	public final int getPingEstimated() {
		return pingEstimated;
	}

	public final void setPingEstimated(int pingEstimated) {
		this.pingEstimated = pingEstimated;
	}

	public final int getLossPacket() {
		return lossPacket;
	}

	public final void setLossPacket(int lossPacket) {
		this.lossPacket = lossPacket;
	}

	public final int getJitter() {
		return jitter;
	}

	public final void setJitter(int jitter) {
		this.jitter = jitter;
	}

	public final String getBandwidthDownload() {
		return bandwidthDownload;
	}

	public final void setBandwidthDownload(String bandwidthDownload) {
		this.bandwidthDownload = bandwidthDownload;
	}

	public final String getBandwidthUpload() {
		return bandwidthUpload;
	}

	public final void setBandwidthUpload(String bandwidthUpload) {
		this.bandwidthUpload = bandwidthUpload;
	}

	public final Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public final int getPingMedUDP() {
		return pingMedUDP;
	}

	public final int getPingMedTCP() {
		return pingMedTCP;
	}

	public final int getPingMaxUDP() {
		return pingMaxUDP;
	}

	public final int getPingMinUDP() {
		return pingMinUDP;
	}

	public final int getPingMaxTCP() {
		return pingMaxTCP;
	}

	public final int getPingMinTCP() {
		return pingMinTCP;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getId() {
		return id;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getType() {
		return type;
	}

	public static String arrayToString(long... vlrs) {
		JSONArray jsonArray = new JSONArray();

		if (vlrs != null) {
			for (long vlr : vlrs) {
				jsonArray.put(vlr);
			}
		}

		return jsonArray.toString();
	}

	public static long[] StringToLongArray(String vlrArray) throws JSONException {
		JSONArray jsonArray = new JSONArray(vlrArray);
		long array[] = new long[jsonArray.length()];

		for (int i = 0; i < jsonArray.length(); i++) {
			array[i] = jsonArray.getLong(i);
		}

		return array;
	}

	public void generatingPingTCPStats() {
		if (resultPingTcp != null) {

			Arrays.sort(resultPingTcp);

			int tam = resultPingTcp.length;
			long soma = 0L;
			for (Long res : resultPingTcp) {
				soma += res;
			}

			pingMedTCP = (int) (soma / tam);
			pingMaxTCP = (int) resultPingTcp[tam - 1];
			pingMinTCP = (int) resultPingTcp[0];
		}
	}

	public void generatingPingUDPStats() {
		if (resultPingUdp != null) {

			Arrays.sort(resultPingUdp);

			int tam = resultPingUdp.length;
			long soma = 0L;
			for (Long res : resultPingUdp) {
				soma += res;
			}

			pingMedUDP = (int) (soma / tam);
			pingMaxUDP = (int) resultPingUdp[tam - 1];
			pingMinUDP = (int) resultPingUdp[0];
		}
	}

	public JSONObject toJSON() throws JSONException {
		JSONObject object = new JSONObject();

		object.put("date", date.getTime());
		object.put("tcp", new JSONArray(Network.arrayToString(resultPingTcp)));
		object.put("udp", new JSONArray(Network.arrayToString(resultPingUdp)));
		object.put("loss", lossPacket);
		object.put("jitter", jitter);
		object.put("down", bandwidthDownload);
		object.put("up", bandwidthUpload);
		object.put("net", type);
		object.put("appName", appName);

		return object;
	}

	@Override
	public String toString() {
		try {
			return toJSON().toString();
		} catch (JSONException e) {
			return null;
		}
	}
}