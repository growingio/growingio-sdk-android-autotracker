/*
 * Copyright (C) 2023 Beijing Yishu Technology Co., Ltd.
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
package com.growingio.android.ads;

import static android.content.Context.CLIPBOARD_SERVICE;

import static com.growingio.android.sdk.track.utils.ConstantPool.PREF_FILE_NAME;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.text.TextUtils;

import com.growingio.android.sdk.track.TrackMainThread;
import com.growingio.android.sdk.track.log.Logger;
import com.growingio.android.sdk.track.middleware.ads.DeepLinkCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * <p>
 *
 * @author cpacm 2022/11/23
 */
class AdsUtils {
    private static final String PREF_DEVICE_ACTIVATED = "pref_device_activated";
    private static final String TAG = "Ads";
    private static final String PREF_DEVICE_ACTIVATE_INFO = "pref_device_activate_info";

    static final String DEEP_LINK_TYPE = "deep_type"; //defer or inapp
    static final String DEEP_LINK_ID = "deep_link_id"; //eg: d1zMK
    static final String DEEP_CLICK_ID = "deep_click_id"; //eg:afcde4b0-a703-45fd-9834-175b1778c2d9
    static final String DEEP_CLICK_TIME = "deep_click_time"; //timestamp
    static final String DEEP_PARAMS = "deep_params"; //{}

    static final String DEEPLINK_AD_HOST = "https://link.growingio.com";

    static final String DEEPLINK_PARAMS_REQUEST_URL = "%1$s/deep/v1/%2$s/android/%3$s/%4$s/%5$s";

    static String getRequestDeepLinkUrl(String url, String deepType, String projectId, String datasourceId, String trackId) {
        if (url != null && url.endsWith("/")) url = url.substring(0, url.length() - 1);
        String deeplinkUrl = String.format(DEEPLINK_PARAMS_REQUEST_URL, url, deepType, projectId, datasourceId, trackId);
        Logger.d(TAG, "Call DeepLink Url:" + deeplinkUrl);
        return deeplinkUrl;
    }

    static AdsData parseDeeplinkResponse(String body) {
        final AdsData info = new AdsData();
        try {
            JSONObject rep = new JSONObject(body);
            int code = rep.getInt("code");
            String msg = rep.optString("msg");

            if (code == 200) {
                JSONObject data = rep.getJSONObject("data");
                info.clickID = data.getString(DEEP_CLICK_ID);
                info.linkID = data.getString(DEEP_LINK_ID);
                info.clickTM = data.getString(DEEP_CLICK_TIME);
                info.customParams = data.getString(DEEP_PARAMS);
                info.tm = System.currentTimeMillis();
                info.errorCode = DeepLinkCallback.SUCCESS;
            } else {
                info.errorCode = code;
                Logger.d(TAG, "onReceiveApplinkArgs returnCode error: ", code, ": ", msg);
            }
        } catch (Exception e) {
            Logger.e(TAG, "parse the applink params error \n" + e);
            info.errorCode = DeepLinkCallback.ERROR_EXCEPTION;
        }
        final Map<String, String> params;
        if (info.errorCode == DeepLinkCallback.SUCCESS) {
            params = new HashMap<>();
            info.errorCode = AdsUtils.parseJson(info.customParams, params);
            info.params = params;
        }
        return info;
    }

    /**
     * @param data json格式
     * @return 广告参数对象
     */
    static AdsData parseClipBoardInfo(String data, String urlScheme) {
        try {
            JSONObject jsonObject = new JSONObject(data);
            if (!"gads".equals(jsonObject.getString("type"))) {
                Logger.e(TAG, "非剪贴板数据！");
                return null;
            }
            if (!urlScheme.equals(jsonObject.getString("scheme"))) {
                Logger.e(TAG, "非此应用的延迟深度链接， urlsheme 不匹配，期望为：" + urlScheme + "， 实际为：" + jsonObject.getString("scheme"));
                return null;
            }
            AdsData info = new AdsData();
            info.linkID = jsonObject.getString(DEEP_LINK_ID);
            info.clickID = jsonObject.getString(DEEP_CLICK_ID);
            info.clickTM = jsonObject.getString(DEEP_CLICK_TIME);
            String customParams = jsonObject.getString(DEEP_PARAMS);
            info.customParams = AdsUtils.decode(customParams);
            info.tm = System.currentTimeMillis();

            return info;
        } catch (JSONException e) {
            Logger.e(TAG, "Clipboard 解析异常 ", e);
        }
        return null;
    }

