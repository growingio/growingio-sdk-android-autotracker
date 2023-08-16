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

package com.growingio.android.sdk.collection;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.webkit.WebView;
import android.widget.EditText;

import com.growingio.android.sdk.autotrack.GrowingAutotracker;
import com.growingio.android.sdk.deeplink.DeeplinkCallback;
import com.growingio.android.sdk.interfaces.IGrowingIO;
import com.growingio.android.sdk.track.SDKConfig;
import com.growingio.android.sdk.track.events.AttributesBuilder;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@Deprecated
public class GrowingIO implements IGrowingIO {
    private static final String TAG = "GrowingIO";

    private static class SingleInstance {
        private static final IGrowingIO INSTANCE = new GrowingIO();
    }

    private GrowingIO() {
    }

    //region CDP 2.0 Compatible API
    @Override
    public IGrowingIO setUserAttributes(Map<String, ?> attributes) {
        HashMap<String, String> stringHashMap = new HashMap<>();
        for (String key : attributes.keySet()) {
            stringHashMap.put(key, String.valueOf(attributes.get(key)));
        }
        GrowingAutotracker.get().setLoginUserAttributes(stringHashMap);
        return this;
    }

    @Override
    public IGrowingIO setUserAttributes(JSONObject jsonObject) {
        GrowingAutotracker.get().setLoginUserAttributes(copyToMap(jsonObject));
        return this;
    }
    //endregion

    //region SaaS 2.0 Compatible API
    @Override
    public IGrowingIO disableDataCollect() {
        GrowingAutotracker.get().setDataCollectionEnabled(false);
        return this;
    }

    @Override
    public IGrowingIO enableDataCollect() {
        GrowingAutotracker.get().setDataCollectionEnabled(true);
        return this;
    }

    @Override
    public String getDeviceId() {
        return GrowingAutotracker.get().getDeviceId();
    }

    @Override
    public String getVisitUserId() {
        return GrowingAutotracker.get().getDeviceId();
    }

    @Override
    public IGrowingIO setGeoLocation(double latitude, double longitude) {
        GrowingAutotracker.get().setLocation(latitude, longitude);
        return this;
    }

    @Override
    public IGrowingIO clearGeoLocation() {
        GrowingAutotracker.get().cleanLocation();
        return this;
    }

    @Override
    public IGrowingIO setUserId(String userId) {
        GrowingAutotracker.get().setLoginUserId(userId);
        return this;
    }

    @Override
    public IGrowingIO clearUserId() {
        GrowingAutotracker.get().cleanLoginUserId();
        return this;
    }

    @Override
    public String getUserId() {
        return GrowingAutotracker.get().getLoginUserId();
    }

    @Override
    public IGrowingIO track(String eventName) {
        GrowingAutotracker.get().trackCustomEvent(eventName);
        return this;
    }

    @Override
    public IGrowingIO track(String eventName, Number number) {
        GrowingAutotracker.get().trackCustomEvent(eventName);
        return this;
    }

    @Override
    public IGrowingIO track(String eventName, JSONObject variable) {
        GrowingAutotracker.get().trackCustomEvent(eventName, copyToMap(variable));
        return this;
    }

    @Override
    public IGrowingIO track(String eventName, Number number, JSONObject variable) {
        GrowingAutotracker.get().trackCustomEvent(eventName, copyToMap(variable));
        return this;
    }

    @Override
    public IGrowingIO setPeopleVariable(JSONObject variables) {
        GrowingAutotracker.get().setLoginUserAttributes(copyToMap(variables));
        return this;
    }

    @Override
    public IGrowingIO setPeopleVariable(String key, Number value) {
        GrowingAutotracker.get().setLoginUserAttributes(new AttributesBuilder().addAttribute(key, String.valueOf(value)).build());
        return this;
    }

    @Override
    public IGrowingIO setPeopleVariable(String key, String value) {
        GrowingAutotracker.get().setLoginUserAttributes(new AttributesBuilder().addAttribute(key, value).build());
        return this;
    }

    @Override
    public IGrowingIO setPeopleVariable(String key, boolean value) {
        GrowingAutotracker.get().setLoginUserAttributes(new AttributesBuilder().addAttribute(key, String.valueOf(value)).build());
        return this;
    }
    //endregion

    //region SaaS 2.0 Incompatible API
    @Override
    public IGrowingIO ignoreFragment(Activity activity, Fragment fragment) {
        return this;
    }

    @Override
    public IGrowingIO ignoreFragment(Activity activity, android.support.v4.app.Fragment fragment) {
        return this;
    }

    @Override
    public IGrowingIO setPageName(Activity activity, String name) {
        return this;
    }

    @Override
    public IGrowingIO setPageName(Fragment fragment, String name) {
        return this;
    }

