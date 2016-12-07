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

import java.io.Serializable;
import java.util.List;

import br.ufc.mdcc.collision.model.Ball;

/**
 * @author Philipp
 */
public class CollisionBall implements Serializable {
    private static final long serialVersionUID = -2208145317740630174L;

    private int xWindowMax;
    private int yWindowMax;

    public void collisionWindow(Ball ball) {
        float ballRadius = ball.getBallRadius();

        // left or right window borders
        if (ball.getBallX() + ballRadius > xWindowMax) {
            ball.setBallSpeedX(-ball.getBallSpeedX());
            ball.setBallX(xWindowMax - ballRadius);
        } else if (ball.getBallX() - ballRadius < 0) {
            ball.setBallSpeedX(-ball.getBallSpeedX());
            ball.setBallX(ballRadius);
        }

        // top and bottom window borders
        if (ball.getBallY() + ballRadius > yWindowMax) {
            ball.setBallSpeedY(-ball.getBallSpeedY());
            ball.setBallY(yWindowMax - ballRadius);
        } else if (ball.getBallY() - ballRadius < 0) {
            ball.setBallSpeedY(-ball.getBallSpeedY());
            ball.setBallY(ballRadius);
        }
    }

    public void detectCollisionBall(Ball current, List<Ball> balls) {
        for (Ball other : balls) {
            if (current != other && intersects(current, other)) {
                changeDirection(current, other);
                current.getDamage();
                other.getDamage();
            }
        }
        current.recoveryTime();
    }

    private void changeDirection(Ball current, Ball other) {
        if (current.getColor() == Ball.RED || other.getColor() == Ball.RED) {
            if ((current.getBallSpeedX() > 0 && other.getBallSpeedX() < 0) || (current.getBallSpeedX() < 0 && other.getBallSpeedX() > 0)) {
                current.setBallSpeedX(-current.getBallSpeedX() * 0.95f);
                other.setBallSpeedX(-other.getBallSpeedX() * 0.95f);
            } else {
                if (current.getBallSpeedX() > 0 && other.getBallSpeedX() > 0 && current.getBallSpeedX() > other.getBallSpeedX()) {
                    // gain 5%
                    other.setBallSpeedX(current.getBallSpeedX() * 1.05f);
                    // loss 3%
                    current.setBallSpeedX(other.getBallSpeedX() * 0.97f);
                } else if (current.getBallSpeedX() < 0 && other.getBallSpeedX() < 0 && current.getBallSpeedX() < other.getBallSpeedX()) {
                    current.setBallSpeedX(other.getBallSpeedX() * 1.05f);
                    other.setBallSpeedX(current.getBallSpeedX() * 0.97f);
                }
            }

            if ((current.getBallSpeedY() > 0 && other.getBallSpeedY() < 0) || (current.getBallSpeedY() < 0 && other.getBallSpeedY() > 0)) {
                // loss 5%
                current.setBallSpeedY(-current.getBallSpeedY() * 0.95f);
                other.setBallSpeedY(-other.getBallSpeedY() * 0.95f);
            } else {
                if (current.getBallSpeedY() > 0 && other.getBallSpeedY() > 0 && current.getBallSpeedY() > other.getBallSpeedY()) {
                    other.setBallSpeedY(current.getBallSpeedY() * 1.05f);
                    current.setBallSpeedY(other.getBallSpeedY() * 0.97f);
                } else if (current.getBallSpeedY() < 0 && other.getBallSpeedY() < 0 && current.getBallSpeedY() < other.getBallSpeedY()) {
                    current.setBallSpeedY(other.getBallSpeedY() * 1.05f);
                    other.setBallSpeedY(current.getBallSpeedY() * 0.97f);
                }
            }
        }
    }

    private boolean intersects(Ball current, Ball other) {
        return current.getLeft() < other.getRight() && other.getLeft() < current.getRight() && current.getTop() < other.getBottom() && other.getTop() < current.getBottom();
    }

    public void setWindowDimensions(int xWindowMax, int yWindowMax) {
        this.xWindowMax = xWindowMax;
        this.yWindowMax = yWindowMax;
    }

    public int getxWindowMax() {
        return xWindowMax;
    }

    public int getyWindowMax() {
        return yWindowMax;
    }
}