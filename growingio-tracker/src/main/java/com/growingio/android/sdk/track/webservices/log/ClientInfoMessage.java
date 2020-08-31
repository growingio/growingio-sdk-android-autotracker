/*
 * Copyright (C) 2020 Beijing Yishu Technology Co., Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package com.growingio.android.sdk.track.webservices.log;

import com.growingio.android.sdk.track.SDKConfig;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class ClientInfoMessage {

    public static final String MSG_TYPE = "client_info";

    private final String mMsgType;
    private final String mSdkVersion;
    private HashMap<String, String> mDevice;
    private final String mOs = "os";
    private final String mAppVersion = "appVersion";
    private final String mAppChannel = "appChannel";
    private final String mOsVersion = "osVersion";
    private final String mDeviceType = "deviceType";
    private final String mDeviceBrand = "deviceBrand";
    private final String mDeviceModel = "deviceModel";

    public ClientInfoMessage(String sdkVersion, HashMap<String, String> device) {
        mMsgType = MSG_TYPE;
        mSdkVersion = sdkVersion;
        mDevice = device;
    }

    public static ClientInfoMessage createMessage() {

        String sdkVersion = SDKConfig.SDK_VERSION;
        String os = "Android";
        String appVersion = "";
        String appChannel = "";
        String osVersion = "";
        String deviceType = "Android";
        String deviceBrand = "";
        String deviceModel = "";
        HashMap<String, String> device = new HashMap<>();
        device.put("os", os);
        device.put("appVersion", appVersion);
        device.put("appChannel", appChannel);
        device.put("osVersion", osVersion);
        device.put("deviceType", deviceType);
        device.put("deviceBrand", deviceBrand);
        device.put("deviceModel", deviceModel);
        return new ClientInfoMessage(sdkVersion, device);

    }

    public JSONObject toJSONObject() {
        JSONObject json = new JSONObject();
        try {
            json.put("msgType", mMsgType);
            json.put("sdkVersion", mSdkVersion);
            json.put("device", mDevice);
        } catch (JSONException ignored) {
        }
        return json;
    }

}
