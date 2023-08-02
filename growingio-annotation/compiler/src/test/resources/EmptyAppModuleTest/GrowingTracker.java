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
package com.growingio.android.sdk.test;

import android.content.Context;

import com.growingio.android.sdk.Tracker;
import com.growingio.android.sdk.track.SDKConfig;
import com.growingio.android.sdk.track.log.Logger;
import com.growingio.android.sdk.track.providers.TrackerLifecycleProviderFactory;

/**
 * The entry point for interacting with GrowingIO Tracker for Applications
 * <p>Includes all generated APIs from Tracker in source.
 *
 * <p>This class is generated and should not be modified
 */
public final class GrowingTracker {
    private static final String TAG = "GrowingTracker";

    private static volatile Tracker _gioTracker;

    public static Tracker get() {
        if (_gioTracker == null) {
            Logger.e(TAG, "Tracker is UNINITIALIZED, please initialized before use API");
            return empty();
        }
        synchronized (GrowingTracker.class) {
            if (_gioTracker != null) {
                return _gioTracker;
            }
            Logger.e(TAG, "Tracker is UNINITIALIZED, please initialized before use API");
            return empty();
        }
    }

    public static void start(Context context) {
        if (_gioTracker != null) {
            Logger.e(TAG, "Tracker is running");
            return;
        }
        throw new IllegalStateException("If you want use start() method, you must define ProjectId and UrlScheme in @GIOTracker annotation");
    }

    public static void startWithConfiguration(Context context,
                                              TestTrackConfiguration trackConfiguration) {
        if (_gioTracker != null) {
            Logger.e(TAG, "Tracker is running");
            return;
        }
        EmptyAppGioModule appModule = new EmptyAppGioModule();
        appModule.config(trackConfiguration);
        TrackerLifecycleProviderFactory.create().createConfigurationProviderWithConfig(trackConfiguration.core(), trackConfiguration.getConfigModules());
        _gioTracker = new Tracker(context);
        initSuccess(_gioTracker.getContext().getConfigurationProvider().printAllConfigurationInfo());
    }

    private static Tracker empty() {
        return new Tracker(null);
    }

    public static void shutdown() {
        if (_gioTracker != null) {
            _gioTracker.shutdown();
        }
        _gioTracker = null;
    }

    private static void initSuccess(String message) {
        Logger.i(TAG, "!!! Thank you very much for using GrowingIO. We will do our best to provide you with the best service. !!!");
        Logger.i(TAG, "!!! GrowingIO Tracker version: " + SDKConfig.SDK_VERSION + " !!!");
        Logger.d(TAG, message);
    }
}