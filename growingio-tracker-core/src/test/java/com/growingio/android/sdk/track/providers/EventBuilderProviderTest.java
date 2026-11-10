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

import androidx.test.core.app.ApplicationProvider;

import com.google.common.truth.Truth;
import com.growingio.android.sdk.Configurable;
import com.growingio.android.sdk.CoreConfiguration;
import com.growingio.android.sdk.Tracker;
import com.growingio.android.sdk.track.events.CustomEvent;
import com.growingio.android.sdk.track.events.PageEvent;
import com.growingio.android.sdk.track.events.PageLevelCustomEvent;
import com.growingio.android.sdk.track.events.hybrid.HybridCustomEvent;
import com.growingio.android.sdk.track.middleware.CdpConfig;

import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.HashMap;
import java.util.Map;

@Config(manifest = Config.NONE)
@RunWith(RobolectricTestRunner.class)
public class EventBuilderProviderTest {
    private final Application application = ApplicationProvider.getApplicationContext();

    @Test
    public void customEventPageConfiguration() {
        CoreConfiguration configuration = new CoreConfiguration("EventBuilderProviderTest", "growingio://eventBuilder");
        Truth.assertThat(configuration.isCustomEventWithPath()).isFalse();
        Truth.assertThat(configuration.isCustomEventWithReferralPath()).isFalse();

        configuration.setCustomEventWithPath(true).setCustomEventWithReferralPath(true);
        Truth.assertThat(configuration.isCustomEventWithPath()).isTrue();
        Truth.assertThat(configuration.isCustomEventWithReferralPath()).isTrue();
    }

    @Test
    public void customEventWithoutPageConfiguration() {
        EventBuilderProvider provider = setup(new CoreConfiguration("EventBuilderProviderTest", "growingio://eventBuilder"));
        sendPage(provider, "/previous", "");
        sendPage(provider, "/current", "/previous");

        CustomEvent event = (CustomEvent) provider.onGenerateGEvent(new CustomEvent.Builder().setEventName("add_cart"));
        JSONObject json = EventBuilderProvider.toJson(event);
        Truth.assertThat(event).isNotInstanceOf(PageLevelCustomEvent.class);
        Truth.assertThat(json.has("path")).isFalse();
        Truth.assertThat(json.has("referralPage")).isFalse();
    }

    @Test
    public void customEventWithReferralPath() {
        CoreConfiguration configuration = new CoreConfiguration("EventBuilderProviderTest", "growingio://eventBuilder")
                .setCustomEventWithReferralPath(true);
        EventBuilderProvider provider = setup(configuration);
        sendPage(provider, "/previous", "");
        sendPage(provider, "/current", "/previous");

        CustomEvent event = (CustomEvent) provider.onGenerateGEvent(new CustomEvent.Builder().setEventName("add_cart"));
        JSONObject json = EventBuilderProvider.toJson(event);
        Truth.assertThat(event).isNotInstanceOf(PageLevelCustomEvent.class);
        Truth.assertThat(event.getReferralPage()).isEqualTo("/previous");
        Truth.assertThat(json.optString("referralPage")).isEqualTo("/previous");
        Truth.assertThat(json.has("path")).isFalse();
    }

    @Test
    public void customEventWithPathKeepsLegacyBehavior() {
        CoreConfiguration configuration = new CoreConfiguration("EventBuilderProviderTest", "growingio://eventBuilder")
                .setCustomEventWithPath(true);
        EventBuilderProvider provider = setup(configuration);
        sendPage(provider, "/previous", "");
        sendPage(provider, "/current", "/previous");

        PageLevelCustomEvent event = (PageLevelCustomEvent) provider.onGenerateGEvent(new CustomEvent.Builder().setEventName("add_cart"));
        JSONObject json = EventBuilderProvider.toJson(event);
        Truth.assertThat(event.getPath()).isEqualTo("/current");
        Truth.assertThat(json.optString("path")).isEqualTo("/current");
        Truth.assertThat(json.has("referralPage")).isFalse();
    }

