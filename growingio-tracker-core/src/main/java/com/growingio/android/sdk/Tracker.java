/*
 * Copyright (C) 2023 Beijing Yishu Technology Co., Ltd.
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
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import androidx.annotation.UiThread;

import com.growingio.android.sdk.track.TrackMainThread;
import com.growingio.android.sdk.track.events.TrackEventGenerator;
import com.growingio.android.sdk.track.events.helper.DynamicGeneralPropsGenerator;
import com.growingio.android.sdk.track.log.Logger;
import com.growingio.android.sdk.track.middleware.abtest.ABExperiment;
import com.growingio.android.sdk.track.middleware.abtest.ABTest;
import com.growingio.android.sdk.track.middleware.abtest.ABTestCallback;
import com.growingio.android.sdk.track.middleware.ads.DeepLinkCallback;
import com.growingio.android.sdk.track.modelloader.ModelLoader;
import com.growingio.android.sdk.track.middleware.hybrid.HybridBridge;
import com.growingio.android.sdk.track.providers.ConfigurationProvider;
import com.growingio.android.sdk.track.providers.DeepLinkProvider;
import com.growingio.android.sdk.track.providers.TrackerLifecycleProvider;
import com.growingio.android.sdk.track.providers.TrackerLifecycleProviderFactory;
import com.growingio.android.sdk.track.providers.SessionProvider;
import com.growingio.android.sdk.track.utils.ClassExistHelper;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

public class Tracker {

    private final TrackerContext trackerContext;

    private static final String TAG = "Tracker";

    protected volatile boolean isInited;

    public Tracker(Context context) {
        isInited = false;
        if (context == null) {
            Logger.e(TAG, "GrowingAutotracker SDK is UNINITIALIZED, please initialized before use API");
            trackerContext = null;
            return;
        }

        if ((!(context instanceof Application) && !(context instanceof Activity))) {
            Logger.e(TAG, "GrowingAutotracker SDK is UNINITIALIZED, please initialized SDK with Application or Activity");
            trackerContext = null;
            return;
        }
        // 初始化
        trackerContext = initTrackerContext(context);

        // 注册所有的模块
        loadAnnotationGeneratedModule(context);

        // 配置业务逻辑
        trackerContext.setup();

        isInited = true;

        TrackMainThread.trackMain().setupWithContext(trackerContext); //need setup

        startAfterSdkSetup(trackerContext);
    }

    private TrackerContext initTrackerContext(Context context) {

        // add Providers in order.
        TrackerLifecycleProviderFactory.create().createActivityStateProvider(context);
        TrackerLifecycleProviderFactory.create().createPersistentDataProvider(context);

        Map<Class<? extends TrackerLifecycleProvider>, TrackerLifecycleProvider> providers = TrackerLifecycleProviderFactory.create().providers();
        Map<Class<? extends TrackerLifecycleProvider>, TrackerLifecycleProvider> extraProviders = extraProviders();
        providers.putAll(extraProviders);

        return new TrackerContext(context.getApplicationContext(), providers);
    }

    private void loadAnnotationGeneratedModule(Context context) {
        loadAnnotationGeneratedModules(context);
        // 支持配置中注册模块, 如加密模块等事件模块需要先于所有事件发送注册
        for (LibraryGioModule component : trackerContext.getConfigurationProvider().core().getPreloadComponents()) {
            // add component provider first.
            component.setupProviders(trackerContext.getProviderStore());
            component.registerComponents(trackerContext);
        }
    }

    protected Map<Class<? extends TrackerLifecycleProvider>, TrackerLifecycleProvider> extraProviders() {
        return TrackerLifecycleProviderFactory.emptyMap();
    }

    public TrackerContext getContext() {
        if (!isInited || null == trackerContext) {
            Logger.e(TAG, new NullPointerException("You should init growingio sdk first!!"));
        }
        return trackerContext;
    }

    private void startAfterSdkSetup(TrackerContext trackerContext) {

        //generate first visit
        SessionProvider sessionProvider = trackerContext.getProvider(SessionProvider.class);
        sessionProvider.createVisitAfterAppStart();

        // release event caches
        TrackMainThread.trackMain().releaseCaches();

        // makeup activity lifecycle
        trackerContext.getActivityStateProvider().makeupActivityLifecycle();

    }

    public void shutdown() {
        isInited = false;
        this.trackerContext.shutdown();
        TrackMainThread.trackMain().shutdown();
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

    public void setDynamicGeneralPropsGenerator(DynamicGeneralPropsGenerator generator) {
        if (!isInited) return;
        if (generator == null) {
            Logger.e(TAG, "setDynamicGeneralPropsGenerator: generator is NULL");
            return;
        }
        trackerContext.getEventBuilderProvider().setDynamicGeneralPropGenerator(generator);
    }

    public void setGeneralProps(Map<String, String> variables) {
        if (!isInited || variables == null || variables.isEmpty()) return;
        TrackMainThread.trackMain().postActionToTrackMain(() ->
                trackerContext.getEventBuilderProvider().setGeneralProps(variables)
        );
    }

    public void clearGeneralProps() {
        if (!isInited) return;
        TrackMainThread.trackMain().postActionToTrackMain(() ->
                trackerContext.getEventBuilderProvider().clearGeneralProps()
        );
    }

    public void removeGeneralProps(String... keys) {
        if (!isInited) return;
        if (keys == null || keys.length == 0) {
            Logger.w(TAG, "keys is NULL or empty.");
            return;
        }
        TrackMainThread.trackMain().postActionToTrackMain(() ->
                trackerContext.getEventBuilderProvider().removeGeneralProps(keys)
        );
    }

    private void requestABExperiment(String layerId, ABTestCallback abTestCallback, boolean requestImmediately) {
        if (layerId == null || layerId.isEmpty() || abTestCallback == null) {
            Logger.e(TAG, "getAbTest:params is illegal");
            return;
        }
        ModelLoader<ABTest, ABExperiment> modelLoader = trackerContext.getRegistry().getModelLoader(ABTest.class, ABExperiment.class);
        if (modelLoader == null) {
            Logger.e(TAG, "getAbTest:please register ABTest Module before use it.");
            return;
        }
        TrackMainThread.trackMain().postActionToTrackMain(() -> modelLoader.buildLoadData(new ABTest(layerId, new ABTestCallback() {
            @Override
            public void onABExperimentReceived(ABExperiment experiment, int dataType) {
                TrackMainThread.trackMain().runOnUiThread(() -> abTestCallback.onABExperimentReceived(experiment, dataType));
            }

            @Override
            public void onABExperimentFailed(Exception error) {
                TrackMainThread.trackMain().runOnUiThread(() -> abTestCallback.onABExperimentFailed(error));

            }
        }, requestImmediately)).fetcher.executeData());

    }

    public void getAbTestImmediately(String layerId, ABTestCallback abTestCallback) {
        if (!isInited) return;
        requestABExperiment(layerId, abTestCallback, true);
    }

    public void getAbTest(String layerId, ABTestCallback abTestCallback) {
        if (!isInited) return;
        requestABExperiment(layerId, abTestCallback, false);
    }

    private void setConversionVariables(Map<String, String> variables) {
        if (!isInited) return;
        if (variables == null || variables.isEmpty()) {
            Logger.w(TAG, "setConversionVariables: variables is NULL, and skip it.");
            return;
        }
        TrackEventGenerator.generateConversionVariablesEvent(new HashMap<>(variables));
    }

    public void setLoginUserAttributes(Map<String, String> attributes) {
        if (!isInited) return;
        if (attributes == null || attributes.isEmpty()) {
            Logger.e(TAG, "setLoginUserAttributes: attributes is NULL, and skip it.");
            return;
        }
        TrackEventGenerator.generateLoginUserAttributesEvent(new HashMap<>(attributes));
    }

    private void setVisitorAttributes(Map<String, String> attributes) {
        if (!isInited) return;
        if (attributes == null || attributes.isEmpty()) {
            Logger.e(TAG, "setVisitorAttributes: attributes is NULL, and skip it");
            return;
        }
        TrackEventGenerator.generateVisitorAttributesEvent(new HashMap<>(attributes));
    }

    public String getDeviceId() {
        if (!isInited) return "UNKNOWN";
        return trackerContext.getDeviceInfoProvider().getDeviceId();
    }

    public void setDataCollectionEnabled(boolean enabled) {
        if (!isInited) return;
        TrackMainThread.trackMain().postActionToTrackMain(new Runnable() {
            @Override
            public void run() {
                ConfigurationProvider configurationProvider = trackerContext.getConfigurationProvider();
                if (enabled != configurationProvider.core().isDataCollectionEnabled()) {
                    Logger.d(TAG, "isDataCollectionEnabled = " + enabled);
                    configurationProvider.core().setDataCollectionEnabled(enabled);
                    if (enabled) {
                        SessionProvider sessionProvider = trackerContext.getProvider(SessionProvider.class);
                        sessionProvider.refreshSessionId();
                        sessionProvider.generateVisit();

                        TrackMainThread.trackMain().releaseCaches();
                    } else {
                        trackerContext.getTimingEventProvider().clearTimer();
                    }
                    // use for modules
                    configurationProvider.onDataCollectionChanged(enabled);
                }
            }
        });
    }

    public void setLoginUserId(final String userId) {
        if (!isInited) return;
        TrackMainThread.trackMain().postActionToTrackMain(() -> trackerContext.getUserInfoProvider().setLoginUserId(userId));
    }

    public void setLoginUserId(final String userId, final String userKey) {
        if (!isInited) return;
        TrackMainThread.trackMain().postActionToTrackMain(() -> trackerContext.getUserInfoProvider().setLoginUserId(userId, userKey));
    }

    public void cleanLoginUserId() {
        if (!isInited) return;
        TrackMainThread.trackMain().postActionToTrackMain(() -> trackerContext.getUserInfoProvider().setLoginUserId(null));
    }

    public void setLocation(final double latitude, final double longitude) {
        if (!isInited) return;
        TrackMainThread.trackMain().postActionToTrackMain(() ->
                trackerContext.getDeviceInfoProvider().setLocation(latitude, longitude)
        );
    }

    public void cleanLocation() {
        if (!isInited) return;
        TrackMainThread.trackMain().postActionToTrackMain(() -> trackerContext.getDeviceInfoProvider().cleanLocation());
    }

    public void onActivityNewIntent(Activity activity, Intent intent) {
        if (!isInited) return;
        trackerContext.getActivityStateProvider().onActivityNewIntent(activity, intent);
    }

    /**
     * inject webview javaInterface bridge.
     *
     * @param webView:WebView or com.tencent.smtt.sdk.WebView or com.uc.webview.export.WebView
     */
    @UiThread
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
        ModelLoader<HybridBridge, Boolean> modelLoader = trackerContext.getRegistry().getModelLoader(HybridBridge.class, Boolean.class);
        if (modelLoader != null) {
            result = modelLoader.buildLoadData(new HybridBridge(view)).fetcher.executeData();
        }
        Logger.d(TAG, "bridgeForWebView: webView = " + view.getClass().getName() + ", result = " + result);
    }

    public String trackTimerStart(final String eventName) {
        if (!isInited || TextUtils.isEmpty(eventName)) {
            return null;
        }
        return trackerContext.getTimingEventProvider().startTimer(eventName);
    }

    public void trackTimerPause(final String timerId) {
        if (!isInited || TextUtils.isEmpty(timerId)) {
            return;
        }
        trackerContext.getTimingEventProvider().updateTimer(timerId, false);
    }

    public void trackTimerResume(final String timerId) {
        if (!isInited || TextUtils.isEmpty(timerId)) {
            return;
        }
        trackerContext.getTimingEventProvider().updateTimer(timerId, true);
    }

    public void trackTimerEnd(final String timerId) {
        if (!isInited || TextUtils.isEmpty(timerId)) {
            return;
        }
        trackerContext.getTimingEventProvider().endTimer(timerId);
    }

    public void trackTimerEnd(final String timerId, Map<String, String> attributes) {
        if (!isInited || TextUtils.isEmpty(timerId)) {
            return;
        }
        trackerContext.getTimingEventProvider().endTimer(timerId, attributes);
    }

    public void removeTimer(final String timerId) {
        if (!isInited || TextUtils.isEmpty(timerId)) {
            return;
        }
        trackerContext.getTimingEventProvider().removeTimer(timerId);
    }

    public void clearTrackTimer() {
        if (!isInited) {
            return;
        }
        trackerContext.getTimingEventProvider().clearTimer();
    }

    public boolean doDeepLinkByUrl(String url, DeepLinkCallback callback) {
        DeepLinkProvider deepLinkProvider = trackerContext.getProvider(DeepLinkProvider.class);
        return deepLinkProvider.doDeepLinkByUrl(url, callback);
    }

    @SuppressWarnings({"unchecked", "PMD.UnusedFormalParameter"})
    private void loadAnnotationGeneratedModules(Context context) {
        try {
            Class<GeneratedGioModule> clazz =
                    (Class<GeneratedGioModule>)
                            Class.forName("com.growingio.android.sdk.GeneratedGioModuleImpl");
            GeneratedGioModule generatedGioModule = clazz.getDeclaredConstructor().newInstance();
            generatedGioModule.registerComponents(trackerContext);
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
        addComponent(module, null);
    }

    public void registerComponent(LibraryGioModule module, Configurable config) {
        addComponent(module, config);
    }

    /**
     * uninstall module
     */
    protected void uninstallComponent(Class modelClazz, Class dataClazz, Class<? extends Configurable> configClazz, Class<? extends TrackerLifecycleProvider> providerClazz) {
        if (modelClazz == null) return;
        if (configClazz != null) {
            trackerContext.getConfigurationProvider().removeConfiguration(configClazz);
        }
        trackerContext.getRegistry().unregister(modelClazz, dataClazz);
        if (providerClazz != null) {
            TrackerLifecycleProvider provider = trackerContext.getProviderStore().remove(providerClazz);
            if (provider != null) provider.shutdown();
        }
    }

    private void addComponent(LibraryGioModule module, Configurable config) {
        if (module == null) return;
        if (config != null) {
            trackerContext.getConfigurationProvider().addConfiguration(config);
        }
        Map<Class<? extends TrackerLifecycleProvider>, TrackerLifecycleProvider> emptyMap = TrackerLifecycleProviderFactory.emptyMap();
        module.setupProviders(emptyMap);
        for (Class<? extends TrackerLifecycleProvider> key : emptyMap.keySet()) {
            TrackerLifecycleProvider provider = emptyMap.get(key);
            if (isInited) {
                provider.setup(trackerContext);
            }
            trackerContext.getProviderStore().put(key, provider);
        }
        module.registerComponents(trackerContext);

    }

    private void throwIncorrectGioModule(Exception e) {
        throw new IllegalStateException(
                "GeneratedGioModuleImpl is implemented incorrectly."
                        + " If you've manually implemented this class, remove your implementation. The"
                        + " Annotation processor will generate a correct implementation.",
                e);
    }

}
