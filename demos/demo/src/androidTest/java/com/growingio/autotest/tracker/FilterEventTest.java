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
import com.growingio.android.sdk.track.events.FilterEventParams;
import com.growingio.android.sdk.track.events.ViewElementEvent;
import com.growingio.autotest.EventsTest;
import com.growingio.autotest.TestTrackConfiguration;
import com.growingio.autotest.help.Awaiter;
import com.growingio.autotest.help.BeforeAppOnCreate;
import com.growingio.autotest.help.DataHelper;
import com.growingio.autotest.help.MockEventsApiServer;
import com.growingio.autotest.help.TrackHelper;

import org.json.JSONArray;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.atomic.AtomicBoolean;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class FilterEventTest extends EventsTest {

    @BeforeAppOnCreate
    public static void beforeAppOnCreate() {
        DataHelper.deleteEventsDatabase();
        DemoApplication.setConfiguration(TestTrackConfiguration.getTestConfig()
                .setFilterEvent(FilterEventParams.of(FilterEventParams.MASK_CLICK_CHANGE_SUBMIT))
        );
    }

    @Test
    public void filterClickChangeEventTest() {
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
         * 根据设置过滤的事件类型设置监听，如果监听到本应该过滤的事件类型，返回 false
         */
        @Override
        protected void onReceivedViewClickEvents(JSONArray jsonArray) {
            mReceivedEvent.set(false);
        }

        @Override
        protected void onReceivedViewChangeEvents(JSONArray jsonArray) {
            mReceivedEvent.set(false);
        }

        @Override
        protected void onReceivedHybridFormSubmitEvents(JSONArray jsonArray) {
            mReceivedEvent.set(false);
        }
    }

}
