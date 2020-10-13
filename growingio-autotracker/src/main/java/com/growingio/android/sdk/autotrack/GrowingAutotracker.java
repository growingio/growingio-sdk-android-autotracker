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

package com.growingio.android.sdk.autotrack;

import android.app.Activity;
import android.app.Application;
import android.app.Fragment;
import android.text.TextUtils;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.growingio.android.sdk.autotrack.change.ViewChangeProvider;
import com.growingio.android.sdk.autotrack.impression.ImpressionProvider;
import com.growingio.android.sdk.autotrack.page.PageProvider;
import com.growingio.android.sdk.autotrack.page.SuperFragment;
import com.growingio.android.sdk.autotrack.view.ViewAttributeUtil;
import com.growingio.android.sdk.autotrack.webservices.circle.CircleService;
import com.growingio.android.sdk.track.GrowingTracker;
import com.growingio.android.sdk.track.interfaces.InitExtraOperation;
import com.growingio.android.sdk.track.interfaces.ResultCallback;
import com.growingio.android.sdk.track.log.Logger;
import com.growingio.android.sdk.track.providers.ConfigurationProvider;
import com.growingio.android.sdk.track.utils.ThreadUtils;
import com.growingio.android.sdk.track.webservices.WebServicesProvider;

import java.util.HashMap;
import java.util.Map;

public class GrowingAutotracker implements IGrowingAutotracker {
    private static final String TAG = "GrowingAutotracker";

    private static volatile GrowingAutotracker sInstance;
    private static volatile boolean sInitializedSuccessfully = false;

    @NonNull
    public static IGrowingAutotracker get() {
        if (sInstance != null) {
            return sInstance;
        }
        synchronized (IGrowingAutotracker.class) {
            if (sInstance != null) {
                return sInstance;
            }
            return makeEmpty();
        }
    }

    public static boolean initializedSuccessfully() {
        return sInitializedSuccessfully;
    }

    public static void startWithConfiguration(Application application, final AutotrackConfiguration autotrackConfiguration) {
        GrowingTracker.startWithConfiguration(application, autotrackConfiguration, new InitExtraOperation() {
            @Override
            public void initializing() {
                initAutotrackSDKInUI(autotrackConfiguration);
            }

            @Override
            public void initSuccess() {
                sInitializedSuccessfully = true;
            }
        });
    }

    private static IGrowingAutotracker makeEmpty() {
        Logger.e(TAG, "GrowingAutotracker is UNINITIALIZED, please initialized before use API");
        return EmptyGrowingAutotracker.INSTANCE;
    }

    private static void initAutotrackSDKInUI(AutotrackConfiguration autotrackConfiguration) {
        ConfigurationProvider.get().addConfiguration(autotrackConfiguration.clone());
        GrowingAutotracker autotrack = new GrowingAutotracker();
        PageProvider.get().start();
        ViewChangeProvider.get().start();
        WebServicesProvider.get().registerService(CircleService.SERVICE_TYPE, CircleService.class);
        sInstance = autotrack;
        Logger.d(TAG, "Autotracker module init success in ui thread");
    }

    @Override
    public void trackCustomEvent(String eventName) {
        GrowingTracker.get().trackCustomEvent(eventName);
    }

    @Override
    public void trackCustomEvent(String eventName, Map<String, String> attributes) {
        GrowingTracker.get().trackCustomEvent(eventName, attributes);
    }

    @Override
    public void setConversionVariables(Map<String, String> variables) {
        GrowingTracker.get().setConversionVariables(variables);
    }

    @Override
    public void setLoginUserAttributes(Map<String, String> attributes) {
        GrowingTracker.get().setLoginUserAttributes(attributes);
    }

    @Override
    public void setVisitorAttributes(Map<String, String> attributes) {
        GrowingTracker.get().setVisitorAttributes(attributes);
    }

    @Override
    public void getDeviceId(@Nullable ResultCallback<String> callback) {
        GrowingTracker.get().getDeviceId(callback);
    }

    @Override
    public void setDataCollectionEnabled(boolean enabled) {
        GrowingTracker.get().setDataCollectionEnabled(enabled);
    }

    @Override
    public void setLoginUserId(String userId) {
        GrowingTracker.get().setLoginUserId(userId);
    }

    @Override
    public void cleanLoginUserId() {
        GrowingTracker.get().cleanLoginUserId();
    }

    @Override
    public void setLocation(double latitude, double longitude) {
        GrowingTracker.get().setLocation(latitude, longitude);
    }

    @Override
    public void cleanLocation() {
        GrowingTracker.get().cleanLocation();
    }

