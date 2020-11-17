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

package com.growingio.autotest.autotracker.impression;

import android.view.View;

import androidx.lifecycle.Lifecycle;
import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.runner.lifecycle.ActivityLifecycleMonitorRegistry;
import androidx.test.runner.lifecycle.Stage;

import com.gio.test.R;
import com.gio.test.three.DemoApplication;
import com.gio.test.three.autotrack.activity.ViewImpressionActivity;
import com.google.common.truth.Truth;
import com.growingio.android.sdk.autotrack.AutotrackConfiguration;
import com.growingio.android.sdk.autotrack.GrowingAutotracker;
import com.growingio.android.sdk.autotrack.events.PageLevelCustomEvent;
import com.growingio.autotest.EventsTest;
import com.growingio.autotest.TestTrackConfiguration;
import com.growingio.autotest.help.Awaiter;
import com.growingio.autotest.help.BeforeAppOnCreate;
import com.growingio.autotest.help.DataHelper;
import com.growingio.autotest.help.TrackHelper;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.atomic.AtomicBoolean;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class TrackConfigurationImpressionScaleTest extends EventsTest {
    @BeforeAppOnCreate
    public static void beforeAppOnCreate() {
        DataHelper.deleteEventsDatabase();
        DemoApplication.setConfiguration(new TestTrackConfiguration().setImpressionScale(1));
    }

    @Test
    public void impressionEventTest() {
        final AtomicBoolean receivedEvent = new AtomicBoolean(false);
        getEventsApiServer().setOnReceivedEventListener(new StopReceivedViewImpressionEventsListener());
        ActivityLifecycleMonitorRegistry.getInstance().addLifecycleCallback((activity, stage) -> {
            if (activity.getClass() == ViewImpressionActivity.class && stage == Stage.CREATED) {
                View blankView = activity.findViewById(R.id.blank_view_impression);
                GrowingAutotracker.get().trackViewImpression(blankView, "blankViewImpressionEvent");
            }
        });
        ActivityScenario<ViewImpressionActivity> scenario = ActivityScenario.launch(ViewImpressionActivity.class);
        onView(withId(R.id.blank_view_impression)).check(matches((isDisplayed())));
        TrackHelper.waitForIdleSync();
        Awaiter.untilFalse(receivedEvent);

        getEventsApiServer().setOnReceivedEventListener(new OnReceivedViewImpressionEventsListener(
                receivedEvent,
                new PageLevelCustomEvent.Builder()
                        .setPageName("/ViewImpressionActivity")
                        .setEventName("blankViewImpressionEvent")
                        .build()
        ));
        scenario.moveToState(Lifecycle.State.CREATED).moveToState(Lifecycle.State.RESUMED);
        onView(withId(R.id.tv_impression)).perform(scrollTo());
        Awaiter.untilTrue(receivedEvent);
        scenario.close();
    }

    @Test
    public void invalidArgumentsTest() {
        ActivityScenario<ViewImpressionActivity> scenario = ActivityScenario.launch(ViewImpressionActivity.class);
        AutotrackConfiguration configuration = new AutotrackConfiguration("xxx", "xxx");
        configuration.setImpressionScale(-1);
        Truth.assertThat(configuration.getImpressionScale()).isEqualTo(0);
        configuration.setImpressionScale(1.12F);
        Truth.assertThat(configuration.getImpressionScale()).isEqualTo(1);
        scenario.close();
    }
}
