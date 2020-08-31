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

import android.app.Application;
import android.os.Build;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import com.growingio.android.sdk.track.crash.CrashManager;
import com.growingio.android.sdk.track.events.TrackEventGenerator;
import com.growingio.android.sdk.track.interfaces.InitExtraOperation;
import com.growingio.android.sdk.track.interfaces.ResultCallback;
import com.growingio.android.sdk.track.log.DebugLogger;
import com.growingio.android.sdk.track.log.Logger;
import com.growingio.android.sdk.track.providers.ActivityStateProvider;
import com.growingio.android.sdk.track.providers.ConfigurationProvider;
import com.growingio.android.sdk.track.providers.DeviceInfoProvider;
import com.growingio.android.sdk.track.providers.SessionProvider;
import com.growingio.android.sdk.track.providers.UserInfoProvider;
import com.growingio.android.sdk.track.utils.ThreadUtils;

import java.util.Map;

public class GrowingTracker implements IGrowingTracker {
    static final String TAG = "GrowingTracker";

    private static IGrowingTracker sInstance;
    private static volatile boolean sInitializedSuccessfully = false;

    private TrackMainThread mTrackMainThread;

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

    public static boolean initializedSuccessfully() {
        return sInitializedSuccessfully;
    }

    public static void startWithConfiguration(Application application, TrackConfiguration trackConfiguration) {
        startWithConfiguration(application, trackConfiguration, null);
    }

    public static void startWithConfiguration(Application application, TrackConfiguration trackConfiguration, InitExtraOperation initExtraOperation) {
        if (sInstance != null) {
            Logger.e(TAG, "GrowingTracker is running");
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
        }

        ConfigurationProvider.get().setTrackConfiguration(trackConfiguration);

        if (trackConfiguration.isDebugEnabled()) {
            Logger.addLogger(new DebugLogger());
        }

        if (trackConfiguration.isUploadExceptionEnabled()) {
            CrashManager.register(application);
        }

        if (initExtraOperation == null) {
            initExtraOperation = new InitExtraOperation() {
                @Override
                public void initializing() {
                }

                @Override
                public void initSuccess() {
                }
            };
        }
/*
        if (configuration.isUploadExceptionEnable()){
            CrashManager.register(application);
        }*/

        GrowingTracker tracker = new GrowingTracker();

        // ActivityState优先级最高
        application.registerActivityLifecycleCallbacks(ActivityStateProvider.get());

        try {
            initExtraOperation.initializing();
        } catch (Throwable e) {
            if (trackConfiguration.isDebugEnabled()) {
                throw e;
            }
            Logger.e(TAG, e, "初始化SDK失败");
        }
        initExtraOperation.initSuccess();
        initCoreService();
        tracker.mTrackMainThread = TrackMainThread.trackMain();
        sInstance = tracker;

        sInitializedSuccessfully = true;
        Log.i(TAG, "!!! Thank you very much for using GrowingIO. We will do our best to provide you with the best service. !!!");
        Log.i(TAG, "!!! GrowingIO Tracker version: " + SDKConfig.SDK_VERSION + " !!!");
    }

    private static void initCoreService() {
        TrackMainThread.trackMain().register(SessionProvider.get());
    }

    private static IGrowingTracker makeEmpty() {
        Logger.e(TAG, "GrowingTracker is UNINITIALIZED, please initialized before use API");
        return EmptyGrowingTracker.INSTANCE;
    }

    @Override
    public void trackCustomEvent(String eventName) {
        trackCustomEvent(eventName, null);
    }

    @Override
    public void trackCustomEvent(String eventName, Map<String, String> attributes) {
        TrackEventGenerator.generateCustomEvent(eventName, attributes);
    }

    @Override
    public void setConversionVariables(Map<String, String> variables) {
        TrackEventGenerator.generateConversionVariablesEvent(variables);
    }

    @Override
    public void setLoginUserAttributes(Map<String, String> attributes) {
        TrackEventGenerator.generateLoginUserAttributesEvent(attributes);
    }

    @Override
    public void setVisitorAttributes(Map<String, String> attributes) {
        TrackEventGenerator.generateVisitorAttributesEvent(attributes);
    }

    @Override
    public void getDeviceId(@NonNull ResultCallback<String> callback) {
        if (callback != null) {
            DeviceInfoProvider.get().getDeviceId(callback);
        } else {
            Log.e(TAG, "getDeviceId was called, but callback is null, return");
        }
    }

    @Override
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

    @Override
    public void setLoginUserId(final String userId) {
        mTrackMainThread.postActionToTrackMain(new Runnable() {
            @Override
            public void run() {
                UserInfoProvider.get().setUserId(userId);
            }
        });
    }

    @Override
    public void cleanLoginUserId() {
        mTrackMainThread.postActionToTrackMain(new Runnable() {
            @Override
            public void run() {
                UserInfoProvider.get().setUserId(null);
            }
        });
    }

    @Override
    public void setLocation(final double latitude, final double longitude) {
        mTrackMainThread.postActionToTrackMain(new Runnable() {
            @Override
            public void run() {
                SessionProvider.get().setLocation(latitude, longitude);
            }
        });
    }

    @Override
    public void cleanLocation() {
        mTrackMainThread.postActionToTrackMain(new Runnable() {
            @Override
            public void run() {
                SessionProvider.get().cleanLocation();
            }
        });
    }
}
