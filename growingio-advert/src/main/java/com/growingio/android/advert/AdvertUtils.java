/*
 *   Copyright (C) 2020 Beijing Yishu Technology Co., Ltd.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package com.growingio.android.advert;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.text.TextUtils;
import android.webkit.WebSettings;
import android.webkit.WebView;

import com.growingio.android.sdk.CoreConfiguration;
import com.growingio.android.sdk.TrackerContext;
import com.growingio.android.sdk.track.log.Logger;
import com.growingio.android.sdk.track.providers.ConfigurationProvider;
import com.growingio.android.sdk.track.utils.PermissionUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.URLDecoder;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;

/**
 * <p>
 *
 * @author cpacm 2022/8/3
 */
class AdvertUtils {
    private static final String PREF_FILE_NAME = "growing_profile";
    private static final String PREF_DEVICE_ACTIVATED = "pref_device_activated";
    private static String sUserAgent = null;
    private static final String TAG = "Advert";

    private AdvertUtils() {
    }

    public static String getUserAgent(Context context) {
        if (context == null || !TextUtils.isEmpty(sUserAgent)) return sUserAgent;
        sUserAgent = System.getProperty("http.agent");
        if (TextUtils.isEmpty(sUserAgent)
                && PermissionUtil.hasInternetPermission()) {
            try {
                sUserAgent = new WebView(context).getSettings().getUserAgentString();
            } catch (Exception e) {
                Logger.e(TAG, e.getMessage());
                try {
                    sUserAgent = WebSettings.getDefaultUserAgent(context);
                } catch (Exception badException) {
                    Logger.e(TAG, badException.getMessage());
                }
            }
        }
        return sUserAgent;
    }

    public static boolean isDeviceActivated() {
        return getSharedPreferences().getBoolean(PREF_DEVICE_ACTIVATED, false);
    }

    public static void setDeviceActivated() {
        getSharedPreferences().edit().putBoolean(PREF_DEVICE_ACTIVATED, true).apply();
    }

    @SuppressLint("WrongConstant")
    public static SharedPreferences getSharedPreferences() {
        return TrackerContext.get().getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE);
    }
}
