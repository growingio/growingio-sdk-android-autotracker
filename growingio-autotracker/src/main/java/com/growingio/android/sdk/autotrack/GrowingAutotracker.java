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
import android.text.TextUtils;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.growingio.android.sdk.autotrack.change.ViewChangeProvider;
import com.growingio.android.sdk.autotrack.impression.ImpressionProvider;
import com.growingio.android.sdk.autotrack.page.PageProvider;
import com.growingio.android.sdk.autotrack.page.SuperFragment;
import com.growingio.android.sdk.autotrack.view.ViewAttributeUtil;
import com.growingio.android.sdk.track.GrowingTracker;
import com.growingio.android.sdk.track.interfaces.InitExtraOperation;
import com.growingio.android.sdk.track.interfaces.ResultCallback;
import com.growingio.android.sdk.track.providers.ConfigurationProvider;
import com.growingio.android.sdk.track.utils.LogUtil;
import com.growingio.android.sdk.track.utils.ThreadUtils;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;

public class GrowingAutotracker implements IGrowingAutotracker {
    private static final String TAG = "GIO.Autotrack";

    private static GrowingAutotracker sInstance;
    private static volatile boolean sInitializedSuccessfully = false;

    @NonNull
    public static IGrowingAutotracker getInstance() {
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

    public static boolean initializedSuccessfully() {
        return sInitializedSuccessfully;
    }

    public static IGrowingAutotracker startWithConfiguration(Application application, final AutotrackConfiguration autotrackConfiguration) {
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
        if (sInitializedSuccessfully) {
            return sInstance;
        } else {
            return makeEmpty();
        }
    }

    private static IGrowingAutotracker makeEmpty() {
        return (IGrowingAutotracker) Proxy.newProxyInstance(GrowingAutotracker.class.getClassLoader(),
                new Class[]{IGrowingAutotracker.class}, new InvocationHandler() {
                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                        if (method.getReturnType() == IGrowingAutotracker.class) {
                            return proxy;
                        }
                        if (method.getReturnType() == boolean.class) {
                            return false;
                        }
                        return null;
                    }
                });
    }

    private static void initAutotrackSDKInUI(AutotrackConfiguration autotrackConfiguration) {
        ConfigurationProvider.get().addConfiguration(autotrackConfiguration.clone());
        GrowingAutotracker autotrack = new GrowingAutotracker();
        PageProvider.get().start();
        ViewChangeProvider.get().start();

        sInstance = autotrack;
        LogUtil.d(TAG, "Autotracker module init success in ui thread");
    }

    @Override
    public IGrowingAutotracker trackCustomEvent(String eventName, Map<String, String> attributes) {
        GrowingTracker.getInstance().trackCustomEvent(eventName, attributes);
        return this;
    }

    @Override
    public IGrowingAutotracker setConversionVariables(Map<String, String> variables) {
        GrowingTracker.getInstance().setConversionVariables(variables);
        return this;
    }

    @Override
    public IGrowingAutotracker setLoginUserAttributes(Map<String, String> attributes) {
        GrowingTracker.getInstance().setLoginUserAttributes(attributes);
        return this;
    }

    @Override
    public IGrowingAutotracker setVisitorAttributes(Map<String, String> attributes) {
        GrowingTracker.getInstance().setVisitorAttributes(attributes);
        return this;
    }

    @Override
    public IGrowingAutotracker getDeviceId(@Nullable ResultCallback<String> callback) {
        GrowingTracker.getInstance().getDeviceId(callback);
        return this;
    }

    @Override
    public IGrowingAutotracker setDataCollectionEnabled(boolean enabled) {
        GrowingTracker.getInstance().setDataCollectionEnabled(enabled);
        return this;
    }

    @Override
    public IGrowingAutotracker setLoginUserId(String userId) {
        GrowingTracker.getInstance().setLoginUserId(userId);
        return this;
    }

    @Override
    public IGrowingAutotracker cleanLoginUserId() {
        GrowingTracker.getInstance().cleanLoginUserId();
        return this;
    }

    @Override
    public IGrowingAutotracker setLocation(double latitude, double longitude) {
        GrowingTracker.getInstance().setLocation(latitude, longitude);
        return this;
    }

    @Override
    public IGrowingAutotracker cleanLocation() {
        GrowingTracker.getInstance().cleanLocation();
        return this;
    }

    @Override
    public IGrowingAutotracker setUniqueTag(final View view, final String tag) {
        return this;
    }

    @Override
    public IGrowingAutotracker setPageAttributes(final Activity activity, final Map<String, String> attributes) {
        PageProvider.get().setPageAttributes(activity, attributes);
        return this;
    }

    @Override
    public IGrowingAutotracker setPageAttributes(android.app.Fragment fragment, Map<String, String> attributes) {
        PageProvider.get().setPageAttributes(SuperFragment.make(fragment), attributes);
        return this;
    }

