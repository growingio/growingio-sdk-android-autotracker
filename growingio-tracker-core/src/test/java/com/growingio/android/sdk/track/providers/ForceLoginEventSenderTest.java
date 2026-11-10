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
import android.content.pm.ProviderInfo;

import androidx.test.core.app.ApplicationProvider;

import com.google.common.truth.Truth;
import com.google.common.util.concurrent.Uninterruptibles;
import com.growingio.android.database.DatabaseDataLoader;
import com.growingio.android.database.EventDataContentProvider;
import com.growingio.android.json.JsonDataLoader;
import com.growingio.android.protobuf.EventV3Protocol;
import com.growingio.android.protobuf.ProtobufDataLoader;
import com.growingio.android.sdk.CoreConfiguration;
import com.growingio.android.sdk.Tracker;
import com.growingio.android.sdk.TrackerContext;
import com.growingio.android.sdk.track.events.CustomEvent;
import com.growingio.android.sdk.track.middleware.EventDatabase;
import com.growingio.android.sdk.track.middleware.EventDbResult;
import com.growingio.android.sdk.track.middleware.SendResponse;
import com.growingio.android.sdk.track.middleware.format.EventByteArray;
import com.growingio.android.sdk.track.middleware.format.EventFormatData;
import com.growingio.android.sdk.track.modelloader.ModelLoader;

import org.json.JSONArray;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.android.controller.ContentProviderController;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@Config(manifest = Config.NONE)
@RunWith(RobolectricTestRunner.class)
public class ForceLoginEventSenderTest {

    private final ContentProviderController<EventDataContentProvider> controller =
            Robolectric.buildContentProvider(EventDataContentProvider.class);
    private final Application application = ApplicationProvider.getApplicationContext();

    private Tracker tracker;
    private TrackerContext context;
    private EventSenderProvider eventSenderProvider;

    @Before
    public void setup() {
        application.getSharedPreferences("growing3_force_login", Context.MODE_PRIVATE).edit().clear().commit();
        CoreConfiguration configuration = new CoreConfiguration("ForceLoginEventSenderTest", "growingio://force-login");
        configuration.setForceLogin(true);
        configuration.setDataCollectionEnabled(false);
        TrackerLifecycleProviderFactory.create().createConfigurationProviderWithConfig(configuration, new HashMap<>());
        tracker = new Tracker(application);
        configuration.setDataCollectionEnabled(true);
        context = tracker.getContext();

        ProviderInfo providerInfo = new ProviderInfo();
        providerInfo.authority = application.getPackageName() + "." + EventDataContentProvider.class.getSimpleName();
        controller.create(providerInfo).get();
        context.getRegistry().register(EventDatabase.class, EventDbResult.class, new DatabaseDataLoader.Factory(context));
        eventSenderProvider = context.getProvider(EventSenderProvider.class);
        eventSenderProvider.removeAllEvents();
    }

    @After
    public void tearDown() {
        eventSenderProvider.removeAllEvents();
        tracker.shutdown();
        application.getSharedPreferences("growing3_force_login", Context.MODE_PRIVATE).edit().clear().commit();
    }

    @Test
    public void identifyReplacesOnlyPendingJsonEvents() throws Exception {
        context.getRegistry().register(EventFormatData.class, EventByteArray.class, new JsonDataLoader.Factory());
        String generatedDeviceId = context.getDeviceInfoProvider().getDeviceId();
        // 老版本或此前启动已确认身份的事件：不带待替换标志
        CustomEvent legacy = legacyEvent("legacy-json");
        // 本次启动 identify 前的事件：自动生成的设备 ID + 待替换标志
        CustomEvent pending = customEvent("pending-json");
        Truth.assertThat(legacy.isForceLoginPending()).isFalse();
        Truth.assertThat(pending.isForceLoginPending()).isTrue();
        Truth.assertThat(pending.getDeviceId()).isEqualTo(generatedDeviceId);
        eventSenderProvider.cacheEvent(legacy);
        eventSenderProvider.cacheEvent(pending);
        CountDownLatch requestLatch = new CountDownLatch(1);
        List<byte[]> requests = new ArrayList<>();
        eventSenderProvider.setEventNetSender((events, mediaType) -> {
            requests.add(events);
            requestLatch.countDown();
            return new SendResponse(204, 1000L);
        });

        eventSenderProvider.sendEvents(false);
        Truth.assertThat(requests).isEmpty();
        Map<String, String> stored = jsonDeviceIds(queryEvents(pending).getData());
        Truth.assertThat(stored).containsExactly("legacy-json", generatedDeviceId, "pending-json", generatedDeviceId);

        // 与 Web SDK 一致：不去除首尾空白
        tracker.identify("  visitor-json  ");
        Truth.assertThat(requestLatch.await(5, TimeUnit.SECONDS)).isTrue();
        Uninterruptibles.sleepUninterruptibly(200, TimeUnit.MILLISECONDS);

        Truth.assertThat(requests).hasSize(1);
        Truth.assertThat(jsonDeviceIds(requests.get(0)))
                .containsExactly("legacy-json", generatedDeviceId, "pending-json", "  visitor-json  ");
        Truth.assertThat(queryEvents(pending).getSum()).isEqualTo(0);
    }

