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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import br.ufc.mdcc.mpos.net.rpc.deploy.model.DependencePath;
import br.ufc.mdcc.mpos.net.rpc.deploy.model.RpcService;

/**
 * This class manipulate the sqlite for <RpcService> object.
 * 
 * @author Philipp B. Costa
 */
public final class RpcServiceDao extends SQLiteJdbc {
	RpcServiceDao() {
		super(RpcServiceDao.class);
	}

	public void add(RpcService service) {
		Connection conn = null;
		PreparedStatement preStmtRpcService = null;
		PreparedStatement preStmtDependence = null;

		ResultSet rsKey = null;
		
		try {
			mutex.acquire();

			conn = openNewConnection();
			preStmtRpcService = conn.prepareStatement("INSERT INTO rpcservice (name,port,version_app) VALUES (?,?,?)", Statement.RETURN_GENERATED_KEYS);
			preStmtRpcService.setString(1, service.getName());
			preStmtRpcService.setInt(2, service.getPort());
			preStmtRpcService.setString(3, service.getVersionApp());
			preStmtRpcService.executeUpdate();

			rsKey = preStmtRpcService.getGeneratedKeys();
			if (rsKey.next()) {
				int id = rsKey.getInt(1);
				for (DependencePath dep : service.getDependencies()) {
					preStmtDependence = conn.prepareStatement("INSERT INTO dependency_app (id_service,path) VALUES (?,?)");
					preStmtDependence.setInt(1, id);
					preStmtDependence.setString(2, dep.getPath());
					preStmtDependence.executeUpdate();
				}
			}
			logger.debug("Service RPC: " + service.getName() + " was added");
		} catch (SQLException e) {
			logger.error("Some error in SQL Execution", e);
		} catch (InterruptedException e) {
			logger.error("Interrupted signal for mutex semaphore", e);
		} finally {
			closeResultSet(rsKey);
			closeStatement(preStmtDependence, preStmtRpcService);
			closeConnection(conn);
			mutex.release();
		}
	}

	public RpcService get(String name, String version, String plataform) {
		RpcService service = new RpcService();

		Connection conn = null;
		PreparedStatement preStmtRpcService = null;
		PreparedStatement preStmtDependence = null;
		ResultSet rsRpcService = null;
		ResultSet rsDependence = null;

		try {
			mutex.acquire();

			conn = openNewConnection();
			preStmtRpcService = conn.prepareStatement("SELECT * FROM rpcservice where name = ? and version_app = ? and plataform = ?");
			preStmtRpcService.setString(1, name);
			preStmtRpcService.setString(2, version);
			preStmtRpcService.setString(3, plataform);

			rsRpcService = preStmtRpcService.executeQuery();
			if (rsRpcService.next()) {
				service.setId(rsRpcService.getInt("id"));
				service.setName(rsRpcService.getString("name"));
				service.setPort(rsRpcService.getInt("port"));
				service.setVersionApp(rsRpcService.getString("version_app"));

				List<DependencePath> lst = new ArrayList<DependencePath>();
				preStmtDependence = conn.prepareStatement("SELECT * FROM dependency_app where id_service = ?");
				preStmtDependence.setInt(1, service.getId());
				rsDependence = preStmtDependence.executeQuery();
				while (rsDependence.next()) {
					lst.add(new DependencePath(rsDependence.getInt(1), rsDependence.getInt(2), rsDependence.getString(3)));
				}
				closeResultSet(rsDependence);
				service.setDependencies(lst);
			}
		} catch (SQLException e) {
			logger.error("Some error in SQL Execution", e);
		} catch (InterruptedException e) {
			logger.error("Interrupted signal for mutex semaphore", e);
		} finally {
			closeResultSet(rsRpcService);
			closeStatement(preStmtDependence, preStmtRpcService);
			closeConnection(conn);
			mutex.release();
		}
		return service;
	}

	public List<Integer> getAllPorts() {
		List<Integer> ports = new ArrayList<Integer>();

		Connection conn = null;
		PreparedStatement preStmtPort = null;
		ResultSet rsPort = null;

		try {
			mutex.acquire();

			conn = openNewConnection();
			preStmtPort = conn.prepareStatement("SELECT port FROM rpcservice");

			rsPort = preStmtPort.executeQuery();
			while (rsPort.next()) {
				ports.add(rsPort.getInt("port"));
			}
		} catch (SQLException e) {
			logger.error("Some error in SQL Execution", e);
		} catch (InterruptedException e) {
			logger.error("Interrupted signal for mutex semaphore", e);
		} finally {
			closeResultSet(rsPort);
			closeStatement(preStmtPort);
			closeConnection(conn);
			mutex.release();
		}
		Collections.sort(ports);
		return ports;
	}

	public List<RpcService> getAll() {
		List<RpcService> services = new ArrayList<RpcService>();

		Connection conn = null;
		PreparedStatement preStmtRpcService = null;
		PreparedStatement preStmtDependence = null;
		ResultSet rsRpcService = null;
		ResultSet rsDependence = null;

		try {
			mutex.acquire();

			conn = openNewConnection();
			preStmtRpcService = conn.prepareStatement("SELECT * FROM rpcservice where plataform = 'android'");

			rsRpcService = preStmtRpcService.executeQuery();
			while (rsRpcService.next()) {
				RpcService rpc = new RpcService();
				rpc.setId(rsRpcService.getInt("id"));
				rpc.setName(rsRpcService.getString("name"));
				rpc.setPort(rsRpcService.getInt("port"));
				rpc.setVersionApp(rsRpcService.getString("version_app"));

				List<DependencePath> lst = new ArrayList<DependencePath>();
				preStmtDependence = conn.prepareStatement("SELECT * FROM dependency_app where id_service = ?");
				preStmtDependence.setInt(1, rpc.getId());
				rsDependence = preStmtDependence.executeQuery();
				while (rsDependence.next()) {
					lst.add(new DependencePath(rsDependence.getInt(1), rsDependence.getInt(2), rsDependence.getString(3)));
				}
				closeResultSet(rsDependence);

				rpc.setDependencies(lst);
				services.add(rpc);
			}
		} catch (SQLException e) {
			logger.error("Some error in SQL Execution", e);
		} catch (InterruptedException e) {
			logger.error("Interrupted signal for mutex semaphore", e);
		} finally {
			closeResultSet(rsRpcService);
			closeStatement(preStmtDependence, preStmtRpcService);
			closeConnection(conn);
			mutex.release();
		}

		Collections.sort(services);
		return services;
	}
}