    @Test
    public void customEventWithPathAndReferralPath() {
        CoreConfiguration configuration = new CoreConfiguration("EventBuilderProviderTest", "growingio://eventBuilder")
                .setCustomEventWithPath(true)
                .setCustomEventWithReferralPath(true);
        EventBuilderProvider provider = setup(configuration);
        sendPage(provider, "/previous", "");
        sendPage(provider, "/current", "/previous");

        PageLevelCustomEvent event = (PageLevelCustomEvent) provider.onGenerateGEvent(new CustomEvent.Builder().setEventName("add_cart"));
        JSONObject json = EventBuilderProvider.toJson(event);
        Truth.assertThat(json.optString("path")).isEqualTo("/current");
        Truth.assertThat(json.optString("referralPage")).isEqualTo("/previous");
    }

    @Test
    public void flutterPageDoesNotUpdateCustomPageInfo() {
        // 与 master 一致：只有原生 PAGE 更新 CUSTOM 页面信息，Flutter / Compose 等未登记的 PAGE 不更新
        CoreConfiguration configuration = new CoreConfiguration("EventBuilderProviderTest", "growingio://eventBuilder")
                .setCustomEventWithPath(true)
                .setCustomEventWithReferralPath(true);
        EventBuilderProvider provider = setup(configuration);
        sendPage(provider, "/native/current", "/native/previous");
        sendUnregisteredPage(provider, "/flutter/current", "/native/current");

        PageLevelCustomEvent event = (PageLevelCustomEvent) provider.onGenerateGEvent(new CustomEvent.Builder().setEventName("add_cart"));
        JSONObject json = EventBuilderProvider.toJson(event);
        Truth.assertThat(json.optString("path")).isEqualTo("/native/current");
        Truth.assertThat(json.optString("referralPage")).isEqualTo("/native/previous");
    }

    @Test
    public void customPageInfoFollowsTrackThreadOrder() {
        // PageProvider 在 UI 线程先登记 A、B，track 线程按顺序处理 PAGE：处理 A 后的 CUSTOM 仍为 A 的页面信息
        CoreConfiguration configuration = new CoreConfiguration("EventBuilderProviderTest", "growingio://eventBuilder")
                .setCustomEventWithPath(true)
                .setCustomEventWithReferralPath(true);
        EventBuilderProvider provider = setup(configuration);
        provider.setCustomEventReferPage("/a", 1L);
        provider.setCustomEventReferPage("/b", 2L);

        provider.onGenerateGEvent(pageBuilder("/a", "/start", 1L));
        JSONObject jsonA = EventBuilderProvider.toJson(provider.onGenerateGEvent(new CustomEvent.Builder().setEventName("on_a")));
        Truth.assertThat(jsonA.optString("path")).isEqualTo("/a");
        Truth.assertThat(jsonA.optString("referralPage")).isEqualTo("/start");

        provider.onGenerateGEvent(pageBuilder("/b", "/a", 2L));
        JSONObject jsonB = EventBuilderProvider.toJson(provider.onGenerateGEvent(new CustomEvent.Builder().setEventName("on_b")));
        Truth.assertThat(jsonB.optString("path")).isEqualTo("/b");
        Truth.assertThat(jsonB.optString("referralPage")).isEqualTo("/a");

        // 登记已被消费：再次处理同一 PAGE 不重复更新
        sendUnregisteredPage(provider, "/a", "/start");
        JSONObject jsonAgain = EventBuilderProvider.toJson(provider.onGenerateGEvent(new CustomEvent.Builder().setEventName("again")));
        Truth.assertThat(jsonAgain.optString("path")).isEqualTo("/b");
    }

    @Test
    public void customEventWithoutPreviousPage() {
        CoreConfiguration configuration = new CoreConfiguration("EventBuilderProviderTest", "growingio://eventBuilder")
                .setCustomEventWithReferralPath(true);
        EventBuilderProvider provider = setup(configuration);
        sendPage(provider, "/first", "");

        CustomEvent event = (CustomEvent) provider.onGenerateGEvent(new CustomEvent.Builder().setEventName("add_cart"));
        Truth.assertThat(event.getReferralPage()).isEmpty();
        Truth.assertThat(EventBuilderProvider.toJson(event).has("referralPage")).isFalse();
    }

