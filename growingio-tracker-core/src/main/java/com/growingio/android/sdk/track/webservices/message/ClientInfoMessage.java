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

package com.growingio.android.sdk.track.webservices.message;

import com.growingio.android.sdk.track.SDKConfig;
import com.growingio.android.sdk.track.providers.AppInfoProvider;
import com.growingio.android.sdk.track.providers.DeviceInfoProvider;
import com.growingio.android.sdk.track.utils.ConstantPool;

import org.json.JSONException;
import org.json.JSONObject;

public class ClientInfoMessage {
    public static final String MSG_TYPE = "client_info";

    private final String mMsgType;
    private final String mSdkVersion;
    private final JSONObject mData = new JSONObject();

    public ClientInfoMessage(String sdkVersion, String os, String appVersion, String appChannel, String osVersion, String deviceType, String deviceBrand, String deviceModel) {
        mMsgType = MSG_TYPE;
        mSdkVersion = sdkVersion;

        try {
            mData.put("os", os);
            mData.put("appVersion", appVersion);
            mData.put("appChannel", appChannel);
            mData.put("osVersion", osVersion);
            mData.put("deviceType", deviceType);
            mData.put("deviceBrand", deviceBrand);
            mData.put("deviceModel", deviceModel);
        } catch (JSONException ignored) {
        }
    }

    public static ClientInfoMessage createMessage() {
        DeviceInfoProvider deviceInfo = DeviceInfoProvider.get();
        AppInfoProvider appInfo = AppInfoProvider.get();
        return new ClientInfoMessage(SDKConfig.SDK_VERSION, ConstantPool.ANDROID, appInfo.getAppVersion(), appInfo.getAppChannel(),
                deviceInfo.getOperatingSystemVersion(), deviceInfo.getDeviceType(), deviceInfo.getDeviceBrand(), deviceInfo.getDeviceModel());
    }

    public JSONObject toJSONObject() {
        JSONObject json = new JSONObject();
        try {
            json.put("msgType", mMsgType);
            json.put("sdkVersion", mSdkVersion);
            json.put("data", mData);
        } catch (JSONException ignored) {
        }
        return json;
    }
}
