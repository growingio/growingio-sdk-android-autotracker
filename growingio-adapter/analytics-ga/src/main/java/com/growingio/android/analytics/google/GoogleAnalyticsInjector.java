package com.growingio.android.analytics.google;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;
import com.growingio.android.sdk.TrackerContext;
import com.growingio.android.sdk.track.log.Logger;

import java.util.Map;

public class GoogleAnalyticsInjector {
    private static final String TAG = "GoogleAnalyticsInjector";

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
