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
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import com.growingio.android.sdk.track.TrackMainThread;
import com.growingio.android.sdk.track.events.CustomEvent;
import com.growingio.android.sdk.track.ipc.PersistentDataProvider;
import com.growingio.android.sdk.track.events.TrackEventGenerator;
import com.growingio.android.sdk.track.log.Logger;
import com.growingio.android.sdk.track.modelloader.ModelLoader;
import com.growingio.android.sdk.track.modelloader.data.HybridBridge;
import com.growingio.android.sdk.track.providers.ActivityStateProvider;
import com.growingio.android.sdk.track.providers.ConfigurationProvider;
import com.growingio.android.sdk.track.providers.DeepLinkProvider;
import com.growingio.android.sdk.track.providers.DeviceInfoProvider;
import com.growingio.android.sdk.track.providers.SessionProvider;
import com.growingio.android.sdk.track.providers.UserInfoProvider;
import com.growingio.android.sdk.track.utils.ClassExistHelper;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

public class Tracker {

    private static final String TAG = "GrowingIO Track SDK";

    protected volatile boolean isInited = false;

    public Tracker(Application application) {
        if (application == null) {
            isInited = false;
            Logger.e(TAG, "GrowingIO Track SDK is UNINITIALIZED, please initialized before use API");
            return;
        }
        // 配置
        setup(application);
        // 业务逻辑
        start();

        TrackerContext.initSuccess();
        isInited = true;
    }

    @CallSuper
    protected void setup(Application application) {
        TrackerContext.init(application);
        // init core service
        application.registerActivityLifecycleCallbacks(ActivityStateProvider.get());
        DeepLinkProvider.get().init();
        SessionProvider.get().init();

        loadAnnotationGeneratedModules(application);
        // 支持配置中注册模块, 如加密模块等事件模块需要先于所有事件发送注册
        for (LibraryGioModule component : ConfigurationProvider.core().getPreloadComponents()) {
            component.registerComponents(application, TrackerContext.get().getRegistry());
        }
    }

    private void start() {
        PersistentDataProvider.get().start();
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

    public void trackCustomEventWithAttrBuilder(String eventName, CustomEvent.AttributesBuilder attributesBuilder) {
        if (!isInited) return;
        if (TextUtils.isEmpty(eventName)) {
            Logger.e(TAG, "trackCustomEvent: eventName is NULL");
            return;
        }

        TrackEventGenerator.generateCustomEvent(eventName, attributesBuilder.getAttributes());
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
        TrackMainThread.trackMain().postActionToTrackMain(new Runnable() {
            @Override
            public void run() {
                if (enabled == ConfigurationProvider.core().isDataCollectionEnabled()) {
                    Logger.e(TAG, "当前数据采集开关 = " + enabled + ", 请勿重复操作");
                } else {
                    ConfigurationProvider.core().setDataCollectionEnabled(enabled);
                    if (enabled) {
                        SessionProvider.get().generateVisit();
                    }
                }
            }
        });
    }

    public void setLoginUserId(final String userId) {
        if (!isInited) return;
        TrackMainThread.trackMain().postActionToTrackMain(() -> UserInfoProvider.get().setLoginUserId(userId));
    }

    public void setLoginUserId(final String userId, final String userKey) {
        if (!isInited) return;
        TrackMainThread.trackMain().postActionToTrackMain(() -> UserInfoProvider.get().setLoginUserId(userId, userKey));
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
        ActivityStateProvider.get().onActivityNewIntent(activity, intent);
    }

    /**
     * inject webview javaInterface bridge.
     *
     * @param webView:WebView or com.tencent.smtt.sdk.WebView or com.uc.webview.export.WebView
     */
    public void bridgeWebView(View webView) {
        if (!isInited) return;
        if (ClassExistHelper.isWebView(webView)) {
            bridgeInnerWebView(webView);
        } else {
            Logger.e(TAG, "please check your " + webView.getClass().getName() + "is WebView or com.tencent.smtt.sdk.WebView or com.uc.webview.export.WebView");
        }
    }

    private void bridgeInnerWebView(View view) {
        boolean result = false;
        ModelLoader<HybridBridge, Boolean> modelLoader = TrackerContext.get().getRegistry().getModelLoader(HybridBridge.class, Boolean.class);
        if (modelLoader != null) {
            result = modelLoader.buildLoadData(new HybridBridge(view)).fetcher.executeData();
        }
        Logger.d(TAG, "bridgeForWebView: webView = " + view.getClass().getName() + ", result = " + result);
    }

    @SuppressWarnings({"unchecked", "PMD.UnusedFormalParameter"})
    private void loadAnnotationGeneratedModules(Context context) {
        try {
            Class<GeneratedGioModule> clazz =
                    (Class<GeneratedGioModule>)
                            Class.forName("com.growingio.android.sdk.GeneratedGioModuleImpl");
            GeneratedGioModule generatedGioModule = clazz.getDeclaredConstructor(Context.class).newInstance(context.getApplicationContext());
            generatedGioModule.registerComponents(context, TrackerContext.get().getRegistry());
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

    /**
     * you can register module manually through this api.
     *
     * @param module GIOLibraryModule
     */
    public void registerComponent(LibraryGioModule module) {
        if (!isInited || module == null) return;
        module.registerComponents(TrackerContext.get(), TrackerContext.get().getRegistry());
    }

    private void throwIncorrectGioModule(Exception e) {
        throw new IllegalStateException(
                "GeneratedGioModuleImpl is implemented incorrectly."
                        + " If you've manually implemented this class, remove your implementation. The"
                        + " Annotation processor will generate a correct implementation.",
                e);
    }

}
