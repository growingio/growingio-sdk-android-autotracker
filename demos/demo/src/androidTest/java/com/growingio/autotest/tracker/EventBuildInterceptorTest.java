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

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.gio.test.three.DemoApplication;
import com.gio.test.three.core.TrackActivity;
import com.growingio.android.sdk.track.GrowingTracker;
import com.growingio.android.sdk.track.TrackMainThread;
import com.growingio.android.sdk.track.events.EventBuildInterceptor;
import com.growingio.android.sdk.track.events.base.BaseEvent;
import com.growingio.android.sdk.track.middleware.GEvent;
import com.growingio.autotest.EventsTest;
import com.growingio.autotest.TestTrackConfiguration;
import com.growingio.autotest.help.Awaiter;
import com.growingio.autotest.help.BeforeAppOnCreate;
import com.growingio.autotest.help.DataHelper;
import com.growingio.autotest.help.MockEventsApiServer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.atomic.AtomicBoolean;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class EventBuildInterceptorTest extends EventsTest {

    @BeforeAppOnCreate
    public static void beforeAppOnCreate() {
        DataHelper.deleteEventsDatabase();
        DemoApplication.setIsAutotracker(false);
        DemoApplication.setConfiguration(new TestTrackConfiguration());
    }

    @Test
    public void eventBuildInterceptorTest() {
        final AtomicBoolean receivedEvent = new AtomicBoolean(false);
        TrackMainThread.trackMain().addEventBuildInterceptor(new EventBuildInterceptor() {
            @Override
            public void eventWillBuild(BaseEvent.BaseBuilder<?> eventBuilder) {
                eventBuilder.addExtraParam("key1", "value1")
                        .addExtraParam("key2", "value2");
            }

            @Override
            public void eventDidBuild(GEvent event) {

            }
        });


        final String testCustomEvent = "testCustomEvent";
        getEventsApiServer().setOnReceivedEventListener(new MockEventsApiServer.OnReceivedEventListener() {
            int mEventCount = 0;

            @Override
            protected void onReceivedEvents(JSONArray jsonArray) throws JSONException {
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    if (jsonObject.getString("key1").equals("value1")
                            && jsonObject.getString("key2").equals("value2")) {
                        mEventCount++;
                    }
                }
                if (mEventCount >= 2) {
                    receivedEvent.set(true);
                }
            }
        });
        ActivityScenario.launch(TrackActivity.class);
        GrowingTracker.get().trackCustomEvent(testCustomEvent);
        Awaiter.untilTrue(receivedEvent);
    }
}
