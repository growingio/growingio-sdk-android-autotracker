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
        String generatedDeviceId = context.getDeviceInfoProvider().getDeviceId();

        Truth.assertThat(provider.canSend()).isFalse();
        Truth.assertThat(provider.isPending()).isTrue();
        Truth.assertThat(provider.identify(null)).isFalse();
        Truth.assertThat(provider.identify("")).isFalse();
        Truth.assertThat(provider.canSend()).isFalse();
        // 未解锁：事件写自动生成的设备 ID，并带待替换标志
        CustomEvent lockedEvent = buildEvent(context, "locked");
        Truth.assertThat(lockedEvent.getDeviceId()).isEqualTo(generatedDeviceId);
        Truth.assertThat(lockedEvent.isForceLoginPending()).isTrue();

        // 与 Web SDK 一致：不去除首尾空白
        Truth.assertThat(provider.identify("  visitor-001  ")).isTrue();
        Truth.assertThat(provider.canSend()).isTrue();
        Truth.assertThat(provider.isPending()).isFalse();
        Truth.assertThat(provider.getDeviceId("generated")).isEqualTo("  visitor-001  ");
        Truth.assertThat(tracker.getDeviceId()).isEqualTo("  visitor-001  ");
        CustomEvent unlockedEvent = buildEvent(context, "unlocked");
        Truth.assertThat(unlockedEvent.getDeviceId()).isEqualTo("  visitor-001  ");
        Truth.assertThat(unlockedEvent.isForceLoginPending()).isFalse();
        Truth.assertThat(provider.identify("visitor-002")).isFalse();
        Truth.assertThat(tracker.getDeviceId()).isEqualTo("  visitor-001  ");

        // 下次启动：重新锁定；getDeviceId 返回已持久化的访问用户 ID，但新事件写自动生成的设备 ID
        provider.setup(context);
        Truth.assertThat(provider.canSend()).isFalse();
        Truth.assertThat(tracker.getDeviceId()).isEqualTo("  visitor-001  ");
        CustomEvent relockedEvent = buildEvent(context, "relocked");
        Truth.assertThat(relockedEvent.getDeviceId()).isEqualTo(generatedDeviceId);
        Truth.assertThat(relockedEvent.isForceLoginPending()).isTrue();
        Truth.assertThat(provider.getPendingReplacement(generatedDeviceId)).isEqualTo("  visitor-001  ");

        // 关闭 forceLogin：不阻塞，事件沿用已持久化的访问用户 ID
        context.getConfigurationProvider().core().setForceLogin(false);
        Truth.assertThat(provider.canSend()).isTrue();
        Truth.assertThat(provider.identify("visitor-003")).isFalse();
        Truth.assertThat(tracker.getDeviceId()).isEqualTo("  visitor-001  ");
        CustomEvent disabledEvent = buildEvent(context, "disabled");
        Truth.assertThat(disabledEvent.getDeviceId()).isEqualTo("  visitor-001  ");
        Truth.assertThat(disabledEvent.isForceLoginPending()).isFalse();
    }

    @Test
    public void overlongIdIsTruncatedWithoutSplittingSurrogatePair() {
        tracker = startTracker(true, false);
        ForceLoginProvider provider = tracker.getContext().getProvider(ForceLoginProvider.class);
        Truth.assertThat(provider.identify(repeat('x', 1000) + "y")).isTrue();
        Truth.assertThat(tracker.getDeviceId()).isEqualTo(repeat('x', 1000));

        // 第 1000 个 UTF-16 单元是 emoji 高位代理时，在 emoji 之前截断
        Truth.assertThat(ForceLoginProvider.truncateDeviceId(repeat('x', 999) + "\uD83D\uDE00y"))
                .isEqualTo(repeat('x', 999));
        // emoji 完整落在前 1000 个单元内时保留
        Truth.assertThat(ForceLoginProvider.truncateDeviceId(repeat('x', 998) + "\uD83D\uDE00y"))
                .isEqualTo(repeat('x', 998) + "\uD83D\uDE00");
        Truth.assertThat(ForceLoginProvider.truncateDeviceId("short")).isEqualTo("short");
    }

    @Test
    public void cdpBehaviorIsUnchanged() {
        tracker = startTracker(true, true);
        TrackerContext context = tracker.getContext();
        ForceLoginProvider provider = context.getProvider(ForceLoginProvider.class);
        String generatedDeviceId = context.getDeviceInfoProvider().getDeviceId();

        Truth.assertThat(provider.canSend()).isTrue();
        Truth.assertThat(provider.identify("visitor-001")).isFalse();
        Truth.assertThat(provider.isPending()).isFalse();
        Truth.assertThat(provider.getPendingReplacement(generatedDeviceId)).isEmpty();
        Truth.assertThat(provider.getDeviceId(generatedDeviceId)).isEqualTo(generatedDeviceId);
        Truth.assertThat(tracker.getDeviceId()).isNotEqualTo("visitor-001");

        CustomEvent cdpEvent = buildEvent(context, "cdp");
        Truth.assertThat(cdpEvent.getDeviceId()).isEqualTo(generatedDeviceId);
        Truth.assertThat(cdpEvent.isForceLoginPending()).isFalse();
    }

    @Test
    public void forceLoginIsDisabledByDefault() {
        CoreConfiguration configuration = new CoreConfiguration("ForceLoginProviderTest", "growingio://force-login");
        Truth.assertThat(configuration.isForceLogin()).isFalse();
    }

    private CustomEvent buildEvent(TrackerContext context, String eventName) {
        CustomEvent.Builder builder = new CustomEvent.Builder().setEventName(eventName);
        builder.readPropertyInTrackThread(context);
        return builder.build();
    }

    private static String repeat(char c, int count) {
        char[] chars = new char[count];
        Arrays.fill(chars, c);
        return new String(chars);
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
