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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

import br.ufc.mdcc.mpos.net.profile.model.Network;
import br.ufc.mdcc.mpos.net.profile.model.ProfileResult;

/**
 * This class manipulate the sqlite for <ProfileResult> object.
 * 
 * @author Philipp B. Costa
 */
public final class NetProfileDao extends SQLiteJdbc {
	NetProfileDao() {
		super(NetProfileDao.class);
	}

	public void add(ProfileResult results) {
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
		String date = dateFormat.format(results.getNetwork().getDate());

		Connection conn = null;
		PreparedStatement preStmtNetwork = null;

		try {
			mutex.acquire();

			int count = 1;
			conn = openNewConnection();
			preStmtNetwork = conn.prepareStatement("INSERT INTO netprofile (mobileId, carrier, deviceName, appName, latitude, longitude, date, ip, tcp, udp, loss, jitter, down, up, net) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
			preStmtNetwork.setString(count++, results.getDevice().getMobileId());
			preStmtNetwork.setString(count++, results.getDevice().getCarrier());
			preStmtNetwork.setString(count++, results.getDevice().getDeviceName());
			preStmtNetwork.setString(count++, results.getNetwork().getAppName());
			preStmtNetwork.setString(count++, results.getDevice().getLatitude());
			preStmtNetwork.setString(count++, results.getDevice().getLongitude());
			preStmtNetwork.setString(count++, date);
			preStmtNetwork.setString(count++, results.getIp());
			preStmtNetwork.setString(count++, Network.arrayToString(results.getNetwork().getResultPingTcp()));
			preStmtNetwork.setString(count++, Network.arrayToString(results.getNetwork().getResultPingUdp()));
			preStmtNetwork.setInt(count++, results.getNetwork().getLossPacket());
			preStmtNetwork.setInt(count++, results.getNetwork().getJitter());
			preStmtNetwork.setString(count++, results.getNetwork().getBandwidthDownload());
			preStmtNetwork.setString(count++, results.getNetwork().getBandwidthUpload());
			preStmtNetwork.setString(count++, results.getNetwork().getType());
			preStmtNetwork.executeUpdate();

			logger.debug("Network results added");

		} catch (SQLException e) {
			logger.error("Some error in SQL Execution", e);
		} catch (InterruptedException e) {
			logger.error("Interrupted signal for mutex semaphore", e);
		} finally {
			closeStatement(preStmtNetwork);
			closeConnection(conn);
			mutex.release();
		}
	}
}