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

package com.growingio.android.sdk.track.providers;

import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;

import com.growingio.android.sdk.TrackerContext;
import com.growingio.android.sdk.track.TrackMainThread;
import com.growingio.android.sdk.track.listener.IActivityLifecycle;
import com.growingio.android.sdk.track.listener.OnConfigurationChangeListener;
import com.growingio.android.sdk.track.listener.event.ActivityLifecycleEvent;
import com.growingio.android.sdk.track.log.Logger;
import com.growingio.android.sdk.track.middleware.advert.Activate;
import com.growingio.android.sdk.track.middleware.advert.AdvertResult;
import com.growingio.android.sdk.track.middleware.advert.DeepLinkCallback;
import com.growingio.android.sdk.track.modelloader.LoadDataFetcher;
import com.growingio.android.sdk.track.webservices.Circler;
import com.growingio.android.sdk.track.webservices.Debugger;
import com.growingio.android.sdk.track.webservices.WebService;


import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

import static com.growingio.android.sdk.track.listener.event.ActivityLifecycleEvent.EVENT_TYPE.ON_CREATED;
import static com.growingio.android.sdk.track.listener.event.ActivityLifecycleEvent.EVENT_TYPE.ON_NEW_INTENT;
import static com.growingio.android.sdk.track.listener.event.ActivityLifecycleEvent.EVENT_TYPE.ON_RESUMED;

/**
 * <p>
 * sdk 统一 url scheme 入口
 *
 * @author cpacm 2021/3/25
 */
public class DeepLinkProvider implements IActivityLifecycle, OnConfigurationChangeListener {

    private static final String TAG = "DeepLinkProvider";

    private WeakReference<Intent> lastIntentRef; //intent 去重，防止多发deeplink
    private Uri needResendUri = null;
    private boolean needResendActivate = false;

    private static class SingleInstance {
        private static final DeepLinkProvider INSTANCE = new DeepLinkProvider();
    }

    private DeepLinkProvider() {
    }

    @Override
    public void onDataCollectionChanged(boolean isEnable) {
        if (isEnable) {
            if (needResendUri != null) {
                handleDeepLink(needResendUri);
            }
            if (needResendActivate) {
                handleDeepLink(null);
            }
        }
    }

    public void init() {
        ActivityStateProvider.get().registerActivityLifecycleListener(this);
        ConfigurationProvider.get().addConfigurationListener(this);
    }

    public static DeepLinkProvider get() {
        return DeepLinkProvider.SingleInstance.INSTANCE;
    }

    @Override
    public void onActivityLifecycle(ActivityLifecycleEvent event) {
        if (event.eventType == ON_CREATED || event.eventType == ON_NEW_INTENT) {
            if (checkIsValid(event.getIntent())) {
                Uri data = event.getIntent().getData();
                String urlScheme = ConfigurationProvider.core().getUrlScheme();
                if (urlScheme.equals(data.getScheme()) && WEB_SERVICES_HOST.equals(data.getHost()) && WEB_SERVICES_PATH.equals(data.getPath())) {
                    TrackMainThread.trackMain().postActionToTrackMain(() -> dispatchWebServiceUri(data));
                    return;
                }
                if (lastIntentRef != null && lastIntentRef.get() == event.getIntent()) return;
                lastIntentRef = new WeakReference<>(event.getIntent());
                AdvertResult result = handleDeepLink(data);
                if (result != null && result.hasDealWithDeepLink()) {
                    event.getIntent().setData(null);
                }
            }
        }
        if (event.eventType == ON_RESUMED) {
            if (event.getIntent() != null) {
                handleDeepLink(null);
            }
        }
    }

    private AdvertResult handleDeepLink(Uri data) {
        if (ConfigurationProvider.core().isDataCollectionEnabled()) {
            if (data != null) {
                needResendUri = null;
                return TrackerContext.get().executeData(Activate.deeplink(data), Activate.class, AdvertResult.class);
            } else {
                needResendActivate = false;
                return TrackerContext.get().executeData(Activate.activate(), Activate.class, AdvertResult.class);
            }
        } else {
            if (data != null) {
                needResendUri = data;
            } else {
                needResendActivate = true;
            }
        }
        return null;
    }

    public boolean doDeepLinkByUrl(String url, DeepLinkCallback callback) {
        if (!TrackerContext.initializedSuccessfully() || url == null) {
            return false;
        }
        Uri uri = Uri.parse(url);
        AdvertResult result = TrackerContext.get().executeData(Activate.handleDeeplink(uri, callback), Activate.class, AdvertResult.class);
        if (result == null) {
            Logger.e(TAG, "AdvertModule is null, please register advert component first.");
            return false;
        }
        return result.hasDealWithDeepLink();
    }

    private boolean checkIsValid(Intent intent) {
        if (intent == null) return false;
        return intent.getData() != null && intent.getData().getScheme() != null;
    }

    private static final String WEB_SERVICES_HOST = "growingio";
    private static final String WEB_SERVICES_PATH = "/webservice";
    private static final String WEB_SERVICES_TYPE = "serviceType";

    public static final String SERVICE_DEBUGGER_TYPE = "debugger";
    public static final String SERVICE_CIRCLE_TYPE = "circle";

    void dispatchWebServiceUri(Uri data) {
        String serviceType = data.getQueryParameter(WEB_SERVICES_TYPE);
        if (!TextUtils.isEmpty(serviceType)) {
            Logger.d(TAG, "Start web service " + serviceType);
            Map<String, String> params = new HashMap<>();
            for (String parameterName : data.getQueryParameterNames()) {
                params.put(parameterName, data.getQueryParameter(parameterName));
            }
            if (serviceType.equals(SERVICE_DEBUGGER_TYPE)) {
                TrackerContext.get().loadData(new Debugger(params), Debugger.class, WebService.class, new LoadDataFetcher.DataCallback<WebService>() {
                    @Override
                    public void onDataReady(WebService data) {
                        Logger.d(TAG, "start debugger success");
                    }

                    @Override
                    public void onLoadFailed(Exception e) {
                        Logger.e(TAG, e.getMessage());
                    }
                });
            } else if (serviceType.equals(SERVICE_CIRCLE_TYPE)) {
                TrackerContext.get().loadData(new Circler(params), Circler.class, WebService.class, new LoadDataFetcher.DataCallback<WebService>() {
                    @Override
                    public void onDataReady(WebService data) {
                        Logger.d(TAG, "start circle choose success");
                    }

                    @Override
                    public void onLoadFailed(Exception e) {
                        Logger.e(TAG, e.getMessage());
                    }
                });
            }
        }
    }
}
