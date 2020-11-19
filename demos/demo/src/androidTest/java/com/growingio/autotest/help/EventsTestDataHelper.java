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

package com.growingio.autotest.help;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.test.core.app.ApplicationProvider;

public enum EventsTestDataHelper {
    INSTANCE;

    public static final String SHARED_PREFERENCES_NAME = "events_test_data_sp";

    private static final String KEY_DEVICE_ID = "DEVICE_ID";
    private static final String KEY_USER_ID = "USER_ID";

    private final SharedPreferences mPreferences;

    EventsTestDataHelper() {
        mPreferences = ApplicationProvider.getApplicationContext().getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
    }

    public String getDeviceId() {
        return mPreferences.getString(KEY_DEVICE_ID, null);
    }

    public void saveDeviceId(String deviceId) {
        mPreferences.edit().putString(KEY_DEVICE_ID, deviceId).apply();
    }

    public String getUserId() {
        return mPreferences.getString(KEY_USER_ID, null);
    }

    public void saveUserId(String deviceId) {
        mPreferences.edit().putString(KEY_USER_ID, deviceId).apply();
    }

}
