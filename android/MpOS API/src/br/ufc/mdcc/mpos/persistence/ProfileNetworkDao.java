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
package br.ufc.mdcc.mpos.persistence;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

import org.json.JSONException;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import br.ufc.mdcc.mpos.R;
import br.ufc.mdcc.mpos.net.endpoint.EndpointType;
import br.ufc.mdcc.mpos.net.profile.model.Network;

/**
 * @author Philipp B. Costa
 */
public final class ProfileNetworkDao extends Dao {
	private final String pattern = "dd-MM-yyyy HH:mm:ss";
	private final DateFormat dateFormat = new SimpleDateFormat(pattern, Locale.US);
	private final SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern, Locale.US);

	private final String TABLE_NAME;

	// FILEDS
	// private final String F_ID = "id";
	private final String F_DATE = "date";
	private final String F_LOSS = "loss";
	private final String F_JITTER = "jitter";
	private final String F_UDP = "udp";
	private final String F_TCP = "tcp";
	private final String F_DOWN = "down";
	private final String F_UP = "up";
	private final String F_NET_TYPE = "network_type";
	private final String F_ENDPOINT_TYPE = "endpoint_type";

	public ProfileNetworkDao(Context con) {
		super(con);

		TABLE_NAME = con.getString(R.string.name_table_netprofile);
	}

	/**
	 * Adiciona os resultados obtidos no PingTask
	 * 
	 * @param network - Objeto com os resultados do PingTask
	 */
	public void add(Network network) {
		openDatabase();

		ContentValues cv = new ContentValues();

		cv.put(F_DATE, dateFormat.format(network.getDate()));
		cv.put(F_LOSS, network.getLossPacket());
		cv.put(F_JITTER, network.getJitter());
		cv.put(F_UDP, Network.arrayToString(network.getResultPingUdp()));
		cv.put(F_TCP, Network.arrayToString(network.getResultPingTcp()));
		cv.put(F_DOWN, network.getBandwidthDownload());
		cv.put(F_UP, network.getBandwidthUpload());
		cv.put(F_NET_TYPE, network.getNetworkType());
		cv.put(F_ENDPOINT_TYPE, network.getEndpointType());

		database.insert(TABLE_NAME, null, cv);

		closeDatabase();
	}

	/**
	 * Consulta os ultimos 15 resultados do profile network
	 * 
	 * @return lista dos 15 ultimos resultados.
	 * @throws JSONException
	 * @throws ParseException
	 */
	public ArrayList<Network> getLast15Results() throws JSONException, ParseException {
		return getLastResults("SELECT * FROM " + TABLE_NAME + " ORDER BY id DESC LIMIT 16");
	}

	public ArrayList<Network> getLast15Results(EndpointType type) throws JSONException, ParseException {
		return getLastResults("SELECT * FROM " + TABLE_NAME + " WHERE endpoint_type = '" + type.getValue() + "' ORDER BY id DESC LIMIT 16");
	}

	private ArrayList<Network> getLastResults(String sql) throws JSONException, ParseException {
		openDatabase();

		Cursor cursor = database.rawQuery(sql, null);

		ArrayList<Network> lista = new ArrayList<Network>(15);

		// obtem todos os indices das colunas da tabela
		int idx_loss = cursor.getColumnIndex(F_LOSS);
		int idx_jitter = cursor.getColumnIndex(F_JITTER);
		int idx_udp = cursor.getColumnIndex(F_UDP);
		int idx_tcp = cursor.getColumnIndex(F_TCP);
		int idx_down = cursor.getColumnIndex(F_DOWN);
		int idx_up = cursor.getColumnIndex(F_UP);
		int idx_net_type = cursor.getColumnIndex(F_NET_TYPE);
		int idx_endpoint_type = cursor.getColumnIndex(F_ENDPOINT_TYPE);
		int idx_date = cursor.getColumnIndex(F_DATE);

		if (cursor != null && cursor.moveToFirst()) {
			do {
				Network network = new Network();
				network.setJitter(cursor.getInt(idx_jitter));
				network.setLossPacket(cursor.getInt(idx_loss));
				network.setBandwidthDownload(cursor.getString(idx_down));
				network.setBandwidthUpload(cursor.getString(idx_up));
				network.setResultPingTcp(Network.stringToLongArray(cursor.getString(idx_tcp)));
				network.setResultPingUdp(Network.stringToLongArray(cursor.getString(idx_udp)));
				network.setEndpointType(cursor.getString(idx_endpoint_type));
				network.setNetworkType(cursor.getString(idx_net_type));
				network.setDate(simpleDateFormat.parse(cursor.getString(idx_date)));

				lista.add(network);
			} while (cursor.moveToNext());
		}

		lista.remove(0);

		cursor.close();
		closeDatabase();

		return lista;
	}

	public void clean() {
		openDatabase();

		database.delete(TABLE_NAME, null, null);

		closeDatabase();
	}
}