    @Override
    public IGrowingIO setPageName(android.support.v4.app.Fragment fragment, String name) {
        return this;
    }

    @Override
    public IGrowingIO trackFragment(Activity activity, android.support.v4.app.Fragment fragment) {
        return this;
    }

    @Override
    public IGrowingIO trackEditText(EditText editText) {
        return this;
    }

    @Override
    public IGrowingIO trackFragment(Activity activity, Fragment fragment) {
        return this;
    }

    @Override
    public IGrowingIO trackFragment(Activity activity, ViewPager viewPager, View view, String pagename) {
        return this;
    }

    @Override
    public IGrowingIO setPageVariable(Activity activity, JSONObject variable) {
        return this;
    }

    @Override
    public IGrowingIO setPageVariable(Activity activity, String key, String value) {
        return this;
    }

    @Override
    public IGrowingIO setPageVariable(Activity activity, String key, boolean value) {
        return this;
    }

    @Override
    public IGrowingIO setPageVariable(Activity activity, String key, Number value) {
        return this;
    }

    @Override
    public IGrowingIO setPageVariable(Fragment fragment, JSONObject variable) {
        return this;
    }

    @Override
    public IGrowingIO setPageVariable(Fragment fragment, String key, String value) {
        return this;
    }

    @Override
    public IGrowingIO setPageVariable(Fragment fragment, String key, boolean value) {
        return this;
    }

    @Override
    public IGrowingIO setPageVariable(Fragment fragment, String key, Number value) {
        return this;
    }

    @Override
    public IGrowingIO setPageVariable(android.support.v4.app.Fragment fragment, JSONObject variable) {
        return this;
    }

    @Override
    public IGrowingIO setPageVariable(android.support.v4.app.Fragment fragment, String key, String value) {
        return this;
    }

    @Override
    public IGrowingIO setPageVariable(android.support.v4.app.Fragment fragment, String key, boolean value) {
        return this;
    }

    @Override
    public IGrowingIO setPageVariable(android.support.v4.app.Fragment fragment, String key, Number value) {
        return this;
    }

    @Override
    public IGrowingIO setThrottle(boolean throttle) {
        return this;
    }

    @Override
    public IGrowingIO disable() {
        return this;
    }

    @Override
    public IGrowingIO resume() {
        return this;
    }

    @Override
    public IGrowingIO stop() {
        return this;
    }

    @Override
    public String getSessionId() {
        return "";
    }

    @Override
    public IGrowingIO setTestHandler(Handler handler) {
        return this;
    }

    @Override
    public IGrowingIO setEvar(JSONObject variable) {
        return this;
    }

    @Override
    public IGrowingIO setEvar(String key, Number value) {
        return this;
    }

    @Override
    public IGrowingIO setEvar(String key, String value) {
        return this;
    }

    @Override
    public IGrowingIO setEvar(String key, boolean value) {
        return this;
    }

    @Override
    public IGrowingIO setVisitor(JSONObject visitorVariable) {
        return null;
    }

    @Override
    public IGrowingIO setAppVariable(JSONObject variable) {
        return this;
    }

    @Override
    public IGrowingIO setAppVariable(String key, Number value) {
        return this;
    }

    @Override
    public IGrowingIO setAppVariable(String key, String value) {
        return this;
    }

    @Override
    public IGrowingIO setAppVariable(String key, boolean value) {
        return this;
    }

    @Override
    public IGrowingIO setChannel(String channel) {
        return this;
    }

    @Override
    public IGrowingIO disableImpression() {
        return this;
    }

    @Override
    public IGrowingIO setImp(boolean enable) {
        return this;
    }

    @Override
    public IGrowingIO trackPage(String pagename, String lastpage, long ptm) {
        return this;
    }

    @Override
    public IGrowingIO saveVisit(String pagename) {
        return this;
    }

    @Override
    public IGrowingIO trackPage(String pageName) {
        return this;
    }

    @Override
    public IGrowingIO markViewImpression(ImpressionMark mark) {
        return this;
    }

    @Override
    public IGrowingIO stopMarkViewImpression(View markedView) {
        return this;
    }

    @Override
    public IGrowingIO setPageVariable(String pageName, JSONObject variable) {
        return this;
    }

    @Override
    public IGrowingIO onNewIntent(Activity activity, Intent intent) {
        return this;
    }

    @Override
    public IGrowingIO ignoreViewImp(View view) {
        return this;
    }

    @Override
    public IGrowingIO manualPageShow(@NonNull @androidx.annotation.NonNull Activity activity, @NonNull @androidx.annotation.NonNull String pageName) {
        return this;
    }

    @Override
    public GrowingIO trackWebView(View webView) {
        return this;
    }

