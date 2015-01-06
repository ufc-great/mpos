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
package br.ufc.mdcc.mpos.util;

import android.content.Context;
import android.location.LocationListener;
import android.location.LocationManager;

/**
 * This a class which use the Android location services, like A-GPS and etc.
 * 
 * @author Philipp B. Costa
 */
public final class LocationTracker {
	private LocationManager locationManager;
	private LocationListener locationListener;

	public LocationTracker(Context context) {
		locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
	}

	public void setLocationListener(LocationListener locationListener) {
		try {
			locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 500, 0, locationListener);
		} catch (IllegalArgumentException e) {
			locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 500, 0, locationListener);
		}
		this.locationListener = locationListener;
	}

	public void removeLocationListener() {
		locationManager.removeUpdates(locationListener);
	}
}