    @Override
    public IGrowingAutotracker setPageAttributes(final android.support.v4.app.Fragment fragment, final Map<String, String> attributes) {
        PageProvider.get().setPageAttributes(SuperFragment.make(fragment), attributes);
        return this;
    }

    @Override
    public IGrowingAutotracker setPageAttributes(androidx.fragment.app.Fragment fragment, Map<String, String> attributes) {
        PageProvider.get().setPageAttributes(SuperFragment.make(fragment), attributes);
        return this;
    }

    @Override
    public IGrowingAutotracker trackViewImpression(View view, String impressionEventName) {
        trackViewImpression(view, impressionEventName, null);
        return this;
    }

    @Override
    public IGrowingAutotracker trackViewImpression(final View view, final String impressionEventName, final Map<String, String> attributes) {
        if (view == null || TextUtils.isEmpty(impressionEventName)) {
            LogUtil.e(TAG, "view or impressionEventName is NULL");
            return this;
        }
        ThreadUtils.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ImpressionProvider.get().trackViewImpression(view, impressionEventName, attributes);
            }
        });
        return this;
    }

    @Override
    public IGrowingAutotracker stopTrackViewImpression(final View trackedView) {
        if (trackedView == null) {
            LogUtil.e(TAG, "trackedView is NULL");
            return this;
        }

        ThreadUtils.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ImpressionProvider.get().stopTrackViewImpression(trackedView);
            }
        });
        return this;
    }

    @Override
    public IGrowingAutotracker setPageAlias(final Activity activity, final String alias) {
        if (activity == null || TextUtils.isEmpty(alias)) {
            LogUtil.e(TAG, "activity or alias is NULL");
            return this;
        }
        ThreadUtils.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                PageProvider.get().setActivityAlias(activity, alias);
            }
        });
        return this;
    }

    @Override
    public IGrowingAutotracker setPageAlias(final android.app.Fragment fragment, final String alias) {
        if (fragment == null || TextUtils.isEmpty(alias)) {
            LogUtil.e(TAG, "fragment or alias is NULL");
            return this;
        }
        ThreadUtils.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                PageProvider.get().setFragmentAlias(SuperFragment.make(fragment), alias);
            }
        });
        return this;
    }

    @Override
    public IGrowingAutotracker setPageAlias(final android.support.v4.app.Fragment fragment, final String alias) {
        if (fragment == null || TextUtils.isEmpty(alias)) {
            LogUtil.e(TAG, "fragment or alias is NULL");
            return this;
        }
        ThreadUtils.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                PageProvider.get().setFragmentAlias(SuperFragment.make(fragment), alias);
            }
        });
        return this;
    }

    @Override
    public IGrowingAutotracker setPageAlias(final androidx.fragment.app.Fragment fragment, final String alias) {
        if (fragment == null || TextUtils.isEmpty(alias)) {
            LogUtil.e(TAG, "fragment or alias is NULL");
            return this;
        }
        ThreadUtils.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                PageProvider.get().setFragmentAlias(SuperFragment.make(fragment), alias);
            }
        });
        return this;
    }

    @Override
    public IGrowingAutotracker ignorePage(final Activity activity, final IgnorePolicy policy) {
        if (activity == null || policy == null) {
            LogUtil.e(TAG, "activity or policy is NULL");
            return this;
        }
        ThreadUtils.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                PageProvider.get().addIgnoreActivity(activity, policy);
            }
        });
        return this;
    }

    @Override
    public IGrowingAutotracker ignorePage(final android.app.Fragment fragment, final IgnorePolicy policy) {
        if (fragment == null || policy == null) {
            LogUtil.e(TAG, "fragment or policy is NULL");
            return this;
        }
        ThreadUtils.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                PageProvider.get().addIgnoreFragment(SuperFragment.make(fragment), policy);
            }
        });
        return this;
    }

    @Override
    public IGrowingAutotracker ignorePage(final android.support.v4.app.Fragment fragment, final IgnorePolicy policy) {
        if (fragment == null || policy == null) {
            LogUtil.e(TAG, "fragment or policy is NULL");
            return this;
        }
        ThreadUtils.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                PageProvider.get().addIgnoreFragment(SuperFragment.make(fragment), policy);
            }
        });
        return this;
    }

    @Override
    public IGrowingAutotracker ignorePage(final androidx.fragment.app.Fragment fragment, final IgnorePolicy policy) {
        if (fragment == null || policy == null) {
            LogUtil.e(TAG, "fragment or policy is NULL");
            return this;
        }
        ThreadUtils.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                PageProvider.get().addIgnoreFragment(SuperFragment.make(fragment), policy);
            }
        });
        return this;
    }

    @Override
    public IGrowingAutotracker ignoreView(final View view, final IgnorePolicy policy) {
        if (view == null || policy == null) {
            LogUtil.e(TAG, "view or policy is NULL");
            return this;
        }

        ThreadUtils.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ViewAttributeUtil.setIgnorePolicy(view, policy);
            }
        });
        return this;
    }

}
