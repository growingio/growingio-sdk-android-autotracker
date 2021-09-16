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
import android.support.annotation.CallSuper;
import android.text.TextUtils;
import android.view.View;

import com.growingio.android.sdk.autotrack.change.ViewChangeProvider;
import com.growingio.android.sdk.autotrack.impression.ImpressionProvider;
import com.growingio.android.sdk.autotrack.page.PageProvider;
import com.growingio.android.sdk.autotrack.page.SuperFragment;
import com.growingio.android.sdk.autotrack.view.ViewAttributeUtil;
import com.growingio.android.sdk.Tracker;
import com.growingio.android.sdk.track.log.Logger;
import com.growingio.android.sdk.track.utils.ThreadUtils;

import java.util.HashMap;
import java.util.Map;

public class Autotracker extends Tracker {
    private static final String TAG = "Autotracker";

    public Autotracker(Application application) {
        super(application);
    }

    @Override
    @CallSuper
    protected void setup(Application application) {
        super.setup(application);
        PageProvider.get().setup();
        ViewChangeProvider mViewChangeProvider;
        mViewChangeProvider = new ViewChangeProvider();
        mViewChangeProvider.setup();
    }

    public void setUniqueTag(final View view, final String tag) {
        if (!isInited) return;
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

    // TODO: 用户cstm事件不关联page，可扩展通过特定API发送的cstm事件支持p字段(通过private隐藏相关方法)
    private void trackCustomEvent(String eventName, Activity page) {

    }

    private void trackCustomEvent(String eventName, Fragment page) {

    }

    private void trackCustomEventSupport(String eventName, android.support.v4.app.Fragment page) {

    }

    private void trackCustomEventX(String eventName, androidx.fragment.app.Fragment page) {

    }

    private void trackCustomEvent(String eventName, Map<String, String> attributes, Activity page) {

    }

    private void trackCustomEvent(String eventName, Map<String, String> attributes, Fragment page) {

    }

    private void trackCustomEventSupport(String eventName, Map<String, String> attributes, android.support.v4.app.Fragment page) {

    }

    private void trackCustomEventX(String eventName, Map<String, String> attributes, androidx.fragment.app.Fragment page) {

    }

    public void setPageAttributes(final Activity page, final Map<String, String> attributes) {
        if (!isInited) return;
        if (page == null || attributes == null || attributes.isEmpty()) {
            Logger.e(TAG, "page or attributes is NULL");
            return;
        }
        ThreadUtils.runOnUiThread(() -> PageProvider.get().setPageAttributes(page, new HashMap<>(attributes)));
    }

    public void setPageAttributes(final android.app.Fragment page, final Map<String, String> attributes) {
        if (!isInited) return;
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

    public void setPageAttributesSupport(final android.support.v4.app.Fragment page, final Map<String, String> attributes) {
        if (!isInited) return;
        if (page == null || attributes == null || attributes.isEmpty()) {
            Logger.e(TAG, "page or attributes is NULL");
            return;
        }

        ThreadUtils.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                PageProvider.get().setPageAttributes(SuperFragment.makeSupport(page), new HashMap<>(attributes));
            }
        });
    }

    public void setPageAttributesX(final androidx.fragment.app.Fragment page, final Map<String, String> attributes) {
        if (!isInited) return;
        if (page == null || attributes == null || attributes.isEmpty()) {
            Logger.e(TAG, "page or attributes is NULL");
            return;
        }

        ThreadUtils.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                PageProvider.get().setPageAttributes(SuperFragment.makeX(page), new HashMap<>(attributes));
            }
        });
    }

    public void trackViewImpression(View view, String impressionEventName) {
        if (!isInited) return;
        trackViewImpression(view, impressionEventName, null);
    }

    public void trackViewImpression(final View view, final String impressionEventName, final Map<String, String> attributes) {
        if (!isInited) return;
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
        ThreadUtils.runOnUiThread(() -> ImpressionProvider.get().trackViewImpression(view, impressionEventName, attributesCopy));
    }

    public void stopTrackViewImpression(final View trackedView) {
        if (!isInited) return;
        if (trackedView == null) {
            Logger.e(TAG, "trackedView is NULL");
            return;
        }

        ThreadUtils.runOnUiThread(() -> ImpressionProvider.get().stopTrackViewImpression(trackedView));
    }

    public void setPageAlias(final Activity page, final String alias) {
        if (!isInited) return;
        if (page == null || TextUtils.isEmpty(alias)) {
            Logger.e(TAG, "activity or alias is NULL");
            return;
        }
        ThreadUtils.runOnUiThread(() -> PageProvider.get().setActivityAlias(page, alias));
    }

    public void setPageAlias(final android.app.Fragment page, final String alias) {
        if (!isInited) return;
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

    public void setPageAliasSupport(final android.support.v4.app.Fragment page, final String alias) {
        if (!isInited) return;
        if (page == null || TextUtils.isEmpty(alias)) {
            Logger.e(TAG, "fragment or alias is NULL");
            return;
        }
        ThreadUtils.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                PageProvider.get().setFragmentAlias(SuperFragment.makeSupport(page), alias);
            }
        });
    }

    public void setPageAliasX(final androidx.fragment.app.Fragment page, final String alias) {
        if (!isInited) return;
        if (page == null || TextUtils.isEmpty(alias)) {
            Logger.e(TAG, "fragment or alias is NULL");
            return;
        }
        ThreadUtils.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                PageProvider.get().setFragmentAlias(SuperFragment.makeX(page), alias);
            }
        });
    }

    public void ignorePage(final Activity page, final IgnorePolicy policy) {
        if (!isInited) return;
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
        if (!isInited) return;
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

    public void ignorePageSupport(final android.support.v4.app.Fragment page, final IgnorePolicy policy) {
        if (!isInited) return;
        if (page == null || policy == null) {
            Logger.e(TAG, "fragment or policy is NULL");
            return;
        }
        ThreadUtils.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                PageProvider.get().addIgnoreFragment(SuperFragment.makeSupport(page), policy);
            }
        });
    }

    public void ignorePageX(final androidx.fragment.app.Fragment page, final IgnorePolicy policy) {
        if (!isInited) return;
        if (page == null || policy == null) {
            Logger.e(TAG, "fragment or policy is NULL");
            return;
        }
        ThreadUtils.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                PageProvider.get().addIgnoreFragment(SuperFragment.makeX(page), policy);
            }
        });
    }

    public void ignoreView(final View view, final IgnorePolicy policy) {
        if (!isInited) return;
        if (view == null || policy == null) {
            Logger.e(TAG, "view or policy is NULL");
            return;
        }
        ThreadUtils.runOnUiThread(() -> ViewAttributeUtil.setIgnorePolicy(view, policy));
    }
}
