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

package com.growingio.android.sdk.autotrack;

import android.app.Activity;
import android.support.annotation.Nullable;
import android.view.View;

import java.util.Map;

/**
 * GrowingAutotracker 无埋点 SDK 对外 API
 */
public interface IGrowingAutotracker {

    void trackCustomEvent(String eventName);

    void trackCustomEvent(String eventName, Map<String, String> attributes);

    void setLoginUserAttributes(Map<String, String> attributes);

    void setVisitorAttributes(Map<String, String> attributes);

    void setConversionVariables(Map<String, String> variables);

    void setLoginUserId(String userId);

    void cleanLoginUserId();

    void setLocation(double latitude, double longitude);

    void cleanLocation();

    void setDataCollectionEnabled(boolean enabled);

    @Nullable
    String getDeviceId();

    void setPageAttributes(Activity page, Map<String, String> attributes);

    void setPageAttributes(android.app.Fragment page, Map<String, String> attributes);

    void setPageAttributes(android.support.v4.app.Fragment page, Map<String, String> attributes);

    void setPageAttributes(androidx.fragment.app.Fragment page, Map<String, String> attributes);

    void setPageAlias(Activity page, String alias);

    void setPageAlias(android.app.Fragment page, String alias);

    void setPageAlias(android.support.v4.app.Fragment page, String alias);

    void setPageAlias(androidx.fragment.app.Fragment page, String alias);

    void ignorePage(Activity page, IgnorePolicy policy);

    void ignorePage(android.app.Fragment page, IgnorePolicy policy);

    void ignorePage(android.support.v4.app.Fragment page, IgnorePolicy policy);

    void ignorePage(androidx.fragment.app.Fragment page, IgnorePolicy policy);

    void trackViewImpression(View view, String impressionEventName);

    void trackViewImpression(View view, String impressionEventName, Map<String, String> attributes);

    void stopTrackViewImpression(View trackedView);

    void ignoreView(View view, IgnorePolicy policy);

    void setUniqueTag(View view, String tag);

    void trackCustomEvent(String eventName, Activity page);

    void trackCustomEvent(String eventName, android.app.Fragment page);

    void trackCustomEvent(String eventName, android.support.v4.app.Fragment page);

    void trackCustomEvent(String eventName, androidx.fragment.app.Fragment page);

    void trackCustomEvent(String eventName, Map<String, String> attributes, Activity page);

    void trackCustomEvent(String eventName, Map<String, String> attributes, android.app.Fragment page);

    void trackCustomEvent(String eventName, Map<String, String> attributes, android.support.v4.app.Fragment page);

    void trackCustomEvent(String eventName, Map<String, String> attributes, androidx.fragment.app.Fragment page);

}
