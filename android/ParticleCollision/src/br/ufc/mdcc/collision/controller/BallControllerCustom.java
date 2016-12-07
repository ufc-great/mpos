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
package br.ufc.mdcc.collision.controller;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import br.ufc.mdcc.collision.model.Ball;
import br.ufc.mdcc.collision.util.CollisionBall;

/**
 * Controller for update ball state in animation. That class have support offloading in you interface and have custom serializable system
 * 
 * @author Philipp
 */
public final class BallControllerCustom implements BallCustomUpdater<Ball> {
    private CollisionBall collision;

    public BallControllerCustom() {
        collision = new CollisionBall();
    }

    @Override
    public List<Ball> updateOffline(List<Ball> balls) {
        return updateState(balls);
    }

    @Override
    public List<Ball> updateStatic(List<Ball> balls) {
        return updateState(balls);
    }

    @Override
    public List<Ball> updateDynamic(List<Ball> balls) {
        return updateState(balls);
    }

    private List<Ball> updateState(List<Ball> balls) {
        for (Ball current : balls) {
            current.setNextPosition();
            collision.collisionWindow(current);
            collision.detectCollisionBall(current, balls);
        }
        return balls;
    }

    @SuppressWarnings("unchecked")
    public void writeMethodParams(DataOutput out, String methodName, Object[] params) throws IOException {
        if (methodName.equals("updateStatic")) {
            List<Ball> balls = (List<Ball>) params[0];
            out.writeInt(balls.size());

            for (Ball ball : balls) {
                ballToDataOutput(out, ball);
            }
            out.writeInt((Integer) collision.getxWindowMax());
            out.writeInt((Integer) collision.getyWindowMax());
        } else if (methodName.equals("updateDynamic")) {
            List<Ball> balls = (List<Ball>) params[0];
            int size = balls.size();
            out.writeInt(size);

            for (Ball ball : balls) {
                ballToDataOutput(out, ball);
            }
            out.writeInt((Integer) collision.getxWindowMax());
            out.writeInt((Integer) collision.getyWindowMax());
        }
    }

    public Object[] readMethodParams(DataInput in, String methodName) throws IOException {
        if (methodName.equals("updateStatic")) {
            Object params[] = new Object[1];

            int size = in.readInt();
            List<Ball> balls = new ArrayList<Ball>(size);
            for (int i = 0; i < size; i++) {
                balls.add(dataInputToBall(in));
            }
            params[0] = balls;
            collision.setWindowDimensions(in.readInt(), in.readInt());

            return params;
        } else if (methodName.equals("updateDynamic")) {
            Object params[] = new Object[1];

            int size = in.readInt();
            List<Ball> balls = new ArrayList<Ball>(size);
            for (int i = 0; i < size; i++) {
                balls.add(dataInputToBall(in));
            }
            params[0] = balls;
            collision.setWindowDimensions(in.readInt(), in.readInt());
            // params[1] = in.readInt();
            // params[2] = in.readInt();

            return params;
        }
        return null;
    }

    public void writeMethodReturn(DataOutput out, String methodName, Object returnParam) throws IOException {
        if (methodName.equals("updateStatic")) {
            @SuppressWarnings("unchecked")
            List<Ball> balls = (List<Ball>) returnParam;
            out.writeInt(balls.size());

            for (Ball ball : balls) {
                ballToDataOutput(out, ball);
            }
        } else if (methodName.equals("updateDynamic")) {
            @SuppressWarnings("unchecked")
            List<Ball> balls = (List<Ball>) returnParam;
            out.writeInt(balls.size());

            for (Ball ball : balls) {
                ballToDataOutput(out, ball);
            }
        }
    }

    public Object readMethodReturn(DataInput in, String methodName) throws IOException {
        if (methodName.equals("updateStatic")) {
            int size = in.readInt();
            List<Ball> balls = new ArrayList<Ball>(size);
            for (int i = 0; i < size; i++) {
                balls.add(dataInputToBall(in));
            }
            return balls;
        } else if (methodName.equals("updateDynamic")) {
            int size = in.readInt();
            List<Ball> balls = new ArrayList<Ball>(size);
            for (int i = 0; i < size; i++) {
                balls.add(dataInputToBall(in));
            }
            return balls;
        }
        return null;
    }

    private void ballToDataOutput(DataOutput out, Ball ball) throws IOException {
        out.writeInt(ball.getDamageColorTime());
        out.writeInt(ball.getColor());
        out.writeFloat(ball.getBallRadius());
        out.writeFloat(ball.getBallX());
        out.writeFloat(ball.getBallY());
        out.writeFloat(ball.getBallSpeedX());
        out.writeFloat(ball.getBallSpeedY());
        out.writeFloat(ball.getLeft());
        out.writeFloat(ball.getTop());
        out.writeFloat(ball.getRight());
        out.writeFloat(ball.getBottom());
    }

    private Ball dataInputToBall(DataInput in) throws IOException {
        Ball ball = new Ball();

        ball.setDamageColorTime(in.readInt());
        ball.setColor(in.readInt());
        ball.setBallRadius(in.readFloat());
        ball.setBallX(in.readFloat());
        ball.setBallY(in.readFloat());
        ball.setBallSpeedX(in.readFloat());
        ball.setBallSpeedY(in.readFloat());
        ball.setLeft(in.readFloat());
        ball.setTop(in.readFloat());
        ball.setRight(in.readFloat());
        ball.setBottom(in.readFloat());

        return ball;
    }

    @Override
    public void setWindowDimensions(int width, int height) {
        collision.setWindowDimensions(width, height);
    }
}