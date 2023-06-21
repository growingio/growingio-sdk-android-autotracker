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
import androidx.test.espresso.action.GeneralClickAction;
import androidx.test.espresso.action.GeneralLocation;
import androidx.test.espresso.action.Press;
import androidx.test.espresso.action.Tap;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.gio.test.R;
import com.gio.test.three.DemoApplication;
import com.gio.test.three.autotrack.activity.ClickTestActivity;
import com.gio.test.three.autotrack.activity.ui.login.LoginActivity;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.matcher.ViewMatchers.withId;


@RunWith(AndroidJUnit4.class)
@LargeTest
public class ViewChangeEventsTest extends EventsTest {
    @BeforeAppOnCreate
    public static void beforeAppOnCreate() {
        DataHelper.deleteEventsDatabase();
        DemoApplication.setConfiguration(TestTrackConfiguration.getTestConfig());
    }

    @Test
    public void editTextChangeEventTest() {
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

    @Test
    public void ratingBarClickEventTest() {
        final AtomicBoolean receivedEvent = new AtomicBoolean(false);
        getEventsApiServer().setOnReceivedEventListener(new OnReceivedViewChangeEventsListener(
                receivedEvent,
                new ViewElementEvent.Builder()
                        .setPath("/ClickTestActivity")
                        .setXpath("/Page/ActionBarOverlayLayout[0]/FrameLayout[0]/LinearLayout[0]#content_parent/RatingBar[0]#rating_bar")
                        .setTextValue("3.0")
                        .setIndex(-1)
                        .build()

        ));
        ActivityScenario<ClickTestActivity> scenario = ActivityScenario.launch(ClickTestActivity.class);
        onView(withId(R.id.rating_bar)).perform(click());
        Awaiter.untilTrue(receivedEvent);
        scenario.close();
    }

    @Test
    public void seekBarClickEventTest() {
        final AtomicBoolean receivedEvent = new AtomicBoolean(false);
        getEventsApiServer().setOnReceivedEventListener(new OnReceivedViewChangeEventsListener(
                receivedEvent,
                new ViewElementEvent.Builder()
                        .setPath("/ClickTestActivity")
                        .setXpath("/Page/ActionBarOverlayLayout[0]/FrameLayout[0]/LinearLayout[0]#content_parent/SeekBar[0]#seek_bar")
                        .setTextValue("100")
                        .setIndex(-1)
                        .build()

        ));
        ActivityScenario<ClickTestActivity> scenario = ActivityScenario.launch(ClickTestActivity.class);
        onView(withId(R.id.seek_bar)).perform(new GeneralClickAction(Tap.SINGLE, GeneralLocation.BOTTOM_RIGHT, Press.FINGER));
        Awaiter.untilTrue(receivedEvent);
        scenario.close();
    }

    private static final class OnReceivedViewChangeEventsListener extends MockEventsApiServer.OnReceivedEventListener {
        private final Map<String, Long> mReceivedPages = new HashMap<>();
        private final List<ViewElementEvent> mExpectReceivedChanges;
        private final AtomicBoolean mReceivedEvent;

        OnReceivedViewChangeEventsListener(AtomicBoolean receivedEvents, ViewElementEvent... viewClickEvent) {
            mReceivedEvent = receivedEvents;
            mReceivedEvent.set(false);
            mExpectReceivedChanges = new ArrayList<>(Arrays.asList(viewClickEvent));
        }

        @Override
        protected void onReceivedPageEvents(JSONArray jsonArray) throws JSONException {
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                String path = jsonObject.getString("path");
                mReceivedPages.put(path, jsonObject.getLong("timestamp"));
            }
        }

        @Override
        protected void onReceivedViewChangeEvents(JSONArray jsonArray) throws JSONException {
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                for (int j = 0; j < mExpectReceivedChanges.size(); j++) {
                    System.out.println(jsonObject.toString());
                    String path = jsonObject.getString("path");
                    ViewElementEvent viewElementEvent = mExpectReceivedChanges.get(j);
                    if (path.equals(viewElementEvent.getPath())
                            && jsonObject.getString("xpath").equals(viewElementEvent.getXpath())
                            && jsonObject.optString("textValue").equals(viewElementEvent.getTextValue())
                            && jsonObject.getInt("index") == viewElementEvent.getIndex()
                            && jsonObject.getLong("pageShowTimestamp") == mReceivedPages.get(path)) {
                        mExpectReceivedChanges.remove(j);
                        break;
                    }
                }
                if (mExpectReceivedChanges.isEmpty()) {
                    mReceivedEvent.set(true);
                }
            }
        }
    }
}
