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
import android.app.Fragment;
import android.support.annotation.Nullable;
import android.view.View;

import java.util.Map;

enum EmptyGrowingAutotracker implements IGrowingAutotracker {

    INSTANCE;

    @Override
    public void setPageAttributes(Activity page, Map<String, String> attributes) {

    }

    @Override
    public void setPageAttributes(Fragment page, Map<String, String> attributes) {

    }

    @Override
    public void setPageAttributes(android.support.v4.app.Fragment page, Map<String, String> attributes) {

    }

    @Override
    public void setPageAttributes(androidx.fragment.app.Fragment page, Map<String, String> attributes) {

    }

    @Override
    public void setPageAlias(Activity page, String alias) {

    }

    @Override
    public void setPageAlias(Fragment page, String alias) {

    }

    @Override
    public void setPageAlias(android.support.v4.app.Fragment page, String alias) {

    }

    @Override
    public void setPageAlias(androidx.fragment.app.Fragment page, String alias) {

    }

    @Override
    public void ignorePage(Activity page, IgnorePolicy policy) {

    }

    @Override
    public void ignorePage(Fragment page, IgnorePolicy policy) {

    }

    @Override
    public void ignorePage(android.support.v4.app.Fragment page, IgnorePolicy policy) {

    }

    @Override
    public void ignorePage(androidx.fragment.app.Fragment page, IgnorePolicy policy) {

    }

    @Override
    public void trackViewImpression(View view, String impressionEventName) {

    }

    @Override
    public void trackViewImpression(View view, String impressionEventName, Map<String, String> attributes) {

    }

    @Override
    public void stopTrackViewImpression(View trackedView) {

    }

    @Override
    public void ignoreView(View view, IgnorePolicy policy) {

    }

    @Override
    public void setUniqueTag(View view, String tag) {

    }

    @Override
    public void trackCustomEvent(String eventName, Activity page) {

    }

    @Override
    public void trackCustomEvent(String eventName, Fragment page) {

    }

    @Override
    public void trackCustomEvent(String eventName, android.support.v4.app.Fragment page) {

    }

    @Override
    public void trackCustomEvent(String eventName, androidx.fragment.app.Fragment page) {

    }

    @Override
    public void trackCustomEvent(String eventName, Map<String, String> attributes, Activity page) {

    }

    @Override
    public void trackCustomEvent(String eventName, Map<String, String> attributes, Fragment page) {

    }

    @Override
    public void trackCustomEvent(String eventName, Map<String, String> attributes, android.support.v4.app.Fragment page) {

    }

    @Override
    public void trackCustomEvent(String eventName, Map<String, String> attributes, androidx.fragment.app.Fragment page) {

    }

    @Override
    public void trackCustomEvent(String eventName) {

    }

    @Override
    public void trackCustomEvent(String eventName, Map<String, String> attributes) {

    }

    @Override
    public void setLoginUserAttributes(Map<String, String> attributes) {

    }

    @Override
    public void setVisitorAttributes(Map<String, String> attributes) {

    }

    @Override
    public void setConversionVariables(Map<String, String> variables) {

    }

    @Override
    public void setLoginUserId(String userId) {

    }

    @Override
    public void cleanLoginUserId() {

    }

    @Override
    public void setLocation(double latitude, double longitude) {

    }

    @Override
    public void cleanLocation() {

    }

    @Override
    public void setDataCollectionEnabled(boolean enabled) {

    }

    @Nullable
    @Override
    public String getDeviceId() {
        return null;
    }
}
