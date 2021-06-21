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
import com.growingio.android.sdk.autotrack.GrowingAutotracker;
import com.growingio.android.sdk.autotrack.IgnorePolicy;
import com.growingio.android.sdk.track.events.PageLevelCustomEvent;
import com.growingio.autotest.EventsTest;
import com.growingio.autotest.TestTrackConfiguration;
import com.growingio.autotest.help.Awaiter;
import com.growingio.autotest.help.BeforeAppOnCreate;
import com.growingio.autotest.help.DataHelper;
import com.growingio.autotest.help.TrackHelper;
import com.growingio.autotest.help.Uninterruptibles;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.hamcrest.Matchers.not;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class ImpressionEventsTest extends EventsTest {
    private static final HashMap<String, String> TEST_ATTRIBUTES = new HashMap<String, String>() {{
        put("key1", "value1");
        put("key2", "value2");
        put("key3", "");
        put("key4", null);
    }};

    @BeforeAppOnCreate
    public static void beforeAppOnCreate() {
        DataHelper.deleteEventsDatabase();
        DemoApplication.setConfiguration(TestTrackConfiguration.getTestConfig());
    }

    @Test
    public void impressionEventTest() {
        final AtomicBoolean receivedEvent = new AtomicBoolean(false);
        getEventsApiServer().setOnReceivedEventListener(new OnReceivedViewImpressionEventsListener(
                receivedEvent,
                new PageLevelCustomEvent.Builder()
                        .setPath("/ViewImpressionActivity")
                        .setEventName("buttonImpressionEvent")
                        .build()
        ));
        ActivityLifecycleMonitorRegistry.getInstance().addLifecycleCallback((activity, stage) -> {
            if (activity.getClass() == ViewImpressionActivity.class && stage == Stage.CREATED) {
                View button = activity.findViewById(R.id.btn_impression);
                GrowingAutotracker.get().trackViewImpression(button, "buttonImpressionEvent");
            }
        });
        ActivityScenario<ViewImpressionActivity> scenario = ActivityScenario.launch(ViewImpressionActivity.class);
        Awaiter.untilTrue(receivedEvent);
        scenario.close();
    }

    @Test
    public void stopImpressionEventTest() {
        final AtomicBoolean receivedEvent = new AtomicBoolean(false);
        getEventsApiServer().setOnReceivedEventListener(new OnReceivedViewImpressionEventsListener(
                receivedEvent,
                new PageLevelCustomEvent.Builder()
                        .setPath("/ViewImpressionActivity")
                        .setEventName("buttonImpressionEvent")
                        .setAttributes(TEST_ATTRIBUTES)
                        .build()
        ));
        AtomicReference<View> button = new AtomicReference<>();
        ActivityLifecycleMonitorRegistry.getInstance().addLifecycleCallback((activity, stage) -> {
            if (activity.getClass() == ViewImpressionActivity.class && stage == Stage.CREATED) {
                button.set(activity.findViewById(R.id.btn_impression));
                GrowingAutotracker.get().trackViewImpression(button.get(), "buttonImpressionEvent", TEST_ATTRIBUTES);
            }
        });
        ActivityScenario<ViewImpressionActivity> scenario = ActivityScenario.launch(ViewImpressionActivity.class);
        onView(withId(R.id.btn_impression)).check(matches((isDisplayed())));
        Awaiter.untilTrue(receivedEvent);

        getEventsApiServer().setOnReceivedEventListener(new StopReceivedViewImpressionEventsListener());
        Truth.assertThat(button.get() != null).isTrue();
        GrowingAutotracker.get().stopTrackViewImpression(button.get());
        scenario.moveToState(Lifecycle.State.CREATED).moveToState(Lifecycle.State.RESUMED);
        onView(withId(R.id.btn_impression)).check(matches((isDisplayed())));
        TrackHelper.waitForIdleSync();
        Uninterruptibles.sleepUninterruptibly(2, TimeUnit.SECONDS);
        scenario.close();
    }

    @Test
    public void impressionAttributesEventTest() {
        final AtomicBoolean receivedEvent = new AtomicBoolean(false);
        getEventsApiServer().setOnReceivedEventListener(new OnReceivedViewImpressionEventsListener(
                receivedEvent,
                new PageLevelCustomEvent.Builder()
                        .setPath("/ViewImpressionActivity")
                        .setEventName("buttonImpressionEvent")
                        .setAttributes(TEST_ATTRIBUTES)
                        .build()
        ));
        ActivityLifecycleMonitorRegistry.getInstance().addLifecycleCallback((activity, stage) -> {
            if (activity.getClass() == ViewImpressionActivity.class && stage == Stage.CREATED) {
                View button = activity.findViewById(R.id.btn_impression);
                GrowingAutotracker.get().trackViewImpression(button, "buttonImpressionEvent", TEST_ATTRIBUTES);
            }
        });
        ActivityScenario<ViewImpressionActivity> scenario = ActivityScenario.launch(ViewImpressionActivity.class);
        Awaiter.untilTrue(receivedEvent);
        scenario.close();
    }

    @Test
    public void impressionEventsTest() {
        final AtomicBoolean receivedEvent = new AtomicBoolean(false);
        getEventsApiServer().setOnReceivedEventListener(new OnReceivedViewImpressionEventsListener(
                receivedEvent,
                new PageLevelCustomEvent.Builder()
                        .setPath("/ViewImpressionActivity")
                        .setEventName("buttonImpressionEvent")
                        .setAttributes(TEST_ATTRIBUTES)
                        .build(),
                new PageLevelCustomEvent.Builder()
                        .setPath("/ViewImpressionActivity")
                        .setEventName("imageViewImpressionEvent")
                        .build(),
                new PageLevelCustomEvent.Builder()
                        .setPath("/ViewImpressionActivity")
                        .setEventName("blankViewImpressionEvent")
                        .build()
        ));
        ActivityLifecycleMonitorRegistry.getInstance().addLifecycleCallback((activity, stage) -> {
            if (activity.getClass() == ViewImpressionActivity.class && stage == Stage.CREATED) {
                View button = activity.findViewById(R.id.btn_impression);
                GrowingAutotracker.get().trackViewImpression(button, "buttonImpressionEvent", TEST_ATTRIBUTES);

                View imageView = activity.findViewById(R.id.iv_impression);
                GrowingAutotracker.get().trackViewImpression(imageView, "imageViewImpressionEvent");

                View blankView = activity.findViewById(R.id.blank_view_impression);
                GrowingAutotracker.get().trackViewImpression(blankView, "blankViewImpressionEvent");
            }
        });
        ActivityScenario<ViewImpressionActivity> scenario = ActivityScenario.launch(ViewImpressionActivity.class);
        Awaiter.untilTrue(receivedEvent);
        scenario.close();
    }

    @Test
    public void impressionEventFromGoneTest() {
        final AtomicBoolean receivedEvent = new AtomicBoolean(false);
        getEventsApiServer().setOnReceivedEventListener(new OnReceivedViewImpressionEventsListener(
                receivedEvent,
                new PageLevelCustomEvent.Builder()
                        .setPath("/ViewImpressionActivity")
                        .setEventName("buttonImpressionEvent")
                        .setAttributes(TEST_ATTRIBUTES)
                        .build()
        ));
        AtomicReference<View> button = new AtomicReference<>();
        ActivityLifecycleMonitorRegistry.getInstance().addLifecycleCallback((activity, stage) -> {
            if (activity.getClass() == ViewImpressionActivity.class && stage == Stage.CREATED) {
                button.set(activity.findViewById(R.id.btn_impression));
                button.get().setVisibility(View.GONE);
                GrowingAutotracker.get().trackViewImpression(button.get(), "buttonImpressionEvent", TEST_ATTRIBUTES);
            }
        });
        ActivityScenario<ViewImpressionActivity> scenario = ActivityScenario.launch(ViewImpressionActivity.class);
        onView(withId(R.id.btn_impression)).check(matches(not((isDisplayed()))));
        Truth.assertThat(button.get() != null).isTrue();
        button.get().post(() -> button.get().setVisibility(View.VISIBLE));
        Awaiter.untilTrue(receivedEvent);
        scenario.close();
    }

    @Test
    public void impressionEventFromInvisibleTest() {
        final AtomicBoolean receivedEvent = new AtomicBoolean(false);
        getEventsApiServer().setOnReceivedEventListener(new OnReceivedViewImpressionEventsListener(
                receivedEvent,
                new PageLevelCustomEvent.Builder()
                        .setPath("/ViewImpressionActivity")
                        .setEventName("buttonImpressionEvent")
                        .setAttributes(TEST_ATTRIBUTES)
                        .build()
        ));
        AtomicReference<View> button = new AtomicReference<>();
        ActivityLifecycleMonitorRegistry.getInstance().addLifecycleCallback((activity, stage) -> {
            if (activity.getClass() == ViewImpressionActivity.class && stage == Stage.CREATED) {
                button.set(activity.findViewById(R.id.btn_impression));
                button.get().setVisibility(View.INVISIBLE);
                GrowingAutotracker.get().trackViewImpression(button.get(), "buttonImpressionEvent", TEST_ATTRIBUTES);
            }
        });
        ActivityScenario<ViewImpressionActivity> scenario = ActivityScenario.launch(ViewImpressionActivity.class);
        onView(withId(R.id.btn_impression)).check(matches(not((isDisplayed()))));
        Truth.assertThat(button.get() != null).isTrue();
        button.get().post(() -> button.get().setVisibility(View.VISIBLE));
        Awaiter.untilTrue(receivedEvent);
        scenario.close();
    }

    @Test
    public void impressionEventFromNotDisplayedTest() {
        final AtomicBoolean receivedEvent = new AtomicBoolean(false);
        getEventsApiServer().setOnReceivedEventListener(new StopReceivedViewImpressionEventsListener());
        AtomicReference<View> textView = new AtomicReference<>();
        ActivityLifecycleMonitorRegistry.getInstance().addLifecycleCallback((activity, stage) -> {
            if (activity.getClass() == ViewImpressionActivity.class && stage == Stage.CREATED) {
                textView.set(activity.findViewById(R.id.tv_impression));
                GrowingAutotracker.get().trackViewImpression(textView.get(), "textViewImpressionEvent", TEST_ATTRIBUTES);
            }
        });
        ActivityScenario<ViewImpressionActivity> scenario = ActivityScenario.launch(ViewImpressionActivity.class);
        onView(withId(R.id.tv_impression)).check(matches(not((isDisplayed()))));
        Truth.assertThat(textView.get() != null).isTrue();
        TrackHelper.waitForIdleSync();
        Uninterruptibles.sleepUninterruptibly(2, TimeUnit.SECONDS);
        Awaiter.untilFalse(receivedEvent);

        getEventsApiServer().setOnReceivedEventListener(new OnReceivedViewImpressionEventsListener(
                receivedEvent,
                new PageLevelCustomEvent.Builder()
                        .setPath("/ViewImpressionActivity")
                        .setEventName("textViewImpressionEvent")
                        .setAttributes(TEST_ATTRIBUTES)
                        .build()
        ));
        scenario.moveToState(Lifecycle.State.CREATED).moveToState(Lifecycle.State.RESUMED);
        onView(withId(R.id.tv_impression)).perform(scrollTo());
        Awaiter.untilTrue(receivedEvent);
        scenario.close();
    }

    @Test
    public void impressionEventForIgnoreTest() {
        getEventsApiServer().setOnReceivedEventListener(new StopReceivedViewImpressionEventsListener());
        ActivityLifecycleMonitorRegistry.getInstance().addLifecycleCallback((activity, stage) -> {
            if (activity.getClass() == ViewImpressionActivity.class && stage == Stage.CREATED) {
                GrowingAutotracker.get().ignoreView(activity.getWindow().getDecorView(), IgnorePolicy.IGNORE_ALL);
                View button = activity.findViewById(R.id.btn_impression);
                GrowingAutotracker.get().trackViewImpression(button, "buttonImpressionEvent", TEST_ATTRIBUTES);
            }
        });
        ActivityScenario<ViewImpressionActivity> scenario = ActivityScenario.launch(ViewImpressionActivity.class);
        onView(withId(R.id.btn_impression)).check(matches((isDisplayed())));
        TrackHelper.waitForIdleSync();
        Uninterruptibles.sleepUninterruptibly(2, TimeUnit.SECONDS);
        scenario.close();
    }

    @Test
    public void invalidArgumentsTest() {
        ActivityScenario<ViewImpressionActivity> scenario = ActivityScenario.launch(ViewImpressionActivity.class);
        GrowingAutotracker.get().trackViewImpression(null, null);
        GrowingAutotracker.get().trackViewImpression(null, "");
        GrowingAutotracker.get().stopTrackViewImpression(null);
        TrackHelper.waitForIdleSync();
        scenario.close();
    }

}
