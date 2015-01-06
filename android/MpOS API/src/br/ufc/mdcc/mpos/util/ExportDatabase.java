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
package br.ufc.mdcc.mpos.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

/**
 * Export local mpos database to sdcard
 * 
 * @author Philipp B. Costa
 */
public final class ExportDatabase extends AsyncTask<Void, Void, Void> {
	private Context context;
	private ProgressDialog progressDialog;

	private String internalDatabase;
	private String exportDatabaseName;

	public ExportDatabase(Context context, String databaseName, String exportDatabaseName) {
		this.context = context;
		this.internalDatabase = "/data/" + context.getPackageName() + "/databases/" + databaseName;
		this.exportDatabaseName = exportDatabaseName;
	}

	@Override
	protected void onPreExecute() {
		progressDialog = ProgressDialog.show(context, "", "Exporting data...", true);
		progressDialog.show();
	}

	@Override
	protected Void doInBackground(Void... params) {
		FileInputStream fisSrc = null;
		FileOutputStream fisDst = null;
		FileChannel fcSrc = null;
		FileChannel fcDst = null;

		try {
			File externalStorage = Environment.getExternalStorageDirectory();
			File internalStorage = Environment.getDataDirectory();

			if (externalStorage.canWrite()) {
				File currentDB = new File(internalStorage, internalDatabase);
				File backupDB = new File(externalStorage, exportDatabaseName);// persist on device root

				if (currentDB.exists()) {
					fisSrc = new FileInputStream(currentDB);
					fisDst = new FileOutputStream(backupDB);
					fcSrc = fisSrc.getChannel();
					fcDst = fisDst.getChannel();

					fcDst.transferFrom(fcSrc, 0, fcSrc.size());
				}
			}
		} catch (IOException e) {
			Log.w("Database exporting suffer with some I/O error", e);
		} finally {
			try {
				if (fcSrc != null) {
					fcSrc.close();
				}
				if (fcDst != null) {
					fcDst.close();
				}
				if (fisSrc != null) {
					fisSrc.close();
				}
				if (fisDst != null) {
					fisDst.close();
				}
			} catch (IOException e) {
				Log.w("I/O error on close file and file channels!", e);
			}
		}
		return null;
	}

	@Override
	protected void onPostExecute(Void result) {
		super.onPostExecute(result);
		// fecha o dialog e avisa que terminou...
		progressDialog.dismiss();
		Toast.makeText(context, "Exporting data finished!", Toast.LENGTH_LONG).show();
	}
}