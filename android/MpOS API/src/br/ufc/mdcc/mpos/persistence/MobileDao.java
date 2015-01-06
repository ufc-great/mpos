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

import java.util.UUID;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import br.ufc.mdcc.mpos.R;

/**
 * @author Philipp B. Costa
 */
public final class MobileDao extends Dao {
	private final String TABLE_NAME;

	// F_ID = FIELD ID
	private final String F_ID = "id";

	public MobileDao(Context con) {
		super(con);

		TABLE_NAME = con.getString(R.string.name_table_user);
	}

	// apenas procura se existe
	private String getMobileId() {
		openDatabase();

		Cursor cursor = database.query(TABLE_NAME, new String[] { F_ID }, null, null, null, null, F_ID);

		String mobileId = null;

		if (cursor != null && cursor.moveToFirst()) {
			do {
				mobileId = cursor.getString(0);
			} while (cursor.moveToNext());
		}

		closeDatabase();

		return mobileId;
	}

	// gera e devolve o mobileId
	private String generatingMobileId() {
		openDatabase();

		String uuid = UUID.randomUUID().toString();

		ContentValues campos = new ContentValues();
		campos.put(F_ID, uuid);

		// insere no banco
		database.insert(TABLE_NAME, null, campos);

		closeDatabase();

		return uuid;
	}

	public String checkMobileId() {
		String mobileId = getMobileId();
		if (mobileId == null || mobileId.equals("")) {
			mobileId = generatingMobileId();
		}

		return mobileId;
	}
}