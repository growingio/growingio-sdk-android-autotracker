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

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.test.core.app.ApplicationProvider;

import com.google.common.truth.Truth;
import com.google.common.util.concurrent.Uninterruptibles;
import com.growingio.android.sdk.Configurable;
import com.growingio.android.sdk.CoreConfiguration;
import com.growingio.android.sdk.Tracker;
import com.growingio.android.sdk.TrackerContext;
import com.growingio.android.sdk.track.listener.IActivityLifecycle;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.Config;

import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

@Config(manifest = Config.NONE)
@RunWith(RobolectricTestRunner.class)
public class ProviderTest {

    private final Application application = ApplicationProvider.getApplicationContext();

    private TrackerContext context;

    @Before
    public void setup() {
        TrackerLifecycleProviderFactory.create().createConfigurationProviderWithConfig(new CoreConfiguration("ProviderTest", "growingio://provider"), new HashMap<>());
        Tracker tracker = new Tracker(application);
        context = tracker.getContext();
    }

    @Test
    public void activityStateProvider() {
        ActivityStateProvider activityStateProvider = context.getActivityStateProvider();
        ActivityController<RobolectricActivity> activityController = Robolectric.buildActivity(RobolectricActivity.class);
        RobolectricActivity activity = activityController.get();

        Bundle testBundle = new Bundle();

        IActivityLifecycle iActivityLifecycle = event -> {
            Truth.assertThat(event.getActivity()).isEqualTo(activity);
            Truth.assertThat(event.eventType).isEqualTo(activity.state);
            if (event.getBundle() != null) {
                Truth.assertThat(event.getBundle()).isEqualTo(testBundle);
            }
            if (event.getIntent() != null) {
                Truth.assertThat(event.getIntent()).isEqualTo(activity.getIntent());
            }
        };
        activityStateProvider.register(null);
        activityStateProvider.register(iActivityLifecycle);
        activityStateProvider.register(null);

        activityController.create(testBundle);
        activityController.resume();
        activityController.pause();
        activityController.stop();
        activityController.destroy();
        activityStateProvider.shutdown();
    }

    @Test
    public void appInfoProvider() {
        AppInfoProvider appInfoProvider = context.getProvider(AppInfoProvider.class);
        Truth.assertThat(appInfoProvider.getPackageName()).isEqualTo(application.getPackageName());
        Truth.assertThat(appInfoProvider.getAppName()).isEqualTo(application.getPackageManager().getApplicationLabel(application.getApplicationInfo()).toString());
        Truth.assertThat(appInfoProvider.getAppVersion()).isNull();
    }

    @Test
    public void configProvider() {
        ConfigurationProvider configurationProvider = context.getConfigurationProvider();
        Truth.assertThat(configurationProvider.core().getUrlScheme()).isEqualTo("growingio://provider");
        TestConfigurable testConfigurable = new TestConfigurable();
        configurationProvider.addConfiguration(testConfigurable);
        Truth.assertThat((TestConfigurable) configurationProvider.getConfiguration(TestConfigurable.class)).isEqualTo(testConfigurable);
    }

    public static class TestConfigurable implements Configurable {
    }

    @Test
    public void deeplinkProvider() {
        // DeepLinkProvider deepLinkProvider = context.getProvider(DeepLinkProvider.class);

        //empty
        Robolectric.buildActivity(RobolectricActivity.class).create().get();

        //debugger
        Intent intent = new Intent();
        String debuggerUri = "UNKNOWN://growingio/webservice?serviceType=debugger";
        intent.setData(Uri.parse(debuggerUri));
        RobolectricActivity activity = Robolectric.buildActivity(RobolectricActivity.class, intent).create().get();
        Truth.assertThat(activity.getIntent().getData().toString()).isEqualTo(debuggerUri);
        Robolectric.flushForegroundThreadScheduler();

        //circler
        Intent intent2 = new Intent();
        String circlerUri = "UNKNOWN://growingio/webservice?serviceType=circle";
        intent2.setData(Uri.parse(circlerUri));
        Activity activity2 = Robolectric.buildActivity(RobolectricActivity.class, intent2).create().get();
        Truth.assertThat(activity2.getIntent().getData().toString()).isEqualTo(circlerUri);
        Robolectric.flushForegroundThreadScheduler();

        //deeplink
        Intent intent3 = new Intent();
        String deeplinkUri = "UNKNOWN://growingio/deeplink?name=cpacm";
        intent3.setData(Uri.parse(deeplinkUri));
        RobolectricActivity activity3 = Robolectric.buildActivity(RobolectricActivity.class, intent3).create().get();
        Truth.assertThat(activity3.getIntent().getData().toString()).isEqualTo(deeplinkUri);
        Robolectric.flushForegroundThreadScheduler();

        Uninterruptibles.sleepUninterruptibly(1, TimeUnit.SECONDS);
    }

    @Test
    public void deviceInfoProvider() {
        DeviceInfoProvider deviceInfoProvider = context.getDeviceInfoProvider();
        Truth.assertThat(deviceInfoProvider.getAndroidId()).isNull();
        Truth.assertThat(deviceInfoProvider.getDeviceBrand()).isNotEmpty();
        Truth.assertThat(deviceInfoProvider.getDeviceId()).isNotEmpty();
        Truth.assertThat(deviceInfoProvider.getDeviceModel()).isEqualTo("robolectric");
        Truth.assertThat(deviceInfoProvider.getDeviceType()).isEqualTo("PHONE");
        Truth.assertThat(deviceInfoProvider.getGoogleAdId()).isNull();
        Truth.assertThat(deviceInfoProvider.getImei()).isNull();
        Truth.assertThat(deviceInfoProvider.getOaid()).isNull();
        Truth.assertThat(deviceInfoProvider.loadPlatformInfo().getPlatformVersion()).isNotEmpty();
        Truth.assertThat(deviceInfoProvider.getScreenHeight()).isEqualTo(470);
        Truth.assertThat(deviceInfoProvider.getScreenWidth()).isEqualTo(320);

        PersistentDataProvider persistentDataProvider = context.getProvider(PersistentDataProvider.class);
        persistentDataProvider.setDeviceId("");
        Truth.assertThat(persistentDataProvider.getDeviceId()).isNotEmpty();

        Truth.assertThat(new Date().getTimezoneOffset()).isEqualTo(deviceInfoProvider.getTimezoneOffset());
    }

}
