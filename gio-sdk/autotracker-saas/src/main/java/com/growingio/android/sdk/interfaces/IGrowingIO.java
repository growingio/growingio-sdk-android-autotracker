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
package com.growingio.android.sdk.interfaces;

import android.app.Activity;
import android.support.v4.app.Fragment;
import android.content.Intent;
import android.os.Handler;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.webkit.WebView;
import android.widget.EditText;

import com.growingio.android.sdk.collection.GrowingIO;
import com.growingio.android.sdk.collection.ImpressionMark;
import com.growingio.android.sdk.deeplink.DeeplinkCallback;

import org.json.JSONObject;

import java.util.Map;

@Deprecated
public interface IGrowingIO {

    /* op */
    @Deprecated
    IGrowingIO setUserAttributes(Map<String, ?> attributes);

    @Deprecated
    IGrowingIO setUserAttributes(JSONObject jsonObject);

    @Deprecated
    IGrowingIO disableDataCollect();

    @Deprecated
    IGrowingIO enableDataCollect();

    /* saas */
    //    @Deprecated void disableDataCollect();
    //    @Deprecated void enableDataCollect();
    @Deprecated
    IGrowingIO ignoreFragment(Activity activity, android.app.Fragment fragment);

    @Deprecated
    IGrowingIO ignoreFragment(Activity activity, Fragment fragment);

    @Deprecated
    IGrowingIO setPageName(Activity activity, String name);

    @Deprecated
    IGrowingIO setPageName(android.app.Fragment fragment, String name);

    @Deprecated
    IGrowingIO setPageName(Fragment fragment, String name);

    @Deprecated
    IGrowingIO trackFragment(Activity activity, Fragment fragment);

    @Deprecated
    IGrowingIO trackEditText(EditText editText);

    @Deprecated
    IGrowingIO trackFragment(Activity activity, android.app.Fragment fragment);

    @Deprecated
    IGrowingIO trackFragment(Activity activity, ViewPager viewPager, View view, String pagename);

    @Deprecated
    IGrowingIO setPageVariable(Activity activity, JSONObject variable);

    @Deprecated
    IGrowingIO setPageVariable(Activity activity, String key, String value);

    @Deprecated
    IGrowingIO setPageVariable(Activity activity, String key, boolean value);

    @Deprecated
    IGrowingIO setPageVariable(Activity activity, String key, Number value);

    @Deprecated
    IGrowingIO setPageVariable(android.app.Fragment fragment, JSONObject variable);

    @Deprecated
    IGrowingIO setPageVariable(android.app.Fragment fragment, String key, String value);

    @Deprecated
    IGrowingIO setPageVariable(android.app.Fragment fragment, String key, boolean value);

    @Deprecated
    IGrowingIO setPageVariable(android.app.Fragment fragment, String key, Number value);

    @Deprecated
    IGrowingIO setPageVariable(Fragment fragment, JSONObject variable);

    @Deprecated
    IGrowingIO setPageVariable(Fragment fragment, String key, String value);

    @Deprecated
    IGrowingIO setPageVariable(Fragment fragment, String key, boolean value);

    @Deprecated
    IGrowingIO setPageVariable(Fragment fragment, String key, Number value);

    @Deprecated
    IGrowingIO setThrottle(boolean throttle);

    @Deprecated
    IGrowingIO disable();

    @Deprecated
    IGrowingIO resume();

    @Deprecated
    IGrowingIO stop();

    @Deprecated
    String getDeviceId();

    @Deprecated
    String getVisitUserId();

    @Deprecated
    String getSessionId();

    @Deprecated
    IGrowingIO setTestHandler(Handler handler);

    @Deprecated
    IGrowingIO setGeoLocation(double latitude, double longitude);

    @Deprecated
    IGrowingIO clearGeoLocation();

    @Deprecated
    IGrowingIO setUserId(String userId);

    @Deprecated
    IGrowingIO clearUserId();

    @Deprecated
    String getUserId();

    @Deprecated
    IGrowingIO setPeopleVariable(JSONObject variables);

    @Deprecated
    IGrowingIO setPeopleVariable(String key, Number value);

