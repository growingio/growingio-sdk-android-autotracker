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
import androidx.test.runner.lifecycle.ActivityLifecycleMonitorRegistry;
import androidx.test.runner.lifecycle.Stage;

import com.gio.test.three.DemoApplication;
import com.gio.test.three.MainActivity;
import com.gio.test.three.autotrack.activity.NestedFragmentActivity;
import com.gio.test.three.autotrack.activity.TabFragmentActivity;
import com.gio.test.three.autotrack.activity.ui.main.PlaceholderFragment;
import com.gio.test.three.autotrack.fragments.FragmentLifecycleCallback;
import com.gio.test.three.autotrack.fragments.FragmentLifecycleMonitor;
import com.gio.test.three.autotrack.fragments.GreenFragment;
import com.google.common.truth.Truth;
import com.growingio.android.sdk.autotrack.GrowingAutotracker;
import com.growingio.android.sdk.track.utils.JsonUtil;
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

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class PageAttributesEventsTest extends EventsTest {
    private static final HashMap<String, String> TEST_ATTRIBUTES = new HashMap<String, String>() {{
        put("key1", "value1");
        put("key2", "value2");
        put("key3", "");
        put("key4", null);
    }};

    @BeforeAppOnCreate
    public static void beforeAppOnCreate() {
        DataHelper.deleteEventsDatabase();
        DemoApplication.setConfiguration(new TestTrackConfiguration());
    }

    @Test
    public void beforeActivityOnResumePageAttributesEventTest() {
        HashMap<String, HashMap<String, String>> expectPagesAttributes = new HashMap<String, HashMap<String, String>>() {{
            put("/MainActivity", TEST_ATTRIBUTES);
        }};
        OnReceivedPageAttributesEventsListener eventsListener = new OnReceivedPageAttributesEventsListener(expectPagesAttributes);
        getEventsApiServer().setOnReceivedEventListener(eventsListener);
        ActivityLifecycleMonitorRegistry.getInstance().addLifecycleCallback((activity, stage) -> {
            if (activity.getClass() == MainActivity.class && stage == Stage.CREATED) {
                GrowingAutotracker.get().setPageAttributes(activity, TEST_ATTRIBUTES);
            }
        });

        ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class);
        TrackHelper.waitForIdleSync();
        Truth.assertThat(eventsListener.checkEvents()).isTrue();

        eventsListener.reset();
        getEventsApiServer().setOnReceivedEventListener(eventsListener);
        scenario.moveToState(Lifecycle.State.CREATED).moveToState(Lifecycle.State.RESUMED);
        TrackHelper.waitForIdleSync();
        Truth.assertThat(eventsListener.checkEvents()).isTrue();
        scenario.close();
    }

    @Test
    public void afterActivityOnResumePageAttributesEventTest() {
        HashMap<String, HashMap<String, String>> expectPagesAttributes = new HashMap<String, HashMap<String, String>>() {{
            put("/MainActivity", TEST_ATTRIBUTES);
        }};

        OnReceivedPageAttributesEventsListener eventsListener = new OnReceivedPageAttributesEventsListener(expectPagesAttributes);
        getEventsApiServer().setOnReceivedEventListener(eventsListener);
        ActivityLifecycleMonitorRegistry.getInstance().addLifecycleCallback((activity, stage) -> {
            if (activity.getClass() == MainActivity.class && stage == Stage.RESUMED) {
                TrackHelper.postToUiThread(() -> GrowingAutotracker.get().setPageAttributes(activity, TEST_ATTRIBUTES));
            }
        });

        ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class);
        TrackHelper.waitForIdleSync();
        Truth.assertThat(eventsListener.checkEvents()).isTrue();

        eventsListener.reset();
        getEventsApiServer().setOnReceivedEventListener(eventsListener);
        scenario.moveToState(Lifecycle.State.CREATED).moveToState(Lifecycle.State.RESUMED);
        TrackHelper.waitForIdleSync();
        Truth.assertThat(eventsListener.checkEvents()).isTrue();
        scenario.close();
    }

    @Test
    public void beforeFragmentOnResumePageAttributesEventTest() {
        HashMap<String, HashMap<String, String>> expectPagesAttributes = new HashMap<String, HashMap<String, String>>() {{
            put("/TabFragmentActivity/PlaceholderFragment[android:switcher:view_pager:0]", TEST_ATTRIBUTES);
        }};
        OnReceivedPageAttributesEventsListener eventsListener = new OnReceivedPageAttributesEventsListener(expectPagesAttributes);
        getEventsApiServer().setOnReceivedEventListener(eventsListener);
        FragmentLifecycleMonitor.get().addLifecycleCallback((fragment, stage) -> {
            if (fragment.getClass() == PlaceholderFragment.class && stage == FragmentLifecycleCallback.Stage.CREATED) {
                GrowingAutotracker.get().setPageAttributes(fragment, TEST_ATTRIBUTES);
            }
        });

        ActivityScenario<TabFragmentActivity> scenario = ActivityScenario.launch(TabFragmentActivity.class);
        TrackHelper.waitForIdleSync();
        Truth.assertThat(eventsListener.checkEvents()).isTrue();

        eventsListener.reset();
        getEventsApiServer().setOnReceivedEventListener(eventsListener);
        scenario.moveToState(Lifecycle.State.CREATED).moveToState(Lifecycle.State.RESUMED);
        TrackHelper.waitForIdleSync();
        Truth.assertThat(eventsListener.checkEvents()).isTrue();
        scenario.close();
    }

    @Test
    public void afterFragmentOnResumePageAttributesEventTest() {
        HashMap<String, HashMap<String, String>> expectPagesAttributes = new HashMap<String, HashMap<String, String>>() {{
            put("/TabFragmentActivity/PlaceholderFragment[android:switcher:view_pager:0]", TEST_ATTRIBUTES);
        }};
        OnReceivedPageAttributesEventsListener eventsListener = new OnReceivedPageAttributesEventsListener(expectPagesAttributes);
        getEventsApiServer().setOnReceivedEventListener(eventsListener);
        FragmentLifecycleMonitor.get().addLifecycleCallback((fragment, stage) -> {
            if (fragment.getClass() == PlaceholderFragment.class && stage == FragmentLifecycleCallback.Stage.RESUMED) {
                TrackHelper.postToUiThread(() -> GrowingAutotracker.get().setPageAttributes(fragment, TEST_ATTRIBUTES));
            }
        });

        ActivityScenario<TabFragmentActivity> scenario = ActivityScenario.launch(TabFragmentActivity.class);
        TrackHelper.waitForIdleSync();
        Truth.assertThat(eventsListener.checkEvents()).isTrue();

        eventsListener.reset();
        getEventsApiServer().setOnReceivedEventListener(eventsListener);
        scenario.moveToState(Lifecycle.State.CREATED).moveToState(Lifecycle.State.RESUMED);
        TrackHelper.waitForIdleSync();
        Truth.assertThat(eventsListener.checkEvents()).isTrue();
        scenario.close();
    }

    @Test
    public void beforeActivityOnResumeMultiPagesAttributesEventTest() {
        HashMap<String, HashMap<String, String>> expectPagesAttributes = new HashMap<String, HashMap<String, String>>() {{
            put("/NestedFragmentActivity", TEST_ATTRIBUTES);
            put("/NestedFragmentActivity/GreenFragment[fragment1]", TEST_ATTRIBUTES);
            put("/NestedFragmentActivity/GreenFragment[fragment1]/OrangeFragment[TestTag]", TEST_ATTRIBUTES);
            put("*/GreenFragment[fragment1]/OrangeFragment[TestTag]/RedFragment[small]", TEST_ATTRIBUTES);
            put("/NestedFragmentActivity/RedFragment[fragment2]", TEST_ATTRIBUTES);
        }};
        OnReceivedPageAttributesEventsListener eventsListener = new OnReceivedPageAttributesEventsListener(expectPagesAttributes);
        getEventsApiServer().setOnReceivedEventListener(eventsListener);
        ActivityLifecycleMonitorRegistry.getInstance().addLifecycleCallback((activity, stage) -> {
            if (activity.getClass() == NestedFragmentActivity.class && stage == Stage.CREATED) {
                GrowingAutotracker.get().setPageAttributes(activity, TEST_ATTRIBUTES);
            }
        });

        ActivityScenario<NestedFragmentActivity> scenario = ActivityScenario.launch(NestedFragmentActivity.class);
        TrackHelper.waitForIdleSync();
        Truth.assertThat(eventsListener.checkEvents()).isTrue();

        eventsListener.reset();
        getEventsApiServer().setOnReceivedEventListener(eventsListener);
        scenario.moveToState(Lifecycle.State.CREATED).moveToState(Lifecycle.State.RESUMED);
        TrackHelper.waitForIdleSync();
        Truth.assertThat(eventsListener.checkEvents()).isTrue();
        scenario.close();
    }

    @Test
    public void afterActivityOnResumeMultiPagesAttributesEventTest() {
        HashMap<String, HashMap<String, String>> expectPagesAttributes = new HashMap<String, HashMap<String, String>>() {{
            put("/NestedFragmentActivity", TEST_ATTRIBUTES);
            put("/NestedFragmentActivity/GreenFragment[fragment1]", TEST_ATTRIBUTES);
            put("/NestedFragmentActivity/GreenFragment[fragment1]/OrangeFragment[TestTag]", TEST_ATTRIBUTES);
            put("*/GreenFragment[fragment1]/OrangeFragment[TestTag]/RedFragment[small]", TEST_ATTRIBUTES);
            put("/NestedFragmentActivity/RedFragment[fragment2]", TEST_ATTRIBUTES);
        }};
        OnReceivedPageAttributesEventsListener eventsListener = new OnReceivedPageAttributesEventsListener(expectPagesAttributes);
        getEventsApiServer().setOnReceivedEventListener(eventsListener);
        ActivityLifecycleMonitorRegistry.getInstance().addLifecycleCallback((activity, stage) -> {
            if (activity.getClass() == NestedFragmentActivity.class && stage == Stage.RESUMED) {
                TrackHelper.postToUiThread(() -> GrowingAutotracker.get().setPageAttributes(activity, TEST_ATTRIBUTES));
            }
        });

        ActivityScenario<NestedFragmentActivity> scenario = ActivityScenario.launch(NestedFragmentActivity.class);
        TrackHelper.waitForIdleSync();
        Truth.assertThat(eventsListener.checkEvents()).isTrue();

        eventsListener.reset();
        getEventsApiServer().setOnReceivedEventListener(eventsListener);
        scenario.moveToState(Lifecycle.State.CREATED).moveToState(Lifecycle.State.RESUMED);
        TrackHelper.waitForIdleSync();
        Truth.assertThat(eventsListener.checkEvents()).isTrue();
        scenario.close();
    }

    @Test
    public void beforeFragmentOnResumeMultiPagesAttributesEventTest() {
        HashMap<String, HashMap<String, String>> expectPagesAttributes = new HashMap<String, HashMap<String, String>>() {{
            put("/NestedFragmentActivity/GreenFragment[fragment1]", TEST_ATTRIBUTES);
            put("/NestedFragmentActivity/GreenFragment[fragment1]/OrangeFragment[TestTag]", TEST_ATTRIBUTES);
            put("*/GreenFragment[fragment1]/OrangeFragment[TestTag]/RedFragment[small]", TEST_ATTRIBUTES);
        }};
        OnReceivedPageAttributesEventsListener eventsListener = new OnReceivedPageAttributesEventsListener(expectPagesAttributes);
        getEventsApiServer().setOnReceivedEventListener(eventsListener);
        FragmentLifecycleMonitor.get().addLifecycleCallback((fragment, stage) -> {
            if (fragment.getClass() == GreenFragment.class && stage == FragmentLifecycleCallback.Stage.CREATED) {
                GrowingAutotracker.get().setPageAttributes(fragment, TEST_ATTRIBUTES);
            }
        });

        ActivityScenario<NestedFragmentActivity> scenario = ActivityScenario.launch(NestedFragmentActivity.class);
        TrackHelper.waitForIdleSync();
        Truth.assertThat(eventsListener.checkEvents()).isTrue();

        eventsListener.reset();
        getEventsApiServer().setOnReceivedEventListener(eventsListener);
        scenario.moveToState(Lifecycle.State.CREATED).moveToState(Lifecycle.State.RESUMED);
        TrackHelper.waitForIdleSync();
        Truth.assertThat(eventsListener.checkEvents()).isTrue();
        scenario.close();
    }

    @Test
    public void afterFragmentOnResumeMultiPagesAttributesEventTest() {
        HashMap<String, HashMap<String, String>> expectPagesAttributes = new HashMap<String, HashMap<String, String>>() {{
            put("/NestedFragmentActivity/GreenFragment[fragment1]", TEST_ATTRIBUTES);
            put("/NestedFragmentActivity/GreenFragment[fragment1]/OrangeFragment[TestTag]", TEST_ATTRIBUTES);
            put("*/GreenFragment[fragment1]/OrangeFragment[TestTag]/RedFragment[small]", TEST_ATTRIBUTES);
        }};
        OnReceivedPageAttributesEventsListener eventsListener = new OnReceivedPageAttributesEventsListener(expectPagesAttributes);
        getEventsApiServer().setOnReceivedEventListener(eventsListener);
        FragmentLifecycleMonitor.get().addLifecycleCallback((fragment, stage) -> {
            if (fragment.getClass() == GreenFragment.class && stage == FragmentLifecycleCallback.Stage.RESUMED) {
                TrackHelper.postToUiThread(() -> GrowingAutotracker.get().setPageAttributes(fragment, TEST_ATTRIBUTES));
            }
        });

        ActivityScenario<NestedFragmentActivity> scenario = ActivityScenario.launch(NestedFragmentActivity.class);
        TrackHelper.waitForIdleSync();
        Truth.assertThat(eventsListener.checkEvents()).isTrue();

        eventsListener.reset();
        getEventsApiServer().setOnReceivedEventListener(eventsListener);
        scenario.moveToState(Lifecycle.State.CREATED).moveToState(Lifecycle.State.RESUMED);
        TrackHelper.waitForIdleSync();
        Truth.assertThat(eventsListener.checkEvents()).isTrue();
        scenario.close();
    }


    private static final class OnReceivedPageAttributesEventsListener extends MockEventsApiServer.OnReceivedEventListener {
        private static final class PagesAttributesParams {
            public final long pageShowTimestamp;
            public final HashMap<String, String> attributes;

            PagesAttributesParams(long pageShowTimestamp, HashMap<String, String> attributes) {
                this.pageShowTimestamp = pageShowTimestamp;
                this.attributes = attributes;
            }
        }


        private final Map<String, Long> mReceivedPages = new HashMap<>();
        private final Map<String, PagesAttributesParams> mReceivedPagesAttributes = new ConcurrentHashMap<>();
        private final HashMap<String, HashMap<String, String>> mExpectPagesAttributes;

        OnReceivedPageAttributesEventsListener(HashMap<String, HashMap<String, String>> expectPagesAttributes) {
            mExpectPagesAttributes = new HashMap<>(expectPagesAttributes);
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
        protected void onReceivedPageAttributesEvents(JSONArray jsonArray) throws JSONException {
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                String path = jsonObject.getString("path");
                long pageShowTimestamp = jsonObject.getLong("pageShowTimestamp");
                HashMap<String, String> attributes = (HashMap<String, String>) JsonUtil.copyToMap(jsonObject.getJSONObject("attributes"));
                mReceivedPagesAttributes.put(path, new PagesAttributesParams(pageShowTimestamp, attributes));
            }
        }

        public void reset() {
            mReceivedPages.clear();
            mReceivedPagesAttributes.clear();
        }

        public boolean checkEvents() {
            for (Map.Entry<String, HashMap<String, String>> entry : mExpectPagesAttributes.entrySet()) {
                String path = entry.getKey();
                PagesAttributesParams receivedAttributesParams = mReceivedPagesAttributes.get(path);
                long expectPageTimestamp = mReceivedPages.get(path);
                long actualPageTimestamp = receivedAttributesParams.pageShowTimestamp;
                if (actualPageTimestamp != expectPageTimestamp) {
                    Truth.assertWithMessage(path + " Page show timestamp is " + expectPageTimestamp
                            + ", but PageAttributesEvent pageShowTimestamp is " + actualPageTimestamp).fail();
                }
                HashMap<String, String> expectAttributes = entry.getValue();
                if (!expectAttributes.equals(receivedAttributesParams.attributes)) {
                    Truth.assertWithMessage(path + " Page attributes is " + receivedAttributesParams.attributes + "not equals expect attributes " + expectAttributes).fail();
                }
            }
            return true;
        }
    }
}
