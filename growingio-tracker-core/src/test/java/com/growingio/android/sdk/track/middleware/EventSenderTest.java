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
package com.growingio.android.sdk.track.middleware;

import android.app.Application;
import android.content.pm.ProviderInfo;

import androidx.test.core.app.ApplicationProvider;

import com.google.common.truth.Truth;
import com.google.common.util.concurrent.Uninterruptibles;
import com.growingio.android.sdk.TrackerContext;
import com.growingio.android.sdk.track.events.CustomEvent;
import com.growingio.android.sdk.track.events.base.BaseEvent;

import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.android.controller.ContentProviderController;
import org.robolectric.annotation.Config;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

@Config(manifest = Config.NONE)
@RunWith(RobolectricTestRunner.class)
public class EventSenderTest {

    private final ContentProviderController<EventsContentProvider> controller =
            Robolectric.buildContentProvider(EventsContentProvider.class);
    private final Application application = ApplicationProvider.getApplicationContext();
    private EventSender eventSender;

    @Before
    public void setup() {
        TrackerContext.init(application);
        ProviderInfo providerInfo = new ProviderInfo();
        providerInfo.authority = application.getPackageName() + ".EventsContentProvider";

        eventSender = new EventSender(application, null, 0, 10);
        controller.create(providerInfo).get();
    }

    @Test
    public void eventSendTest() {
        eventSender.setEventNetSender(new IEventNetSender() {
            @Override
            public SendResponse send(List<GEvent> events) {
                Truth.assertThat(events.size()).isEqualTo(1);
                GEvent gEvent = events.get(0);
                if (gEvent instanceof BaseEvent) {
                    JSONObject jsonObject = ((BaseEvent) gEvent).toJSONObject();
                    Truth.assertThat(jsonObject.opt("eventName")).isEqualTo("cpacm");
                }
                return new SendResponse(true, 1000L);
            }
        });
        eventSender.sendEvent(new CustomEvent.Builder()
                .setEventName("cpacm")
                .build());
        Robolectric.flushForegroundThreadScheduler();
        Uninterruptibles.sleepUninterruptibly(1, TimeUnit.SECONDS);
    }


    @Test
    public void eventCacheTest() {
        CustomEvent ce = new CustomEvent.Builder()
                .setEventName("cpacm").build();
        eventSender.cacheEvent(ce);
        eventSender.cacheEvent(ce);
        eventSender.cacheEvent(ce);
        List<GEvent> gEvents = eventSender.getGEventsFromPolicy(ce.getSendPolicy());
        Truth.assertThat(gEvents.size()).isEqualTo(3);

        eventSender.setEventNetSender(new IEventNetSender() {
            @Override
            public SendResponse send(List<GEvent> events) {
                Truth.assertThat(events.size()).isEqualTo(2);
                events.forEach(new Consumer<GEvent>() {
                    @Override
                    public void accept(GEvent gEvent) {
                        if (gEvent instanceof BaseEvent) {
                            JSONObject jsonObject = ((BaseEvent) gEvent).toJSONObject();
                            Truth.assertThat(jsonObject.opt("eventName")).isEqualTo("cpacm");
                        }
                    }
                });
                return new SendResponse(true, 1000L);
            }
        });
        eventSender.cacheEvent(ce);
        eventSender.cacheEvent(ce);
        eventSender.sendEvents(false);
    }
}
