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
import android.support.v4.app.Fragment;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.growingio.android.sdk.autotrack.page.PageProvider;
import com.growingio.android.sdk.autotrack.page.SuperFragment;
import com.growingio.android.sdk.track.GrowingTracker;
import com.growingio.android.sdk.track.ListenerContainer;
import com.growingio.android.sdk.track.interfaces.InitExtraOperation;
import com.growingio.android.sdk.track.interfaces.ResultCallback;
import com.growingio.android.sdk.track.utils.LogUtil;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;

public class GrowingAutotracker implements IGrowingAutotracker {
    private static final String TAG = "GIO.Autotrack";

    private static GrowingAutotracker sInstance;
    private static boolean sInitSuccess = false;

    AutotrackAppState mAutotrackAppState;

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

    public static IGrowingAutotracker startWithConfiguration(Application application, AutotrackConfiguration autotrackConfiguration) {
        GrowingTracker.startWithConfiguration(application, autotrackConfiguration, new InitExtraOperation() {
            @Override
            public void initializing() {
                initAutotrackSDKInUI();
            }

            @Override
            public void initSuccess() {
                sInitSuccess = true;
            }
        });
        if (sInitSuccess) {
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

    private static void initAutotrackSDKInUI() {
        GrowingAutotracker autotrack = new GrowingAutotracker();
        autotrack.mAutotrackAppState = new AutotrackAppState();
        ListenerContainer.gioMainInitSDKListeners().register(autotrack.mAutotrackAppState);
        PageProvider.get().start();

        sInstance = autotrack;
        LogUtil.d(TAG, "Autotrackrt module init success in ui thread");
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
    public IGrowingAutotracker setPageAttributes(final Fragment fragment, final Map<String, String> attributes) {
        PageProvider.get().setPageAttributes(SuperFragment.make(fragment), attributes);
        return this;
    }

    @Override
    public IGrowingAutotracker trackViewImpression(final ImpressionConfig config) {
        return this;
    }

    @Override
    public IGrowingAutotracker stopTrackViewImpression(final View trackedView) {
        return this;
    }

    @Override
    public IGrowingAutotracker setPageAlias(final Activity activity, final String alias) {
        PageProvider.get().setActivityAlias(activity, alias);
        return this;
    }

    @Override
    public IGrowingAutotracker setPageAlias(final Fragment fragment, final String alias) {
        PageProvider.get().setFragmentAlias(SuperFragment.make(fragment), alias);
        return this;
    }

    @Override
    public IGrowingAutotracker ignorePage(final Activity activity, final IgnorePolicy policy) {
        return this;
    }

    @Override
    public IGrowingAutotracker ignorePage(final Fragment fragment, final IgnorePolicy policy) {
        return this;
    }

    @Override
    public IGrowingAutotracker ignoreView(final View view, final IgnorePolicy policy) {
        return this;
    }

}
