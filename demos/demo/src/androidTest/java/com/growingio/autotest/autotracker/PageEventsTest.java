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

import androidx.lifecycle.Lifecycle;
import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.runner.lifecycle.ActivityLifecycleCallback;
import androidx.test.runner.lifecycle.ActivityLifecycleMonitorRegistry;
import androidx.test.runner.lifecycle.Stage;

import com.gio.test.R;
import com.gio.test.three.DemoApplication;
import com.gio.test.three.MainActivity;
import com.gio.test.three.autotrack.activity.HideFragmentActivity;
import com.gio.test.three.autotrack.activity.NavFragmentActivity;
import com.gio.test.three.autotrack.activity.NestedFragmentActivity;
import com.gio.test.three.autotrack.activity.TabFragmentActivity;
import com.gio.test.three.autotrack.fragments.FragmentLifecycleCallback;
import com.gio.test.three.autotrack.fragments.FragmentLifecycleMonitor;
import com.gio.test.three.autotrack.fragments.GreenFragment;
import com.google.common.truth.Truth;
import com.growingio.android.sdk.autotrack.GrowingAutotracker;
import com.growingio.android.sdk.autotrack.IgnorePolicy;
import com.growingio.autotest.EventsTest;
import com.growingio.autotest.TestTrackConfiguration;
import com.growingio.autotest.help.BeforeAppOnCreate;
import com.growingio.autotest.help.DataHelper;
import com.growingio.autotest.help.MockEventsApiServer;
import com.growingio.autotest.help.TrackHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class PageEventsTest extends EventsTest {
    private static final String TAG = "PageEventsTest";

    @BeforeAppOnCreate
    public static void beforeAppOnCreate() {
        DataHelper.deleteEventsDatabase();
        DemoApplication.setConfiguration(new TestTrackConfiguration());
    }

    @Test
    public void activityPageEventTest() {
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
        receivedEvent.set(false);
        scenario.moveToState(Lifecycle.State.CREATED).moveToState(Lifecycle.State.RESUMED);
        await().atMost(5, SECONDS).untilTrue(receivedEvent);
        scenario.close();
    }

    @Test
    public void fragmentPageEventTest() {
        final AtomicBoolean receivedEvent = new AtomicBoolean(false);
        getEventsApiServer().setOnReceivedEventListener(new OnReceivedPageEventsListener(receivedEvent,
                "/NestedFragmentActivity",
                "/NestedFragmentActivity/GreenFragment[fragment1]",
                "/NestedFragmentActivity/GreenFragment[fragment1]/OrangeFragment[TestTag]",
                "*/GreenFragment[fragment1]/OrangeFragment[TestTag]/RedFragment[small]",
                "/NestedFragmentActivity/RedFragment[fragment2]"
        ));

        ActivityScenario<NestedFragmentActivity> scenario = ActivityScenario.launch(NestedFragmentActivity.class);
        await().atMost(5, SECONDS).untilTrue(receivedEvent);

        getEventsApiServer().setOnReceivedEventListener(new OnReceivedPageEventsListener(receivedEvent,
                "/NestedFragmentActivity",
                "/NestedFragmentActivity/GreenFragment[fragment1]",
                "/NestedFragmentActivity/GreenFragment[fragment1]/OrangeFragment[TestTag]",
                "*/GreenFragment[fragment1]/OrangeFragment[TestTag]/RedFragment[small]",
                "/NestedFragmentActivity/RedFragment[fragment2]"
        ));
        scenario.moveToState(Lifecycle.State.CREATED);
        scenario.moveToState(Lifecycle.State.RESUMED);
        await().atMost(5, SECONDS).untilTrue(receivedEvent);
        scenario.close();
    }

    @Test
    public void ignoreSelfPageTest() {
        final AtomicBoolean receivedEvent = new AtomicBoolean(false);
        getEventsApiServer().setOnReceivedEventListener(new OnReceivedPageEventsListener(receivedEvent,
                "/NestedFragmentActivity/GreenFragment[fragment1]/OrangeFragment[TestTag]",
                "*/GreenFragment[fragment1]/OrangeFragment[TestTag]/RedFragment[small]",
                "/NestedFragmentActivity/RedFragment[fragment2]"
        ));
        ActivityLifecycleCallback activityLifecycleCallback = (activity, stage) -> {
            if (stage == Stage.CREATED && activity.getClass() == NestedFragmentActivity.class) {
                GrowingAutotracker.get().ignorePage(activity, IgnorePolicy.IGNORE_SELF);
            }
        };
        ActivityLifecycleMonitorRegistry.getInstance().addLifecycleCallback(activityLifecycleCallback);

        FragmentLifecycleCallback fragmentLifecycleCallback = (fragment, stage) -> {
            if (stage == FragmentLifecycleCallback.Stage.CREATED && fragment.getClass() == GreenFragment.class) {
                GrowingAutotracker.get().ignorePage(fragment, IgnorePolicy.IGNORE_SELF);
            }
        };
        FragmentLifecycleMonitor.get().addLifecycleCallback(fragmentLifecycleCallback);

        ActivityScenario<NestedFragmentActivity> scenario = ActivityScenario.launch(NestedFragmentActivity.class);
        await().atMost(5, SECONDS).untilTrue(receivedEvent);
        ActivityLifecycleMonitorRegistry.getInstance().removeLifecycleCallback(activityLifecycleCallback);
        FragmentLifecycleMonitor.get().removeLifecycleCallback(fragmentLifecycleCallback);
        scenario.close();
    }

    @Test
    public void ignoreActivityChildPageTest() {
        final AtomicBoolean receivedEvent = new AtomicBoolean(false);
        getEventsApiServer().setOnReceivedEventListener(new OnReceivedPageEventsListener(receivedEvent,
                "/NestedFragmentActivity"
        ));
        ActivityLifecycleCallback activityLifecycleCallback = (activity, stage) -> {
            if (stage == Stage.CREATED && activity.getClass() == NestedFragmentActivity.class) {
                GrowingAutotracker.get().ignorePage(activity, IgnorePolicy.IGNORE_CHILD);
            }
        };
        ActivityLifecycleMonitorRegistry.getInstance().addLifecycleCallback(activityLifecycleCallback);
        ActivityScenario<NestedFragmentActivity> scenario = ActivityScenario.launch(NestedFragmentActivity.class);
        TrackHelper.waitForIdleSync();
        await().atMost(5, SECONDS).untilTrue(receivedEvent);
        ActivityLifecycleMonitorRegistry.getInstance().removeLifecycleCallback(activityLifecycleCallback);
        scenario.close();
    }

    @Test
    public void ignoreFragmentChildPageTest() {
        final AtomicBoolean receivedEvent = new AtomicBoolean(false);
        getEventsApiServer().setOnReceivedEventListener(new OnReceivedPageEventsListener(receivedEvent,
                "/NestedFragmentActivity",
                "/NestedFragmentActivity/GreenFragment[fragment1]",
                "/NestedFragmentActivity/RedFragment[fragment2]"
        ));

        FragmentLifecycleCallback fragmentLifecycleCallback = (fragment, stage) -> {
            if (stage == FragmentLifecycleCallback.Stage.CREATED && fragment.getClass() == GreenFragment.class) {
                GrowingAutotracker.get().ignorePage(fragment, IgnorePolicy.IGNORE_CHILD);
            }
        };
        FragmentLifecycleMonitor.get().addLifecycleCallback(fragmentLifecycleCallback);

        ActivityScenario<NestedFragmentActivity> scenario = ActivityScenario.launch(NestedFragmentActivity.class);
        await().atMost(5, SECONDS).untilTrue(receivedEvent);
        FragmentLifecycleMonitor.get().removeLifecycleCallback(fragmentLifecycleCallback);
        scenario.close();
    }

    @Test
    public void ignoreActivityAllPageTest() {
        getEventsApiServer().setOnReceivedEventListener(new StopReceivedPageEventsListener());
        ActivityLifecycleCallback activityLifecycleCallback = (activity, stage) -> {
            if (stage == Stage.CREATED && activity.getClass() == NestedFragmentActivity.class) {
                GrowingAutotracker.get().ignorePage(activity, IgnorePolicy.IGNORE_ALL);
            }
        };
        ActivityLifecycleMonitorRegistry.getInstance().addLifecycleCallback(activityLifecycleCallback);
        ActivityScenario<NestedFragmentActivity> scenario = ActivityScenario.launch(NestedFragmentActivity.class);
        TrackHelper.waitForIdleSync();
        ActivityLifecycleMonitorRegistry.getInstance().removeLifecycleCallback(activityLifecycleCallback);
        scenario.close();
    }

    @Test
    public void ignoreFragmentAllPageTest() {
        final AtomicBoolean receivedEvent = new AtomicBoolean(false);
        getEventsApiServer().setOnReceivedEventListener(new OnReceivedPageEventsListener(receivedEvent,
                "/NestedFragmentActivity",
                "/NestedFragmentActivity/RedFragment[fragment2]"
        ));

        FragmentLifecycleCallback fragmentLifecycleCallback = (fragment, stage) -> {
            if (stage == FragmentLifecycleCallback.Stage.CREATED && fragment.getClass() == GreenFragment.class) {
                GrowingAutotracker.get().ignorePage(fragment, IgnorePolicy.IGNORE_ALL);
            }
        };
        FragmentLifecycleMonitor.get().addLifecycleCallback(fragmentLifecycleCallback);

        ActivityScenario<NestedFragmentActivity> scenario = ActivityScenario.launch(NestedFragmentActivity.class);
        await().atMost(5, SECONDS).untilTrue(receivedEvent);
        FragmentLifecycleMonitor.get().removeLifecycleCallback(fragmentLifecycleCallback);
        scenario.close();
    }

    @Test
    public void setActivityPageAliasTest() {
        final AtomicBoolean receivedEvent = new AtomicBoolean(false);
        getEventsApiServer().setOnReceivedEventListener(new MockEventsApiServer.OnReceivedEventListener() {
            @Override
            protected void onReceivedPageEvents(JSONArray jsonArray) throws JSONException {
                JSONObject jsonObject = jsonArray.getJSONObject(0);
                if (jsonObject.getString("pageName").equals("/TestActivityPage")
                        && jsonObject.getString("orientation").equals("PORTRAIT")
                        && jsonObject.getString("title").equals("demos")) {
                    receivedEvent.set(true);
                }
            }
        });
        ActivityLifecycleCallback activityLifecycleCallback = (activity, stage) -> {
            if (stage == Stage.CREATED && activity.getClass() == MainActivity.class) {
                GrowingAutotracker.get().setPageAlias(activity, "TestActivityPage");
            }
        };
        ActivityLifecycleMonitorRegistry.getInstance().addLifecycleCallback(activityLifecycleCallback);
        ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class);
        await().atMost(5, SECONDS).untilTrue(receivedEvent);
        ActivityLifecycleMonitorRegistry.getInstance().removeLifecycleCallback(activityLifecycleCallback);
        scenario.close();
    }

    @Test
    public void setFragmentPageAliasTest() {
        final AtomicBoolean receivedEvent = new AtomicBoolean(false);
        getEventsApiServer().setOnReceivedEventListener(new OnReceivedPageEventsListener(receivedEvent,
                "/TestActivityPage",
                "/TestGreenPage",
                "/TestGreenPage/OrangeFragment[TestTag]",
                "*/TestGreenPage/OrangeFragment[TestTag]/RedFragment[small]",
                "/TestActivityPage/RedFragment[fragment2]"
        ));

        ActivityLifecycleCallback activityLifecycleCallback = (activity, stage) -> {
            if (stage == Stage.CREATED && activity.getClass() == NestedFragmentActivity.class) {
                GrowingAutotracker.get().setPageAlias(activity, "TestActivityPage");
            }
        };
        ActivityLifecycleMonitorRegistry.getInstance().addLifecycleCallback(activityLifecycleCallback);

        FragmentLifecycleCallback fragmentLifecycleCallback = (fragment, stage) -> {
            if (stage == FragmentLifecycleCallback.Stage.CREATED && fragment.getClass() == GreenFragment.class) {
                GrowingAutotracker.get().setPageAlias(fragment, "TestGreenPage");
            }
        };
        FragmentLifecycleMonitor.get().addLifecycleCallback(fragmentLifecycleCallback);

        ActivityScenario<NestedFragmentActivity> scenario = ActivityScenario.launch(NestedFragmentActivity.class);
        await().atMost(5, SECONDS).untilTrue(receivedEvent);
        ActivityLifecycleMonitorRegistry.getInstance().removeLifecycleCallback(activityLifecycleCallback);
        FragmentLifecycleMonitor.get().removeLifecycleCallback(fragmentLifecycleCallback);
        scenario.close();
    }

    @Test
    public void fragmentOnHiddenChangedTest() {
        final AtomicBoolean receivedEvent = new AtomicBoolean(false);
        getEventsApiServer().setOnReceivedEventListener(new OnReceivedPageEventsListener(receivedEvent,
                "/HideFragmentActivity"
        ));
        ActivityScenario<HideFragmentActivity> scenario = ActivityScenario.launch(HideFragmentActivity.class);
        await().atMost(5, SECONDS).untilTrue(receivedEvent);

        getEventsApiServer().setOnReceivedEventListener(new OnReceivedPageEventsListener(receivedEvent,
                "/HideFragmentActivity/RedFragment[frame_content]"
        ));
        onView(withId(R.id.add)).perform(click());
        await().atMost(5, SECONDS).untilTrue(receivedEvent);

        getEventsApiServer().setOnReceivedEventListener(new StopReceivedPageEventsListener());
        onView(withId(R.id.hide)).perform(click());
        TrackHelper.waitForIdleSync();

        getEventsApiServer().setOnReceivedEventListener(new OnReceivedPageEventsListener(receivedEvent,
                "/HideFragmentActivity/RedFragment[frame_content]"
        ));
        onView(withId(R.id.show)).perform(click());
        await().atMost(5, SECONDS).untilTrue(receivedEvent);

        getEventsApiServer().setOnReceivedEventListener(new OnReceivedPageEventsListener(receivedEvent,
                "/HideFragmentActivity/GreenFragment[frame_content]",
                "/HideFragmentActivity/GreenFragment[frame_content]/OrangeFragment[TestTag]",
                "*/GreenFragment[frame_content]/OrangeFragment[TestTag]/RedFragment[small]"
        ));
        onView(withId(R.id.add)).perform(click());
        await().atMost(5, SECONDS).untilTrue(receivedEvent);

        getEventsApiServer().setOnReceivedEventListener(new StopReceivedPageEventsListener());
        onView(withId(R.id.hide)).perform(click());
        TrackHelper.waitForIdleSync();

        getEventsApiServer().setOnReceivedEventListener(new OnReceivedPageEventsListener(receivedEvent,
                "/HideFragmentActivity/GreenFragment[frame_content]",
                "/HideFragmentActivity/GreenFragment[frame_content]/OrangeFragment[TestTag]",
                "*/GreenFragment[frame_content]/OrangeFragment[TestTag]/RedFragment[small]"
        ));
        onView(withId(R.id.show)).perform(click());
        await().atMost(5, SECONDS).untilTrue(receivedEvent);

        scenario.close();
    }

    @Test
    public void hiddenFragmentOnResumeTest() {
        final AtomicBoolean receivedEvent = new AtomicBoolean(false);
        ActivityScenario<HideFragmentActivity> scenario = ActivityScenario.launch(HideFragmentActivity.class);
        onView(withId(R.id.add)).perform(click());
        onView(withId(R.id.hide)).perform(click());
        TrackHelper.waitForIdleSync();

        scenario.moveToState(Lifecycle.State.CREATED).moveToState(Lifecycle.State.RESUMED);
        getEventsApiServer().setOnReceivedEventListener(new OnReceivedPageEventsListener(receivedEvent,
                "/HideFragmentActivity"
        ));
        TrackHelper.waitForIdleSync();
        await().atMost(5, SECONDS).untilTrue(receivedEvent);
        scenario.close();
    }

    @Test
    public void hiddenFragmentsOnResumeTest() {
        final AtomicBoolean receivedEvent = new AtomicBoolean(false);
        ActivityScenario<HideFragmentActivity> scenario = ActivityScenario.launch(HideFragmentActivity.class);
        onView(withId(R.id.add)).perform(click());
        onView(withId(R.id.add)).perform(click());
        onView(withId(R.id.hide)).perform(click());

        scenario.moveToState(Lifecycle.State.CREATED).moveToState(Lifecycle.State.RESUMED);
        getEventsApiServer().setOnReceivedEventListener(new OnReceivedPageEventsListener(receivedEvent,
                "/HideFragmentActivity",
                "/HideFragmentActivity/RedFragment[frame_content]"
        ));
        TrackHelper.waitForIdleSync();
        await().atMost(5, SECONDS).untilTrue(receivedEvent);
        scenario.close();
    }

    @Test
    public void navFragmentPageTest() {
        final AtomicBoolean receivedEvent = new AtomicBoolean(false);
        getEventsApiServer().setOnReceivedEventListener(new OnReceivedPageEventsListener(receivedEvent,
                "/NavFragmentActivity",
                "/NavFragmentActivity/NavHostFragment[nav_host_fragment]",
                "/NavFragmentActivity/NavHostFragment[nav_host_fragment]/HomeFragment[nav_host_fragment]"));

        ActivityScenario<NavFragmentActivity> scenario = ActivityScenario.launch(NavFragmentActivity.class);
        await().atMost(5, SECONDS).untilTrue(receivedEvent);

        getEventsApiServer().setOnReceivedEventListener(new OnReceivedPageEventsListener(receivedEvent,
                "/NavFragmentActivity/NavHostFragment[nav_host_fragment]/DashboardFragment[nav_host_fragment]"
        ));
        onView(withId(R.id.navigation_dashboard)).perform(click());
        await().atMost(5, SECONDS).untilTrue(receivedEvent);

        getEventsApiServer().setOnReceivedEventListener(new OnReceivedPageEventsListener(receivedEvent,
                "/NavFragmentActivity/NavHostFragment[nav_host_fragment]/NotificationsFragment[nav_host_fragment]"
        ));
        onView(withId(R.id.navigation_notifications)).perform(click());
        await().atMost(5, SECONDS).untilTrue(receivedEvent);

        getEventsApiServer().setOnReceivedEventListener(new OnReceivedPageEventsListener(receivedEvent,
                "/NavFragmentActivity/NavHostFragment[nav_host_fragment]/HomeFragment[nav_host_fragment]"
        ));
        onView(withId(R.id.navigation_home)).perform(click());
        await().atMost(5, SECONDS).untilTrue(receivedEvent);
        scenario.close();
    }

    @Test
    public void tabFragmentPageTest() {
        final AtomicBoolean receivedEvent = new AtomicBoolean(false);
        getEventsApiServer().setOnReceivedEventListener(new OnReceivedPageEventsListener(receivedEvent,
                "/TabFragmentActivity",
                "/TabFragmentActivity/PlaceholderFragment[android:switcher:view_pager:0]"
        ));

        ActivityScenario<TabFragmentActivity> scenario = ActivityScenario.launch(TabFragmentActivity.class);
        await().atMost(5, SECONDS).untilTrue(receivedEvent);

        for (int i = 2; i <= 5; i++) {
            getEventsApiServer().setOnReceivedEventListener(new OnReceivedPageEventsListener(receivedEvent,
                    "/TabFragmentActivity/PlaceholderFragment[android:switcher:view_pager:" + (i - 1) + "]"
            ));
            onView(withText("TAB " + i)).perform(click());
            await().atMost(5, SECONDS).untilTrue(receivedEvent);
        }

        for (int i = 4; i >= 1; i--) {
            getEventsApiServer().setOnReceivedEventListener(new OnReceivedPageEventsListener(receivedEvent,
                    "/TabFragmentActivity/PlaceholderFragment[android:switcher:view_pager:" + (i - 1) + "]"
            ));
            onView(withText("TAB " + i)).perform(click());
            await().atMost(5, SECONDS).untilTrue(receivedEvent);
        }

        scenario.close();
    }

    private static final class OnReceivedPageEventsListener extends MockEventsApiServer.OnReceivedEventListener {
        private final AtomicBoolean mReceivedAllEvents;
        private final List<String> mPages;

        private final Map<String, Long> mReceivedPages = new HashMap<>();

        OnReceivedPageEventsListener(AtomicBoolean receivedAllEvents, String... pageNames) {
            mReceivedAllEvents = receivedAllEvents;
            mReceivedAllEvents.set(false);
            mPages = Arrays.asList(pageNames);
        }

        @Override
        protected void onReceivedPageEvents(JSONArray jsonArray) throws JSONException {
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                String pageName = jsonObject.getString("pageName");
                if (mPages.contains(pageName) && jsonObject.getString("orientation").equals("PORTRAIT")) {
                    mReceivedPages.put(pageName, jsonObject.getLong("timestamp"));
                } else {
                    Truth.assertWithMessage("Received " + pageName + " Page Event").fail();
                }
            }

            if (receivedAllPageEvent(mPages, mReceivedPages)) {
                mReceivedAllEvents.set(true);
            }
        }

        private boolean receivedAllPageEvent(List<String> expectedPages, Map<String, Long> receivedPages) {
            // 同时需要对比时间戳
            if (receivedPages.size() == expectedPages.size()) {
                long timestamp = 1;
                // 同时需要对比时间戳
                for (String page : expectedPages) {
                    if (receivedPages.get(page) >= timestamp) {
                        timestamp = receivedPages.get(page);
                    } else {
                        Truth.assertWithMessage("Received Page timestamp is " + receivedPages.get(page) + " < " + timestamp).fail();
                    }
                }
                return true;
            }
            return false;
        }
    }

    private static final class StopReceivedPageEventsListener extends MockEventsApiServer.OnReceivedEventListener {
        @Override
        protected void onReceivedPageEvents(JSONArray jsonArray) throws JSONException {
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                String pageName = jsonObject.getString("pageName");
                Truth.assertWithMessage("Received " + pageName + " Page Event").fail();
            }
        }
    }
}
