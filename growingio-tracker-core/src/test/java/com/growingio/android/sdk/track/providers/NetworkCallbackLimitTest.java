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
package com.growingio.android.sdk.track.providers;

import android.app.Application;
import android.net.ConnectivityManager;
import android.net.NetworkRequest;

import androidx.test.core.app.ApplicationProvider;

import com.google.common.truth.Truth;
import com.growingio.android.sdk.CoreConfiguration;
import com.growingio.android.sdk.Tracker;
import com.growingio.android.sdk.TrackerContext;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.shadows.ShadowConnectivityManager;

import java.util.HashMap;

/**
 * When the per-UID limit of outstanding network requests is exceeded, registerNetworkCallback throws
 * (ConnectivityManager$TooManyRequestsException on Android 11+, RuntimeException on older versions).
 * SDK initialization should still succeed, since the callback is only used to flush events sooner.
 */
@Config(manifest = Config.NONE, shadows = {NetworkCallbackLimitTest.ShadowLimitedConnectivityManager.class})
@RunWith(RobolectricTestRunner.class)
public class NetworkCallbackLimitTest {

    private final Application application = ApplicationProvider.getApplicationContext();

    @Test
    public void trackerInitWhenNetworkCallbackLimitExceeded() {
        TrackerLifecycleProviderFactory.create().createConfigurationProviderWithConfig(
                new CoreConfiguration("NetworkCallbackLimitTest", "growingio://network"), new HashMap<>());

        // must not throw
        Tracker tracker = new Tracker(application);
        TrackerContext context = tracker.getContext();

        Truth.assertThat(context).isNotNull();
        Truth.assertThat(TrackerContext.initializedSuccessfully()).isTrue();

        // must not try to unregister a callback that was never registered
        context.getActivityStateProvider().shutdown();
    }

    @Implements(ConnectivityManager.class)
    public static class ShadowLimitedConnectivityManager extends ShadowConnectivityManager {
        @Implementation
        @Override
        protected void registerNetworkCallback(NetworkRequest request, ConnectivityManager.NetworkCallback networkCallback) {
            throw new RuntimeException("Too many NetworkRequests filed");
        }
    }
}
