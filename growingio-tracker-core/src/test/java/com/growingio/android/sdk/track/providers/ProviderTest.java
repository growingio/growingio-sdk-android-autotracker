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
package com.growingio.android.sdk.track.providers;

import android.app.Application;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.test.core.app.ApplicationProvider;

import com.google.common.truth.Truth;
import com.growingio.android.sdk.Configurable;
import com.growingio.android.sdk.CoreConfiguration;
import com.growingio.android.sdk.TrackerContext;
import com.growingio.android.sdk.track.listener.IActivityLifecycle;
import com.growingio.android.sdk.track.listener.event.ActivityLifecycleEvent;
import com.growingio.android.sdk.track.utils.ConstantPool;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.Config;

import java.util.HashMap;

@Config(manifest = Config.NONE)
@RunWith(RobolectricTestRunner.class)
public class ProviderTest {

    private final Application application = ApplicationProvider.getApplicationContext();

    @Before
    public void setup() {
        TrackerContext.init(application);
    }

    @Test
    public void activityStateProvider() {
        ActivityController<RobolectricActivity> activityController = Robolectric.buildActivity(RobolectricActivity.class);
        RobolectricActivity activity = activityController.get();
        application.registerActivityLifecycleCallbacks(ActivityStateProvider.get());

        Bundle testBundle = new Bundle();

        IActivityLifecycle iActivityLifecycle = new IActivityLifecycle() {
            @Override
            public void onActivityLifecycle(ActivityLifecycleEvent event) {
                Truth.assertThat(event.getActivity()).isEqualTo(activity);
                Truth.assertThat(event.eventType).isEqualTo(activity.state);
                if (event.getBundle() != null) {
                    Truth.assertThat(event.getBundle()).isEqualTo(testBundle);
                }
                if (event.getIntent() != null) {
                    Truth.assertThat(event.getIntent()).isEqualTo(activity.getIntent());
                }
            }
        };
        ActivityStateProvider.get().register(iActivityLifecycle);

        activityController.create(testBundle);
        activityController.resume();
        activityController.pause();
        activityController.stop();
        activityController.destroy();
        ActivityStateProvider.get().unregisterActivityLifecycleListener(iActivityLifecycle);
    }

    @Test
    public void appInfoProvider() {
        Truth.assertThat(AppInfoProvider.get().getPackageName()).isEqualTo(application.getPackageName());
        Truth.assertThat(AppInfoProvider.get().getAppChannel()).isNull();
        Truth.assertThat(AppInfoProvider.get().getAppName()).isEqualTo(application.getPackageManager().getApplicationLabel(application.getApplicationInfo()).toString());
        Truth.assertThat(AppInfoProvider.get().getAppVersion()).isNull();
    }

    @Test
    public void configProvider() {
        ConfigurationProvider.initWithConfig(new CoreConfiguration("test", ConstantPool.UNKNOWN), new HashMap<>());
        Truth.assertThat(ConfigurationProvider.core().getUrlScheme()).isEqualTo(ConstantPool.UNKNOWN);
        ConfigurationProvider.initWithConfig(new CoreConfiguration("test", "test"), new HashMap<>());
        TestConfigurable testConfigurable = new TestConfigurable();
        ConfigurationProvider.get().addConfiguration(testConfigurable);
        Truth.assertThat((TestConfigurable) ConfigurationProvider.get().getConfiguration(TestConfigurable.class)).isEqualTo(testConfigurable);
    }

    public static class TestConfigurable implements Configurable {
    }

    @Test
    public void deeplinkProvider() {
        application.registerActivityLifecycleCallbacks(ActivityStateProvider.get());
        DeepLinkProvider.get().init();

        //empty
        Robolectric.buildActivity(RobolectricActivity.class).create().get();

        //debugger
        Intent intent = new Intent();
        intent.setData(Uri.parse("UNKNOWN://growingio/webservice?serviceType=debugger"));
        Robolectric.buildActivity(RobolectricActivity.class, intent).create().get();
        Robolectric.flushForegroundThreadScheduler();

        //circler
        Intent intent2 = new Intent();
        intent2.setData(Uri.parse("UNKNOWN://growingio/webservice?serviceType=circle"));
        Robolectric.buildActivity(RobolectricActivity.class, intent2).create().get();
        Robolectric.flushForegroundThreadScheduler();

        //deeplink
        Intent intent3 = new Intent();
        intent3.setData(Uri.parse("UNKNOWN://growingio/deeplink?name=cpacm"));
        Robolectric.buildActivity(RobolectricActivity.class, intent3).create().get();
        Robolectric.flushForegroundThreadScheduler();

    }

    @Test
    public void deviceInfoProvider() {
        Truth.assertThat(DeviceInfoProvider.get().getAndroidId()).isNull();
        Truth.assertThat(DeviceInfoProvider.get().getDeviceBrand()).isEqualTo("robolectric");
        Truth.assertThat(DeviceInfoProvider.get().getDeviceId()).isNotEmpty();
        Truth.assertThat(DeviceInfoProvider.get().getDeviceModel()).isEqualTo("robolectric");
        Truth.assertThat(DeviceInfoProvider.get().getDeviceType()).isEqualTo("PHONE");
        Truth.assertThat(DeviceInfoProvider.get().getGoogleAdId()).isNull();
        Truth.assertThat(DeviceInfoProvider.get().getImei()).isNull();
        Truth.assertThat(DeviceInfoProvider.get().getOaid()).isNull();
        Truth.assertThat(DeviceInfoProvider.get().getOperatingSystemVersion()).isEqualTo("11");
        Truth.assertThat(DeviceInfoProvider.get().getScreenHeight()).isEqualTo(470);
        Truth.assertThat(DeviceInfoProvider.get().getScreenWidth()).isEqualTo(320);
    }

    @Test
    public void sessionProvider() {
        String sessionId = SessionProvider.get().getSessionId();
        Truth.assertThat(sessionId).isEmpty();
        SessionProvider.get().checkAndSendVisit(0);
        sessionId = SessionProvider.get().getSessionId();
        Truth.assertThat(sessionId).isEmpty();
        SessionProvider.get().setLocation(0d, 1d);
        Truth.assertThat(SessionProvider.get().getLatitude()).isEqualTo(0d);
        Truth.assertThat(SessionProvider.get().getLongitude()).isEqualTo(1d);
        SessionProvider.get().cleanLocation();

        SessionProvider.get().onTrackMainInitSDK();
        UserInfoProvider.get().setLoginUserId("cpacm");
        String newSessionId1 = SessionProvider.get().refreshSessionId();
        String newSessionId2 = SessionProvider.get().getSessionId();
        Truth.assertThat(newSessionId1).isEqualTo(newSessionId2);

        UserInfoProvider.get().setLoginUserId("cpacm2");
        String newSessionId3 = SessionProvider.get().getSessionId();
        Truth.assertThat(newSessionId3).isNotEqualTo(newSessionId2);
        UserInfoProvider.get().unregisterUserIdChangedListener(SessionProvider.get());
    }


}