    @Deprecated
    IGrowingIO setPeopleVariable(String key, String value);

    @Deprecated
    IGrowingIO setPeopleVariable(String key, boolean value);

    @Deprecated
    IGrowingIO setEvar(JSONObject variable);

    @Deprecated
    IGrowingIO setEvar(String key, Number value);

    @Deprecated
    IGrowingIO setEvar(String key, String value);

    @Deprecated
    IGrowingIO setEvar(String key, boolean value);

    @Deprecated
    IGrowingIO setVisitor(JSONObject visitorVariable);

    @Deprecated
    IGrowingIO setAppVariable(JSONObject variable);

    @Deprecated
    IGrowingIO setAppVariable(String key, Number value);

    @Deprecated
    IGrowingIO setAppVariable(String key, String value);

    @Deprecated
    IGrowingIO setAppVariable(String key, boolean value);

    @Deprecated
    IGrowingIO setChannel(String channel);

    @Deprecated
    IGrowingIO disableImpression();

    @Deprecated
    IGrowingIO setImp(boolean enable);

    @Deprecated
    IGrowingIO track(String eventName);

    @Deprecated
    IGrowingIO track(String eventName, Number number);

    @Deprecated
    IGrowingIO track(String eventName, JSONObject variable);

    @Deprecated
    IGrowingIO track(String eventName, Number number, JSONObject variable);

    @Deprecated
    IGrowingIO trackPage(String pagename, String lastpage, long ptm);

    @Deprecated
    IGrowingIO saveVisit(String pagename);

    @Deprecated
    IGrowingIO trackPage(String pageName);

    @Deprecated
    IGrowingIO markViewImpression(ImpressionMark mark);

    @Deprecated
    IGrowingIO stopMarkViewImpression(View markedView);

    @Deprecated
    IGrowingIO setPageVariable(String pageName, JSONObject variable);

    @Deprecated
    IGrowingIO onNewIntent(Activity activity, Intent intent);

    @Deprecated
    IGrowingIO ignoreViewImp(final View view);

    @Deprecated
    IGrowingIO manualPageShow(Activity activity, String pageName);

    @Deprecated
    GrowingIO trackWebView(final View webView);

    @Deprecated
    IGrowingIO setTrackAllFragment(Activity activity, boolean trackAllFragment);

    @Deprecated
    IGrowingIO ignoreFragmentX(Activity activity, androidx.fragment.app.Fragment fragment);

    @Deprecated
    IGrowingIO setPageNameX(androidx.fragment.app.Fragment fragment, String name);

    @Deprecated
    IGrowingIO trackFragmentX(Activity activity, androidx.fragment.app.Fragment fragment);

    @Deprecated
    IGrowingIO trackFragmentX(Activity activity, androidx.viewpager.widget.ViewPager viewPager, View view, String pagename);

    @Deprecated
    IGrowingIO setPageVariableX(androidx.fragment.app.Fragment fragment, JSONObject variable);

    @Deprecated
    IGrowingIO setPageVariableX(androidx.fragment.app.Fragment fragment, String key, String value);

    @Deprecated
    IGrowingIO setPageVariableX(androidx.fragment.app.Fragment fragment, String key, boolean value);

    @Deprecated
    IGrowingIO setPageVariableX(androidx.fragment.app.Fragment fragment, String key, Number value);

    @Deprecated
    IGrowingIO setImeiEnable(boolean imeiEnable);

    @Deprecated
    IGrowingIO setGoogleAdIdEnable(boolean googleAdIdEnable);

    @Deprecated
    IGrowingIO setAndroidIdEnable(boolean androidIdEnable);

    @Deprecated
    IGrowingIO setOAIDEnable(boolean oaidEnable);

    @Deprecated
    boolean isDeepLinkUrl(@androidx.annotation.Nullable String url);

    @Deprecated
    boolean doDeeplinkByUrl(String url, DeeplinkCallback callback);

    @Deprecated
    void bridgeForWebView(WebView webView);

    @Deprecated
    void bridgeForX5WebView(com.tencent.smtt.sdk.WebView x5WebView);

    @Deprecated
    void bridgeForUcWebView(com.uc.webview.export.WebView ucWebView);
}
