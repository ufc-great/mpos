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

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Access the sqlite database for open and close connections.
 * 
 * @author Philipp B. Costa
 */
public abstract class Dao {
	private final SQLiteOpenHelper databaseManager;

	protected SQLiteDatabase database;

	public Dao(Context con) {
		databaseManager = new DatabaseManager(con);
	}

	public Dao(SQLiteOpenHelper databaseManager) {
		this.databaseManager = databaseManager;
	}

	public void openDatabase() {
		database = databaseManager.getWritableDatabase();
	}

	public void closeDatabase() {
		database.close();
	}
}