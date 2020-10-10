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

import android.util.Log;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.gio.test.three.DemoApplication;
import com.gio.test.three.autotrack.activity.ClickTestActivity;
import com.growingio.android.sdk.autotrack.webservices.circle.CircleService;
import com.growingio.android.sdk.track.log.Logger;
import com.growingio.autotest.TestTrackConfiguration;
import com.growingio.autotest.WebServicesTest;
import com.growingio.autotest.help.Awaiter;
import com.growingio.autotest.help.BeforeAppOnCreate;
import com.growingio.autotest.help.DataHelper;
import com.growingio.autotest.help.TrackHelper;

import org.json.JSONArray;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.HashSet;
import java.util.concurrent.atomic.AtomicBoolean;

import static androidx.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class CircleServiceTest extends WebServicesTest {
    private static final String TAG = "CircleServiceTest";

    @BeforeAppOnCreate
    public static void beforeAppOnCreate() {
        DataHelper.deleteEventsDatabase();
        DemoApplication.setConfiguration(new TestTrackConfiguration());
    }

    @Test
    public void circleServiceTest() {
        HashSet<String> allElements = new HashSet<String>() {{
            add("/Page/ActionBarOverlayLayout[0]/FrameLayout[0]/LinearLayout[0]#content_parent");
            add("/Page/ActionBarOverlayLayout[0]/FrameLayout[0]/LinearLayout[0]#content_parent/Button[0]#btn_test_click");
            add("/Page/ActionBarOverlayLayout[0]/FrameLayout[0]/LinearLayout[0]#content_parent/CheckBox[0]#check_box_android");
            add("/Page/ActionBarOverlayLayout[0]/FrameLayout[0]/LinearLayout[0]#content_parent/CheckBox[1]#check_box_ios");
            add("/Page/ActionBarOverlayLayout[0]/FrameLayout[0]/LinearLayout[0]#content_parent/Switch[0]#switch1");
            add("/Page/ActionBarOverlayLayout[0]/FrameLayout[0]/LinearLayout[0]#content_parent/SeekBar[0]#seek_bar");
            add("/Page/ActionBarOverlayLayout[0]/FrameLayout[0]/LinearLayout[0]#content_parent/RatingBar[0]#rating_bar");
            add("/Page/ActionBarOverlayLayout[0]/FrameLayout[0]/LinearLayout[0]#content_parent/Spinner[0]#spinner_test/TextView[-]");
            add("/Page/ActionBarOverlayLayout[0]/FrameLayout[0]/LinearLayout[0]#content_parent/RadioGroup[0]#radio_group_gender/RadioButton[0]");
            add("/Page/ActionBarOverlayLayout[0]/FrameLayout[0]/LinearLayout[0]#content_parent/RadioGroup[0]#radio_group_gender/RadioButton[1]");
            add("/Page/MenuView/MenuItem#navigation_home");
            add("/Page/MenuView/MenuItem#navigation_dashboard");
            add("/Page/MenuView/MenuItem#navigation_notifications");
        }};
        AtomicBoolean receivedMessage = new AtomicBoolean(false);
        setOnReceivedMessageListener(message -> {
            message.remove("screenshot");
            Logger.printJson(TAG, "receivedMessage: ", message.toString());
            JSONArray jsonArray = message.getJSONArray("elements");
            HashSet<String> receivedElements = new HashSet<>();
            for (int i = 0; i < jsonArray.length(); i++) {
                receivedElements.add(jsonArray.getJSONObject(i).getString("xpath"));
            }
            if (receivedElements.containsAll(allElements)) {
                receivedMessage.set(true);
            } else {
                HashSet<String> result = new HashSet<>(allElements);
                result.removeAll(allElements);
                Log.e(TAG, "circleServiceTest: allElements has more " + result);

                result = new HashSet<>(receivedElements);
                result.removeAll(allElements);
                Log.e(TAG, "circleServiceTest: receivedElements has more " + result);
            }
        });

        ActivityScenario<ClickTestActivity> scenario = ActivityScenario.launch(ClickTestActivity.class);
        openActionBarOverflowOrOptionsMenu(ApplicationProvider.getApplicationContext());
        TrackHelper.waitUiThreadForIdleSync();
        new CircleService(getWsUrl()).start();
        Awaiter.untilTrue(receivedMessage);
        scenario.close();
    }
}
