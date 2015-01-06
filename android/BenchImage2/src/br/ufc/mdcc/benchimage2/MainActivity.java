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
package br.ufc.mdcc.benchimage2;

import java.io.File;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Process;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import br.ufc.mdcc.benchimage2.dao.model.AppConfiguration;
import br.ufc.mdcc.benchimage2.dao.model.ResultImage;
import br.ufc.mdcc.benchimage2.image.Filter;
import br.ufc.mdcc.benchimage2.image.CloudletFilter;
import br.ufc.mdcc.benchimage2.image.InternetFilter;
import br.ufc.mdcc.benchimage2.image.ImageFilter;
import br.ufc.mdcc.benchimage2.image.ImageFilterTask;
import br.ufc.mdcc.benchimage2.util.ExportData;
import br.ufc.mdcc.mpos.MposFramework;
import br.ufc.mdcc.mpos.config.Inject;
import br.ufc.mdcc.mpos.config.MposConfig;
import br.ufc.mdcc.mpos.util.TaskResultAdapter;

/**
 * @author Philipp
 */
@MposConfig(endpointSecondary = "54.94.172.61")
public final class MainActivity extends Activity {
    private final String clsName = MainActivity.class.getName();

    private Filter filterLocal = new ImageFilter();

    @Inject(ImageFilter.class)
    private CloudletFilter cloudletFilter;

    @Inject(ImageFilter.class)
    private InternetFilter internetFilter;

    private AppConfiguration config;
    private String photoName;
    private long vmSize = 0L;

    private boolean quit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        quit = false;
        MposFramework.getInstance().start(this);

        config = new AppConfiguration();

        configureSpinner();
        getConfigFromSpinner();

        configureButton();
        configureStatusView("Status: Sem Atividade");

        createDirOutput();
        processImage();

        Log.i(clsName, "Iniciou PicFilter");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        
        if (quit) {
            MposFramework.getInstance().stop();
            Process.killProcess(Process.myPid());
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        switch (item.getItemId()) {
            case R.id.menu_action_export:
                alertDialogBuilder.setTitle("Exportar Resultados");
                alertDialogBuilder.setIcon(android.R.drawable.ic_dialog_alert);
                alertDialogBuilder.setPositiveButton(R.string.button_ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        new ExportData(MainActivity.this, "benchimage2_data.csv").execute();
                    }
                });
                alertDialogBuilder.setNegativeButton(R.string.button_cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                alertDialogBuilder.setMessage("Deseja exportar resultados?");
                alertDialogBuilder.create().show();
                break;
        }

