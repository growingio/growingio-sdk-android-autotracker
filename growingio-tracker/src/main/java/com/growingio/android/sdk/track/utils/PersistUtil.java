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

package com.growingio.android.sdk.track.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class PersistUtil {
    private static final String DEVICE_ID_KEY = "device_id";
    private static final String HOST_INFORMATION_KEY = "host_info";
    private static final String PERSIST_FILE_NAME = "growing_persist_data";
    private static final String LAST_VERSION = "host_app_last_version";

    private static SharedPreferences sSharedPreferences;

    private PersistUtil() {
    }

    public static void init(Context context) {
        if (sSharedPreferences == null) {
            sSharedPreferences = context.getSharedPreferences(PERSIST_FILE_NAME, Context.MODE_PRIVATE);
        }
    }

    public static String fetchDeviceId() {
        return sSharedPreferences.getString(DEVICE_ID_KEY, null);
    }

    public static void saveDeviceId(String deviceId) {
        sSharedPreferences.edit().putString(DEVICE_ID_KEY, deviceId).commit();
    }

    /**
     * 获取存储在本地的HttpDNS后的数据
     */
    public static String fetchHostInformationData() {
        return sSharedPreferences.getString(HOST_INFORMATION_KEY, null);
    }

    /**
     * 存储HttpDNS后的数据
     */
    public static void saveHostInformationData(String hostInformationData) {
        sSharedPreferences.edit().putString(HOST_INFORMATION_KEY, hostInformationData).commit();
    }

    public static int fetchLastAppVersion() {
        return sSharedPreferences.getInt(LAST_VERSION, 0);
    }

    public static void saveLastAppVersion(int versionCode) {
        sSharedPreferences.edit().putInt(LAST_VERSION, versionCode).commit();
    }
}
