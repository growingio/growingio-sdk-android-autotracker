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

package com.growingio.autotest.autotracker;

import androidx.test.core.app.ActivityScenario;

import com.gio.test.three.DemoApplication;
import com.gio.test.three.MainActivity;
import com.gio.test.three.autotrack.activity.NestedFragmentActivity;
import com.google.common.truth.Truth;
import com.growingio.autotest.EventsTest;
import com.growingio.autotest.TestTrackConfiguration;
import com.growingio.autotest.help.BeforeAppOnCreate;
import com.growingio.autotest.help.MockEventsApiServer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;

public class PageEventsTest extends EventsTest {
    private static final String TAG = "PageEventsTest";

    @BeforeAppOnCreate
    public static void beforeAppOnCreate() {
        DemoApplication.setConfiguration(new TestTrackConfiguration());
    }

    @Test
    public void pageEventsTest() {
        activityPageEvent();
        fragmentPageEvent();
    }

    private void activityPageEvent() {
        final AtomicBoolean receivedEvent = new AtomicBoolean(false);
        getEventsApiServer().setOnReceivedEventListener(new MockEventsApiServer.OnReceivedEventListener() {
            @Override
            protected void onReceivedPageEvents(JSONArray jsonArray) throws JSONException {
                JSONObject jsonObject = jsonArray.getJSONObject(0);
                if (jsonObject.getString("pageName").equals("/MainActivity")
                        && jsonObject.getString("orientation").equals("PORTRAIT")
                        && jsonObject.getString("title").equals("demos")) {
                    receivedEvent.set(true);
                }
            }
        });
        ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class);
        await().atMost(5, SECONDS).untilTrue(receivedEvent);
        scenario.close();
    }

    private void fragmentPageEvent() {
        final AtomicBoolean receivedEvent = new AtomicBoolean(false);
        getEventsApiServer().setOnReceivedEventListener(new MockEventsApiServer.OnReceivedEventListener() {
            private final List<String> mPages = Arrays.asList(
                    "/NestedFragmentActivity",
                    "/NestedFragmentActivity/GreenFragment[fragment1]",
                    "/NestedFragmentActivity/GreenFragment[fragment1]/OrangeFragment[TestTag]",
                    "*/GreenFragment[fragment1]/OrangeFragment[TestTag]/RedFragment[small]",
                    "/NestedFragmentActivity/RedFragment[fragment2]"
            );

            private final Map<String, Long> mReceivedPages = new HashMap<>();

            @Override
            protected void onReceivedPageEvents(JSONArray jsonArray) throws JSONException {
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    if (jsonObject.getString("orientation").equals("PORTRAIT")) {
                        mReceivedPages.put(jsonObject.getString("pageName"), jsonObject.getLong("timestamp"));
                    }
                }

                // 同时需要对比时间戳
                if (mReceivedPages.size() == mPages.size()) {
                    long timestamp = 1;
                    for (String page : mPages) {
                        if (mReceivedPages.get(page) >= timestamp) {
                            timestamp = mReceivedPages.get(page);
                        } else {
                            Truth.assertWithMessage("Received Page timestamp is " + mReceivedPages.get(page) + " < " + timestamp).fail();
                        }
                    }
                    receivedEvent.set(true);
                }
            }
        });

        ActivityScenario<NestedFragmentActivity> scenario = ActivityScenario.launch(NestedFragmentActivity.class);
        await().atMost(500, SECONDS).untilTrue(receivedEvent);
        scenario.close();
    }
}
