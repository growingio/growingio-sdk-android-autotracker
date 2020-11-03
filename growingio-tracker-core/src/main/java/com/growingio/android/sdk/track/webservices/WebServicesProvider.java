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

package com.growingio.android.sdk.track.webservices;

import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;

import com.growingio.android.sdk.track.ContextProvider;
import com.growingio.android.sdk.track.listener.IActivityLifecycle;
import com.growingio.android.sdk.track.listener.event.ActivityLifecycleEvent;
import com.growingio.android.sdk.track.log.Logger;
import com.growingio.android.sdk.track.providers.ActivityStateProvider;
import com.growingio.android.sdk.track.webservices.log.MobileLogService;
import com.growingio.android.sdk.track.webservices.widget.TipView;

import java.util.HashMap;
import java.util.Map;

import static com.growingio.android.sdk.track.listener.event.ActivityLifecycleEvent.EVENT_TYPE.ON_CREATED;
import static com.growingio.android.sdk.track.listener.event.ActivityLifecycleEvent.EVENT_TYPE.ON_NEW_INTENT;
import static com.growingio.android.sdk.track.listener.event.ActivityLifecycleEvent.EVENT_TYPE.ON_PAUSED;
import static com.growingio.android.sdk.track.listener.event.ActivityLifecycleEvent.EVENT_TYPE.ON_RESUMED;

public class WebServicesProvider implements IActivityLifecycle {
    private static final String TAG = "WebServicesProvider";

    private static final String WEB_SERVICES_HOST = "growingio";
    private static final String WEB_SERVICES_PATH = "/webservice";
    private static final String WEB_SERVICES_TYPE = "serviceType";

    private final Map<String, Class<? extends IWebService>> mRegisteredServices = new HashMap<>();
    private IWebService mRunningWebService;

    private TipView mTipView;
    private final String mUrlScheme;

    public WebServicesProvider(String urlScheme, ActivityStateProvider activityStateProvider) {
        mUrlScheme = urlScheme;
        registerService(MobileLogService.SERVICE_TYPE, MobileLogService.class);
        activityStateProvider.registerActivityLifecycleListener(this);
    }

    public void registerService(String type, Class<? extends IWebService> service) {
        mRegisteredServices.put(type, service);
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

            if (mUrlScheme.equals(data.getScheme())
                    && WEB_SERVICES_HOST.equals(data.getHost())
                    && WEB_SERVICES_PATH.equals(data.getPath())) {
                String serviceType = data.getQueryParameter(WEB_SERVICES_TYPE);
                if (!TextUtils.isEmpty(serviceType)) {
                    Logger.d(TAG, "Start web service " + serviceType);
                    Map<String, String> params = new HashMap<>();
                    for (String parameterName : data.getQueryParameterNames()) {
                        params.put(parameterName, data.getQueryParameter(parameterName));
                    }
                    startWebService(serviceType, params);
                }
//                intent.setData(null);
            }
        } else if (event.eventType == ON_RESUMED) {
            if (mTipView != null && !mTipView.isDismissed()) {
                mTipView.show(event.getActivity());
            }
        } else if (event.eventType == ON_PAUSED) {
            if (mTipView != null && !mTipView.isDismissed()) {
                mTipView.remove();
            }
        }
    }

    private void startWebService(String type, Map<String, String> params) {
        if (mRunningWebService != null) {
            mRunningWebService.end();
        }

        Class<? extends IWebService> serviceClass = mRegisteredServices.get(type);
        if (serviceClass != null) {
            try {
                mRunningWebService = serviceClass.newInstance();
                if (mTipView == null) {
                    mTipView = new TipView(ContextProvider.getApplicationContext());
                }
                mRunningWebService.start(params, mTipView);
            } catch (Exception e) {
                Logger.e(TAG, e);
                mRunningWebService = null;
            }
        }
    }
}
