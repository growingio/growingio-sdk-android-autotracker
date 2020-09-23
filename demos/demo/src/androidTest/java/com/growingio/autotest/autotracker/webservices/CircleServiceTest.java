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

package com.growingio.autotest.autotracker.webservices;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.gio.test.three.DemoApplication;
import com.gio.test.three.autotrack.activity.ClickTestActivity;
import com.growingio.android.sdk.autotrack.webservices.circle.CircleService;
import com.growingio.autotest.TestTrackConfiguration;
import com.growingio.autotest.WebServicesTest;
import com.growingio.autotest.help.BeforeAppOnCreate;
import com.growingio.autotest.help.DataHelper;
import com.growingio.autotest.help.TrackHelper;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.atomic.AtomicBoolean;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class CircleServiceTest extends WebServicesTest {
    @BeforeAppOnCreate
    public static void beforeAppOnCreate() {
        DataHelper.deleteEventsDatabase();
        DemoApplication.setConfiguration(new TestTrackConfiguration());
    }

    @Test
    public void circleServiceTest() {
        AtomicBoolean receivedMessage = new AtomicBoolean(false);
        setOnReceivedMessageListener(message -> {
            receivedMessage.set(true);
        });

        ActivityScenario<ClickTestActivity> scenario = ActivityScenario.launch(ClickTestActivity.class);
        TrackHelper.waitUiThreadForIdleSync();
        new CircleService(getWsUrl()).start();
        await().atMost(5, SECONDS).untilTrue(receivedMessage);
        scenario.close();
    }
}
