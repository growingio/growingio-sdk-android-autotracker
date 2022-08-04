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
    private static final String PREF_DEVICE_ACTIVATE_INFO = "pref_device_activate_info";
    private static String sUserAgent = null;
    private static String IP = null;
    private static String TAG = "Advert";

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

    private static synchronized void initIp() {
        if (PermissionUtil.hasInternetPermission()) {
            try {
                for (Enumeration<NetworkInterface> enNetI = NetworkInterface.getNetworkInterfaces(); enNetI.hasMoreElements(); ) {
                    NetworkInterface netI = enNetI.nextElement();
                    for (Enumeration<InetAddress> enumIpAddr = netI.getInetAddresses();
                         enumIpAddr.hasMoreElements();
                    ) {
                        InetAddress inetAddress = enumIpAddr.nextElement();
                        if (inetAddress instanceof Inet4Address && !inetAddress.isLoopbackAddress()) {
                            IP = inetAddress.getHostAddress() != null ? inetAddress.getHostAddress() : "";
                        }
                    }
                }
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
    }

    public static String getIP() {
        if (IP == null) {
            initIp();
        }
        return IP;
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

    public static void setActivateInfo(String activateInfo) {
        getSharedPreferences().edit().putString(
                PREF_DEVICE_ACTIVATE_INFO,
                activateInfo
        ).apply();
    }

    public static String getActivateInfo() {
        return getSharedPreferences().getString(PREF_DEVICE_ACTIVATE_INFO, "");
    }

    public static String getAppLinkParamsUrl(String trackId, boolean isInApp) {
        String ai = ConfigurationProvider.core().getProjectId();
        String spn = TrackerContext.get().getPackageName();
        String cl = isInApp ? "inapp" : "defer";
        return "https://" +
                "t.growingio.com" +
//                "testlink.growingio.com" +
                "/app/at6/" + cl + "/android/" +
                ai + "/" + spn + "/" + trackId;
    }

    public static String decode(String original) {
        if (original != null) {
            try {
                return URLDecoder.decode(original, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                Logger.d(TAG, e);
            }
        }
        return "";
    }

    public static int parseJson(String json, Map<String, String> map) {
        if (TextUtils.isEmpty(json)) {
            map.clear();
            return AdvertReceiveCallback.NO_QUERY;
        }
        try {
            JSONObject jsonObject = new JSONObject(json);
            Iterator<String> keys = jsonObject.keys();
            while (keys.hasNext()) {
                String key = keys.next();
                if ("_gio_var".equals(key))
                    continue;
                map.put(key, jsonObject.getString(key));
            }
        } catch (JSONException jsonException) {
            map.clear();
            return AdvertReceiveCallback.PARSE_ERROR;
        }
        return AdvertReceiveCallback.SUCCESS;
    }

    static String parseTrackerId(String url) {
        String schemePart;
        if (url.startsWith("https://")) {
            schemePart = "https://";
        } else {
            schemePart = "http://";
        }
        return url.substring(url.indexOf("/", schemePart.length()) + 1);
    }
}
