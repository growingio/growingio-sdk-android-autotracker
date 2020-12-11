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
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.growingio.android.sdk.track.log.Logger;
import com.growingio.android.sdk.track.utils.ThreadUtils;

import java.util.Map;

public class GrowingTracker implements IGrowingTracker {
    private static final String TAG = "GrowingTracker";

    private final Tracker mTracker;

    private static volatile IGrowingTracker sInstance;

    public static IGrowingTracker get() {
        if (sInstance != null) {
            return sInstance;
        }
        synchronized (GrowingTracker.class) {
            if (sInstance != null) {
                return sInstance;
            }
            return makeEmpty();
        }
    }

    private GrowingTracker(Tracker tracker) {
        mTracker = tracker;
    }

    public static void startWithConfiguration(Application application, TrackConfiguration trackConfiguration) {
        if (sInstance != null) {
            Logger.e(TAG, "GrowingTracker is running");
            return;
        }
        if (application == null) {
            throw new IllegalStateException("application is NULL");
        }
        ContextProvider.setContext(application);

        if (TextUtils.isEmpty(trackConfiguration.getProjectId())) {
            throw new IllegalStateException("ProjectId is NULL");
        }

        if (TextUtils.isEmpty(trackConfiguration.getUrlScheme())) {
            throw new IllegalStateException("UrlScheme is NULL");
        }

        if (!ThreadUtils.runningOnUiThread()) {
            throw new IllegalStateException("startWithConfiguration必须在主线程中调用。");
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
            Log.e(TAG, "GrowingTracker 暂不支持Android 4.2以下版本");
            return;
        }

        sInstance = new GrowingTracker(new Tracker(application, trackConfiguration));

        Log.i(TAG, "!!! Thank you very much for using GrowingIO. We will do our best to provide you with the best service. !!!");
        Log.i(TAG, "!!! GrowingIO Tracker version: " + SDKConfig.SDK_VERSION + " !!!");
    }

    private static IGrowingTracker makeEmpty() {
        Logger.e(TAG, "GrowingTracker is UNINITIALIZED, please initialized before use API");
        return EmptyGrowingTracker.INSTANCE;
    }

    @Override
    public void trackCustomEvent(String eventName) {
        mTracker.trackCustomEvent(eventName);
    }

    @Override
    public void trackCustomEvent(String eventName, Map<String, String> attributes) {
        mTracker.trackCustomEvent(eventName, attributes);
    }

    @Override
    public void setConversionVariables(Map<String, String> variables) {
        mTracker.setConversionVariables(variables);
    }

    @Override
    public void setLoginUserAttributes(Map<String, String> attributes) {
        mTracker.setLoginUserAttributes(attributes);
    }

    @Override
    public void setVisitorAttributes(Map<String, String> attributes) {
        mTracker.setVisitorAttributes(attributes);
    }

    @Override
    public void onActivityNewIntent(@NonNull Activity activity, Intent intent) {
        mTracker.onActivityNewIntent(activity, intent);
    }

    @Override
    public void setDataCollectionEnabled(boolean enabled) {
        mTracker.setDataCollectionEnabled(enabled);
    }

    @Nullable
    @Override
    public String getDeviceId() {
        return mTracker.getDeviceId();
    }

    @Override
    public void setLoginUserId(String userId) {
        mTracker.setLoginUserId(userId);
    }

    @Override
    public void cleanLoginUserId() {
        mTracker.cleanLoginUserId();
    }

    @Override
    public void setLocation(double latitude, double longitude) {
        mTracker.setLocation(latitude, longitude);
    }

    @Override
    public void cleanLocation() {
        mTracker.cleanLocation();
    }
}
