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

import br.ufc.mdcc.mpos.MposFramework;

/**
 * Model used for save locally and sent all data to server.
 * 
 * @author Philipp B. Costa
 */
public final class Network {
    private int id;
    private int pingEstimated;
    private int pingMedUdp, pingMedTcp;
    private int pingMaxUdp, pingMinUdp;
    private int pingMaxTcp, pingMinTcp;

    private Date date;
    private long resultPingUdp[];
    private long resultPingTcp[];
    private int lossPacket;
    private int jitter;

    private String bandwidthDownload, bandwidthUpload;
    private String networkType;
    private String endpointType;
    private String appName;

    public Network() {
        date = new Date();
        bandwidthDownload = "0";
        bandwidthUpload = "0";

        networkType = MposFramework.getInstance().getDeviceController().getNetworkConnectedType();
        appName = MposFramework.getInstance().getDeviceController().getAppName();
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

    public final int getPingMedUdp() {
        return pingMedUdp;
    }

    public final int getPingMedTcp() {
        return pingMedTcp;
    }

    public final int getPingMaxUdp() {
        return pingMaxUdp;
    }

    public final int getPingMinUdp() {
        return pingMinUdp;
    }

    public final int getPingMaxTcp() {
        return pingMaxTcp;
    }

    public final int getPingMinTcp() {
        return pingMinTcp;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public String getEndpointType() {
        return endpointType;
    }

    public void setEndpointType(String endpointType) {
        this.endpointType = endpointType;
    }

    public String getNetworkType() {
        return networkType;
    }

    public void setNetworkType(String networkType) {
        this.networkType = networkType;
    }

    public final String getAppName() {
        return appName;
    }

    public final void setAppName(String appName) {
        this.appName = appName;
    }

    public static String arrayToString(long... vlrs) {
        JSONArray jsonArray = new JSONArray();

        if (vlrs != null) {
            for (long vlr : vlrs) {
                jsonArray.put(vlr);
            }
            return jsonArray.toString();
        } else {
            return "[]";
        }
    }

    public static long[] stringToLongArray(String vlrArray) throws JSONException {
        JSONArray jsonArray = new JSONArray(vlrArray);
        long array[] = new long[jsonArray.length()];

        for (int i = 0; i < jsonArray.length(); i++) {
            array[i] = jsonArray.getLong(i);
        }

        return array;
    }

    public void generatingPingTcpStats() {
        if (resultPingTcp != null) {
            Arrays.sort(resultPingTcp);

            int tam = resultPingTcp.length;
            long soma = 0L;
            for (Long res : resultPingTcp) {
                soma += res;
            }

            pingMedTcp = (int) (soma / tam);
            pingMaxTcp = (int) resultPingTcp[tam - 1];
            pingMinTcp = (int) resultPingTcp[0];
        }
    }

    public void generatingPingUdpStats() {
        if (resultPingUdp != null) {
            Arrays.sort(resultPingUdp);

            int tam = resultPingUdp.length;
            long soma = 0L;
            for (Long res : resultPingUdp) {
                soma += res;
            }

            pingMedUdp = (int) (soma / tam);
            pingMaxUdp = (int) resultPingUdp[tam - 1];
            pingMinUdp = (int) resultPingUdp[0];
        }
    }

    public JSONObject toJson() throws JSONException {
        JSONObject object = new JSONObject();

        object.put("date", date.getTime());
        object.put("tcp", new JSONArray(Network.arrayToString(resultPingTcp)));
        object.put("udp", new JSONArray(Network.arrayToString(resultPingUdp)));
        object.put("loss", lossPacket);
        object.put("jitter", jitter);
        object.put("down", bandwidthDownload);
        object.put("up", bandwidthUpload);
        object.put("net", networkType);
        object.put("appName", appName);

        return object;
    }

    @Override
    public String toString() {
        try {
            return toJson().toString();
        } catch (JSONException e) {
            return null;
        }
    }
}