    @Test
    public void hybridCustomEventIgnoresPageConfiguration() {
        EventBuilderProvider provider = setup(new CoreConfiguration("EventBuilderProviderTest", "growingio://eventBuilder"));
        HybridCustomEvent.Builder builder = new HybridCustomEvent.Builder();
        builder.setEventName("hybrid_custom");
        builder.setPath("/current");
        builder.setReferralPage("/previous");
        builder.setQuery("from=hybrid");

        HybridCustomEvent event = (HybridCustomEvent) provider.onGenerateGEvent(builder);
        JSONObject json = EventBuilderProvider.toJson(event);
        Truth.assertThat(json.optString("path")).isEqualTo("/current");
        Truth.assertThat(json.optString("referralPage")).isEqualTo("/previous");

        HybridCustomEvent.Builder emptyBuilder = new HybridCustomEvent.Builder();
        emptyBuilder.setEventName("hybrid_custom");
        emptyBuilder.setPath("/current");
        emptyBuilder.setReferralPage("");
        HybridCustomEvent emptyEvent = (HybridCustomEvent) provider.onGenerateGEvent(emptyBuilder);
        Truth.assertThat(EventBuilderProvider.toJson(emptyEvent).has("referralPage")).isFalse();
    }

    @Test
    public void downgradeCustomEventKeepsLegacyBehavior() {
        CoreConfiguration configuration = new CoreConfiguration("EventBuilderProviderTest", "growingio://eventBuilder")
                .setCustomEventWithPath(true)
                .setCustomEventWithReferralPath(true);
        Map<Class<? extends Configurable>, Configurable> moduleConfigs = new HashMap<>();
        moduleConfigs.put(CdpConfig.class, new CdpConfig());
        EventBuilderProvider provider = setup(configuration, moduleConfigs);
        sendPage(provider, "/current", "/previous");

        PageLevelCustomEvent event = (PageLevelCustomEvent) provider.onGenerateGEvent(new CustomEvent.Builder().setEventName("add_cart"));
        JSONObject json = EventBuilderProvider.toJson(event);
        Truth.assertThat(json.optString("path")).isEqualTo("/current");
        Truth.assertThat(event.getPageShowTimestamp()).isEqualTo(123456789L);
        Truth.assertThat(json.has("referralPage")).isFalse();

        HybridCustomEvent.Builder hybridBuilder = new HybridCustomEvent.Builder();
        hybridBuilder.setEventName("hybrid_custom");
        hybridBuilder.setPath("/current");
        hybridBuilder.setReferralPage("/previous");
        HybridCustomEvent hybridEvent = (HybridCustomEvent) provider.onGenerateGEvent(hybridBuilder);
        JSONObject hybridJson = EventBuilderProvider.toJson(hybridEvent);
        Truth.assertThat(hybridJson.optString("path")).isEqualTo("/current");
        Truth.assertThat(hybridJson.has("referralPage")).isFalse();
    }

    private EventBuilderProvider setup(CoreConfiguration configuration) {
        return setup(configuration, null);
    }

    private EventBuilderProvider setup(CoreConfiguration configuration, Map<Class<? extends Configurable>, Configurable> moduleConfigs) {
        TrackerLifecycleProviderFactory.create().createConfigurationProviderWithConfig(configuration, moduleConfigs);
        Tracker tracker = new Tracker(application);
        return tracker.getContext().getEventBuilderProvider();
    }

    /**
     * 模拟原生 PAGE：PageProvider 先登记再发送
     */
    private void sendPage(EventBuilderProvider provider, String path, String referralPage) {
        provider.setCustomEventReferPage(path, 123456789L);
        provider.onGenerateGEvent(pageBuilder(path, referralPage, 123456789L));
    }

    /**
     * 模拟 Flutter / Compose PAGE：不登记
     */
    private void sendUnregisteredPage(EventBuilderProvider provider, String path, String referralPage) {
        provider.onGenerateGEvent(pageBuilder(path, referralPage, 123456789L));
    }

    private PageEvent.Builder pageBuilder(String path, String referralPage, long timestamp) {
        return new PageEvent.Builder()
                .setPath(path)
                .setReferralPage(referralPage)
                .setTimestamp(timestamp);
    }
}
