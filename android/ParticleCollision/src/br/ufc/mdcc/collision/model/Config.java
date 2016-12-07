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

import android.content.Context;
import android.content.SharedPreferences;
import br.ufc.mdcc.collision.R;

/**
 * @author Philipp
 */
public final class Config {
    private int quantity;
    private float radiusBall;
    private boolean placeStart;
    private boolean serialNative;
    private String executitonType;

    public Config(Context context, SharedPreferences sharedPreferences) {
        String ballConfig[] = sharedPreferences.getString(context.getString(R.string.pref_ball_quantity_key), context.getString(R.string.pref_ball_quantity_default_value)).split(":");
        quantity = Integer.parseInt(ballConfig[0]);
        radiusBall = Float.parseFloat(ballConfig[1]);

        serialNative = sharedPreferences.getBoolean(context.getString(R.string.pref_mpos_serial_key), true);
        placeStart = sharedPreferences.getBoolean(context.getString(R.string.pref_start_place_key), true);
        executitonType = sharedPreferences.getString(context.getString(R.string.pref_mpos_key), context.getString(R.string.pref_mpos_default_value));
    }

    public boolean isSerialNative() {
        return serialNative;
    }

    public int getQuantity() {
        return quantity;
    }

    public float getRadiusBall() {
        return radiusBall;
    }

    public boolean isPlaceStart() {
        return placeStart;
    }

    public String getExecutitonType() {
        return executitonType;
    }

    @Override
    public String toString() {
        return "Config [quantity=" + quantity + ", radiusBall=" + radiusBall + ", placeStart=" + placeStart + ", serialNative=" + serialNative + ", executitonType=" + executitonType + "]";
    }
}