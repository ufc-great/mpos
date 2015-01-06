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
using System.Data.SQLite;
using Ufc.MpOS.Util;

namespace Ufc.MpOS.Persistence
{
	public abstract class SQLiteDriver
	{
		protected readonly object mutex = new object();
		protected readonly Logger logger;

		public SQLiteDriver(Type type)
		{
			logger = Logger.GetLogger(type);
		}

		protected SQLiteConnection OpenNewConnection()
		{
			SQLiteConnection conn = null;
			try
			{
				conn = new SQLiteConnection(@"Data Source=" + EnvironmentFramework.MPOS_PATH + "mpos.data;Version=3;");
				conn.Open();

				logger.Debug("Database connected with success!");

				return conn;
			}
			catch (Exception e)
			{
				logger.Error("Error on database open!", e);
			}
			return conn;
		}

		protected void CloseConnection(SQLiteConnection conn)
		{
			try
			{
				if (conn != null)
				{
					conn.Close();
				}
			}
			catch (Exception e)
			{
				logger.Error("Commit and Close connection error!", e);
			}
		}
	}
}