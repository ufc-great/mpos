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
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.Semaphore;

import org.apache.log4j.Logger;

/**
 * Access the sqlite database. It's thread-safe the open and close connections.
 * 
 * @author Philipp B. Costa
 */
public abstract class SQLiteJdbc {
	protected final Logger logger;
	protected Semaphore mutex = new Semaphore(1);

	protected SQLiteJdbc(Class<?> cls) {
		logger = Logger.getLogger(cls);
	}

	protected Connection openNewConnection() {
		Connection conn = null;

		try {
			Class.forName("org.sqlite.JDBC");
			conn = DriverManager.getConnection("jdbc:sqlite:mpos.data");

			conn.setAutoCommit(false);

			logger.debug("Database connected with success!");

			return conn;

		} catch (ClassNotFoundException e) {
			logger.error("Sqlite Class not found!", e);
		} catch (SQLException e) {
			logger.error("Error on database open!", e);
		}

		return conn;
	}

	protected void closeConnection(Connection conn) {
		try {
			if (conn != null) {
				conn.commit();
				conn.close();
			}
		} catch (SQLException e) {
			logger.error("Commit and Close connection error!", e);
		}
	}

	protected void closeStatement(Statement... statements) {
		try {
			for (Statement stmt : statements) {
				if (stmt != null) {
					stmt.close();
				}
			}
		} catch (SQLException e) {
			logger.error("Statement got error on close!", e);
		}
	}

	protected void closeResultSet(ResultSet... resultSets) {
		try {
			for (ResultSet rs : resultSets) {
				if (rs != null) {
					rs.close();
				}
			}
		} catch (SQLException e) {
			logger.error("ResultSet got error on close!", e);
		}
	}
}