    @Test
    public void protobufRetryReplacesOnlyPendingEventsAndKeepsDatabase() throws Exception {
        context.getRegistry().register(EventFormatData.class, EventByteArray.class, new ProtobufDataLoader.Factory());
        String generatedDeviceId = context.getDeviceInfoProvider().getDeviceId();
        CustomEvent legacy = legacyEvent("legacy-protobuf");
        CustomEvent pending = customEvent("pending-protobuf");
        eventSenderProvider.cacheEvent(legacy);
        eventSenderProvider.cacheEvent(pending);
        List<byte[]> requests = new ArrayList<>();
        eventSenderProvider.setEventNetSender((events, mediaType) -> {
            requests.add(events);
            return new SendResponse(requests.size() == 1 ? 503 : 204, 1000L);
        });

        eventSenderProvider.sendEvents(false);
        Truth.assertThat(requests).isEmpty();
        ForceLoginProvider forceLoginProvider = context.getProvider(ForceLoginProvider.class);
        Truth.assertThat(forceLoginProvider.identify("visitor-protobuf")).isTrue();
        eventSenderProvider.sendEvents(false);

        Truth.assertThat(requests).hasSize(1);
        Truth.assertThat(protobufDeviceIds(requests.get(0)))
                .containsExactly("legacy-protobuf", generatedDeviceId, "pending-protobuf", "visitor-protobuf");
        // 上传失败：数据库原文不变
        EventDbResult cachedAfterFailure = queryEvents(pending);
        Truth.assertThat(cachedAfterFailure.getSum()).isEqualTo(2);
        Truth.assertThat(protobufDeviceIds(cachedAfterFailure.getData()))
                .containsExactly("legacy-protobuf", generatedDeviceId, "pending-protobuf", generatedDeviceId);

        // 重试时再次替换
        eventSenderProvider.sendEvents(false);
        Truth.assertThat(requests).hasSize(2);
        Truth.assertThat(protobufDeviceIds(requests.get(1)))
                .containsExactly("legacy-protobuf", generatedDeviceId, "pending-protobuf", "visitor-protobuf");
        Truth.assertThat(queryEvents(pending).getSum()).isEqualTo(0);
    }

    /**
     * 未开启强制登录时构建的事件，模拟老版本 SDK 或此前启动已确认身份的事件
     */
    private CustomEvent legacyEvent(String eventName) {
        CoreConfiguration core = context.getConfigurationProvider().core();
        core.setForceLogin(false);
        try {
            return customEvent(eventName);
        } finally {
            core.setForceLogin(true);
        }
    }

    private Map<String, String> jsonDeviceIds(byte[] data) throws Exception {
        Map<String, String> result = new HashMap<>();
        JSONArray events = new JSONArray(new String(data));
        for (int i = 0; i < events.length(); i++) {
            result.put(events.getJSONObject(i).getString("eventName"), events.getJSONObject(i).getString("deviceId"));
        }
        return result;
    }

    private Map<String, String> protobufDeviceIds(byte[] data) throws Exception {
        Map<String, String> result = new HashMap<>();
        EventV3Protocol.EventV3List list = EventV3Protocol.EventV3List.parseFrom(data);
        for (EventV3Protocol.EventV3Dto event : list.getValuesList()) {
            result.put(event.getEventName(), event.getDeviceId());
        }
        return result;
    }

    private CustomEvent customEvent(String eventName) {
        CustomEvent.Builder builder = new CustomEvent.Builder().setEventName(eventName);
        builder.readPropertyInTrackThread(context);
        return builder.build();
    }

    private EventDbResult queryEvents(CustomEvent event) {
        ModelLoader<EventDatabase, EventDbResult> modelLoader =
                context.getRegistry().getModelLoader(EventDatabase.class, EventDbResult.class);
        return modelLoader.buildLoadData(EventDatabase.query(event.getSendPolicy(), 10)).fetcher.executeData();
    }
}