    @Override
    public IGrowingIO setTrackAllFragment(Activity activity, boolean trackAllFragment) {
        return this;
    }

    @Override
    public IGrowingIO ignoreFragmentX(Activity activity, androidx.fragment.app.Fragment fragment) {
        return this;
    }

    @Override
    public IGrowingIO setPageNameX(androidx.fragment.app.Fragment fragment, String name) {
        return this;
    }

    @Override
    public IGrowingIO trackFragmentX(Activity activity, androidx.fragment.app.Fragment fragment) {
        return this;
    }

    @Override
    public IGrowingIO trackFragmentX(Activity activity, androidx.viewpager.widget.ViewPager viewPager, View view, String pagename) {
        return this;
    }

    @Override
    public IGrowingIO setPageVariableX(androidx.fragment.app.Fragment fragment, JSONObject variable) {
        return this;
    }

    @Override
    public IGrowingIO setPageVariableX(androidx.fragment.app.Fragment fragment, String key, String value) {
        return this;
    }

    @Override
    public IGrowingIO setPageVariableX(androidx.fragment.app.Fragment fragment, String key, boolean value) {
        return this;
    }

    @Override
    public IGrowingIO setPageVariableX(androidx.fragment.app.Fragment fragment, String key, Number value) {
        return this;
    }

    @Override
    public IGrowingIO setImeiEnable(boolean imeiEnable) {
        return this;
    }

    @Override
    public IGrowingIO setGoogleAdIdEnable(boolean googleAdIdEnable) {
        return this;
    }

    @Override
    public IGrowingIO setAndroidIdEnable(boolean androidIdEnable) {
        return this;
    }

    @Override
    public IGrowingIO setOAIDEnable(boolean oaidEnable) {
        return this;
    }

    @Override
    public boolean isDeepLinkUrl(@androidx.annotation.Nullable String url) {
        return false;
    }

    @Override
    public boolean doDeeplinkByUrl(@Nullable @androidx.annotation.Nullable String url, @Nullable @androidx.annotation.Nullable DeeplinkCallback callback) {
        return false;
    }

    @Override
    public void bridgeForWebView(WebView webView) {
    }

    @Override
    public void bridgeForX5WebView(com.tencent.smtt.sdk.WebView x5WebView) {
    }

    @Override
    public void bridgeForUcWebView(com.uc.webview.export.WebView ucWebView) {
    }
    //endregion

    //region Saas 2.0 Compatible static API
    @Deprecated
    public static IGrowingIO getInstance() {
        return SingleInstance.INSTANCE;
    }

    @Deprecated
    public static String getVersion() {
        return SDKConfig.SDK_VERSION;
    }
    //endregion

    //region SaaS 2.0 Incompatible static API
    @Deprecated
    public static void setViewID(final View view, final String id) {
    }

    @Deprecated
    public static void setScheme(String scheme) {
    }

    @Deprecated
    public static void setTrackerHost(String trackerHost) {
    }

    @Deprecated
    public static void setAdHost(String adHost) {
    }

    @Deprecated
    public static void setTagsHost(String tagsHost) {
    }

    @Deprecated
    public static void setGtaHost(String gtaHost) {
    }

    @Deprecated
    public static void setJavaCirclePluginHost(String javaCirclePluginHost) {
    }

    @Deprecated
    public static void setWsHost(String wsHost) {
    }

    @Deprecated
    public static void setDataHost(String dataHost) {
    }

    @Deprecated
    public static void setAssetsHost(String assetsHost) {
    }

    @Deprecated
    public static void setReportHost(String reportHost) {
    }

    @Deprecated
    public static void setHybridJSSDKUrlPrefix(String urlPrefix) {
    }

    @Deprecated
    public static void setZone(String zone) {
    }

    @Deprecated
    public static void trackBanner(final View banner, final List<String> bannerContents) {
    }

    @Deprecated
    public static void ignoredView(final View view) {
    }

    @Deprecated
    public static void setViewInfo(View view, String info) {
    }

    @Deprecated
    public static void setViewContent(View view, String content) {
    }

    @Deprecated
    public static void setTabName(View tab, String name) {
    }

    @Deprecated
    public static void setPressed(final View view) {
    }
    //endregion

    private static Map<String, String> copyToMap(JSONObject jsonObject) {
        if (jsonObject == null)
            return null;
        Map<String, String> map = new HashMap<>();
        Iterator<String> keys = jsonObject.keys();
        while (keys.hasNext()) {
            String key = keys.next();
            try {
                if (jsonObject.get(key) == JSONObject.NULL) {
                    map.put(key, null);
                } else {
                    map.put(key, String.valueOf(jsonObject.get(key)));
                }
            } catch (JSONException e) {
                // ignore
            }
        }
        return map;
    }
}
