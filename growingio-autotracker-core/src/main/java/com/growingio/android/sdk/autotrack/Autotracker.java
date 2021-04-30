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

import com.growingio.android.sdk.autotrack.change.ViewChangeProvider;
import com.growingio.android.sdk.autotrack.impression.ImpressionProvider;
import com.growingio.android.sdk.autotrack.page.PageProvider;
import com.growingio.android.sdk.autotrack.page.SuperFragment;
import com.growingio.android.sdk.autotrack.view.ViewAttributeUtil;
import com.growingio.android.sdk.autotrack.webservices.circle.CircleService;
import com.growingio.android.sdk.autotrack.webservices.debugger.DebuggerEventWrapper;
import com.growingio.android.sdk.autotrack.webservices.debugger.DebuggerService;
import com.growingio.android.sdk.Tracker;
import com.growingio.android.sdk.track.log.Logger;
import com.growingio.android.sdk.track.providers.ConfigurationProvider;
import com.growingio.android.sdk.track.utils.ThreadUtils;

import java.util.HashMap;
import java.util.Map;

public class Autotracker extends Tracker {
    private static final String TAG = "Autotracker";

    private static volatile boolean sInitializedSuccessfully = false;

    private final ViewChangeProvider mViewChangeProvider;

    public static boolean initializedSuccessfully() {
        return sInitializedSuccessfully;
    }

    Autotracker(Application application, AutotrackConfiguration trackConfiguration) {
        super(application, trackConfiguration);

        ConfigurationProvider.get().addConfiguration(trackConfiguration.clone());
        PageProvider.get().start();
        mViewChangeProvider = new ViewChangeProvider();
        mViewChangeProvider.start();

        mWebServicesProvider.registerService(CircleService.SERVICE_TYPE, CircleService.class);
        mWebServicesProvider.registerService(DebuggerService.SERVICE_TYPE, DebuggerService.class);
        DebuggerEventWrapper.get().observeEventBuild();

        sInitializedSuccessfully = true;
    }

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

    public void trackCustomEvent(String eventName, Activity page) {

    }

    public void trackCustomEvent(String eventName, Fragment page) {

    }

    public void trackCustomEvent(String eventName, android.support.v4.app.Fragment page) {

    }

    public void trackCustomEvent(String eventName, androidx.fragment.app.Fragment page) {

    }

    public void trackCustomEvent(String eventName, Map<String, String> attributes, Activity page) {

    }

    public void trackCustomEvent(String eventName, Map<String, String> attributes, Fragment page) {

    }

    public void trackCustomEvent(String eventName, Map<String, String> attributes, android.support.v4.app.Fragment page) {

    }

    public void trackCustomEvent(String eventName, Map<String, String> attributes, androidx.fragment.app.Fragment page) {

    }

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

    public void setPageAttributes(final android.app.Fragment page, final Map<String, String> attributes) {
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

    public void setPageAttributes(final androidx.fragment.app.Fragment page, final Map<String, String> attributes) {
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

    public void trackViewImpression(View view, String impressionEventName) {
        trackViewImpression(view, impressionEventName, null);
    }

    public void trackViewImpression(final View view, final String impressionEventName, final Map<String, String> attributes) {
        if (view == null || TextUtils.isEmpty(impressionEventName)) {
            Logger.e(TAG, "view or impressionEventName is NULL");
            return;
        }
        final HashMap<String, String> attributesCopy;
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
