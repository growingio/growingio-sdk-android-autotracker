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

package com.growingio.android.sdk;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.growingio.android.sdk.track.TrackMainThread;
import com.growingio.android.sdk.track.TrackerContext;
import com.growingio.android.sdk.track.events.TrackEventGenerator;
import com.growingio.android.sdk.track.log.DebugLogger;
import com.growingio.android.sdk.track.log.Logger;
import com.growingio.android.sdk.track.providers.ActivityStateProvider;
import com.growingio.android.sdk.track.providers.ConfigurationProvider;
import com.growingio.android.sdk.track.providers.DeviceInfoProvider;
import com.growingio.android.sdk.track.providers.SessionProvider;
import com.growingio.android.sdk.track.providers.UserInfoProvider;
import com.growingio.android.sdk.track.utils.ThreadUtils;
import com.growingio.android.sdk.track.webservices.WebServicesProvider;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

public class Tracker {

    private static final String TAG = "GrowingIO Track SDK";

    protected WebServicesProvider mWebServicesProvider;
    protected boolean isInited = false;

    public Tracker(Application application, TrackConfiguration trackConfiguration) {
        if (application == null || trackConfiguration == null) {
            isInited = false;
            Logger.e(TAG, "GrowingIO Track SDK is UNINITIALIZED, please initialized before use API");
            return;
        }
        initTracker(application, trackConfiguration);
    }

    private void initTracker(Application application, TrackConfiguration trackConfiguration) {
        if (TextUtils.isEmpty(trackConfiguration.getProjectId())) {
            throw new IllegalStateException("ProjectId is NULL");
        }

        if (TextUtils.isEmpty(trackConfiguration.getUrlScheme())) {
            throw new IllegalStateException("UrlScheme is NULL");
        }

        if (!ThreadUtils.runningOnUiThread()) {
            throw new IllegalStateException("Growing Sdk 初始化必须在主线程中调用。");
        }


        TrackerContext.init(application);
        loadAnnotationGeneratedModules(application);

        ConfigurationProvider.get().setTrackConfiguration(trackConfiguration);

        if (trackConfiguration.isDebugEnabled()) {
            Logger.addLogger(new DebugLogger());
        }

        // init core service
        application.registerActivityLifecycleCallbacks(ActivityStateProvider.get());
        TrackMainThread.trackMain().register(SessionProvider.get());
        // init other service
        mWebServicesProvider = new WebServicesProvider(trackConfiguration.getUrlScheme(), ActivityStateProvider.get());

        isInited = true;
    }

    @SuppressWarnings({"unchecked", "PMD.UnusedFormalParameter"})
    private void loadAnnotationGeneratedModules(Context context) {
        try {
            Class<GeneratedGioModule> clazz =
                    (Class<GeneratedGioModule>)
                            Class.forName("com.growingio.android.sdk.GeneratedGioModuleImpl");
            GeneratedGioModule generatedGioModule = clazz.getDeclaredConstructor(Context.class).newInstance(context.getApplicationContext());

            if (generatedGioModule != null) {
                generatedGioModule.registerComponents(context, TrackerContext.get().getRegistry());
            }
        } catch (ClassNotFoundException e) {
            if (Log.isLoggable(TAG, Log.WARN)) {
                Log.w(
                        TAG,
                        "Failed to find GeneratedGioModule. You should include an"
                                + " annotationProcessor compile dependency on com.growingio.android.sdk:compiler"
                                + " in your application and a @GIOModule annotated AppGioModule implementation"
                                + " or LibraryGioModules will be silently ignored");
            }
            // These exceptions can't be squashed across all versions of Android.
        } catch (InstantiationException e) {
            throwIncorrectGioModule(e);
        } catch (IllegalAccessException e) {
            throwIncorrectGioModule(e);
        } catch (NoSuchMethodException e) {
            throwIncorrectGioModule(e);
        } catch (InvocationTargetException e) {
            throwIncorrectGioModule(e);
        }
    }

    private static void throwIncorrectGioModule(Exception e) {
        throw new IllegalStateException(
                "GeneratedGioModuleImpl is implemented incorrectly."
                        + " If you've manually implemented this class, remove your implementation. The"
                        + " Annotation processor will generate a correct implementation.",
                e);
    }

    public void trackCustomEvent(String eventName) {
        if (!isInited) return;
        trackCustomEvent(eventName, null);
    }

    public void trackCustomEvent(String eventName, Map<String, String> attributes) {
        if (!isInited) return;
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
        if (!isInited) return;
        if (variables == null || variables.isEmpty()) {
            Logger.e(TAG, "setConversionVariables: variables is NULL");
            return;
        }
        TrackEventGenerator.generateConversionVariablesEvent(new HashMap<>(variables));
    }

    public void setLoginUserAttributes(Map<String, String> attributes) {
        if (!isInited) return;
        if (attributes == null || attributes.isEmpty()) {
            Logger.e(TAG, "setLoginUserAttributes: attributes is NULL");
            return;
        }
        TrackEventGenerator.generateLoginUserAttributesEvent(new HashMap<>(attributes));
    }

    public void setVisitorAttributes(Map<String, String> attributes) {
        if (!isInited) return;
        if (attributes == null || attributes.isEmpty()) {
            Logger.e(TAG, "setVisitorAttributes: attributes is NULL");
            return;
        }
        TrackEventGenerator.generateVisitorAttributesEvent(new HashMap<>(attributes));
    }

    @Nullable
    public String getDeviceId() {
        if (!isInited) return "UNKNOWN";
        return DeviceInfoProvider.get().getDeviceId();
    }

    public void setDataCollectionEnabled(boolean enabled) {
        if (!isInited) return;
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
        if (!isInited) return;
        TrackMainThread.trackMain().postActionToTrackMain(() -> UserInfoProvider.get().setLoginUserId(userId));
    }

    public void cleanLoginUserId() {
        if (!isInited) return;
        TrackMainThread.trackMain().postActionToTrackMain(() -> UserInfoProvider.get().setLoginUserId(null));
    }

    public void setLocation(final double latitude, final double longitude) {
        if (!isInited) return;
        TrackMainThread.trackMain().postActionToTrackMain(() -> SessionProvider.get().setLocation(latitude, longitude));
    }

    public void cleanLocation() {
        if (!isInited) return;
        TrackMainThread.trackMain().postActionToTrackMain(() -> SessionProvider.get().cleanLocation());
    }

    public void onActivityNewIntent(@NonNull Activity activity, Intent intent) {
        if (!isInited) return;
        if (activity == null) {
            Logger.e(TAG, "activity is NULL");
            return;
        }
        ActivityStateProvider.get().onActivityNewIntent(activity, intent);
    }

}
