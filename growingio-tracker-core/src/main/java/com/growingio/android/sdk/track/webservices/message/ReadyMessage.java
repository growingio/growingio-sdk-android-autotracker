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

import android.content.Context;
import android.util.DisplayMetrics;

import com.growingio.android.sdk.track.ContextProvider;
import com.growingio.android.sdk.track.SDKConfig;
import com.growingio.android.sdk.track.providers.AppInfoProvider;
import com.growingio.android.sdk.track.providers.ConfigurationProvider;
import com.growingio.android.sdk.track.utils.DeviceUtil;

import org.json.JSONException;
import org.json.JSONObject;

public class ReadyMessage {
    public static final String MSG_TYPE = "ready";

    private final String mProjectId;
    private final String mMsgType;
    private final long mTimestamp;
    private final String mDomain;
    private final String mSdkVersion;
    private final int mSdkVersionCode;
    private final String mOs;
    private final int mScreenWidth;
    private final int mScreenHeight;


    private ReadyMessage(String projectId, String domain, String sdkVersion, int sdkVersionCode, int screenWidth, int screenHeight) {
        mMsgType = MSG_TYPE;
        mOs = "Android";
        mTimestamp = System.currentTimeMillis();
        mProjectId = projectId;
        mDomain = domain;
        mSdkVersion = sdkVersion;
        mSdkVersionCode = sdkVersionCode;
        mScreenWidth = screenWidth;
        mScreenHeight = screenHeight;
    }

    public static ReadyMessage createMessage() {
        Context context = ContextProvider.getApplicationContext();
        String projectId = ConfigurationProvider.get().getTrackConfiguration().getProjectId();
        String domain = AppInfoProvider.get().getPackageName();
        String sdkVersion = SDKConfig.SDK_VERSION;
        int sdkVersionCode = SDKConfig.SDK_VERSION_CODE;

        DisplayMetrics metrics = DeviceUtil.getDisplayMetrics(context);
        return new ReadyMessage(projectId, domain, sdkVersion, sdkVersionCode, metrics.widthPixels, metrics.heightPixels);
    }

    public JSONObject toJSONObject() {
        JSONObject json = new JSONObject();
        try {
            json.put("projectId", mProjectId);
            json.put("msgType", mMsgType);
            json.put("timestamp", mTimestamp);
            json.put("domain", mDomain);
            json.put("sdkVersion", mSdkVersion);
            json.put("sdkVersionCode", mSdkVersionCode);
            json.put("os", mOs);
            json.put("screenWidth", mScreenWidth);
            json.put("screenHeight", mScreenHeight);
        } catch (JSONException ignored) {
        }
        return json;
    }
}