        return true;
    }

    public void onBackPressed() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle(R.string.alert_exit_title);
        alertDialogBuilder.setIcon(android.R.drawable.ic_dialog_alert);
        alertDialogBuilder.setPositiveButton(R.string.button_ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Log.i(clsName, "BenchImage Particle finished");
                quit = true;
                finish();
            }
        });
        alertDialogBuilder.setNegativeButton(R.string.button_cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        alertDialogBuilder.setMessage(R.string.alert_exit_message);
        alertDialogBuilder.create().show();
    }

    private void processImage() {
        getConfigFromSpinner();
        configureStatusViewOnTaskStart();

        System.gc();

        if ((config.getFilter().equals("Cartoonizer") || config.getFilter().equals("Benchmark")) && vmSize <= 64 && (config.getSize().equals("8MP") || config.getSize().equals("4MP"))) {
            dialogSupportFilter();
        } else {
            if (config.getLocal().equals("Local")) {
                new ImageFilterTask(getApplication(), filterLocal, config, taskResultAdapter).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            } else if (config.getLocal().equals("Cloudlet")) {
                new ImageFilterTask(getApplication(), cloudletFilter, config, taskResultAdapter).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            } else {
                new ImageFilterTask(getApplication(), internetFilter, config, taskResultAdapter).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }
        }
    }

    private void configureButton() {
        Button but = (Button) findViewById(R.id.button_execute);
        but.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                buttonStatusChange(R.id.button_execute, false, "Processando");
                processImage();
            }
        });
    }

    private void configureSpinner() {
        Spinner spinnerImage = (Spinner) findViewById(R.id.spin_image);
        Spinner spinnerFilter = (Spinner) findViewById(R.id.spin_filter);
        Spinner spinnerSize = (Spinner) findViewById(R.id.spin_size);
        Spinner spinnerLocal = (Spinner) findViewById(R.id.spin_local);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.spinner_img, R.layout.spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerImage.setAdapter(adapter);
        spinnerImage.setSelection(2);

        adapter = ArrayAdapter.createFromResource(this, R.array.spinner_filter, R.layout.spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFilter.setAdapter(adapter);

        adapter = ArrayAdapter.createFromResource(this, R.array.spinner_local, R.layout.spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerLocal.setAdapter(adapter);

        adapter = ArrayAdapter.createFromResource(this, R.array.spinner_size, R.layout.spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSize.setAdapter(adapter);
        spinnerSize.setSelection(4);
    }

    private void configureStatusViewOnTaskStart() {
        configureStatusView("Status: Submetendo Tarefa");

        ImageView imageView = (ImageView) findViewById(R.id.imageView);
        imageView.setImageBitmap(null);
    }

    private void configureStatusView(String status) {
        TextView tv_vmsize = (TextView) findViewById(R.id.text_vmsize);
        vmSize = Runtime.getRuntime().maxMemory() / (1024 * 1024);
        tv_vmsize.setText("VMSize " + vmSize + "MB");

        if (vmSize < 128) {
            tv_vmsize.setTextColor(Color.RED);
        } else if (vmSize == 128) {
            tv_vmsize.setTextColor(Color.YELLOW);
        } else {
            tv_vmsize.setTextColor(Color.GREEN);
        }

        TextView tv_execucao = (TextView) findViewById(R.id.text_exec);
        tv_execucao.setText("Tempo de\nExecução: 0s");

        TextView tv_tamanho = (TextView) findViewById(R.id.text_size);
        tv_tamanho.setText("Tamanho/Foto: " + config.getSize() + "/" + photoName);

        TextView tv_status = (TextView) findViewById(R.id.text_status);
        tv_status.setText(status);
    }

    private void getConfigFromSpinner() {
        Spinner spinnerImage = (Spinner) findViewById(R.id.spin_image);
        Spinner spinnerFilter = (Spinner) findViewById(R.id.spin_filter);
        Spinner spinnerSize = (Spinner) findViewById(R.id.spin_size);
        Spinner spinnerLocal = (Spinner) findViewById(R.id.spin_local);

        photoName = (String) spinnerImage.getSelectedItem();
        config.setImage(photoNameToFileName(photoName));
        config.setLocal((String) spinnerLocal.getSelectedItem());

        config.setFilter((String) spinnerFilter.getSelectedItem());
        if (config.getFilter().equals("Benchmark")) {
            config.setSize("All");
        } else {
            config.setSize((String) spinnerSize.getSelectedItem());
        }
    }

    private void dialogSupportFilter() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle("Celular limitado!");
        alertDialogBuilder.setIcon(android.R.drawable.ic_dialog_alert);
        alertDialogBuilder.setNegativeButton(R.string.button_ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        alertDialogBuilder.setMessage("Celular não suporta o Cartoonizer, minimo recomendo é 128MB de VMSize");
        // cria e mostra
        alertDialogBuilder.create().show();

        buttonStatusChange(R.id.button_execute, true, "Inicia");
        TextView tv_status = (TextView) findViewById(R.id.text_status);
        tv_status.setText("Status: Requisição anterior não suporta Filtro!");
    }

    private void buttonStatusChange(int id, boolean state, String text) {
        Button but = (Button) findViewById(id);
        but.setEnabled(state);
        but.setText(text);
    }

    private String photoNameToFileName(String name) {
        if (name.equals("FAB Show")) {
            return "img1.jpg";
        } else if (name.equals("Cidade")) {
            return "img4.jpg";
        } else if (name.equals("SkyLine")) {
            return "img5.jpg";
        }
        return null;
    }

    private void createDirOutput() {
        File storage = Environment.getExternalStorageDirectory();
        String outputDir = storage.getAbsolutePath() + File.separator + "BenchImageOutput";

        File dir = new File(outputDir);
        if (!dir.exists()) {
            dir.mkdir();
        }

        config.setOutputDirectory(outputDir);
    }

    private TaskResultAdapter<ResultImage> taskResultAdapter = new TaskResultAdapter<ResultImage>() {
        @Override
        public void completedTask(ResultImage obj) {
            if (obj != null) {
                ImageView imageView = (ImageView) findViewById(R.id.imageView);
                imageView.setImageBitmap(obj.getBitmap());

                TextView tv_tamanho = (TextView) findViewById(R.id.text_size);
                tv_tamanho.setText("Tamanho/Foto: " + config.getSize() + "/" + photoName);

                TextView tv_execucao = (TextView) findViewById(R.id.text_exec);
                if (obj.getTotalTime() != 0) {
                    double segundos = obj.getTotalTime() / 1000.0;
                    tv_execucao.setText("Tempo de\nExecução: " + String.format("%.3f", segundos) + "s");
                } else {
                    tv_execucao.setText("Tempo de\nExecução: 0s");
                }
                if (obj.getConfig().getFilter().equals("Benchmark")) {
                    double segundos = obj.getTotalTime() / 1000.0;
                    tv_execucao.setText("Tempo de\nExecução: " + String.format("%.3f", segundos) + "s");
                }
            } else {
                TextView tv_status = (TextView) findViewById(R.id.text_status);
                tv_status.setText("Status: Algum Error na transmissão!");
            }
            buttonStatusChange(R.id.button_execute, true, "Inicia");
        }

        @Override
        public void taskOnGoing(int completed, String statusText) {
            TextView tv_status = (TextView) findViewById(R.id.text_status);
            tv_status.setText("Status: " + statusText);
        }
    };
}