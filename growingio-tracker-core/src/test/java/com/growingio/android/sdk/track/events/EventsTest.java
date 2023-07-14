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

package com.growingio.android.sdk.track.events;

import android.app.Application;

import androidx.test.core.app.ApplicationProvider;

import com.google.common.truth.Truth;
import com.growingio.android.sdk.TrackerContext;
import com.growingio.android.sdk.track.events.base.BaseEvent;
import com.growingio.android.sdk.track.providers.EventStateProvider;

import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        inRobolectric(event);
    }

    @Test
    public void eventCV() {
        ConversionVariablesEvent event = new ConversionVariablesEvent.Builder()
                .setAttributes(new HashMap<>())
                .build();
        Truth.assertThat(event.getEventType()).isEqualTo(TrackEventType.CONVERSION_VARIABLES);
        //TrackEventGenerator.generateConversionVariablesEvent(new HashMap<>());
        inRobolectric(event);
    }

    @Test
    public void eventCustom() {
        CustomEvent event = new CustomEvent.Builder()
                .setEventName("test")
                .setAttributes(new HashMap<>())
                .build();
        Truth.assertThat(event.getEventType()).isEqualTo(TrackEventType.CUSTOM);
        //TrackEventGenerator.generateCustomEvent("test", new HashMap<>());
        inRobolectric(event);
    }

    @Test
    public void eventCustomAttrBuilder() {
        List<String> list = new ArrayList<>();
        list.add("1111");
        list.add("2222");
        AttributesBuilder builder = new AttributesBuilder();
        Map<String, String> map = builder
                .addAttribute("key1", "value1")
                .addAttribute("key2", Arrays.asList(1, 2, 3, 4))
                .addAttribute(null, "value3")
                .addAttribute(null, Arrays.asList(5, 4, 3, 2))
                .addAttribute("key5", Arrays.asList())
                .addAttribute(null, (List) null)
                .addAttribute(null, (String) null)
                .addAttribute("key8", "value8")
                .addAttribute("key9", list)
                .addAttribute("", Arrays.asList("", "", ""))
                .addAttribute("key10", Arrays.asList(null, "1"))
                .build();
        Truth.assertThat(map.size()).isEqualTo(6);
        Truth.assertThat(map.containsKey("key5")).isFalse();
        Truth.assertThat("1||2||3||4").isEqualTo(map.get("key2"));
        Truth.assertThat("1111||2222").isEqualTo(map.get("key9"));
        Truth.assertThat("||||").isEqualTo(map.get(""));
        Truth.assertThat("||1").isEqualTo(map.get("key10"));
    }


    @Test
    public void eventLoginUserAttr() {
        LoginUserAttributesEvent event = new LoginUserAttributesEvent.Builder()
                .setAttributes(new HashMap<>())
                .build();
        Truth.assertThat(event.getEventType()).isEqualTo(TrackEventType.LOGIN_USER_ATTRIBUTES);
        //TrackEventGenerator.generateLoginUserAttributesEvent(new HashMap<>());
        inRobolectric(event);
    }

    @Test
    public void eventLoginUserBuilder() {
        List<String> list = new ArrayList<>();
        list.add("1111");
        list.add("2222");
        AttributesBuilder builder = new AttributesBuilder();
        Map<String, String> map = builder
                .addAttribute("key1", "value1")
                .addAttribute("key2", Arrays.asList(1, 2, 3, 4))
                .addAttribute(null, "value3")
                .addAttribute(null, Arrays.asList(5, 4, 3, 2))
                .addAttribute("key5", Arrays.asList())
                .addAttribute(null, (List) null)
                .addAttribute(null, (String) null)
                .addAttribute("key8", "value8")
                .addAttribute("key9", list)
                .addAttribute("", Arrays.asList("", "", ""))
                .addAttribute("key10", Arrays.asList(null, "1"))
                .build();
        Truth.assertThat(map.size()).isEqualTo(6);
        Truth.assertThat(map.containsKey("key5")).isFalse();
        Truth.assertThat("1||2||3||4").isEqualTo(map.get("key2"));
        Truth.assertThat("1111||2222").isEqualTo(map.get("key9"));
        Truth.assertThat("||||").isEqualTo(map.get(""));
        Truth.assertThat("||1").isEqualTo(map.get("key10"));
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
        inRobolectric(event);
    }

    @Test
    public void eventPageLevel() {
        PageLevelCustomEvent event = new PageLevelCustomEvent.Builder()
                .setPath("/blank")
                .build();
        Truth.assertThat(event.getEventType()).isEqualTo(TrackEventType.CUSTOM);
        inRobolectric(event);
    }

    @Test
    public void eventViewElement() {
        ViewElementEvent event = new ViewElementEvent.Builder(AutotrackEventType.VIEW_CLICK)
                .setPath("/blank")
                .setIndex(1)
                .setTextValue("this is a test element")
                .setXpath("/test/path")
                .setPageShowTimestamp(System.currentTimeMillis())
                .build();
        Truth.assertThat(event.getEventType()).isEqualTo(AutotrackEventType.VIEW_CLICK);
        inRobolectric(event);
    }

    @Test
    public void eventVisit() {
        VisitEvent event = new VisitEvent.Builder()
                .setExtraSdk(new HashMap<>())
                .build();
        Truth.assertThat(event.getEventType()).isEqualTo(TrackEventType.VISIT);
        //TrackEventGenerator.generateVisitEvent("adfajls", System.currentTimeMillis());
        inRobolectric(event);
    }

    @Test
    public void eventVisitAttr() {
        VisitorAttributesEvent event = new VisitorAttributesEvent.Builder()
                .setAttributes(new HashMap<>())
                .build();
        Truth.assertThat(event.getEventType()).isEqualTo(TrackEventType.VISITOR_ATTRIBUTES);
        //TrackEventGenerator.generateVisitorAttributesEvent(new HashMap<>());
        inRobolectric(event);
    }

    public void inRobolectric(BaseEvent event) {
        JSONObject jsonObject = EventStateProvider.get().toJson(event);
        Truth.assertThat(jsonObject.opt("platform")).isEqualTo("Android");
    }


}
