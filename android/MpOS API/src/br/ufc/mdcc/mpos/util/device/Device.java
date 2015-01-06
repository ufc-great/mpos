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

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Model about device data.
 * 
 * @author Philipp B. Costa
 */
public final class Device {
	private String mobileId;
	private String deviceName;
	private String carrier;
	private String longitude;
	private String latitude;

	public Device(){
		this.longitude = "0.0";
		this.latitude = "0.0";
	}
	
	public final String getMobileId() {
		return mobileId;
	}

	public final void setMobileId(String mobileId) {
		this.mobileId = mobileId;
	}

	public final String getDeviceName() {
		return deviceName;
	}

	public final void setDeviceName(String deviceName) {
		this.deviceName = deviceName;
	}

	public void setCarrier(String carrier) {
		this.carrier = carrier;
	}

	public String getCarrier() {
		return carrier;
	}

	public final String getLongitude() {
		return longitude;
	}

	public final void setLongitude(String longitude) {
		this.longitude = longitude;
	}

	public final String getLatitude() {
		return latitude;
	}

	public final void setLatitude(String latitude) {
		this.latitude = latitude;
	}

	public JSONObject toJSON() throws JSONException {
		JSONObject object = new JSONObject();

		object.put("mobileId", mobileId);
		object.put("deviceName", deviceName);
		object.put("carrier", carrier);
		object.put("lat", latitude);
		object.put("lon", longitude);

		return object;
	}

	public String toString() {
		try {
			return toJSON().toString();
		} catch (JSONException e) {
			return "Object with problems in json compose!";
		}
	}
}