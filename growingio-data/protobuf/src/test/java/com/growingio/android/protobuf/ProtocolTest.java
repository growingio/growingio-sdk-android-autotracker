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
package com.growingio.android.protobuf;


import com.google.common.truth.Truth;
import com.google.protobuf.InvalidProtocolBufferException;
import com.growingio.android.sdk.track.events.AppClosedEvent;
import com.growingio.android.sdk.track.events.ConversionVariablesEvent;
import com.growingio.android.sdk.track.events.CustomEvent;
import com.growingio.android.sdk.track.events.LoginUserAttributesEvent;
import com.growingio.android.sdk.track.events.PageEvent;
import com.growingio.android.sdk.track.events.PageLevelCustomEvent;
import com.growingio.android.sdk.track.events.ViewElementEvent;
import com.growingio.android.sdk.track.events.VisitorAttributesEvent;
import com.growingio.android.sdk.track.events.base.BaseEvent;
import com.growingio.android.sdk.track.events.hybrid.HybridCustomEvent;
import com.growingio.android.sdk.track.events.hybrid.HybridPageEvent;
import com.growingio.android.sdk.track.events.hybrid.HybridViewElementEvent;
import com.growingio.android.sdk.track.middleware.GEvent;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 *
 * @author cpacm 2021/11/24
 */
@RunWith(RobolectricTestRunner.class)
public class ProtocolTest {
    @Test
    public void protocolTest() throws InvalidProtocolBufferException {
        Map<String, String> defaultMap = new HashMap<>();
        defaultMap.put("user", "cpacm");

        PageEvent pageEvent = new PageEvent.Builder()
                .setTitle("databaseTest")
                .setPath("com.growingio.database.test")
                .setReferralPage("test")
                .setOrientation("vertical")
                .setTimestamp(System.currentTimeMillis())
                .build();
        Truth.assertThat(protocol(pageEvent).getTitle()).isEqualTo("databaseTest");
        Truth.assertThat(protocol(pageEvent).getPath()).isEqualTo("com.growingio.database.test");
        Truth.assertThat(protocol(pageEvent).getReferralPage()).isEqualTo("test");
        Truth.assertThat(protocol(pageEvent).getOrientation()).isEqualTo("vertical");

        CustomEvent customEvent = new CustomEvent.Builder()
                .setEventName("databaseTest")
                .build();
        Truth.assertThat(protocol(customEvent).getEventName()).isEqualTo("databaseTest");

        PageLevelCustomEvent plcEnent = new PageLevelCustomEvent.Builder()
                .setPath("PageLevelCustomEvent")
                .build();
        Truth.assertThat(protocol(plcEnent).getPath()).isEqualTo("PageLevelCustomEvent");

        ViewElementEvent vEvent = new ViewElementEvent.Builder("VIEW_CHANGE")
                .setXpath("xxxx")
                .setIndex(0)
                .build();
        Truth.assertThat(protocol(vEvent).getXpath()).isEqualTo("xxxx");
        Truth.assertThat(protocol(vEvent).getEventType()).isEqualTo(EventV3Protocol.EventType.VIEW_CHANGE);
        Truth.assertThat(protocol(vEvent).getIndex()).isEqualTo(0);

        HybridCustomEvent hcEvent = new HybridCustomEvent.Builder()
                .setQuery("hybrid")
                .build();
        Truth.assertThat(protocol(hcEvent).getQuery()).isEqualTo("hybrid");

        HybridPageEvent hpEvent = new HybridPageEvent.Builder()
                .setProtocolType("protobuf")
                .build();
        Truth.assertThat(protocol(hpEvent).getProtocolType()).isEqualTo("protobuf");
        Truth.assertThat(protocol(hpEvent).getPath()).isEqualTo("");


        HybridViewElementEvent hvEvent = new HybridViewElementEvent.Builder("test")
                .setHyperlink("www.cpacm.net")
                .build();
        Truth.assertThat(protocol(hvEvent).getHyperlink()).isEqualTo("www.cpacm.net");

        VisitorAttributesEvent vaEvent = new VisitorAttributesEvent.Builder()
                .setAttributes(defaultMap)
                .build();
        Truth.assertThat(protocol(vaEvent).getAttributesOrDefault("user", "gio")).isEqualTo("cpacm");

        LoginUserAttributesEvent laEvent = new LoginUserAttributesEvent.Builder()
                .setAttributes(defaultMap)
                .build();
        Truth.assertThat(protocol(laEvent).getAttributesOrDefault("user", "gio")).isEqualTo("cpacm");

        ConversionVariablesEvent cvEnent = new ConversionVariablesEvent.Builder()
                .setAttributes(defaultMap)
                .build();
        Truth.assertThat(protocol(cvEnent).getAttributesOrDefault("user", "gio")).isEqualTo("cpacm");

        AppClosedEvent acEvent = new AppClosedEvent.Builder()
                .build();
        Truth.assertThat(protocol(acEvent).getEventType()).isEqualTo(EventV3Protocol.EventType.APP_CLOSED);

    }

    private EventV3Protocol.EventV3Dto protocol(GEvent gEvent) throws InvalidProtocolBufferException {
        byte[] data = EventProtocolTransfer.protocolByte(gEvent);
        return EventV3Protocol.EventV3Dto.parseFrom(data);
    }

    @Test
    public void protocolListTest() throws InvalidProtocolBufferException {
        Map<String, String> defaultMap = new HashMap<>();
        defaultMap.put("user", "cpacm");
        defaultMap.put("age", "24");
        List<BaseEvent> events = new ArrayList<>();

        CustomEvent customEvent = new CustomEvent.Builder()
                .setEventName("pbTest")
                .setAttributes(defaultMap)
                .build();
        events.add(customEvent);


        EventV3Protocol.EventV3List.Builder listBuilder = EventV3Protocol.EventV3List.newBuilder();

        for (BaseEvent event : events) {
            listBuilder.addValues(protocol(event));
        }

        byte[] result = listBuilder.build().toByteArray();

        EventV3Protocol.EventV3List parser = EventV3Protocol.EventV3List.parseFrom(result);
        for (EventV3Protocol.EventV3Dto dto : parser.getValuesList()) {
            Truth.assertThat(dto.getAttributesCount()).isEqualTo(2);
            Truth.assertThat(dto.getAttributesMap()).containsEntry("user", "cpacm");
        }

    }
}
