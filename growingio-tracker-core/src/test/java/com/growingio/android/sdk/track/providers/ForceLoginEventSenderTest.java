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
    public void identifyUploadsCachedJsonEvents() throws Exception {
        context.getRegistry().register(EventFormatData.class, EventByteArray.class, new JsonDataLoader.Factory());
        CustomEvent event = customEvent("cached-json");
        eventSenderProvider.cacheEvent(event);
        CountDownLatch requestLatch = new CountDownLatch(1);
        List<byte[]> requests = new ArrayList<>();
        eventSenderProvider.setEventNetSender((events, mediaType) -> {
            requests.add(events);
            requestLatch.countDown();
            return new SendResponse(204, 1000L);
        });

        eventSenderProvider.sendEvents(false);
        Truth.assertThat(requests).isEmpty();
        EventDbResult cached = queryEvents(event);
        Truth.assertThat(cached.getSum()).isEqualTo(1);
        Truth.assertThat(new JSONArray(new String(cached.getData())).getJSONObject(0).getString("deviceId"))
                .isEqualTo(context.getDeviceInfoProvider().getDeviceId());

        tracker.identify("  visitor-json  ");
        Truth.assertThat(requestLatch.await(5, TimeUnit.SECONDS)).isTrue();
        Uninterruptibles.sleepUninterruptibly(200, TimeUnit.MILLISECONDS);

        Truth.assertThat(requests).hasSize(1);
        JSONArray sentEvents = new JSONArray(new String(requests.get(0)));
        Truth.assertThat(sentEvents.getJSONObject(0).getString("deviceId")).isEqualTo("visitor-json");
        Truth.assertThat(sentEvents.getJSONObject(0).getString("eventName")).isEqualTo("cached-json");
        Truth.assertThat(queryEvents(event).getSum()).isEqualTo(0);
    }

    @Test
    public void protobufRetryReplacesOnlyOutgoingDeviceId() throws Exception {
        context.getRegistry().register(EventFormatData.class, EventByteArray.class, new ProtobufDataLoader.Factory());
        CustomEvent event = customEvent("cached-protobuf");
        String generatedDeviceId = context.getDeviceInfoProvider().getDeviceId();
        eventSenderProvider.cacheEvent(event);
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
        assertProtobufDeviceId(requests.get(0), "visitor-protobuf");
        EventDbResult cachedAfterFailure = queryEvents(event);
        Truth.assertThat(cachedAfterFailure.getSum()).isEqualTo(1);
        EventV3Protocol.EventV3List cachedList = EventV3Protocol.EventV3List.parseFrom(cachedAfterFailure.getData());
        Truth.assertThat(cachedList.getValues(0).getDeviceId()).isEqualTo(generatedDeviceId);

        eventSenderProvider.sendEvents(false);
        Truth.assertThat(requests).hasSize(2);
        assertProtobufDeviceId(requests.get(1), "visitor-protobuf");
        Truth.assertThat(queryEvents(event).getSum()).isEqualTo(0);
    }

    @Test
    public void identifyUploadsCachedEventsWhenDataCollectionDisabled() throws Exception {
        context.getRegistry().register(EventFormatData.class, EventByteArray.class, new JsonDataLoader.Factory());
        eventSenderProvider.cacheEvent(customEvent("cached-before-identify"));
        CountDownLatch requestLatch = new CountDownLatch(1);
        List<byte[]> requests = new ArrayList<>();
        eventSenderProvider.setEventNetSender((events, mediaType) -> {
            requests.add(events);
            requestLatch.countDown();
            return new SendResponse(204, 1000L);
        });
        context.getConfigurationProvider().core().setDataCollectionEnabled(false);

        tracker.identify("visitor-disabled-collection");

        Truth.assertThat(requestLatch.await(5, TimeUnit.SECONDS)).isTrue();
        Truth.assertThat(requests).hasSize(1);
        JSONArray sentEvents = new JSONArray(new String(requests.get(0)));
        Truth.assertThat(sentEvents.getJSONObject(0).getString("deviceId")).isEqualTo("visitor-disabled-collection");
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

    private void assertProtobufDeviceId(byte[] data, String deviceId) throws Exception {
        EventV3Protocol.EventV3List list = EventV3Protocol.EventV3List.parseFrom(data);
        Truth.assertThat(list.getValuesCount()).isEqualTo(1);
        Truth.assertThat(list.getValues(0).getDeviceId()).isEqualTo(deviceId);
        Truth.assertThat(list.getValues(0).getEventName()).isEqualTo("cached-protobuf");
    }
}
