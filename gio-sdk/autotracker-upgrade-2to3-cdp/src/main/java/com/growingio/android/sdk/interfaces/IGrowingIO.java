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

package com.growingio.android.sdk.interfaces;


import android.app.Activity;
import android.app.Application;
import android.content.Intent;

import com.growingio.android.sdk.autotrack.GrowingAutotracker;

import org.json.JSONObject;

import java.util.Map;

/**
 * @deprecated {@link GrowingAutotracker )}
 */
@Deprecated
public interface IGrowingIO {

    void upgrade(Application context);

    /**
     * @deprecated {@link GrowingAutotracker#get()#setLoginUserAttributes(Map)}
     */
    @Deprecated
    IGrowingIO setUserAttributes(Map<String, ?> attributes);

    /**
     * @deprecated {@link GrowingAutotracker#get()#setLoginUserAttributes(Map)}
     */
    @Deprecated
    IGrowingIO setUserAttributes(JSONObject jsonObject);

    /**
     * @deprecated {@link GrowingAutotracker#get()#setDataCollectionEnabled(boolean)}
     */
    @Deprecated
    IGrowingIO disableDataCollect();

    /**
     * @deprecated {@link GrowingAutotracker#get()#setDataCollectionEnabled(boolean)}
     */
    @Deprecated
    IGrowingIO enableDataCollect();

    /**
     * @deprecated {@link GrowingAutotracker#get()#getDeviceId()}
     */
    @Deprecated
    String getDeviceId();

    /**
     * @deprecated {@link GrowingAutotracker#get()#getDeviceId()}
     */
    @Deprecated
    String getVisitUserId();


    /**
     * @deprecated {@link GrowingAutotracker#get()#setLocation(double, double)}
     */
    @Deprecated
    IGrowingIO setGeoLocation(double latitude, double longitude);

    /**
     * @deprecated {@link GrowingAutotracker#get()#cleanLocation()}
     */
    @Deprecated
    IGrowingIO clearGeoLocation();

    /**
     * @deprecated {@link GrowingAutotracker#get()#setLoginUserId(String)}
     */
    @Deprecated
    IGrowingIO setUserId(String userId);

    /**
     * @deprecated {@link GrowingAutotracker#get()#cleanLoginUserId()}
     */
    @Deprecated
    IGrowingIO clearUserId();

    /**
     * @deprecated {@link GrowingAutotracker#get()#trackCustomEvent(String)}
     */
    @Deprecated
    IGrowingIO track(String eventName);

    /**
     * @deprecated {@link GrowingAutotracker#get()#trackCustomEvent(String, Map)}
     */
    @Deprecated
    IGrowingIO track(String eventName, JSONObject var);

    /**
     * @deprecated {@link GrowingAutotracker#get()#trackCustomEvent(String, Map, String, String)}
     */
    @Deprecated
    IGrowingIO track(String eventName, JSONObject var, String itemId, String itemKey);

    /**
     * @deprecated 无埋点不需要手动发送page事件
     */
    @Deprecated
    IGrowingIO trackPage(String pageName);

    /**
     * @deprecated 无埋点不需要手动发送page事件
     */
    @Deprecated
    IGrowingIO trackPage(String pageName, JSONObject var);

    /**
     * @deprecated imei自动采集无需添加设置
     */
    @Deprecated
    IGrowingIO setImeiEnable(boolean imeiEnable);

    /**
     * @deprecated androidId自动采集无需添加设置
     */
    @Deprecated
    IGrowingIO setAndroidIdEnable(boolean androidIdEnable);

    /**
     * @deprecated WebView自动注入bridge，无需手动注入
     */
    @Deprecated
    IGrowingIO bridgeForWebView(Object webView);

    /**
     * @deprecated WebView自动注入bridge，无需手动注入
     */
    @Deprecated
    IGrowingIO bridgeForX5WebView(Object x5WebView);

    /**
     * @deprecated Activity自动注入，无需手动注入
     */
    @Deprecated
    IGrowingIO onNewIntent(Activity activity, Intent intent);
}

