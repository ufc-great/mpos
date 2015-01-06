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
package br.ufc.mdcc.mpos.util.device;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.DetailedState;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.util.Log;
import br.ufc.mdcc.mpos.MposFramework;
import br.ufc.mdcc.mpos.persistence.MobileDao;
import br.ufc.mdcc.mpos.util.LocationListenerAdapter;
import br.ufc.mdcc.mpos.util.LocationTracker;

/**
 * This controller get all informations about mobile
 * 
 * @author Philipp B. Costa
 */
public final class DeviceController {
	private final String clsName = MposFramework.class.getName();

	private Context context;
	private Device device;
	private LocationTracker tracker;

	private String appName;
	private String appVersion;

	public DeviceController(Activity activity) throws NameNotFoundException {
	    this.context = activity.getApplicationContext();
        appStatus(activity);
	}

	public String getAppName() {
		return appName;
	}

	public Device getDevice() {
		return device;
	}

	public String getAppVersion() {
		return appVersion;
	}

	public boolean isOnline() {
		return connectionStatus(ConnectivityManager.TYPE_WIFI) || connectionStatus(ConnectivityManager.TYPE_MOBILE);
	}

	// Fine-grained state
	public boolean connectionStatus(int type) {
		ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		return connectivityManager.getNetworkInfo(type).getDetailedState() == DetailedState.CONNECTED;
	}

	public String getNetworkConnectedType() {
		NetworkInfo info = ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();

		if (info != null) {
			int type = info.getType();
			int subtype = info.getSubtype();

			if (type == ConnectivityManager.TYPE_WIFI) {
				return "WiFi";
			} else if (type == ConnectivityManager.TYPE_MOBILE) {
				switch (subtype) {
					case TelephonyManager.NETWORK_TYPE_CDMA:
						return "CDMA";
					case TelephonyManager.NETWORK_TYPE_IDEN:
						return "iDen";
					case TelephonyManager.NETWORK_TYPE_EHRPD:
						return "eHRPD";
					case TelephonyManager.NETWORK_TYPE_EDGE:
						return "EDGE";
					case TelephonyManager.NETWORK_TYPE_1xRTT:
						return "1xRTT";
					case TelephonyManager.NETWORK_TYPE_GPRS:
						return "GPRS";
					case TelephonyManager.NETWORK_TYPE_UMTS:
						return "UMTS";
					case TelephonyManager.NETWORK_TYPE_EVDO_0:
					case TelephonyManager.NETWORK_TYPE_EVDO_A:
					case TelephonyManager.NETWORK_TYPE_EVDO_B:
						return "EVDO";
					case TelephonyManager.NETWORK_TYPE_HSPA:
						return "HSPA";
					case TelephonyManager.NETWORK_TYPE_HSDPA:
						return "HSDPA";
					case TelephonyManager.NETWORK_TYPE_HSPAP:
						return "HSPA+";
					case TelephonyManager.NETWORK_TYPE_HSUPA:
						return "HSUPA";
					case TelephonyManager.NETWORK_TYPE_LTE:
						return "LTE";
					default:
						return "Unknown";
				}
			}
		}
		return "Offline";
	}

	public void collectDeviceConfig() {
		device = new Device();
		device.setMobileId(new MobileDao(context).checkMobileId());
		device.setCarrier(((TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE)).getNetworkOperatorName());
		device.setDeviceName(Build.MANUFACTURER + " " + Build.MODEL);
	}

	public void collectLocation() {
		tracker = new LocationTracker(context);
		tracker.setLocationListener(new LocationListenerAdapter() {
			public void onLocationChanged(Location location) {
				// 200m
				if (location.getAccuracy() < 200.0f) {
					if (device != null) {
						device.setLatitude(Double.toString(location.getLatitude()));
						device.setLongitude(Double.toString(location.getLongitude()));
					}

					// collect completed
					tracker.removeLocationListener();

					Log.d(clsName, "Location collect completed");
					Log.d(clsName, "lat: " + location.getLatitude());
					Log.d(clsName, "lon: " + location.getLongitude());
				}
			}
		});
	}

	public void removeLocationListener() {
		if (tracker != null) {
			tracker.removeLocationListener();
		}
	}

	public void destroy() {
		removeLocationListener();
		tracker = null;
		device = null;
		appName = null;
	}
	
	private void appStatus(Activity activity) throws NameNotFoundException{
	    appName = activity.getString(activity.getApplicationInfo().labelRes).replace(' ', '_');
        appVersion = activity.getPackageManager().getPackageInfo(activity.getPackageName(), 0).versionName;
	}
}