    @Override
    public void setUniqueTag(final View view, final String tag) {
        if (view == null || TextUtils.isEmpty(tag)) {
            Logger.e(TAG, "setUniqueTag: view or tag is NULL");
            return;
        }

        ThreadUtils.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ViewAttributeUtil.setCustomId(view, tag);
            }
        });
    }

    @Override
    public void trackCustomEvent(String eventName, Activity page) {

    }

    @Override
    public void trackCustomEvent(String eventName, Fragment page) {

    }

    @Override
    public void trackCustomEvent(String eventName, android.support.v4.app.Fragment page) {

    }

    @Override
    public void trackCustomEvent(String eventName, androidx.fragment.app.Fragment page) {

    }

    @Override
    public void trackCustomEvent(String eventName, Map<String, String> attributes, Activity page) {

    }

    @Override
    public void trackCustomEvent(String eventName, Map<String, String> attributes, Fragment page) {

    }

    @Override
    public void trackCustomEvent(String eventName, Map<String, String> attributes, android.support.v4.app.Fragment page) {

    }

    @Override
    public void trackCustomEvent(String eventName, Map<String, String> attributes, androidx.fragment.app.Fragment page) {

    }

    @Override
    public void setPageAttributes(final Activity page, final Map<String, String> attributes) {
        if (page == null || attributes == null || attributes.isEmpty()) {
            Logger.e(TAG, "page or attributes is NULL");
            return;
        }

        ThreadUtils.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                PageProvider.get().setPageAttributes(page, new HashMap<>(attributes));
            }
        });
    }

    @Override
    public void setPageAttributes(android.app.Fragment page, Map<String, String> attributes) {
        if (page == null || attributes == null || attributes.isEmpty()) {
            Logger.e(TAG, "page or attributes is NULL");
            return;
        }

        ThreadUtils.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                PageProvider.get().setPageAttributes(SuperFragment.make(page), new HashMap<>(attributes));
            }
        });
    }

    @Override
    public void setPageAttributes(final android.support.v4.app.Fragment page, final Map<String, String> attributes) {
        if (page == null || attributes == null || attributes.isEmpty()) {
            Logger.e(TAG, "page or attributes is NULL");
            return;
        }

        ThreadUtils.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                PageProvider.get().setPageAttributes(SuperFragment.make(page), new HashMap<>(attributes));
            }
        });
    }

    @Override
    public void setPageAttributes(androidx.fragment.app.Fragment page, Map<String, String> attributes) {
        if (page == null || attributes == null || attributes.isEmpty()) {
            Logger.e(TAG, "page or attributes is NULL");
            return;
        }

        ThreadUtils.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                PageProvider.get().setPageAttributes(SuperFragment.make(page), new HashMap<>(attributes));
            }
        });
    }

    @Override
    public void trackViewImpression(View view, String impressionEventName) {
        trackViewImpression(view, impressionEventName, null);
    }

    @Override
    public void trackViewImpression(final View view, final String impressionEventName, final Map<String, String> attributes) {
        if (view == null || TextUtils.isEmpty(impressionEventName)) {
            Logger.e(TAG, "view or impressionEventName is NULL");
            return;
        }
        HashMap<String, String> attributesCopy;
        if (attributes == null) {
            attributesCopy = null;
        } else {
            attributesCopy = new HashMap<>(attributes);
        }
        ThreadUtils.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ImpressionProvider.get().trackViewImpression(view, impressionEventName, attributesCopy);
            }
        });
    }

    @Override
    public void stopTrackViewImpression(final View trackedView) {
        if (trackedView == null) {
            Logger.e(TAG, "trackedView is NULL");
            return;
        }

        ThreadUtils.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ImpressionProvider.get().stopTrackViewImpression(trackedView);
            }
        });
    }

    @Override
    public void setPageAlias(final Activity page, final String alias) {
        if (page == null || TextUtils.isEmpty(alias)) {
            Logger.e(TAG, "activity or alias is NULL");
            return;
        }
        ThreadUtils.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                PageProvider.get().setActivityAlias(page, alias);
            }
        });
    }

    @Override
    public void setPageAlias(final android.app.Fragment page, final String alias) {
        if (page == null || TextUtils.isEmpty(alias)) {
            Logger.e(TAG, "fragment or alias is NULL");
            return;
        }
        ThreadUtils.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                PageProvider.get().setFragmentAlias(SuperFragment.make(page), alias);
            }
        });
    }

    @Override
    public void setPageAlias(final android.support.v4.app.Fragment page, final String alias) {
        if (page == null || TextUtils.isEmpty(alias)) {
            Logger.e(TAG, "fragment or alias is NULL");
            return;
        }
        ThreadUtils.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                PageProvider.get().setFragmentAlias(SuperFragment.make(page), alias);
            }
        });
    }

    @Override
    public void setPageAlias(final androidx.fragment.app.Fragment page, final String alias) {
        if (page == null || TextUtils.isEmpty(alias)) {
            Logger.e(TAG, "fragment or alias is NULL");
            return;
        }
        ThreadUtils.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                PageProvider.get().setFragmentAlias(SuperFragment.make(page), alias);
            }
        });
    }

    @Override
    public void ignorePage(final Activity page, final IgnorePolicy policy) {
        if (page == null || policy == null) {
            Logger.e(TAG, "activity or policy is NULL");
            return;
        }
        ThreadUtils.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                PageProvider.get().addIgnoreActivity(page, policy);
            }
        });
    }

    @Override
    public void ignorePage(final android.app.Fragment page, final IgnorePolicy policy) {
        if (page == null || policy == null) {
            Logger.e(TAG, "fragment or policy is NULL");
            return;
        }
        ThreadUtils.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                PageProvider.get().addIgnoreFragment(SuperFragment.make(page), policy);
            }
        });
    }

    @Override
    public void ignorePage(final android.support.v4.app.Fragment page, final IgnorePolicy policy) {
        if (page == null || policy == null) {
            Logger.e(TAG, "fragment or policy is NULL");
            return;
        }
        ThreadUtils.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                PageProvider.get().addIgnoreFragment(SuperFragment.make(page), policy);
            }
        });
    }

    @Override
    public void ignorePage(final androidx.fragment.app.Fragment page, final IgnorePolicy policy) {
        if (page == null || policy == null) {
            Logger.e(TAG, "fragment or policy is NULL");
            return;
        }
        ThreadUtils.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                PageProvider.get().addIgnoreFragment(SuperFragment.make(page), policy);
            }
        });
    }

    @Override
    public void ignoreView(final View view, final IgnorePolicy policy) {
        if (view == null || policy == null) {
            Logger.e(TAG, "view or policy is NULL");
            return;
        }

        ThreadUtils.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ViewAttributeUtil.setIgnorePolicy(view, policy);
            }
        });
    }

}
