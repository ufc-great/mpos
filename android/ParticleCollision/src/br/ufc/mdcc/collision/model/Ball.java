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
package br.ufc.mdcc.collision.model;

import java.io.Serializable;
import java.util.Random;

/**
 * @author Philipp
 */
public final class Ball implements Serializable {
    private static final long serialVersionUID = 3218555378686239985L;

    public static final int YELLOW = 0xFFFFFF00;
    public static final int RED = 0xffff0000;

    private int damageColorTime = 0;// time in fps
    private int color;
    private float ballRadius;

    // Ball's center (x,y)
    private float ballX;
    private float ballY;

    // Ball's speed (x,y)
    private float ballSpeedX;
    private float ballSpeedY;

    private float left;
    private float top;
    private float right;
    private float bottom;

    public Ball() {}

    private Ball(int color, float ballRadius) {
        this.color = color;
        this.ballRadius = ballRadius;
    }

    public Ball(Random random, boolean fixLocation) {
        this(3.5f, random, fixLocation, 240, 320);
    }

    public Ball(float ballRadius, Random random, boolean fixLocation, int width, int height) {
        this(RED, ballRadius);

        if (fixLocation) {
            this.ballX = 100.0f;
            this.ballY = 100.0f;
        } else {
            this.ballX = 15.0f + random.nextFloat() * width;
            this.ballY = 10.0f + random.nextFloat() * height;
        }

        // 35% reverse start
        if (random.nextFloat() > 0.35f) {
            this.ballSpeedX = 3.0f + random.nextFloat() * 15.0f;
            this.ballSpeedY = 3.0f + random.nextFloat() * 15.0f;
        } else {
            this.ballSpeedX = -(3.0f + random.nextFloat() * 15.0f);
            this.ballSpeedY = -(3.0f + random.nextFloat() * 15.0f);
        }

        generateBounds();
    }

    public Ball(float ballRadius, float ballX, float ballY, float ballSpeedX, float ballSpeedY) {
        this(RED, ballRadius);
        this.ballX = ballX;
        this.ballY = ballY;
        this.ballSpeedX = ballSpeedX;
        this.ballSpeedY = ballSpeedY;

        generateBounds();
    }

    public int getColor() {
        return color;
    }

    public float getBallRadius() {
        return ballRadius;
    }

    public float getBallX() {
        return ballX;
    }

    public void setBallX(float ballX) {
        this.ballX = ballX;
    }

    public float getBallY() {
        return ballY;
    }

    public void setBallY(float ballY) {
        this.ballY = ballY;
    }

    public float getBallSpeedX() {
        return ballSpeedX;
    }

    public void setBallSpeedX(float ballSpeedX) {
        this.ballSpeedX = ballSpeedX;
    }

    public float getBallSpeedY() {
        return ballSpeedY;
    }

    public void setBallSpeedY(float ballSpeedY) {
        this.ballSpeedY = ballSpeedY;
    }

    public float getTop() {
        return top;
    }

    public float getBottom() {
        return bottom;
    }

    public float getLeft() {
        return left;
    }

    public float getRight() {
        return right;
    }

    public int getDamageColorTime() {
        return damageColorTime;
    }

    public void setDamageColorTime(int damageColorTime) {
        this.damageColorTime = damageColorTime;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public void setBallRadius(float ballRadius) {
        this.ballRadius = ballRadius;
    }

    public void setLeft(float left) {
        this.left = left;
    }

    public void setTop(float top) {
        this.top = top;
    }

    public void setRight(float right) {
        this.right = right;
    }

    public void setBottom(float bottom) {
        this.bottom = bottom;
    }

    public void setNextPosition() {
        ballY += ballSpeedY;
        ballX += ballSpeedX;
    }

    public void generateBounds() {
        left = ballX - ballRadius;
        right = ballX + ballRadius;
        top = ballY - ballRadius;
        bottom = ballY + ballRadius;
    }

    public void getDamage() {
        if (color == RED) {
            color = YELLOW;
            this.damageColorTime = 45;// next 45fps
        }
    }

    public void recoveryTime() {
        if (damageColorTime > 0) {
            damageColorTime--;
        } else {
            color = RED;
        }
    }

    @Override
    public String toString() {
        return "Ball [ballRadius=" + ballRadius + ", ballX=" + ballX + ", ballY=" + ballY + ", ballSpeedX=" + ballSpeedX + ", ballSpeedY=" + ballSpeedY + ", left=" + left + ", top=" + top + ", right=" + right + ", bottom=" + bottom + "]";
    }
}