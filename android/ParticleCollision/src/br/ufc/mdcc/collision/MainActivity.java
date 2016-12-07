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
package br.ufc.mdcc.collision;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Process;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;
import br.ufc.mdcc.collision.controller.BallController;
import br.ufc.mdcc.collision.controller.BallControllerCustom;
import br.ufc.mdcc.collision.controller.BallCustomUpdater;
import br.ufc.mdcc.collision.controller.BallUpdater;
import br.ufc.mdcc.collision.model.Ball;
import br.ufc.mdcc.collision.model.Config;
import br.ufc.mdcc.collision.util.WindowSurfaceView;
import br.ufc.mdcc.mpos.MposFramework;
import br.ufc.mdcc.mpos.config.Inject;
import br.ufc.mdcc.mpos.config.MposConfig;

/**
 * @author Philipp
 */
@MposConfig
public final class MainActivity extends Activity {
    private final String clsName = MainActivity.class.getName();

    private Point windowSize;
    private WindowSurfaceView window;

    @Inject(BallControllerCustom.class)
    private BallCustomUpdater<Ball> controllerCustom;

    @Inject(BallController.class)
    private BallUpdater<Ball> controllerSerializable;

    private List<Ball> balls;
    private Config appConfig;

    private boolean quit;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        quit = false;
        
        MposFramework.getInstance().start(this);
        
        // getWindowSize
        windowSize = new Point();
        getWindowManager().getDefaultDisplay().getSize(windowSize);

        appConfig = new Config(getApplication(), PreferenceManager.getDefaultSharedPreferences(MainActivity.this));

        LinearLayout container = (LinearLayout) findViewById(R.id.rootLayout);
        initWindowSurface(container);
        Log.i(clsName, "Collision Particle started");
    }
    
    private void initWindowSurface(LinearLayout container) {
        generateBalls();

        window = new WindowSurfaceView(getApplication());
        if (appConfig.isSerialNative()) {
            window.setControllerSerializable(controllerSerializable);
        } else {
            window.setControllerCustom(controllerCustom);
        }
        window.setBalls(balls);
        window.setExecutionType(appConfig.getExecutitonType());

        container.addView(window);
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        
        if (quit) {
            MposFramework.getInstance().stop();
            Process.killProcess(Process.myPid());
        }
    }

    private void resetWindowSurface(LinearLayout container) {
        container.removeView(window);
        window = null;

        initWindowSurface(container);
    }

    public void onBackPressed() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle(R.string.alert_exit_title);
        alertDialogBuilder.setIcon(android.R.drawable.ic_dialog_alert);
        alertDialogBuilder.setPositiveButton(R.string.button_ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Log.i(clsName, "Collision Particle finished");
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

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_action_settings:
                Intent settings = new Intent(getBaseContext(), SettingsActivity.class);
                settings.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(settings);
                break;

            case R.id.menu_action_start:
                if (!window.isStartAnimation()) {
                    item.setIcon(R.drawable.stop);
                    window.changeStartAnimation();
                } else {
                    item.setIcon(R.drawable.play);
                    resetWindowSurface((LinearLayout) findViewById(R.id.rootLayout));
                }
                break;
        }
        return true;
    }

    private void generateBalls() {
        Random random = new Random();

        int size = appConfig.getQuantity();
        int width = windowSize.x - 30;
        int height = windowSize.y - 80;

        balls = new ArrayList<Ball>(size);
        for (int i = 0; i < size; i++) {
            balls.add(new Ball(appConfig.getRadiusBall(), random, appConfig.isPlaceStart(), width, height));
        }
    }
}