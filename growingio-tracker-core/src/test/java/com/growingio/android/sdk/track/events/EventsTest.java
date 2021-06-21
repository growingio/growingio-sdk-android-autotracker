/*
 *
 *  Copyright (C) 2020 Beijing Yishu Technology Co., Ltd.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package com.growingio.android.sdk.track.events;

import android.app.Application;

import androidx.test.core.app.ApplicationProvider;

import com.google.common.truth.Truth;
import com.growingio.android.sdk.TrackerContext;

import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.HashMap;

@Config(manifest = Config.NONE)
@RunWith(RobolectricTestRunner.class)
public class EventsTest {

    Application application = ApplicationProvider.getApplicationContext();

    @Before
    public void setup() {
        TrackerContext.init(application);
    }

    @Test
    public void eventAppClose() {
        AppClosedEvent event = new AppClosedEvent.Builder().build();
        Truth.assertThat(event.getEventType()).isEqualTo(TrackEventType.APP_CLOSED);
        //TrackEventGenerator.generateAppClosedEvent();
        inRobolectric(event.toJSONObject());
    }

    @Test
    public void eventCV() {
        ConversionVariablesEvent event = new ConversionVariablesEvent.Builder()
                .setAttributes(new HashMap<>())
                .build();
        Truth.assertThat(event.getEventType()).isEqualTo(TrackEventType.CONVERSION_VARIABLES);
        //TrackEventGenerator.generateConversionVariablesEvent(new HashMap<>());
        inRobolectric(event.toJSONObject());
    }

    @Test
    public void eventCustom() {
        CustomEvent event = new CustomEvent.Builder()
                .setEventName("test")
                .setAttributes(new HashMap<>())
                .build();
        Truth.assertThat(event.getEventType()).isEqualTo(TrackEventType.CUSTOM);
        //TrackEventGenerator.generateCustomEvent("test", new HashMap<>());
        inRobolectric(event.toJSONObject());
    }


    @Test
    public void eventLoginUserAttr() {
        LoginUserAttributesEvent event = new LoginUserAttributesEvent.Builder()
                .setAttributes(new HashMap<>())
                .build();
        Truth.assertThat(event.getEventType()).isEqualTo(TrackEventType.LOGIN_USER_ATTRIBUTES);
        //TrackEventGenerator.generateLoginUserAttributesEvent(new HashMap<>());
        inRobolectric(event.toJSONObject());
    }

    @Test
    public void eventPageAttr() {
        PageAttributesEvent event = new PageAttributesEvent.Builder()
                .setPath("/blank")
                .setPageShowTimestamp(System.currentTimeMillis())
                .setAttributes(new HashMap<>())
                .build();
        Truth.assertThat(event.getEventType()).isEqualTo(AutotrackEventType.PAGE_ATTRIBUTES);
        inRobolectric(event.toJSONObject());
    }

    @Test
    public void eventPage() {
        PageEvent event = new PageEvent.Builder()
                .setPath("/blank")
                .setTitle("test title")
                .setOrientation("ver")
                .setReferralPage("test")
                .setTimestamp(System.currentTimeMillis())
                .build();
        Truth.assertThat(event.getEventType()).isEqualTo(AutotrackEventType.PAGE);
        inRobolectric(event.toJSONObject());
    }

    @Test
    public void eventPageLevel() {
        PageLevelCustomEvent event = new PageLevelCustomEvent.Builder()
                .setPath("/blank")
                .setAttributes(new HashMap<>())
                .setPageShowTimestamp(System.currentTimeMillis())
                .setEventName("test")
                .build();
        Truth.assertThat(event.getEventType()).isEqualTo(TrackEventType.CUSTOM);
        inRobolectric(event.toJSONObject());
    }

    @Test
    public void eventViewElement() {
        ViewElementEvent event = new ViewElementEvent.Builder()
                .setPath("/blank")
                .setIndex(1)
                .setTextValue("this is a test element")
                .setXpath("/test/path")
                .setPageShowTimestamp(System.currentTimeMillis())
                .build();
        Truth.assertThat(event.getEventType()).isNull();
        inRobolectric(event.toJSONObject());
    }

    @Test
    public void eventVisit() {
        VisitEvent event = new VisitEvent.Builder()
                .setExtraSdk(new HashMap<>())
                .setTimestamp(System.currentTimeMillis())
                .setSessionId("adfajls")
                .build();
        Truth.assertThat(event.getEventType()).isEqualTo(TrackEventType.VISIT);
        //TrackEventGenerator.generateVisitEvent("adfajls", System.currentTimeMillis());
        inRobolectric(event.toJSONObject());
    }

    @Test
    public void eventVisitAttr() {
        VisitorAttributesEvent event = new VisitorAttributesEvent.Builder()
                .setAttributes(new HashMap<>())
                .build();
        Truth.assertThat(event.getEventType()).isEqualTo(TrackEventType.VISITOR_ATTRIBUTES);
        //TrackEventGenerator.generateVisitorAttributesEvent(new HashMap<>());
        inRobolectric(event.toJSONObject());
    }

    public void inRobolectric(JSONObject jsonObject) {
        Truth.assertThat(jsonObject.opt("domain")).isEqualTo("com.growingio.android.sdk.track.test");
    }


}
