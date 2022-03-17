/*
 *   Copyright (C) 2020 Beijing Yishu Technology Co., Ltd.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package com.growingio.android.sdk.track.middleware;

import android.app.Application;
import android.content.pm.ProviderInfo;

import androidx.test.core.app.ApplicationProvider;

import com.google.common.truth.Truth;
import com.google.common.util.concurrent.Uninterruptibles;
import com.google.protobuf.InvalidProtocolBufferException;
import com.growingio.android.json.JsonDataLoader;
import com.growingio.android.sdk.TrackerContext;
import com.growingio.android.sdk.track.events.CustomEvent;
import com.growingio.android.sdk.track.middleware.format.EventByteArray;
import com.growingio.android.sdk.track.middleware.format.EventFormatData;
import com.growingio.android.database.DatabaseDataLoader;
import com.growingio.android.database.EventDataContentProvider;
import com.growingio.protobuf.EventV3Protocol;
import com.growingio.protobuf.ProtobufDataLoader;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.android.controller.ContentProviderController;
import org.robolectric.annotation.Config;

import java.util.concurrent.TimeUnit;

@Config(manifest = Config.NONE)
@RunWith(RobolectricTestRunner.class)
public class EventSenderTest {

    private final ContentProviderController<EventDataContentProvider> controller =
            Robolectric.buildContentProvider(EventDataContentProvider.class);
    private final Application application = ApplicationProvider.getApplicationContext();
    private EventSender eventSender;

    @Before
    public void setup() {
        TrackerContext.init(application);
        ProviderInfo providerInfo = new ProviderInfo();
        providerInfo.authority = application.getPackageName() + "." + EventDataContentProvider.class.getSimpleName();

        eventSender = new EventSender(application, null, 0, 10);
        controller.create(providerInfo).get();
    }

    @Test
    public void eventSendTest() {
        eventSender.setEventNetSender((events, mediaType) -> {
            try {
                EventV3Protocol.EventV3List list = EventV3Protocol.EventV3List.parseFrom(events);
                Truth.assertThat(list.getSerializedSize()).isEqualTo(1);
                Truth.assertThat(list.getValues(0).getEventName()).isEqualTo("cpacm");
            } catch (InvalidProtocolBufferException e) {
                e.printStackTrace();
            }
            return new SendResponse(true, 1000L);
        });
        eventSender.sendEvent(new CustomEvent.Builder()
                .setEventName("cpacm")
                .build());
        Robolectric.flushForegroundThreadScheduler();
        Uninterruptibles.sleepUninterruptibly(1, TimeUnit.SECONDS);
    }


    @Test
    public void eventCacheTestPb() throws InvalidProtocolBufferException {
        TrackerContext.get().getRegistry().register(EventDatabase.class, EventDbResult.class, new DatabaseDataLoader.Factory(application));
        TrackerContext.get().getRegistry().register(EventFormatData.class, EventByteArray.class, new ProtobufDataLoader.Factory());

        CustomEvent ce = new CustomEvent.Builder()
                .setEventName("cpacm").build();
        eventSender.cacheEvent(ce);
        eventSender.cacheEvent(ce);
        eventSender.cacheEvent(ce);
        EventDbResult dbResult = eventSender.getGEventsFromPolicy(ce.getSendPolicy());
        EventV3Protocol.EventV3List list = EventV3Protocol.EventV3List.parseFrom(dbResult.getData());
        Truth.assertThat(list.getValuesCount()).isEqualTo(3);

        eventSender.setEventNetSender((events, mediaType) -> {
            try {
                EventV3Protocol.EventV3List list1 = EventV3Protocol.EventV3List.parseFrom(events);
                Truth.assertThat(list1.getValuesCount()).isEqualTo(2);
                Truth.assertThat(list1.getValues(0).getEventName()).isEqualTo("cpacm");
            } catch (InvalidProtocolBufferException e) {
                e.printStackTrace();
            }
            return new SendResponse(true, 1000L);
        });
        eventSender.cacheEvent(ce);
        eventSender.cacheEvent(ce);
        eventSender.sendEvents(false);
        Uninterruptibles.sleepUninterruptibly(1, TimeUnit.SECONDS);
    }

    @Test
    public void eventCacheTestJson() throws JSONException {
        TrackerContext.get().getRegistry().register(EventDatabase.class, EventDbResult.class, new DatabaseDataLoader.Factory(application));
        TrackerContext.get().getRegistry().register(EventFormatData.class, EventByteArray.class, new JsonDataLoader.Factory());

        CustomEvent ce = new CustomEvent.Builder()
                .setEventName("cpacm").build();
        eventSender.cacheEvent(ce);
        eventSender.cacheEvent(ce);
        eventSender.cacheEvent(ce);
        EventDbResult dbResult = eventSender.getGEventsFromPolicy(ce.getSendPolicy());
        String result = new String(dbResult.getData());
        JSONArray jsonArray = new JSONArray(result);
        Truth.assertThat(jsonArray.length()).isEqualTo(3);

        eventSender.setEventNetSender((events, mediaType) -> {
            try {
                JSONArray array = new JSONArray(new String(events));
                Truth.assertThat(array.length()).isEqualTo(2);

                JSONObject obj1 = array.getJSONObject(0);
                Truth.assertThat(obj1.opt("eventName")).isEqualTo("cpacm");
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return new SendResponse(true, 1000L);
        });
        eventSender.cacheEvent(ce);
        eventSender.cacheEvent(ce);
        eventSender.sendEvents(false);
        Uninterruptibles.sleepUninterruptibly(1, TimeUnit.SECONDS);
    }
}
