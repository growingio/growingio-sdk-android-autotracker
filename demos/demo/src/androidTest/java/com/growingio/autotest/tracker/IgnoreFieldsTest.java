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

import com.gio.test.R;
import com.gio.test.three.DemoApplication;
import com.gio.test.three.autotrack.activity.ui.login.LoginActivity;
import com.growingio.android.sdk.track.events.IgnoreFieldsParams;
import com.growingio.android.sdk.track.events.ViewElementEvent;
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
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.atomic.AtomicBoolean;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class IgnoreFieldsTest extends EventsTest {

    @BeforeAppOnCreate
    public static void beforeAppOnCreate() {
        DataHelper.deleteEventsDatabase();
        DemoApplication.setConfiguration(TestTrackConfiguration.getTestConfig()
                .setIgnoreFieldsMask(IgnoreFieldsParams.of(IgnoreFieldsParams.IGNORE_ALL_FIELDS))
        );
    }

    @Test
    public void ignoreFieldsAllTest() {
        final AtomicBoolean receivedEvent = new AtomicBoolean(false);
        getEventsApiServer().setOnReceivedEventListener(new OnReceivedViewChangeEventsListener(
                receivedEvent,
                new ViewElementEvent.Builder()
                        .setPath("/LoginActivity")
                        .setXpath("/Page/LinearLayout[0]/FrameLayout[0]/FitWindowsLinearLayout[0]#action_bar_root/ContentFrameLayout[0]/ConstraintLayout[0]#container/AppCompatEditText[0]#username")
                        .setTextValue("")
                        .setIndex(-1)
                        .build(),
                new ViewElementEvent.Builder()
                        .setPath("/LoginActivity")
                        .setXpath("/Page/LinearLayout[0]/FrameLayout[0]/FitWindowsLinearLayout[0]#action_bar_root/ContentFrameLayout[0]/ConstraintLayout[0]#container/AppCompatEditText[1]#password")
                        .setTextValue("")
                        .setIndex(-1)
                        .build()

        ));
        ActivityScenario.launch(LoginActivity.class);
        onView(withId(R.id.username)).perform(typeText("sdk@growing.io"));
        onView(withId(R.id.password)).perform(typeText("password123456"));
        onView(withId(R.id.login)).perform(click());
        TrackHelper.waitForIdleSync();
        Awaiter.untilTrue(receivedEvent);
    }

    private static final class OnReceivedViewChangeEventsListener extends MockEventsApiServer.OnReceivedEventListener {
        private final AtomicBoolean mReceivedEvent;

        OnReceivedViewChangeEventsListener(AtomicBoolean receivedEvents, ViewElementEvent... viewClickEvent) {
            mReceivedEvent = receivedEvents;
            mReceivedEvent.set(true);
        }

        /**
         * 监听事件进行解析，如果事件中包含本应该被过滤的字段返回false
         */
        @Override
        protected void onReceivedVisitEvents(JSONArray jsonArray) throws JSONException {
            isContainIgnoreFields(jsonArray);
        }

        @Override
        protected void onReceivedPageEvents(JSONArray jsonArray) throws JSONException {
            isContainIgnoreFields(jsonArray);
        }

        @Override
        protected void onReceivedViewClickEvents(JSONArray jsonArray) throws JSONException {
            isContainIgnoreFields(jsonArray);
        }

        @Override
        protected void onReceivedViewChangeEvents(JSONArray jsonArray) throws JSONException {
            isContainIgnoreFields(jsonArray);
        }

        private void isContainIgnoreFields(JSONArray jsonArray) throws JSONException {
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                if (jsonObject.opt("networkState") != null
                        || jsonObject.opt("screenHeight") != null
                        || jsonObject.opt("screenWidth") != null
                        || jsonObject.opt("deviceBrand") != null
                        || jsonObject.opt("deviceModel") != null
                        || jsonObject.opt("deviceType") != null) {
                    mReceivedEvent.set(false);
                }
            }
        }
    }

}
