/*
 * Copyright (C) 2026 Beijing Yishu Technology Co., Ltd.
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
import android.content.Context;

import androidx.test.core.app.ApplicationProvider;

import com.google.common.truth.Truth;
import com.growingio.android.sdk.Configurable;
import com.growingio.android.sdk.CoreConfiguration;
import com.growingio.android.sdk.Tracker;
import com.growingio.android.sdk.TrackerContext;
import com.growingio.android.sdk.track.events.CustomEvent;
import com.growingio.android.sdk.track.middleware.CdpConfig;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Config(manifest = Config.NONE)
@RunWith(RobolectricTestRunner.class)
public class ForceLoginProviderTest {

    private final Application application = ApplicationProvider.getApplicationContext();
    private Tracker tracker;

    @Before
    public void clearIdentifyDeviceId() {
        application.getSharedPreferences("growing3_force_login", Context.MODE_PRIVATE).edit().clear().commit();
    }

    @After
    public void tearDown() {
        if (tracker != null) {
            tracker.shutdown();
        }
        application.getSharedPreferences("growing3_force_login", Context.MODE_PRIVATE).edit().clear().commit();
    }

    @Test
    public void validationPersistenceAndEventDeviceId() {
        tracker = startTracker(true, false);
        TrackerContext context = tracker.getContext();
        ForceLoginProvider provider = context.getProvider(ForceLoginProvider.class);

        Truth.assertThat(provider.canSend()).isFalse();
        Truth.assertThat(provider.identify(null)).isFalse();
        Truth.assertThat(provider.identify("  \n  ")).isFalse();
        char[] overlongChars = new char[1001];
        Arrays.fill(overlongChars, 'x');
        Truth.assertThat(provider.identify(new String(overlongChars))).isFalse();
        Truth.assertThat(provider.canSend()).isFalse();

        Truth.assertThat(provider.identify("  visitor-001  ")).isTrue();
        Truth.assertThat(provider.canSend()).isTrue();
        Truth.assertThat(provider.getDeviceId("generated")).isEqualTo("visitor-001");
        Truth.assertThat(tracker.getDeviceId()).isEqualTo("visitor-001");

        CustomEvent.Builder builder = new CustomEvent.Builder().setEventName("forced");
        builder.readPropertyInTrackThread(context);
        Truth.assertThat(builder.build().getDeviceId()).isEqualTo("visitor-001");
        Truth.assertThat(provider.identify("visitor-002")).isFalse();
        Truth.assertThat(tracker.getDeviceId()).isEqualTo("visitor-001");

        provider.setup(context);
        Truth.assertThat(provider.canSend()).isFalse();
        Truth.assertThat(tracker.getDeviceId()).isEqualTo("visitor-001");

        context.getConfigurationProvider().core().setForceLogin(false);
        Truth.assertThat(provider.canSend()).isTrue();
        Truth.assertThat(provider.identify("visitor-003")).isFalse();
        Truth.assertThat(tracker.getDeviceId()).isEqualTo("visitor-001");
    }

    @Test
    public void cdpBehaviorIsUnchanged() {
        tracker = startTracker(true, true);
        TrackerContext context = tracker.getContext();
        ForceLoginProvider provider = context.getProvider(ForceLoginProvider.class);
        String generatedDeviceId = context.getDeviceInfoProvider().getDeviceId();

        Truth.assertThat(provider.canSend()).isTrue();
        Truth.assertThat(provider.identify("visitor-001")).isFalse();
        Truth.assertThat(provider.getOverrideDeviceId()).isEmpty();
        Truth.assertThat(provider.getDeviceId(generatedDeviceId)).isEqualTo(generatedDeviceId);
        Truth.assertThat(tracker.getDeviceId()).isNotEqualTo("visitor-001");

        CustomEvent.Builder builder = new CustomEvent.Builder().setEventName("cdp");
        builder.readPropertyInTrackThread(context);
        Truth.assertThat(builder.build().getDeviceId()).isNotEqualTo("visitor-001");
    }

    @Test
    public void forceLoginIsDisabledByDefault() {
        CoreConfiguration configuration = new CoreConfiguration("ForceLoginProviderTest", "growingio://force-login");
        Truth.assertThat(configuration.isForceLogin()).isFalse();
    }

    private Tracker startTracker(boolean forceLogin, boolean cdp) {
        CoreConfiguration configuration = new CoreConfiguration("ForceLoginProviderTest", "growingio://force-login");
        configuration.setForceLogin(forceLogin);
        Map<Class<? extends Configurable>, Configurable> moduleConfigs = new HashMap<>();
        if (cdp) {
            moduleConfigs.put(CdpConfig.class, new CdpConfig());
        }
        TrackerLifecycleProviderFactory.create().createConfigurationProviderWithConfig(configuration, moduleConfigs);
        return new Tracker(application);
    }
}
