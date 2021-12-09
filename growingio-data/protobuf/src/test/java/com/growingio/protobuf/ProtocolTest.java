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

package com.growingio.protobuf;

import android.app.Application;

import androidx.test.core.app.ApplicationProvider;

import com.google.common.truth.Truth;
import com.google.protobuf.InvalidProtocolBufferException;
import com.growingio.android.sdk.TrackerContext;
import com.growingio.android.sdk.track.events.AppClosedEvent;
import com.growingio.android.sdk.track.events.ConversionVariablesEvent;
import com.growingio.android.sdk.track.events.CustomEvent;
import com.growingio.android.sdk.track.events.LoginUserAttributesEvent;
import com.growingio.android.sdk.track.events.PageAttributesEvent;
import com.growingio.android.sdk.track.events.PageEvent;
import com.growingio.android.sdk.track.events.PageLevelCustomEvent;
import com.growingio.android.sdk.track.events.ViewElementEvent;
import com.growingio.android.sdk.track.events.VisitorAttributesEvent;
import com.growingio.android.sdk.track.events.cdp.ResourceItem;
import com.growingio.android.sdk.track.events.cdp.ResourceItemCustomEvent;
import com.growingio.android.sdk.track.events.hybrid.HybridCustomEvent;
import com.growingio.android.sdk.track.events.hybrid.HybridPageAttributesEvent;
import com.growingio.android.sdk.track.events.hybrid.HybridPageEvent;
import com.growingio.android.sdk.track.events.hybrid.HybridViewElementEvent;
import com.growingio.android.sdk.track.middleware.GEvent;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.util.HashMap;
import java.util.Map;

/**
 * <p>
 *
 * @author cpacm 2021/11/24
 */
@RunWith(RobolectricTestRunner.class)
public class ProtocolTest {
    private final Application application = ApplicationProvider.getApplicationContext();

    @Before
    public void setup() {
        TrackerContext.init(application);
    }

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

        PageAttributesEvent paEvent = new PageAttributesEvent.Builder()
                .setPath("database")
                .setPageShowTimestamp(1000L)
                .setAttributes(defaultMap)
                .build();
        Truth.assertThat(protocol(paEvent).getPath()).isEqualTo("database");
        Truth.assertThat(protocol(paEvent).getPageShowTimestamp()).isEqualTo(1000L);
        Truth.assertThat(protocol(paEvent).getAttributesOrDefault("user", "gio")).isEqualTo("cpacm");

        CustomEvent customEvent = new CustomEvent.Builder()
                .setEventName("databaseTest")
                .build();
        Truth.assertThat(protocol(customEvent).getEventName()).isEqualTo("databaseTest");

        PageLevelCustomEvent plcEnent = new PageLevelCustomEvent.Builder()
                .setPath("PageLevelCustomEvent")
                .setAttributes(defaultMap)
                .setEventName("page_level")
                .build();
        Truth.assertThat(protocol(plcEnent).getEventName()).isEqualTo("page_level");
        Truth.assertThat(protocol(plcEnent).getAttributesOrDefault("user", "gio")).isEqualTo("cpacm");
        Truth.assertThat(protocol(plcEnent).getPath()).isEqualTo("PageLevelCustomEvent");

        ViewElementEvent vEvent = new ViewElementEvent.Builder()
                .setXpath("xxxx")
                .setEventType("VIEW_CHANGE")
                .setIndex(0)
                .build();
        Truth.assertThat(protocol(vEvent).getXpath()).isEqualTo("xxxx");
        Truth.assertThat(protocol(vEvent).getEventType()).isEqualTo(EventV3Protocol.EventType.VIEW_CHANGE);
        Truth.assertThat(protocol(vEvent).getIndex()).isEqualTo(0);

        HybridCustomEvent hcEvent = new HybridCustomEvent.Builder()
                .setEventName("hybrid")
                .setAttributes(defaultMap)
                .build();
        Truth.assertThat(protocol(hcEvent).getAttributesOrDefault("user", "gio")).isEqualTo("cpacm");
        Truth.assertThat(protocol(hcEvent).getEventName()).isEqualTo("hybrid");

        HybridPageAttributesEvent hpaEvent = new HybridPageAttributesEvent.Builder()
                .setDomain("growingio.com")
                .setQuery("aaaa")
                .setAttributes(defaultMap)
                .setPath("test")
                .build();
        Truth.assertThat(protocol(hpaEvent).getAttributesOrDefault("user", "gio")).isEqualTo("cpacm");
        Truth.assertThat(protocol(hpaEvent).getQuery()).isEqualTo("aaaa");
        Truth.assertThat(protocol(hpaEvent).getDomain()).isEqualTo("growingio.com");
        Truth.assertThat(protocol(hpaEvent).getPath()).isEqualTo("test");

        HybridPageEvent hpEvent = new HybridPageEvent.Builder()
                .setProtocolType("protobuf")
                .build();
        Truth.assertThat(protocol(hpEvent).getProtocolType()).isEqualTo("protobuf");
        Truth.assertThat(protocol(hpEvent).getPath()).isEqualTo("");


        HybridViewElementEvent hvEvent = new HybridViewElementEvent.Builder()
                .setHyperlink("www.cpacm.net")
                .setTextValue("cpacm")
                .build();
        Truth.assertThat(protocol(hvEvent).getTextValue()).isEqualTo("cpacm");
        Truth.assertThat(protocol(hvEvent).getHyperlink()).isEqualTo("www.cpacm.net");

        ResourceItemCustomEvent ricEvent = new ResourceItemCustomEvent.Builder()
                .setResourceItem(new ResourceItem("key", "value"))
                .build();
        Truth.assertThat(protocol(ricEvent).getResourceItem().getKey()).isEqualTo("key");
        Truth.assertThat(protocol(ricEvent).getResourceItem().getId()).isEqualTo("value");

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
        byte[] data = EventProtocolTransfer.protocol(gEvent);
        return EventV3Protocol.EventV3Dto.parseFrom(data);
    }

}
