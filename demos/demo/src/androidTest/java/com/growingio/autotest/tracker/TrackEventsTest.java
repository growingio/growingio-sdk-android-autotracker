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

package com.growingio.autotest.tracker;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.gio.test.three.DemoApplication;
import com.gio.test.three.core.TrackActivity;
import com.growingio.android.sdk.track.GrowingTracker;
import com.growingio.autotest.EventsTest;
import com.growingio.autotest.TestTrackConfiguration;
import com.growingio.autotest.help.Awaiter;
import com.growingio.autotest.help.BeforeAppOnCreate;
import com.growingio.autotest.help.DataHelper;
import com.growingio.autotest.help.MockEventsApiServer;
import com.growingio.autotest.help.TrackHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;


@RunWith(AndroidJUnit4.class)
@LargeTest
public class TrackEventsTest extends EventsTest {

    @BeforeAppOnCreate
    public static void beforeAppOnCreate() {
        DataHelper.deleteEventsDatabase();
        DemoApplication.setIsAutotracker(false);
        DemoApplication.setConfiguration(new TestTrackConfiguration());
    }

    @Rule
    public ActivityScenarioRule<TrackActivity> scenarioRule = new ActivityScenarioRule<>(TrackActivity.class);

    @Test
    public void trackCustomEventTest() {
        final AtomicBoolean receivedEvent = new AtomicBoolean(false);
        final String testCustomEvent = "testCustomEvent";
        getEventsApiServer().setOnReceivedEventListener(new MockEventsApiServer.OnReceivedEventListener() {
            @Override
            protected void onReceivedCustomEvents(JSONArray jsonArray) throws JSONException {
                JSONObject jsonObject = jsonArray.getJSONObject(0);
                if (jsonObject.getString("eventName").equals(testCustomEvent)) {
                    receivedEvent.set(true);
                }
            }
        });
        GrowingTracker.get().trackCustomEvent(testCustomEvent);
        Awaiter.untilTrue(receivedEvent);
    }

    @Test
    public void trackAttributesCustomEventTest() {
        final String testCustomEvent = "testAttributeCustomEvent";
        Map<String, String> attributes = new HashMap<>();
        attributes.put("key1", "value1");
        attributes.put("key2", "value2");
        attributes.put("key3", "");
        attributes.put("key4", null);
        final AtomicBoolean receivedEvent = new AtomicBoolean(false);
        GrowingTracker.get().trackCustomEvent(testCustomEvent, attributes);
        getEventsApiServer().setOnReceivedEventListener(new MockEventsApiServer.OnReceivedEventListener() {
            @Override
            protected void onReceivedCustomEvents(JSONArray jsonArray) throws JSONException {
                JSONObject jsonObject = jsonArray.getJSONObject(0);
                if (jsonObject.getString("eventName").equals(testCustomEvent)) {
                    JSONObject attrs = jsonObject.getJSONObject("attributes");
                    if (attrs.getString("key1").equals("value1")
                            && attrs.getString("key2").equals("value2")
                            && attrs.getString("key3").equals("")
                            && attrs.isNull("key4")) {
                        receivedEvent.set(true);
                    }
                }
            }
        });
        Awaiter.untilTrue(receivedEvent);
    }

    @Test
    public void trackVisitorAttributesEventTest() {
        Map<String, String> attributes = new HashMap<>();
        attributes.put("key1", "value1");
        attributes.put("key2", "value2");
        attributes.put("key3", "");
        attributes.put("key4", null);
        final AtomicBoolean receivedEvent = new AtomicBoolean(false);
        GrowingTracker.get().setVisitorAttributes(attributes);
        getEventsApiServer().setOnReceivedEventListener(new MockEventsApiServer.OnReceivedEventListener() {
            @Override
            protected void onReceivedVisitorAttributesEvents(JSONArray jsonArray) throws JSONException {
                JSONObject jsonObject = jsonArray.getJSONObject(0);
                JSONObject attrs = jsonObject.getJSONObject("attributes");
                if (attrs.getString("key1").equals("value1")
                        && attrs.getString("key2").equals("value2")
                        && attrs.getString("key3").equals("")
                        && attrs.isNull("key4")) {
                    receivedEvent.set(true);
                }
            }
        });
        Awaiter.untilTrue(receivedEvent);
    }

    @Test
    public void trackLoginUserAttributesEventTest() {
        Map<String, String> attributes = new HashMap<>();
        attributes.put("key1", "value1");
        attributes.put("key2", "value2");
        attributes.put("key3", "");
        attributes.put("key4", null);
        final AtomicBoolean receivedEvent = new AtomicBoolean(false);
        GrowingTracker.get().setLoginUserAttributes(attributes);
        getEventsApiServer().setOnReceivedEventListener(new MockEventsApiServer.OnReceivedEventListener() {
            @Override
            protected void onReceivedLoginUserAttributesEvents(JSONArray jsonArray) throws JSONException {
                JSONObject jsonObject = jsonArray.getJSONObject(0);
                JSONObject attrs = jsonObject.getJSONObject("attributes");
                if (attrs.getString("key1").equals("value1")
                        && attrs.getString("key2").equals("value2")
                        && attrs.getString("key3").equals("")
                        && attrs.isNull("key4")) {
                    receivedEvent.set(true);
                }
            }
        });
        Awaiter.untilTrue(receivedEvent);
    }

    @Test
    public void trackConversionVariablesEventTest() {
        Map<String, String> attributes = new HashMap<>();
        attributes.put("key1", "value1");
        attributes.put("key2", "value2");
        attributes.put("key3", "");
        attributes.put("key4", null);
        final AtomicBoolean receivedEvent = new AtomicBoolean(false);
        GrowingTracker.get().setConversionVariables(attributes);
        getEventsApiServer().setOnReceivedEventListener(new MockEventsApiServer.OnReceivedEventListener() {
            @Override
            protected void onReceivedConversionVariablesEvents(JSONArray jsonArray) throws JSONException {
                JSONObject jsonObject = jsonArray.getJSONObject(0);
                JSONObject attrs = jsonObject.getJSONObject("attributes");
                if (attrs.getString("key1").equals("value1")
                        && attrs.getString("key2").equals("value2")
                        && attrs.getString("key3").equals("")
                        && attrs.isNull("key4")) {
                    receivedEvent.set(true);
                }
            }
        });
        Awaiter.untilTrue(receivedEvent);
    }

    @Test
    public void invalidArgumentsTest() {
        GrowingTracker.get().trackCustomEvent(null);
        GrowingTracker.get().trackCustomEvent(null, null);
        GrowingTracker.get().trackCustomEvent(null, new HashMap<>());
        GrowingTracker.get().trackCustomEvent("");
        GrowingTracker.get().trackCustomEvent("", null);
        GrowingTracker.get().trackCustomEvent("", new HashMap<>());

        GrowingTracker.get().setVisitorAttributes(null);
        GrowingTracker.get().setVisitorAttributes(new HashMap<>());

        GrowingTracker.get().setLoginUserAttributes(null);
        GrowingTracker.get().setLoginUserAttributes(new HashMap<>());

        GrowingTracker.get().setConversionVariables(null);
        GrowingTracker.get().setConversionVariables(new HashMap<>());

        TrackHelper.waitForIdleSync();
    }
}
