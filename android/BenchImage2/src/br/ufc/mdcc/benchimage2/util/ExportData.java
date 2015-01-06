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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

import org.json.JSONException;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;
import br.ufc.mdcc.benchimage2.dao.ResultDao;
import br.ufc.mdcc.benchimage2.dao.model.ResultImage;

/**
 * @author Philipp
 */
public final class ExportData extends AsyncTask<Void, Void, Void> {
    private final String clsName = ExportData.class.getName();

    private final String pattern = "dd-MM-yyyy HH:mm:ss";
    private final DateFormat dateFormat = new SimpleDateFormat(pattern, Locale.US);

    private Context context;
    private ProgressDialog progressDialog;

    private ResultDao dao;
    private String fileNameExport;

    public ExportData(Context context, String fileNameExport) {
        this.context = context;
        this.fileNameExport = fileNameExport;

        dao = new ResultDao(context);
    }

    @Override
    protected void onPreExecute() {
        progressDialog = ProgressDialog.show(context, "", "Exportando banco para arquivo CSV...", true);
        progressDialog.show();
    }

    protected Void doInBackground(Void... params) {
        try {
            FileOutputStream file = new FileOutputStream(new File(Environment.getExternalStorageDirectory(), fileNameExport));
            PrintWriter writer = new PrintWriter(file);

            ArrayList<ResultImage> results = dao.getAll();

            writer.println("id;date;photo_name;filter_name;local;photo_size;cpu_time;upload_time;download_time;total_time;upload_size;download_size;");
            for (ResultImage result : results) {
                writer.print(result.getId() + ";");
                writer.print(dateFormat.format(result.getDate()) + ";");
                writer.print(result.getConfig().getImage() + ";");
                writer.print(result.getConfig().getFilter() + ";");
                writer.print(result.getConfig().getLocal() + ";");
                writer.print(result.getConfig().getSize() + ";");
                writer.print(result.getRpcProfile().getExecutionCpuTime() + ";");
                writer.print(result.getRpcProfile().getUploadTime() + ";");
                writer.print(result.getRpcProfile().getDonwloadTime() + ";");
                writer.print(result.getTotalTime() + ";");
                writer.print(result.getRpcProfile().getUploadSize() + ";");
                writer.println(result.getRpcProfile().getDownloadSize() + ";");
            }

            writer.close();
            file.close();
        } catch (JSONException e) {
            Log.w(clsName, e);
        } catch (ParseException e) {
            Log.w(clsName, e);
        } catch (IOException e) {
            Log.w(clsName, e);
        }

        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
        super.onPostExecute(result);
        // fecha o dialog e avisa que terminou...
        progressDialog.dismiss();
        Toast.makeText(context, "Exportação completada!", Toast.LENGTH_LONG).show();
    }
}