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
using System;
using System.Collections.Generic;
using System.Data;
using System.Data.SQLite;
using Ufc.MpOS.Net.Rpc.Deploy.Model;

namespace Ufc.MpOS.Persistence
{
	class RpcServiceDao : SQLiteDriver
	{
		public RpcServiceDao() : base(typeof(RpcServiceDao)) { }
		
		public void Add(RpcService service)
		{
			SQLiteConnection conn = OpenNewConnection();
			try
			{
				lock (mutex)
				{
					using (var cmdRpcService = new SQLiteCommand(conn))
					{
						using (var transaction = conn.BeginTransaction())
						{
							cmdRpcService.CommandText = @"INSERT INTO rpcservice (name,port,version_app,plataform) VALUES (@name, @port, @version_app, @plataform); SELECT last_insert_rowid();";
							cmdRpcService.Parameters.AddWithValue("@name", service.Name);
							cmdRpcService.Parameters.AddWithValue("@port", service.Port);
							cmdRpcService.Parameters.AddWithValue("@version_app", service.VersionApp);
							cmdRpcService.Parameters.AddWithValue("@plataform", "wp");

							int id = Convert.ToInt32(cmdRpcService.ExecuteScalar().ToString());

							using (var cmdDependence = new SQLiteCommand(conn))
							{
								foreach (DependencePath dep in service.Dependencies)
								{
									cmdDependence.CommandText = @"INSERT INTO dependency_app (id_service,path) VALUES (@id_service, @path);";
									cmdDependence.Parameters.AddWithValue("@id_service", id);
									cmdDependence.Parameters.AddWithValue("@path", dep.Path);
									cmdDependence.ExecuteNonQuery();
								}
							}
							transaction.Commit();
						}
					}
				}
			}
			catch (Exception e)
			{
				logger.Error("Some error in SQL Execution", e);
			}
			finally
			{
				CloseConnection(conn);
			}
		}

		public RpcService Get(string name, int version, string plataform)
		{
			RpcService service = new RpcService();
			SQLiteConnection conn = OpenNewConnection();

			try
			{
				lock (mutex)
				{
					using (var cmdSelect = new SQLiteCommand(conn))
					{
						//TODO:
					}
				}
			}
			catch (Exception e)
			{
				logger.Error("Some error in SQL Execution", e);
			}
			finally
			{
				CloseConnection(conn);
			}

			return service;
		}

		public List<int> GetAllPorts()
		{
			List<int> ports = new List<int>();
			SQLiteConnection conn = OpenNewConnection();

			try
			{
				lock (mutex)
				{
					using (var cmdSelect = new SQLiteCommand(conn))
					{
						cmdSelect.CommandText = @"SELECT port FROM rpcservice";
						cmdSelect.CommandType = CommandType.Text;

						SQLiteDataReader reader = cmdSelect.ExecuteReader();
						while (reader.Read())
						{
							ports.Add(Convert.ToInt32(reader["port"]));
						}
					}
				}
			}
			catch (Exception e)
			{
				logger.Error("Some error in SQL Execution", e);
			}
			finally
			{
				CloseConnection(conn);
				ports.Sort();
			}

			return ports;
		}

		public List<RpcService> GetAll()
		{
			List<RpcService> services = new List<RpcService>();

			SQLiteConnection conn = OpenNewConnection();
			try
			{
				lock (mutex)
				{
					using (var selectRpcService = new SQLiteCommand(conn))
					{
						selectRpcService.CommandText = @"SELECT * FROM rpcservice WHERE plataform = 'wp'";
						selectRpcService.CommandType = CommandType.Text;

						SQLiteDataReader rpcReader = selectRpcService.ExecuteReader();
						while (rpcReader.Read())
						{
							RpcService service = new RpcService();
							service.Id = Convert.ToInt32(rpcReader["id"]);
							service.Name = Convert.ToString(rpcReader["name"]);
							service.Port = Convert.ToInt32(rpcReader["port"]);
							service.VersionApp = Convert.ToString(rpcReader["version_app"]);

							using (var selectDep = new SQLiteCommand(conn))
							{
								selectDep.CommandText = @"SELECT * FROM dependency_app WHERE id_service = " + service.Id + "";
								selectDep.CommandType = CommandType.Text;

								SQLiteDataReader depReader = selectDep.ExecuteReader();
								while (depReader.Read())
								{
									DependencePath dep = new DependencePath();
									dep.Id = Convert.ToInt32(depReader["id"]);
									dep.IdService = Convert.ToInt32(depReader["id_service"]);
									dep.Path = Convert.ToString(depReader["path"]);

									service.Dependencies.Add(dep);
								}
							}

							services.Add(service);
						}
					}
				}
			}
			catch (Exception e)
			{
				logger.Error("Some error in SQL Execution", e);
			}
			finally
			{
				CloseConnection(conn);
				services.Sort();
			}

			return services;
		}
	}
}
