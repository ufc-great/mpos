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
package br.ufc.mdcc.collision.util;

import java.util.List;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import br.ufc.mdcc.collision.controller.BallCustomUpdater;
import br.ufc.mdcc.collision.controller.BallUpdater;
import br.ufc.mdcc.collision.model.Ball;

/**
 * Surface View uses a threaded render for processing image updates at 60fps
 * 
 * @author Philipp
 */
public final class WindowSurfaceView extends SurfaceView {
    private final String clsName = WindowSurfaceView.class.getName();

    private Render render;

    private BallCustomUpdater<Ball> controllerCustom;
    private BallUpdater<Ball> controllerSerializable;

    private List<Ball> balls;
    private String executionType;

    private volatile boolean startAnimation = false;

    private int width;
    private int height;

    public WindowSurfaceView(Context context) {
        super(context);
        SurfaceHolder holder = getHolder();
        render = new Render(holder);

        // starting render
        holder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                if (render != null) {
                    render.running = true;
                    render.start();
                    Log.i(clsName, "Start Render Animation");
                }
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                if (render != null) {
                    render.running = false;
                    boolean retry = true;
                    while (retry) {
                        try {
                            render.join();
                            retry = false;
                        } catch (InterruptedException e) {}
                    }
                    clean();
                    Log.i(clsName, "Stopping Render Animation");
                }
            }

            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {}

            private void clean() {
                render = null;
                balls.clear();
                balls = null;
            }
        });
    }

    @Override
    protected void onSizeChanged(int width, int height, int oldw, int oldh) {
        this.width = width;
        this.height = height;
        
        Log.i(clsName, "[DEBUG]: WindowSize -> " + width + "x" + height);
    }

    public void setControllerCustom(BallCustomUpdater<Ball> controllerCustom) {
        this.controllerCustom = controllerCustom;
    }

    public void setControllerSerializable(BallUpdater<Ball> controllerSerializable) {
        this.controllerSerializable = controllerSerializable;
    }

    public void setBalls(List<Ball> balls) {
        this.balls = balls;
    }

    public void setExecutionType(String executionType) {
        this.executionType = executionType;
    }

    public boolean isStartAnimation() {
        return startAnimation;
    }

    public void changeStartAnimation() {
        startAnimation = !startAnimation;
    }

    /**
     * @author Philipp
     */
    private final class Render extends Thread {
        private volatile boolean running;

        private Canvas canvas;
        private SurfaceHolder surfaceHolder;

        private Paint paint;
        private RectF ballBounds;

        // for calcule fps and cycle time
        private long initTime;
        private int countFps = 0;
        private float fps = 60.0f;
        private long cycleTime = 1000L;
        private final int FRAME_PER_SEC = 15;

        public Render(SurfaceHolder surfaceHolder) {
            this.surfaceHolder = surfaceHolder;
            running = false;
            
            paint = new Paint();
            paint.setFlags(Paint.ANTI_ALIAS_FLAG);
            ballBounds = new RectF();
        }

        @Override
        public void run() {
            if (controllerCustom != null) {
                controllerCustom.setWindowDimensions(width, height);
            }

            while (running) {
                // this lock stable the fps in maxium 60 per second
                canvas = surfaceHolder.lockCanvas();
                if (canvas != null) {
                    initTimeFrame();
                    endTimeFrame();

                    canvas.drawColor(Color.BLACK);
                    drawScreen(canvas);
                    drawStringFps(canvas);
                    surfaceHolder.unlockCanvasAndPost(canvas);

                    countFps++;
                }
                canvas = null;
            }
        }

        private void initTimeFrame() {
            if (countFps % FRAME_PER_SEC == 0) {
                initTime = System.currentTimeMillis();
            }
        }

        private void endTimeFrame() {
            //capture the last frame from 15fps for calcule the cycle...
            //cycletime how many 'ms' spent for made 15fps
            if (countFps % FRAME_PER_SEC == 14) {
                cycleTime = System.currentTimeMillis() - initTime;
                fps = (FRAME_PER_SEC / ((float) cycleTime / 1000.0f));
            }
        }

        private void drawScreen(Canvas canvas) {
            if (startAnimation) {
                if (controllerCustom != null) {
                    if (executionType.equals("local")) {
                        balls = controllerCustom.updateOffline(balls);
                    } else if (executionType.equals("static")) {
                        balls = controllerCustom.updateStatic(balls);
                    } else if (executionType.equals("dynamic")) {
                        balls = controllerCustom.updateDynamic(balls);
                    }
                } else {
                    if (executionType.equals("local")) {
                        balls = controllerSerializable.updateOffline(balls, width, height);
                    } else if (executionType.equals("static")) {
                        balls = controllerSerializable.updateStatic(balls, width, height);
                    } else if (executionType.equals("dynamic")) {
                        balls = controllerSerializable.updateDynamic(balls, width, height);
                    }
                }
            }
            for (Ball ball : balls) {
                ball.generateBounds();
                ballBounds.set(ball.getLeft(), ball.getTop(), ball.getRight(), ball.getBottom());
                paint.setColor(ball.getColor());
                canvas.drawOval(ballBounds, paint);
            }
        }

        private void drawStringFps(Canvas canvas) {
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Color.CYAN);
            paint.setTextSize(20.0f);
            canvas.drawText(String.format("FPS: %.2f; cycle process: %dms", fps, cycleTime), 5.0f, 20.0f, paint);
        }
    }
}