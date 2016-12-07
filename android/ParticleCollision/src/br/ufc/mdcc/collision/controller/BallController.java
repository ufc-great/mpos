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

import java.util.List;

import br.ufc.mdcc.collision.model.Ball;
import br.ufc.mdcc.collision.util.CollisionBall;

/**
 * Controller for update ball state in animation. That class have support offloading in you interface and uses java built-in serialization for param
 * and return values.
 * 
 * @author Philipp
 */
public final class BallController implements BallUpdater<Ball> {
    private CollisionBall collision;

    public BallController() {
        collision = new CollisionBall();
    }

    @Override
    public List<Ball> updateOffline(List<Ball> balls, int width, int height) {
        collision.setWindowDimensions(width, height);
        return updateState(balls);
    }

    @Override
    public List<Ball> updateStatic(List<Ball> balls, int width, int height) {
        collision.setWindowDimensions(width, height);
        return updateState(balls);
    }

    @Override
    public List<Ball> updateDynamic(List<Ball> balls, int width, int height) {
        collision.setWindowDimensions(width, height);
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
}