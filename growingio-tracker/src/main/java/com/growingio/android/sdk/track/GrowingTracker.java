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
import com.growingio.android.sdk.track.interfaces.IGrowingTracker;
import com.growingio.android.sdk.track.interfaces.InitExtraOperation;
import com.growingio.android.sdk.track.interfaces.ResultCallback;
import com.growingio.android.sdk.track.providers.ActivityStateProvider;
import com.growingio.android.sdk.track.providers.DeviceInfoProvider;
import com.growingio.android.sdk.track.providers.EventCoreGeneratorProvider;
import com.growingio.android.sdk.track.providers.ProjectInfoProvider;
import com.growingio.android.sdk.track.providers.SendPolicyProvider;
import com.growingio.android.sdk.track.providers.SessionProvider;
import com.growingio.android.sdk.track.utils.LogUtil;
import com.growingio.android.sdk.track.utils.ThreadUtils;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;

public class GrowingTracker implements IGrowingTracker {
    static final String TAG = "GrowingIO";

    private static IGrowingTracker sInstance;
    GIOMainThread mGioMain;

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

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
            Log.e(TAG, "GrowingIO 暂不支持Android 4.2以下版本");
            return makeEmpty();
        }
        if (!ThreadUtils.runningOnUiThread()) {
            throw new IllegalStateException("GrowingIO.startWithConfiguration必须在主线程中调用。");
        }

        if (trackConfiguration.isLogEnabled()) {
            LogUtil.add(LogUtil.DebugUtil.getInstance());
        } else {
            LogUtil.add(LogUtil.ReleaseUitl.getInstance());
        }

        if (trackConfiguration.isUploadExceptionEnable()) {
            CrashManager.register(application);
            LogUtil.add(CrashUtil.getInstance());
        }

        if (initExtraOperation == null) {
            initExtraOperation = new InitExtraOperation() {
                @Override
                public boolean requireWaitForCompletion() {
                    return false;
                }

                @Override
                public void init() {
                }

                @Override
                public void initSuccess() {
                }
            };
        }

        if (initExtraOperation.requireWaitForCompletion()) {
            GIOMainThread.initWaitLock();
        }
/*
        if (configuration.isUploadExceptionEnable()){
            CrashManager.register(application);
        }*/

        initAccountInfoProvider(trackConfiguration);

        GrowingTracker result = new GrowingTracker();
        try {
            InitTrackSDKInUICallback.InitTrackSDKInUIPolicy.get(application).initSDK(result);
            initExtraOperation.init();
            GIOMainThread.releaseWaitLock();
        } catch (Throwable e) {
            if (GConfig.getInstance().debug()) {
                throw e;
            }
            LogUtil.e(TAG, e, "初始化SDK失败");
            return makeEmpty();
        }
        initExtraOperation.initSuccess();
        initConfiguration(result, trackConfiguration);
        sInstance = result;

        Log.i(TAG, "!!! Thank you very much for using GrowingIO. We will do our best to provide you with the best service. !!!");
        Log.i(TAG, "!!! GrowingIO version: " + GConfig.SDK_VERSION + " !!!");
        return sInstance;
    }

    private static void initAccountInfoProvider(TrackConfiguration trackConfiguration) {
        if (!TextUtils.isEmpty(trackConfiguration.getProjectId())) {
            ProjectInfoProvider.AccountInfoPolicy.get().setProjectId(trackConfiguration.getProjectId());
        }

        if (!TextUtils.isEmpty(trackConfiguration.getChannel())) {
            ProjectInfoProvider.AccountInfoPolicy.get().setChannel(trackConfiguration.getChannel());
        }

        if (!TextUtils.isEmpty(trackConfiguration.getUrlScheme())) {
            ProjectInfoProvider.AccountInfoPolicy.get().setUrlScheme(trackConfiguration.getUrlScheme());
        }
    }

    private static void initConfiguration(GrowingTracker gio, TrackConfiguration trackConfiguration) {
        if (trackConfiguration.getSessionInterval() > 1e-8) {
            SessionProvider.SessionPolicy.get(gio.mGioMain.getCoreAppState()).setSessionInterval(trackConfiguration.getSessionInterval());
        }

        if (trackConfiguration.getCellularDataLimit() > 0) {
            SendPolicyProvider.SendPolicy.get().setCellularDataLimit(trackConfiguration.getCellularDataLimit());
        }

        if (trackConfiguration.getDataUploadInterval() > 1e-8) {
            SendPolicyProvider.SendPolicy.get().setFlushInterval(trackConfiguration.getDataUploadInterval());
        }
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

    static void initSDK(Application application, GrowingTracker gio) {
        // ActivityState优先级最高
        application.registerActivityLifecycleCallbacks((Application.ActivityLifecycleCallbacks) ActivityStateProvider.ActivityStatePolicy.get());
        gio.mGioMain = new GIOMainThread(application.getApplicationContext());
    }

    @Override
    public IGrowingTracker trackCustomEvent(String eventName, Map<String, String> attributes) {
        eventCoreGenerator().generateCustomEvent(eventName, attributes);
        return this;
    }

    @Override
    public IGrowingTracker setConversionVariables(Map<String, String> variables) {
        eventCoreGenerator().generateConversionVariablesEvent(variables);
        return this;
    }

    @Override
    public IGrowingTracker setLoginUserAttributes(Map<String, String> attributes) {
        eventCoreGenerator().generateLoginUserAttributesEvent(attributes);
        return this;
    }

    @Override
    public IGrowingTracker setVisitorAttributes(Map<String, String> attributes) {
        eventCoreGenerator().generateVisitorAttributesEvent(attributes);
        return this;
    }

    @Override
    public IGrowingTracker getDeviceId(@NonNull ResultCallback<String> callback) {
        if (callback != null) {
            DeviceInfoProvider.DeviceInfoPolicy.get(mGioMain.getContext()).getDeviceId(callback);
        } else {
            Log.e(TAG, "getDeviceId was called, but callback is null, return");
        }
        return this;
    }

    @Override
    public IGrowingTracker setDataCollectionEnabled(boolean enabled) {
        if (enabled == GConfig.getInstance().isEnableDataCollect()) {
            LogUtil.e(TAG, "当前数据采集开关 = " + enabled + ", 请勿重复操作");
        } else {
            GConfig.getInstance().setIsEnableDataCollect(enabled);
            if (enabled) {
                SessionProvider.SessionPolicy.get(mGioMain.getCoreAppState()).forceReissueVisit();
            }
        }

        return this;
    }

    private EventCoreGeneratorProvider eventCoreGenerator() {
        return EventCoreGeneratorProvider.EventCoreGenerator.get(mGioMain.getCoreAppState());
    }

    @Override
    public IGrowingTracker setLoginUserId(String userId) {
        mGioMain.setUserIdToGMain(userId);
        return this;
    }

    @Override
    public IGrowingTracker cleanLoginUserId() {
        mGioMain.setUserIdToGMain(null);
        return this;
    }

    @Override
    public IGrowingTracker setLocation(Double latitude, Double longitude) {
        mGioMain.setLocationToGMain(latitude, longitude);
        return this;
    }

    @Override
    public IGrowingTracker cleanLocation() {
        mGioMain.setLocationToGMain(null, null);
        return this;
    }
}
