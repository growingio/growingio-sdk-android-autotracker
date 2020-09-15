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

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.gio.test.R;
import com.gio.test.three.DemoApplication;
import com.gio.test.three.autotrack.activity.ClickTestActivity;
import com.growingio.autotest.EventsTest;
import com.growingio.autotest.TestTrackConfiguration;
import com.growingio.autotest.help.BeforeAppOnCreate;
import com.growingio.autotest.help.MockEventsApiServer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.atomic.AtomicBoolean;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class ViewClickEventsTest extends EventsTest {
    @Rule
    public ActivityScenarioRule<ClickTestActivity> scenarioRule = new ActivityScenarioRule<>(ClickTestActivity.class);

    @BeforeAppOnCreate
    public static void beforeAppOnCreate() {
        DemoApplication.setConfiguration(new TestTrackConfiguration());
    }

    @Test
    public void viewClickEventsTest() {
        buttonClickEvent();
    }

    private void buttonClickEvent() {
        final AtomicBoolean receivedEvent = new AtomicBoolean(false);
        getEventsApiServer().setOnReceivedEventListener(new MockEventsApiServer.OnReceivedEventListener() {
            @Override
            protected void onReceivedViewClickEvents(JSONArray jsonArray) throws JSONException {
                JSONObject jsonObject = jsonArray.getJSONObject(0);
                if (jsonObject.getString("pageName").equals("/ClickTestActivity")
                        && jsonObject.getString("xpath").equals("/Page/ActionBarOverlayLayout[0]/FrameLayout[0]/LinearLayout[0]#content_parent/Button[0]#btn_test_click")
                        && jsonObject.getString("textValue").equals("测试Button点击")
                        && jsonObject.getInt("index") == -1) {
                    receivedEvent.set(true);
                }
            }
        });

        onView(withId(R.id.btn_test_click)).perform(click());
        await().atMost(5, SECONDS).untilTrue(receivedEvent);
    }
}