    /**
     * 剪切板数据类型不做校验, 不同浏览器返回MIME类型不同
     * 1. 夸克返回 ClipDescription.MIMETYPE_TEXT_HTML
     * 2. 小米原生浏览器返回 ClipDescription.MIMETYPE_TEXT_PLAIN
     * <p>
     * 点击短链跳转下载后打开的 app ，期望剪贴板被前端落地页写入自定义参数等信息
     * wiki : https://growingio.atlassian.net/wiki/spaces/ads/pages/954336317
     * wiki : https://growingio.atlassian.net/wiki/spaces/ads/pages/942310739/Deferred+Deeplink
     * ZWSP : https://zh.wikipedia.org/wiki/%E9%9B%B6%E5%AE%BD%E7%A9%BA%E6%A0%BC
     *
     * @return true 是 Deffer 延迟深度链接，拿到了有效的剪贴板数据
     * false 是普通打开，剪贴板里没有广告组写进去的数据
     */
    static boolean checkClipBoard(Context context, AdsData info, String urlScheme) {
        try {
            @SuppressLint("WrongConstant") ClipboardManager cm = (ClipboardManager) context.getSystemService(CLIPBOARD_SERVICE);
            ClipData clipData = cm != null ? cm.getPrimaryClip() : null;
            if (clipData == null) return false;
            if (clipData.getItemCount() == 0) return false;
            ClipData.Item item = clipData.getItemAt(0);
            CharSequence charSequence = item.coerceToText(context);

            if (charSequence == null || charSequence.length() == 0) return false;
            String data = parseZwsData(charSequence);
            if (data == null) return false;

            AdsData adsData = parseClipBoardInfo(data, urlScheme);
            if (adsData != null) {
                info.copy(adsData);
                //保存剪切板数据
                AdsUtils.setActivateInfo(data);
                //clean up the clip board
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    cm.clearPrimaryClip();
                } else {
                    cm.setPrimaryClip(ClipData.newPlainText(null, null));
                }
                return true;
            } else {
                return false;
            }

        } catch (Exception e) {
            Logger.e(TAG, e.toString());
            return false;
        }
    }

    /**
     * 不可见字符 → 二进制字符串 → CharCode → 原始数据
     */
    static String parseZwsData(CharSequence charSequence) {
        char zero = (char) 8204;
        StringBuilder binaryList = new StringBuilder();
        for (int i = 0; i < charSequence.length(); i++) {
            binaryList.append(charSequence.charAt(i) == zero ? 0 : 1);
        }
        final int singleCharLength = 16;
        if (binaryList.length() % 16 != 0) {
            return null;
        }
        ArrayList<String> bs = new ArrayList<>();
        int i = 0;
        while (i < binaryList.length()) {
            bs.add(binaryList.substring(i, i + singleCharLength));
            i += singleCharLength;
        }
        StringBuilder listString = new StringBuilder();
        for (String s : bs) {
            listString.append((char) Integer.parseInt(s, 2));
        }
        return listString.toString();
    }

    private AdsUtils() {
    }

    static void clearAdsSharedPreferences() {
        getSharedPreferences().edit().putBoolean(PREF_DEVICE_ACTIVATED, false).apply();
        getSharedPreferences().edit().putString(PREF_DEVICE_ACTIVATE_INFO, "").apply();
    }

    static boolean isDeviceActivated() {
        return getSharedPreferences().getBoolean(PREF_DEVICE_ACTIVATED, false);
    }

    static void setDeviceActivated() {
        getSharedPreferences().edit().putBoolean(PREF_DEVICE_ACTIVATED, true).apply();
    }

    @SuppressLint("WrongConstant")
    private static SharedPreferences getSharedPreferences() {
        return TrackMainThread.trackMain().getContext().getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE);
    }

    static void setActivateInfo(String activateInfo) {
        getSharedPreferences().edit().putString(PREF_DEVICE_ACTIVATE_INFO, activateInfo).apply();
    }

    static String getActivateInfo() {
        return getSharedPreferences().getString(PREF_DEVICE_ACTIVATE_INFO, "");
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
            return DeepLinkCallback.NO_QUERY;
        }
        try {
            JSONObject jsonObject = new JSONObject(json);
            Iterator<String> keys = jsonObject.keys();
            while (keys.hasNext()) {
                String key = keys.next();
                if ("_gio_var".equals(key)) continue;
                map.put(key, jsonObject.getString(key));
            }
        } catch (JSONException jsonException) {
            map.clear();
            return DeepLinkCallback.PARSE_ERROR;
        }
        return DeepLinkCallback.SUCCESS;
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
