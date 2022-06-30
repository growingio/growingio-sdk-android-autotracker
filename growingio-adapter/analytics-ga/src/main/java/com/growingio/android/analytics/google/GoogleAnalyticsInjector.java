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

package com.growingio.android.analytics.google;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;
import com.growingio.android.sdk.TrackerContext;
import com.growingio.android.sdk.track.log.Logger;

import java.util.Map;

public class GoogleAnalyticsInjector {
    private static final String TAG = "GoogleAnalyticsInjector";

    private GoogleAnalyticsInjector() {
    }

    public static void newTracker(Tracker tracker, GoogleAnalytics googleAnalytics, int resId) {
        if (!TrackerContext.initializedSuccessfully()) {
            Logger.e(TAG, "Tracker do not initialized successfully");
            return;
        }

        GoogleAnalyticsAdapter.get().newTracker(tracker, resId);
    }

    public static void newTracker(Tracker tracker, GoogleAnalytics googleAnalytics, String measurementId) {
        if (!TrackerContext.initializedSuccessfully()) {
            Logger.e(TAG, "Tracker do not initialized successfully");
            return;
        }

        GoogleAnalyticsAdapter.get().newTracker(tracker, measurementId);
    }

    public static void setAppOptOut(GoogleAnalytics googleAnalytics, boolean optOut) {
        if (!TrackerContext.initializedSuccessfully()) {
            Logger.e(TAG, "Tracker do not initialized successfully");
            return;
        }

        GoogleAnalyticsAdapter.get().setAppOptOut(optOut);
    }

    public static void setClientId(Tracker tracker, String clientId) {
        if (!TrackerContext.initializedSuccessfully()) {
            Logger.e(TAG, "Tracker do not initialized successfully");
            return;
        }

        GoogleAnalyticsAdapter.get().setClientId(tracker, clientId);
    }

    public static void send(Tracker tracker, Map<String, String> params) {
        if (!TrackerContext.initializedSuccessfully()) {
            Logger.e(TAG, "Tracker do not initialized successfully");
            return;
        }

        GoogleAnalyticsAdapter.get().send(tracker, params);
    }

    public static void set(Tracker tracker, String key, String value) {
        if (!TrackerContext.initializedSuccessfully()) {
            Logger.e(TAG, "Tracker do not initialized successfully");
            return;
        }

        GoogleAnalyticsAdapter.get().set(tracker, key, value);
    }
}
