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
import com.growingio.android.sdk.autotrack.util.ViewAttributeUtil;
import com.growingio.android.sdk.autotrack.variation.AutotrackEventJsonMarshaller;
import com.growingio.android.sdk.track.GConfig;
import com.growingio.android.sdk.track.GInternal;
import com.growingio.android.sdk.track.GrowingTracker;
import com.growingio.android.sdk.track.ListenerContainer;
import com.growingio.android.sdk.track.interfaces.IActionCallback;
import com.growingio.android.sdk.track.interfaces.InitExtraOperation;
import com.growingio.android.sdk.track.interfaces.ResultCallback;
import com.growingio.android.sdk.track.providers.EventSenderProvider;
import com.growingio.android.sdk.track.utils.ArgumentChecker;
import com.growingio.android.sdk.track.utils.LogUtil;
import com.growingio.android.sdk.track.variation.EventHttpSender;
import com.growingio.android.sdk.autotrack.webservices.ScreenshotProvider;

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
            public boolean requireWaitForCompletion() {
                return true;
            }

            @Override
            public void init() {
                initAutotrackSDKInUI();
            }

            @Override
            public void initSuccess() {
                sInitSuccess = true;
            }
        });

        ImpressionConfig.setVisibleScale(autotrackConfiguration.getImpressionScale());

        ScreenshotProvider.ScreenshotPolicy.get();
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
        GConfig.getInstance().checkVersion(GrowingAutotracker.class.getSimpleName(),
                BuildConfig.VERSION_NAME);

        GrowingAutotracker autotrack = new GrowingAutotracker();
        autotrack.mAutotrackAppState = new AutotrackAppState();
        ListenerContainer.gioMainInitSDKListeners().register(autotrack.mAutotrackAppState);
        EventSenderProvider.EventSenderPolicy.get().registerEventSender(new EventHttpSender(new AutotrackEventJsonMarshaller()));
        PageProvider.PagePolicy.get().start();

        sInstance = autotrack;
        GConfig.getInstance().setInitSucceeded(true);
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
    public IGrowingAutotracker setLocation(Double latitude, Double longitude) {
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
        GInternal.getInstance().getMainThread().postActionToGMain(new IActionCallback() {
            @Override
            public void action() {
                ViewAttributeUtil.setCustomId(view, tag);
            }
        });
        return this;
    }

    @Override
    public IGrowingAutotracker setPageAttributes(final Activity activity, final Map<String, String> attributes) {
        PageProvider.PagePolicy.get().setPageAttributes(activity, attributes);
        return this;
    }

    @Override
    public IGrowingAutotracker setPageAttributes(final Fragment fragment, final Map<String, String> attributes) {
        PageProvider.PagePolicy.get().setPageAttributes(SuperFragment.make(fragment), attributes);
        return this;
    }

    @Override
    public IGrowingAutotracker trackViewImpression(final ImpressionConfig config) {
        GInternal.getInstance().getMainThread().postActionToGMain(new IActionCallback() {
            @Override
            public void action() {
                if (ArgumentChecker.isIllegalEventName(config.getEventName())) {
                    return;
                }
                AutotrackAppState.impObserver().markViewImpression(config);
            }
        });

        return this;
    }

    @Override
    public IGrowingAutotracker stopTrackViewImpression(final View trackedView) {
        GInternal.getInstance().getMainThread().postActionToGMain(new IActionCallback() {
            @Override
            public void action() {
                AutotrackAppState.impObserver().stopStampViewImp(trackedView);
            }
        });

        return this;
    }

    @Override
    public IGrowingAutotracker setPageAlias(final Activity activity, final String alias) {
        PageProvider.PagePolicy.get().setActivityAlias(activity, alias);
        return this;
    }

    @Override
    public IGrowingAutotracker setPageAlias(final Fragment fragment, final String alias) {
        PageProvider.PagePolicy.get().setFragmentAlias(SuperFragment.make(fragment), alias);
        return this;
    }

    @Override
    public IGrowingAutotracker ignorePage(final Activity activity, final IgnorePolicy policy) {
        PageProvider.PagePolicy.get().addActivityWithIgnorePolicy(activity, policy);
        return this;
    }

    @Override
    public IGrowingAutotracker ignorePage(final Fragment fragment, final IgnorePolicy policy) {
        PageProvider.PagePolicy.get().addFragmentWithIgnorePolicy(SuperFragment.make(fragment), policy);
        return this;
    }

    @Override
    public IGrowingAutotracker ignoreView(final View view, final IgnorePolicy policy) {

        GInternal.getInstance().getMainThread().postActionToGMain(new IActionCallback() {
            @Override
            public void action() {
                ViewAttributeUtil.setIgnoreViewKey(view, policy);
            }
        });
        return this;
    }

}
