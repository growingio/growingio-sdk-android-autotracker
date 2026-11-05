/*
 * Copyright (C) 2026 Beijing Yishu Technology Co., Ltd.
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
package com.growingio.android.sdk.track.providers;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.growingio.android.sdk.TrackerContext;
import com.growingio.android.sdk.track.log.Logger;
import com.growingio.android.sdk.track.middleware.CdpConfig;
import com.growingio.android.sdk.track.middleware.SaaSConfig;

public class ForceLoginProvider implements TrackerLifecycleProvider {
    private static final String TAG = "ForceLoginProvider";
    private static final String PREFERENCES_NAME = "growing3_force_login";
    private static final String KEY_IDENTIFY_DEVICE_ID = "identify_device_id";
    private static final int MAX_DEVICE_ID_LENGTH = 1000;

    private final Object lock = new Object();
    private ConfigurationProvider configurationProvider;
    private SharedPreferences sharedPreferences;
    private String identifiedDeviceId = "";
    private boolean unlocked = false;

    @Override
    public void setup(TrackerContext context) {
        configurationProvider = context.getConfigurationProvider();
        sharedPreferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
        synchronized (lock) {
            identifiedDeviceId = sharedPreferences.getString(KEY_IDENTIFY_DEVICE_ID, "");
            unlocked = false;
        }
    }

    @Override
    public void shutdown() {
        synchronized (lock) {
            identifiedDeviceId = "";
            unlocked = false;
        }
    }

    public boolean identify(String deviceId) {
        if (!isForceLoginEnabled()) {
            Logger.i(TAG, "Failed to identify, only available when NewSaaS forceLogin is enabled");
            return false;
        }
        if (deviceId == null) {
            Logger.i(TAG, "Failed to identify, deviceId is empty");
            return false;
        }

        String trimmedDeviceId = deviceId.trim();
        if (TextUtils.isEmpty(trimmedDeviceId) || trimmedDeviceId.length() > MAX_DEVICE_ID_LENGTH) {
            Logger.i(TAG, "Failed to identify, deviceId must contain 1 to 1000 non-whitespace characters");
            return false;
        }

        synchronized (lock) {
            if (unlocked) {
                Logger.i(TAG, "Failed to identify, deviceId has already been set in this launch");
                return false;
            }
            identifiedDeviceId = trimmedDeviceId;
            unlocked = true;
            sharedPreferences.edit().putString(KEY_IDENTIFY_DEVICE_ID, trimmedDeviceId).apply();
        }
        Logger.i(TAG, "Identify deviceId for forced login");
        return true;
    }

    public boolean canSend() {
        if (!isForceLoginEnabled()) {
            return true;
        }
        synchronized (lock) {
            return unlocked;
        }
    }

    public String getDeviceId(String generatedDeviceId) {
        if (!isNewSaaS()) {
            return generatedDeviceId;
        }
        synchronized (lock) {
            return TextUtils.isEmpty(identifiedDeviceId) ? generatedDeviceId : identifiedDeviceId;
        }
    }

    public String getOverrideDeviceId() {
        if (!isNewSaaS()) {
            return "";
        }
        synchronized (lock) {
            return identifiedDeviceId;
        }
    }

    private boolean isForceLoginEnabled() {
        return isNewSaaS() && configurationProvider.core().isForceLogin();
    }

    private boolean isNewSaaS() {
        return configurationProvider != null
                && configurationProvider.getConfiguration(CdpConfig.class) == null
                && configurationProvider.getConfiguration(SaaSConfig.class) == null;
    }
}
