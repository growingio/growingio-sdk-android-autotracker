/*
 * Copyright (C) 2023 Beijing Yishu Technology Co., Ltd.
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
package com.growingio.autotest.autotracker.hybrid;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.anything;

import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.Espresso;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.gio.test.R;
import com.gio.test.three.DemoApplication;
import com.gio.test.three.autotrack.AutotrackEntryActivity;
import com.growingio.android.sdk.autotrack.AutotrackConfiguration;
import com.growingio.android.sdk.autotrack.GrowingAutotracker;
import com.growingio.autotest.EventsTest;
import com.growingio.autotest.TestTrackConfiguration;
import com.growingio.autotest.help.Awaiter;
import com.growingio.autotest.help.BeforeAppOnCreate;
import com.growingio.autotest.help.DataHelper;
import com.growingio.autotest.help.MockEventsApiServer;

import org.json.JSONArray;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.atomic.AtomicBoolean;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class HybridSpecialScenarioTest extends EventsTest {
    private static AutotrackConfiguration sTestTrackConfiguration;

    @BeforeAppOnCreate
    public static void beforeAppOnCreate() {
        DataHelper.deleteEventsDatabase();
        sTestTrackConfiguration = TestTrackConfiguration.getTestConfig()
                .setDataCollectionEnabled(false);
        DemoApplication.setConfiguration(sTestTrackConfiguration);
    }
    /**
     * 线上bug：不初始化sdk访问webview, NPE崩溃
     */
    @Test
    public void testWebView() {
        getEventsApiServer().setCheckUserId(false);
        getEventsApiServer().setCheckSessionId(false);
        final AtomicBoolean receivedVisit = new AtomicBoolean(false);
        final AtomicBoolean receivedPage = new AtomicBoolean(false);
        getEventsApiServer().setOnReceivedEventListener(new MockEventsApiServer.OnReceivedEventListener() {
            @Override
            protected void onReceivedVisitEvents(JSONArray jsonArray) {
                receivedVisit.set(true);
            }
        });
        ActivityScenario<AutotrackEntryActivity> scenario = ActivityScenario.launch(AutotrackEntryActivity.class);
        //点击Go To WebViewActivity
        onData(anything()).inAdapterView(withId(R.id.content)).atPosition(4).perform(click());

        Espresso.pressBack();
        onView(allOf(withId(R.id.et_search), isDisplayed()));
        GrowingAutotracker.get().setDataCollectionEnabled(true);
        Awaiter.untilTrue(receivedVisit);
    }
}
