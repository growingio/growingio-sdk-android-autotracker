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
import com.growingio.android.sdk.track.crash.CrashUtil;
import com.growingio.android.sdk.track.events.TrackEventGenerator;
import com.growingio.android.sdk.track.interfaces.IGrowingTracker;
import com.growingio.android.sdk.track.interfaces.InitExtraOperation;
import com.growingio.android.sdk.track.interfaces.ResultCallback;
import com.growingio.android.sdk.track.providers.ActivityStateProvider;
import com.growingio.android.sdk.track.providers.ConfigurationProvider;
import com.growingio.android.sdk.track.providers.DeviceInfoProvider;
import com.growingio.android.sdk.track.providers.SessionProvider;
import com.growingio.android.sdk.track.providers.UserInfoProvider;
import com.growingio.android.sdk.track.utils.LogUtil;
import com.growingio.android.sdk.track.utils.ThreadUtils;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;

public class GrowingTracker implements IGrowingTracker {
    static final String TAG = "GrowingIO";

    private static IGrowingTracker sInstance;
    private static volatile boolean sInitSucceeded = false;

    private TrackMainThread mTrackMainThread;

    public static IGrowingTracker getInstance() {
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

    public static boolean isInitSucceeded() {
        return sInitSucceeded;
    }

    public static IGrowingTracker startWithConfiguration(Application application, TrackConfiguration trackConfiguration) {
        return startWithConfiguration(application, trackConfiguration, null);
    }

    public static IGrowingTracker startWithConfiguration(Application application, TrackConfiguration trackConfiguration, InitExtraOperation initExtraOperation) {
        if (sInstance != null) {
            Log.e(TAG, "GrowingIO已经初始化过了");
            return sInstance;
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

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
            Log.e(TAG, "GrowingIO 暂不支持Android 4.2以下版本");
            return makeEmpty();
        }
        if (!ThreadUtils.runningOnUiThread()) {
            throw new IllegalStateException("GrowingIO.startWithConfiguration必须在主线程中调用。");
        }

        ConfigurationProvider.get().setTrackConfiguration(trackConfiguration);

        if (trackConfiguration.isLogEnabled()) {
            LogUtil.add(LogUtil.DebugUtil.getInstance());
        } else {
            LogUtil.add(LogUtil.ReleaseUitl.getInstance());
        }

        if (trackConfiguration.isUploadExceptionEnabled()) {
            CrashManager.register(application);
            LogUtil.add(CrashUtil.getInstance());
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
            if (trackConfiguration.isLogEnabled()) {
                throw e;
            }
            LogUtil.e(TAG, e, "初始化SDK失败");
            return makeEmpty();
        }
        initExtraOperation.initSuccess();
        initCoreService();
        tracker.mTrackMainThread = TrackMainThread.trackMain();
        sInstance = tracker;

        sInitSucceeded = true;
        Log.i(TAG, "!!! Thank you very much for using GrowingIO. We will do our best to provide you with the best service. !!!");
        Log.i(TAG, "!!! GrowingIO Tracker version: " + SDKConfig.SDK_VERSION + " !!!");
        return sInstance;
    }

    private static void initCoreService() {
        TrackMainThread.trackMain().register(SessionProvider.get());
    }

    private static IGrowingTracker makeEmpty() {
        return (IGrowingTracker) Proxy.newProxyInstance(GrowingTracker.class.getClassLoader(), new Class[]{IGrowingTracker.class}, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                Log.e(TAG, "SDK尚未初始化");
                if (method.getReturnType() == IGrowingTracker.class) {
                    return proxy;
                }
                return null;
            }
        });
    }

    @Override
    public IGrowingTracker trackCustomEvent(String eventName, Map<String, String> attributes) {
        TrackEventGenerator.generateCustomEvent(eventName, attributes);
        return this;
    }

    @Override
    public IGrowingTracker setConversionVariables(Map<String, String> variables) {
        TrackEventGenerator.generateConversionVariablesEvent(variables);
        return this;
    }

    @Override
    public IGrowingTracker setLoginUserAttributes(Map<String, String> attributes) {
        TrackEventGenerator.generateLoginUserAttributesEvent(attributes);
        return this;
    }

    @Override
    public IGrowingTracker setVisitorAttributes(Map<String, String> attributes) {
        TrackEventGenerator.generateVisitorAttributesEvent(attributes);
        return this;
    }

    @Override
    public IGrowingTracker getDeviceId(@NonNull ResultCallback<String> callback) {
        if (callback != null) {
            DeviceInfoProvider.get().getDeviceId(callback);
        } else {
            Log.e(TAG, "getDeviceId was called, but callback is null, return");
        }
        return this;
    }

    @Override
    public IGrowingTracker setDataCollectionEnabled(boolean enabled) {
        if (enabled == ConfigurationProvider.get().isDataCollectionEnabled()) {
            LogUtil.e(TAG, "当前数据采集开关 = " + enabled + ", 请勿重复操作");
        } else {
            ConfigurationProvider.get().setDataCollectionEnabled(true);
            if (enabled) {
                SessionProvider.get().forceReissueVisit();
            }
        }

        return this;
    }

    @Override
    public IGrowingTracker setLoginUserId(final String userId) {
        mTrackMainThread.postActionToTrackMain(new Runnable() {
            @Override
            public void run() {
                UserInfoProvider.get().setUserId(userId);
            }
        });
        return this;
    }

    @Override
    public IGrowingTracker cleanLoginUserId() {
        mTrackMainThread.postActionToTrackMain(new Runnable() {
            @Override
            public void run() {
                UserInfoProvider.get().setUserId(null);
            }
        });
        return this;
    }

    @Override
    public IGrowingTracker setLocation(final double latitude, final double longitude) {
        mTrackMainThread.postActionToTrackMain(new Runnable() {
            @Override
            public void run() {
                SessionProvider.get().setLocation(latitude, longitude);
            }
        });
        return this;
    }

    @Override
    public IGrowingTracker cleanLocation() {
        mTrackMainThread.postActionToTrackMain(new Runnable() {
            @Override
            public void run() {
                SessionProvider.get().cleanLocation();
            }
        });
        return this;
    }
}
