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

package com.growingio.android.sdk.track;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import com.growingio.android.sdk.track.events.TrackEventGenerator;
import com.growingio.android.sdk.track.interfaces.ResultCallback;
import com.growingio.android.sdk.track.log.DebugLogger;
import com.growingio.android.sdk.track.log.Logger;
import com.growingio.android.sdk.track.providers.ActivityStateProvider;
import com.growingio.android.sdk.track.providers.ConfigurationProvider;
import com.growingio.android.sdk.track.providers.DeviceInfoProvider;
import com.growingio.android.sdk.track.providers.SessionProvider;
import com.growingio.android.sdk.track.providers.UserInfoProvider;
import com.growingio.android.sdk.track.webservices.WebServicesProvider;

import java.util.HashMap;
import java.util.Map;

public class Tracker {
    private static final String TAG = "Tracker";

    private final Application mApplication;
    private final TrackConfiguration mTrackConfiguration;

    private final TrackMainThread mTrackMainThread;

    protected final ActivityStateProvider mActivityStateProvider;
    protected final WebServicesProvider mWebServicesProvider;

    protected Tracker(Application application, TrackConfiguration trackConfiguration) {
        mApplication = application;
        mTrackConfiguration = trackConfiguration;
        ContextProvider.setContext(application);

        ConfigurationProvider.get().setTrackConfiguration(trackConfiguration);

        if (trackConfiguration.isDebugEnabled()) {
            Logger.addLogger(new DebugLogger());
        }

        // init core service
        mActivityStateProvider = ActivityStateProvider.get();
        mApplication.registerActivityLifecycleCallbacks(mActivityStateProvider);
        TrackMainThread.trackMain().register(SessionProvider.get());

        mTrackMainThread = TrackMainThread.trackMain();

        // init other service
        mWebServicesProvider = new WebServicesProvider(mTrackConfiguration.getUrlScheme(), ActivityStateProvider.get());
    }

    public void trackCustomEvent(String eventName) {
        trackCustomEvent(eventName, null);
    }

    public void trackCustomEvent(String eventName, Map<String, String> attributes) {
        if (TextUtils.isEmpty(eventName)) {
            Logger.e(TAG, "trackCustomEvent: eventName is NULL");
            return;
        }

        if (attributes != null) {
            attributes = new HashMap<>(attributes);
        }
        TrackEventGenerator.generateCustomEvent(eventName, attributes);
    }

    public void setConversionVariables(Map<String, String> variables) {
        if (variables == null || variables.isEmpty()) {
            Logger.e(TAG, "setConversionVariables: variables is NULL");
            return;
        }
        TrackEventGenerator.generateConversionVariablesEvent(new HashMap<>(variables));
    }

    public void setLoginUserAttributes(Map<String, String> attributes) {
        if (attributes == null || attributes.isEmpty()) {
            Logger.e(TAG, "setLoginUserAttributes: attributes is NULL");
            return;
        }
        TrackEventGenerator.generateLoginUserAttributesEvent(new HashMap<>(attributes));
    }

    public void setVisitorAttributes(Map<String, String> attributes) {
        if (attributes == null || attributes.isEmpty()) {
            Logger.e(TAG, "setVisitorAttributes: attributes is NULL");
            return;
        }
        TrackEventGenerator.generateVisitorAttributesEvent(new HashMap<>(attributes));
    }

    public void getDeviceId(@NonNull ResultCallback<String> callback) {
        if (callback != null) {
            DeviceInfoProvider.get().getDeviceId(callback);
        } else {
            Log.e(TAG, "getDeviceId was called, but callback is null, return");
        }
    }

    public void setDataCollectionEnabled(boolean enabled) {
        if (enabled == ConfigurationProvider.get().isDataCollectionEnabled()) {
            Logger.e(TAG, "当前数据采集开关 = " + enabled + ", 请勿重复操作");
        } else {
            ConfigurationProvider.get().setDataCollectionEnabled(true);
            if (enabled) {
                SessionProvider.get().forceReissueVisit();
            }
        }
    }

    public void setLoginUserId(final String userId) {
        mTrackMainThread.postActionToTrackMain(new Runnable() {
            @Override
            public void run() {
                UserInfoProvider.get().setLoginUserId(userId);
            }
        });
    }

    public void cleanLoginUserId() {
        mTrackMainThread.postActionToTrackMain(new Runnable() {
            @Override
            public void run() {
                UserInfoProvider.get().setLoginUserId(null);
            }
        });
    }

    public void setLocation(final double latitude, final double longitude) {
        mTrackMainThread.postActionToTrackMain(new Runnable() {
            @Override
            public void run() {
                SessionProvider.get().setLocation(latitude, longitude);
            }
        });
    }

    public void cleanLocation() {
        mTrackMainThread.postActionToTrackMain(new Runnable() {
            @Override
            public void run() {
                SessionProvider.get().cleanLocation();
            }
        });
    }

    public void onActivityNewIntent(@NonNull Activity activity, Intent intent) {
        if (activity == null) {
            Logger.e(TAG, "activity is NULL");
            return;
        }
        ActivityStateProvider.get().onActivityNewIntent(activity, intent);
    }

}
