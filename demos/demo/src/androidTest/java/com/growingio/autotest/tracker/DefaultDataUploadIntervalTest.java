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
package com.growingio.autotest.tracker;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.gio.test.three.DemoApplication;
import com.gio.test.three.MainActivity;
import com.growingio.android.sdk.track.log.DebugLogger;
import com.growingio.android.sdk.track.log.Logger;
import com.growingio.autotest.EventsTest;
import com.growingio.autotest.TestTrackConfiguration;
import com.growingio.autotest.help.BeforeAppOnCreate;
import com.growingio.autotest.help.DataHelper;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class DefaultDataUploadIntervalTest extends EventsTest {
    private static volatile long sStartedTime = 0;

    @BeforeAppOnCreate
    public static void beforeAppOnCreate() {
        DataHelper.deleteEventsDatabase();
        DemoApplication.setConfiguration(TestTrackConfiguration.getTestConfig()
                .setDebugEnabled(false)
        );
        sStartedTime = System.currentTimeMillis();
    }

    @Override
    public void setUp() throws IOException {
        super.setUp();
        Logger.addLogger(new DebugLogger());
    }

    @Rule
    public ActivityScenarioRule<MainActivity> scenarioRule = new ActivityScenarioRule<>(MainActivity.class);

    @Test
    public void defaultDelayDataUploadTest() {
        /*Truth.assertThat(sStartedTime > 0).isTrue();

        final AtomicLong receivedVisitEventTime = new AtomicLong(0);
        final AtomicLong receivedTrackEventTime = new AtomicLong(0);
        final String testCustomEvent = "testCustomEvent";
        getEventsApiServer().setOnReceivedEventListener(new MockEventsApiServer.OnReceivedEventListener() {
            @Override
            protected void onReceivedVisitEvents(JSONArray jsonArray) throws JSONException {
                receivedVisitEventTime.set(System.currentTimeMillis());
            }

            @Override
            protected void onReceivedCustomEvents(JSONArray jsonArray) throws JSONException {
                JSONObject jsonObject = jsonArray.getJSONObject(0);
                if (jsonObject.getString("eventName").equals(testCustomEvent)) {
                    receivedTrackEventTime.set(System.currentTimeMillis());
                }
            }
        });

        AtomicLong activityStartedTime = new AtomicLong(0);
        ActivityLifecycleMonitorRegistry.getInstance().addLifecycleCallback(new ActivityLifecycleCallback() {
            @Override
            public void onActivityLifecycleChanged(Activity activity, Stage stage) {
                if (stage == Stage.STARTED) {
                    activityStartedTime.set(System.currentTimeMillis());
                }
            }
        });
        ActivityScenario.launch(MainActivity.class);
        Awaiter.untilTrue(() -> activityStartedTime.get() > 0);
        Awaiter.untilTrue(() -> receivedVisitEventTime.get() > 0 && (receivedVisitEventTime.get() - activityStartedTime.get()) < 1000);

        GrowingTracker.get().trackCustomEvent(testCustomEvent);
        Awaiter.untilTrue(() -> receivedTrackEventTime.get() > 0
                        && (receivedTrackEventTime.get() - sStartedTime) > 14000
                        && (receivedTrackEventTime.get() - sStartedTime) < 16000,
                16, TimeUnit.SECONDS);

        receivedTrackEventTime.set(0);
        GrowingTracker.get().trackCustomEvent(testCustomEvent);
        Awaiter.untilTrue(() -> receivedTrackEventTime.get() > 0
                        && (receivedTrackEventTime.get() - sStartedTime) > 29000
                        && (receivedTrackEventTime.get() - sStartedTime) < 31000,
                16, TimeUnit.SECONDS);*/
    }
}
