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
package br.ufc.mdcc.benchimage2.util;

import java.util.ArrayList;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * @author Philipp
 */
public final class DatabaseManager extends SQLiteOpenHelper {
	private static final int DATABASE_VERSION = 5;

	private final ArrayList<String> tabelas = new ArrayList<String>(1);

	public DatabaseManager(Context con) {
		super(con, "app.db", null, DATABASE_VERSION);
		tabelas.add("CREATE TABLE result (id INTEGER PRIMARY KEY  AUTOINCREMENT  NOT NULL, photo_name TEXT NOT NULL, filter_name TEXT NOT NULL, local TEXT NOT NULL, photo_size TEXT NOT NULL, " + "execution_cpu_time INTEGER NOT NULL, download_time INTEGER NOT NULL, upload_time INTEGER NOT NULL, total_time INTEGER NOT NULL, " + "download_size TEXT NOT NULL, upload_size TEXT NOT NULL, date DATETIME NOT NULL) ");
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		for (String tabela : tabelas) {
			db.execSQL(tabela);
		}
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		if (oldVersion != newVersion) {
			db.execSQL("DROP TABLE IF EXISTS result");
			onCreate(db);
		}
	}
}