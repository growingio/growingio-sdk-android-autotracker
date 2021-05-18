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
import com.growingio.android.sdk.track.listener.event.ActivityLifecycleEvent;
import com.growingio.android.sdk.track.log.DebugLogger;
import com.growingio.android.sdk.track.log.Logger;
import com.growingio.android.sdk.track.modelloader.DataFetcher;
import com.growingio.android.sdk.track.webservices.Circler;
import com.growingio.android.sdk.track.webservices.Debugger;
import com.growingio.android.sdk.track.webservices.DeepLink;
import com.growingio.android.sdk.track.webservices.WebService;


import java.util.HashMap;
import java.util.Map;

import static com.growingio.android.sdk.track.listener.event.ActivityLifecycleEvent.EVENT_TYPE.ON_CREATED;
import static com.growingio.android.sdk.track.listener.event.ActivityLifecycleEvent.EVENT_TYPE.ON_NEW_INTENT;

/**
 * <p>
 * sdk 统一 url scheme 入口
 *
 * @author cpacm 2021/3/25
 */
public class DeepLinkProvider implements IActivityLifecycle {

    private static final String TAG = "DeepLinkProvider";

    private static class SingleInstance {
        private static final DeepLinkProvider INSTANCE = new DeepLinkProvider();
    }

    private DeepLinkProvider() {
    }

    public void init() {
        ActivityStateProvider.get().registerActivityLifecycleListener(this);
    }

    public static DeepLinkProvider get() {
        return DeepLinkProvider.SingleInstance.INSTANCE;
    }

    @Override
    public void onActivityLifecycle(ActivityLifecycleEvent event) {
        if (event.eventType == ON_CREATED || event.eventType == ON_NEW_INTENT) {
            Intent intent = event.getIntent();
            if (intent == null) {
                return;
            }

            Uri data = intent.getData();
            if (data == null) {
                return;
            }
            String urlScheme = ConfigurationProvider.get().getTrackConfiguration().getUrlScheme();

            if (urlScheme.equals(data.getScheme())) {
                Logger.d(TAG, "enter growingio:" + data.toString());
                TrackMainThread.trackMain().postActionToTrackMain(() -> dispatchUri(data));
            }
        }
    }

    private static final String WEB_SERVICES_HOST = "growingio";
    private static final String WEB_SERVICES_PATH = "/webservice";
    private static final String WEB_SERVICES_TYPE = "serviceType";

    public static final String SERVICE_DEBUGGER_TYPE = "debugger";
    public static final String SERVICE_CIRCLE_TYPE = "circle";

    private void dispatchUri(Uri data) {
        if (WEB_SERVICES_HOST.equals(data.getHost()) && WEB_SERVICES_PATH.equals(data.getPath())) {
            String serviceType = data.getQueryParameter(WEB_SERVICES_TYPE);
            if (!TextUtils.isEmpty(serviceType)) {
                Logger.d(TAG, "Start web service " + serviceType);
                Map<String, String> params = new HashMap<>();
                for (String parameterName : data.getQueryParameterNames()) {
                    params.put(parameterName, data.getQueryParameter(parameterName));
                }
                if (serviceType.equals(SERVICE_DEBUGGER_TYPE)) {
                    TrackerContext.get().loadData(new Debugger(params), Debugger.class, WebService.class, new DataFetcher.DataCallback<WebService>() {
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
                    TrackerContext.get().loadData(new Circler(params), Circler.class, WebService.class, new DataFetcher.DataCallback<WebService>() {
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

            String openConsole = data.getQueryParameter("openConsoleLog");
            if (!TextUtils.isEmpty(openConsole)) {
                if ("YES".equalsIgnoreCase(openConsole)) {
                    Logger.addLogger(new DebugLogger());
                }
            }
//                intent.setData(null);
        } else {
            Map<String, String> params = new HashMap<>();
            for (String parameterName : data.getQueryParameterNames()) {
                params.put(parameterName, data.getQueryParameter(parameterName));
            }
            TrackerContext.get().loadData(new DeepLink(params), DeepLink.class, WebService.class, new DataFetcher.DataCallback<WebService>() {
                @Override
                public void onDataReady(WebService data) {
                    Logger.d(TAG, "open deep link.");
                }

                @Override
                public void onLoadFailed(Exception e) {
                    Logger.e(TAG, e.getMessage());
                }
            });
        }
    }
}
