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

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.support.v4.app.NavUtils;
import android.view.MenuItem;
import br.ufc.mdcc.collision.model.Config;

/**
 * @author Philipp
 */

public final class SettingsActivity extends PreferenceActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getFragmentManager().beginTransaction().replace(android.R.id.content, new SettingsFragment()).commit();
        getActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        NavUtils.navigateUpFromSameTask(this);
    }

    public static final class SettingsFragment extends PreferenceFragment {
        private SharedPreferences.OnSharedPreferenceChangeListener onSharedPreferenceChangeListener;

        private ListPreference ballQuantity;
        private ListPreference mposConfig;
        private CheckBoxPreference startPlace;
        private CheckBoxPreference mposSerial;

        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.preference);

            ballQuantity = (ListPreference) getPreferenceScreen().findPreference(getString(R.string.pref_ball_quantity_key));
            mposConfig = (ListPreference) getPreferenceScreen().findPreference(getString(R.string.pref_mpos_key));
            startPlace = (CheckBoxPreference) getPreferenceScreen().findPreference(getString(R.string.pref_start_place_key));
            mposSerial = (CheckBoxPreference) getPreferenceScreen().findPreference(getString(R.string.pref_mpos_serial_key));

            SharedPreferences sharedPreferences = getPreferenceScreen().getSharedPreferences();
            Config config = new Config(getActivity(), sharedPreferences);

            ballQuantity.setSummary(config.getQuantity() + " Bolas");
            String mposConf = config.getExecutitonType();
            if (mposConf.equals("local")) {
                mposConfig.setSummary("Local");
            } else if (mposConf.equals("static")) {
                mposConfig.setSummary("Static Offload");
            } else if (mposConf.equals("dynamic")) {
                mposConfig.setSummary("Dynamic Offload");
            }
            if (config.isPlaceStart()) {
                startPlace.setSummaryOn(getString(R.string.pref_start_place_on));
            } else {
                startPlace.setSummaryOff(getString(R.string.pref_start_place_off));
            }
            if (config.isSerialNative()) {
                mposSerial.setSummaryOn(getString(R.string.pref_mpos_serial_on));
            } else {
                mposSerial.setSummaryOff(getString(R.string.pref_mpos_serial_off));
            }
            configureOnSharedPreferenceChangeListener(sharedPreferences);
        }

        private void configureOnSharedPreferenceChangeListener(SharedPreferences sharedPreferences) {
            onSharedPreferenceChangeListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
                @Override
                public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

                    if (key.equals(getString(R.string.pref_ball_quantity_key))) {
                        String ballConfig[] = sharedPreferences.getString(getString(R.string.pref_ball_quantity_key), getString(R.string.pref_ball_quantity_default_value)).split(":");
                        ballQuantity.setSummary(ballConfig[0] + " Bolas");
                    } else if (key.equals(getString(R.string.pref_mpos_key))) {
                        String mposConf = sharedPreferences.getString(getString(R.string.pref_mpos_key), getString(R.string.pref_mpos_default_value));
                        if (mposConf.equals("local")) {
                            mposConfig.setSummary("Local");
                        } else if (mposConf.equals("static")) {
                            mposConfig.setSummary("Static Offload");
                        } else if (mposConf.equals("dynamic")) {
                            mposConfig.setSummary("Dynamic Offload");
                        }
                    } else if (key.equals(getString(R.string.pref_start_place_key))) {
                        if (sharedPreferences.getBoolean(getString(R.string.pref_start_place_key), true)) {
                            startPlace.setSummaryOn(getString(R.string.pref_start_place_on));
                        } else {
                            startPlace.setSummaryOff(getString(R.string.pref_start_place_off));
                        }
                    } else if (key.equals(getString(R.string.pref_mpos_serial_key))) {
                        if (sharedPreferences.getBoolean(getString(R.string.pref_mpos_serial_key), true)) {
                            mposSerial.setSummaryOn(getString(R.string.pref_mpos_serial_on));
                        } else {
                            mposSerial.setSummaryOff(getString(R.string.pref_mpos_serial_off));
                        }
                    }
                }
            };
            sharedPreferences.registerOnSharedPreferenceChangeListener(onSharedPreferenceChangeListener);
        }

        public void onDestroy() {
            super.onDestroy();
            if (onSharedPreferenceChangeListener != null) {
                getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(onSharedPreferenceChangeListener);
            }
        }
    }
}