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
import com.gio.test.three.MainActivity;
import com.google.common.truth.Truth;
import com.growingio.android.sdk.track.GrowingTracker;
import com.growingio.autotest.EventsTest;
import com.growingio.autotest.TestTrackConfiguration;
import com.growingio.autotest.help.Awaiter;
import com.growingio.autotest.help.BeforeAppOnCreate;
import com.growingio.autotest.help.DataHelper;
import com.growingio.autotest.help.MockEventsApiServer;
import com.growingio.autotest.help.Uninterruptibles;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.atomic.AtomicBoolean;

import static java.util.concurrent.TimeUnit.SECONDS;


@RunWith(AndroidJUnit4.class)
@LargeTest
public class DataCollectionEnabledTest extends EventsTest {
    @Rule
    public ActivityScenarioRule<MainActivity> scenarioRule = new ActivityScenarioRule<>(MainActivity.class);

    @BeforeAppOnCreate
    public static void beforeAppOnCreate() {
        DataHelper.deleteEventsDatabase();
        DemoApplication.setIsAutotracker(false);
        DemoApplication.setConfiguration(new TestTrackConfiguration()
                .setDataCollectionEnabled(false)
        );
    }

    @Test
    public void dataCollectionEnabledTest() {
        dataCollectionDisabled();
        dataCollectionFirstEnabled();

        GrowingTracker.get().setDataCollectionEnabled(false);
        dataCollectionSecondEnabled();
    }

    private void dataCollectionDisabled() {
        getEventsApiServer().setOnReceivedEventListener(new MockEventsApiServer.OnReceivedEventListener() {
            @Override
            protected void onReceivedEvents(JSONArray jsonArray) throws JSONException {
                Truth.assertWithMessage("Received Events").fail();
            }
        });
        Uninterruptibles.sleepUninterruptibly(1, SECONDS);
        GrowingTracker.get().trackCustomEvent("test");
        Uninterruptibles.sleepUninterruptibly(1, SECONDS);
    }

    private void dataCollectionFirstEnabled() {
        final AtomicBoolean receivedVisit = new AtomicBoolean(false);
        final AtomicBoolean receivedCustom = new AtomicBoolean(false);
        getEventsApiServer().setOnReceivedEventListener(new MockEventsApiServer.OnReceivedEventListener() {
            @Override
            protected void onReceivedVisitEvents(JSONArray jsonArray) throws JSONException {
                receivedVisit.set(true);
            }

            @Override
            protected void onReceivedCustomEvents(JSONArray jsonArray) throws JSONException {
                JSONObject jsonObject = jsonArray.getJSONObject(0);
                if (jsonObject.getString("eventName").equals("test")) {
                    receivedCustom.set(true);
                }
            }
        });
        GrowingTracker.get().setDataCollectionEnabled(true);
        Awaiter.untilTrue(receivedVisit);

        GrowingTracker.get().trackCustomEvent("test");
        Awaiter.untilTrue(receivedCustom);
    }

    private void dataCollectionSecondEnabled() {
        final AtomicBoolean receivedCustom = new AtomicBoolean(false);
        getEventsApiServer().setOnReceivedEventListener(new MockEventsApiServer.OnReceivedEventListener() {
            @Override
            protected void onReceivedVisitEvents(JSONArray jsonArray) throws JSONException {
                Truth.assertWithMessage("Received Events").fail();
            }

            @Override
            protected void onReceivedCustomEvents(JSONArray jsonArray) throws JSONException {
                JSONObject jsonObject = jsonArray.getJSONObject(0);
                if (jsonObject.getString("eventName").equals("test")) {
                    receivedCustom.set(true);
                }
            }
        });
        GrowingTracker.get().setDataCollectionEnabled(true);
        Uninterruptibles.sleepUninterruptibly(1, SECONDS);

        GrowingTracker.get().trackCustomEvent("test");
        Awaiter.untilTrue(receivedCustom);
    }
}
