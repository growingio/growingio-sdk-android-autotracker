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

import android.os.Build;
import android.view.View;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.filters.SdkSuppress;
import androidx.test.runner.lifecycle.ActivityLifecycleMonitorRegistry;
import androidx.test.runner.lifecycle.Stage;

import com.gio.test.R;
import com.gio.test.three.DemoApplication;
import com.gio.test.three.autotrack.AutotrackEntryActivity;
import com.gio.test.three.autotrack.activity.ActionMenuViewActivity;
import com.gio.test.three.autotrack.activity.ClickTestActivity;
import com.gio.test.three.autotrack.activity.DialogTestActivity;
import com.gio.test.three.autotrack.activity.ExpandableListSubActivity;
import com.gio.test.three.autotrack.activity.ExpandableListViewActivity;
import com.gio.test.three.autotrack.activity.NestedFragmentActivity;
import com.gio.test.three.autotrack.activity.ToolBarActivity;
import com.gio.test.three.autotrack.fragments.FragmentLifecycleCallback;
import com.gio.test.three.autotrack.fragments.FragmentLifecycleMonitor;
import com.gio.test.three.autotrack.fragments.GreenFragment;
import com.gio.test.three.autotrack.fragments.RedFragment;
import com.google.common.truth.Truth;
import com.growingio.android.sdk.autotrack.GrowingAutotracker;
import com.growingio.android.sdk.autotrack.IgnorePolicy;
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
import static androidx.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withTagKey;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class ViewClickEventsTest extends EventsTest {
    @BeforeAppOnCreate
    public static void beforeAppOnCreate() {
        DataHelper.deleteEventsDatabase();
        DemoApplication.setConfiguration(TestTrackConfiguration.getTestConfig());
    }

    @Test
    public void buttonClickEventTest() {
        final AtomicBoolean receivedEvent = new AtomicBoolean(false);
        getEventsApiServer().setOnReceivedEventListener(new OnReceivedViewClickEventsListener(
                receivedEvent,
                new ViewElementEvent.Builder()
                        .setPath("/ClickTestActivity")
                        .setXpath("/Page/ActionBarOverlayLayout[0]/FrameLayout[0]/LinearLayout[0]#content_parent/Button[0]#btn_test_click")
                        .setTextValue("测试Button点击")
                        .setIndex(-1)
                        .build()

        ));
        ActivityScenario<ClickTestActivity> scenario = ActivityScenario.launch(ClickTestActivity.class);
        onView(withId(R.id.btn_test_click)).perform(click());
        Awaiter.untilTrue(receivedEvent);
        scenario.close();
    }

/*    @Test
    public void spinnerClickEventTest() {
        final AtomicBoolean receivedEvent = new AtomicBoolean(false);
        getEventsApiServer().setOnReceivedEventListener(new OnReceivedViewClickEventsListener(
                receivedEvent,
                new ViewElementEvent.Builder()
                        .setPath("/ClickTestActivity")
                        .setXpath("/Page/ActionBarOverlayLayout[0]/FrameLayout[0]/LinearLayout[0]#content_parent/Spinner[0]#spinner_test/TextView[-]")
                        .setTextValue("c语言")
                        .setIndex(0)
                        .build(),
                new ViewElementEvent.Builder()
                        .setPath("/ClickTestActivity")
                        .setXpath("/Page/ActionBarOverlayLayout[0]/FrameLayout[0]/LinearLayout[0]#content_parent/Spinner[0]#spinner_test/TextView[-]")
                        .setTextValue("java")
                        .setIndex(1)
                        .build(),
                new ViewElementEvent.Builder()
                        .setPath("/ClickTestActivity")
                        .setXpath("/Page/ActionBarOverlayLayout[0]/FrameLayout[0]/LinearLayout[0]#content_parent/Spinner[0]#spinner_test/TextView[-]")
                        .setTextValue("xml")
                        .setIndex(3)
                        .build()

        ));
        ActivityScenario<ClickTestActivity> scenario = ActivityScenario.launch(ClickTestActivity.class);
        onView(withId(R.id.spinner_test)).perform(click());
        onView(withText("java")).perform(click());
        onView(withId(R.id.spinner_test)).perform(click());
        onView(withText("xml")).perform(click());
        Awaiter.untilTrue(receivedEvent);

        scenario.close();
    }*/

    @Test
    public void radioButtonClickEventTest() {
        final AtomicBoolean receivedEvent = new AtomicBoolean(false);
        getEventsApiServer().setOnReceivedEventListener(new OnReceivedViewClickEventsListener(
                receivedEvent,
                new ViewElementEvent.Builder()
                        .setPath("/ClickTestActivity")
                        .setXpath("/Page/ActionBarOverlayLayout[0]/FrameLayout[0]/LinearLayout[0]#content_parent/RadioGroup[0]#radio_group_gender/RadioButton[1]#rb_female")
                        .setTextValue("female[true]")
                        .setIndex(-1)
                        .build(),
                new ViewElementEvent.Builder()
                        .setPath("/ClickTestActivity")
                        .setXpath("/Page/ActionBarOverlayLayout[0]/FrameLayout[0]/LinearLayout[0]#content_parent/RadioGroup[0]#radio_group_gender/RadioButton[0]#rb_male")
                        .setTextValue("male[true]")
                        .setIndex(-1)
                        .build()

        ));
        ActivityScenario<ClickTestActivity> scenario = ActivityScenario.launch(ClickTestActivity.class);
        onView(withText("female")).perform(click());
        onView(withText("male")).perform(click());
        Awaiter.untilTrue(receivedEvent);

        scenario.close();
    }

    @Test
    public void switchClickEventTest() {
        final AtomicBoolean receivedEvent = new AtomicBoolean(false);
        getEventsApiServer().setOnReceivedEventListener(new OnReceivedViewClickEventsListener(
                receivedEvent,
                new ViewElementEvent.Builder()
                        .setPath("/ClickTestActivity")
                        .setXpath("/Page/ActionBarOverlayLayout[0]/FrameLayout[0]/LinearLayout[0]#content_parent/Switch[0]#switch1")
                        .setTextValue("Switch[true]")
                        .setIndex(-1)
                        .build(),
                new ViewElementEvent.Builder()
                        .setPath("/ClickTestActivity")
                        .setXpath("/Page/ActionBarOverlayLayout[0]/FrameLayout[0]/LinearLayout[0]#content_parent/Switch[0]#switch1")
                        .setTextValue("Switch[false]")
                        .setIndex(-1)
                        .build()

        ));
        ActivityScenario<ClickTestActivity> scenario = ActivityScenario.launch(ClickTestActivity.class);
        onView(withId(R.id.switch1)).perform(click());
        onView(withId(R.id.switch1)).perform(click());
        Awaiter.untilTrue(receivedEvent);

        scenario.close();
    }

    @Test
    public void checkBoxClickEventTest() {
        final AtomicBoolean receivedEvent = new AtomicBoolean(false);
        getEventsApiServer().setOnReceivedEventListener(new OnReceivedViewClickEventsListener(
                receivedEvent,
                new ViewElementEvent.Builder()
                        .setPath("/ClickTestActivity")
                        .setXpath("/Page/ActionBarOverlayLayout[0]/FrameLayout[0]/LinearLayout[0]#content_parent/CheckBox[0]#check_box_android")
                        .setTextValue("Android[false]")
                        .setIndex(-1)
                        .build(),
                new ViewElementEvent.Builder()
                        .setPath("/ClickTestActivity")
                        .setXpath("/Page/ActionBarOverlayLayout[0]/FrameLayout[0]/LinearLayout[0]#content_parent/CheckBox[1]#check_box_ios")
                        .setTextValue("iOS[true]")
                        .setIndex(-1)
                        .build()

        ));
        ActivityScenario<ClickTestActivity> scenario = ActivityScenario.launch(ClickTestActivity.class);
        onView(withId(R.id.check_box_android)).perform(click());
        onView(withId(R.id.check_box_ios)).perform(click());
        Awaiter.untilTrue(receivedEvent);

        scenario.close();
    }

    @Test
    public void layoutClickEventTest() {
        final AtomicBoolean receivedEvent = new AtomicBoolean(false);
        getEventsApiServer().setOnReceivedEventListener(new OnReceivedViewClickEventsListener(
                receivedEvent,
                new ViewElementEvent.Builder()
                        .setPath("/ClickTestActivity")
                        .setXpath("/Page/ActionBarOverlayLayout[0]/FrameLayout[0]/LinearLayout[0]#content_parent")
                        .setTextValue("")
                        .setIndex(-1)
                        .build()

        ));
        ActivityScenario<ClickTestActivity> scenario = ActivityScenario.launch(ClickTestActivity.class);
        onView(withId(R.id.tv_click_placeholder)).perform(click());
        Awaiter.untilTrue(receivedEvent);
        scenario.close();
    }

    @Test
    public void pageSelfClickEventTest() {
        final AtomicBoolean receivedEvent = new AtomicBoolean(false);
        getEventsApiServer().setOnReceivedEventListener(new OnReceivedViewClickEventsListener(
                receivedEvent,
                new ViewElementEvent.Builder()
                        .setPath("/NestedFragmentActivity/GreenFragment[fragment1]/OrangeFragment[TestTag]")
                        .setXpath("/Page")
                        .setTextValue("")
                        .setIndex(-1)
                        .build()

        ));
        ActivityScenario<NestedFragmentActivity> scenario = ActivityScenario.launch(NestedFragmentActivity.class);
        onView(withTagKey(R.layout.fragment_orange)).perform(click());
        Awaiter.untilTrue(receivedEvent);
        scenario.close();
    }

    @Test
    public void listViewClickEventTest() {
        final AtomicBoolean receivedEvent = new AtomicBoolean(false);
        getEventsApiServer().setOnReceivedEventListener(new OnReceivedViewClickEventsListener(
                receivedEvent,
                new ViewElementEvent.Builder()
                        .setPath("/AutotrackEntryActivity")
                        .setXpath("/Page/ActionBarOverlayLayout[0]/FrameLayout[0]/LinearLayout[0]/ListView[0]#content/TextView[-]")
                        .setTextValue("Go To HideFragmentActivity")
                        .setIndex(1)
                        .build()

        ));
        ActivityScenario<AutotrackEntryActivity> scenario = ActivityScenario.launch(AutotrackEntryActivity.class);
        onView(withText("Go To HideFragmentActivity")).perform(click());
        Awaiter.untilTrue(receivedEvent);
        scenario.close();
    }

    @Test
    public void expandableListViewClickEventTest() {
        final AtomicBoolean receivedEvent = new AtomicBoolean(false);
        getEventsApiServer().setOnReceivedEventListener(new OnReceivedViewClickEventsListener(
                receivedEvent,
                new ViewElementEvent.Builder()
                        .setPath("/ExpandableListViewActivity")
                        .setXpath("/Page/ActionBarOverlayLayout[0]/FrameLayout[0]/LinearLayout[0]/ExpandableListView[0]#expand_lv/ELH[0]/LinearLayout[0]")
                        .setTextValue("")
                        .setIndex(-1)
                        .build(),
                new ViewElementEvent.Builder()
                        .setPath("/ExpandableListViewActivity")
                        .setXpath("/Page/ActionBarOverlayLayout[0]/FrameLayout[0]/LinearLayout[0]/ExpandableListView[0]#expand_lv/ELH[1]/LinearLayout[0]")
                        .setTextValue("")
                        .setIndex(-1)
                        .build(),

                new ViewElementEvent.Builder()
                        .setPath("/ExpandableListViewActivity")
                        .setXpath("/Page/ActionBarOverlayLayout[0]/FrameLayout[0]/LinearLayout[0]/ExpandableListView[0]#expand_lv/ELF[0]/LinearLayout[0]")
                        .setTextValue("")
                        .setIndex(-1)
                        .build(),
                new ViewElementEvent.Builder()
                        .setPath("/ExpandableListViewActivity")
                        .setXpath("/Page/ActionBarOverlayLayout[0]/FrameLayout[0]/LinearLayout[0]/ExpandableListView[0]#expand_lv/ELF[1]/LinearLayout[0]")
                        .setTextValue("")
                        .setIndex(-1)
                        .build(),

                new ViewElementEvent.Builder()
                        .setPath("/ExpandableListViewActivity")
                        .setXpath("/Page/ActionBarOverlayLayout[0]/FrameLayout[0]/LinearLayout[0]/ExpandableListView[0]#expand_lv/ELVG[-]/RelativeLayout[0]")
                        .setTextValue("")
                        .setIndex(0)
                        .build(),
                new ViewElementEvent.Builder()
                        .setPath("/ExpandableListViewActivity")
                        .setXpath("/Page/ActionBarOverlayLayout[0]/FrameLayout[0]/LinearLayout[0]/ExpandableListView[0]#expand_lv/ELVG[0]/ELVC[-]/LinearLayout[0]")
                        .setTextValue("")
                        .setIndex(1)
                        .build(),
                new ViewElementEvent.Builder()
                        .setPath("/ExpandableListViewActivity")
                        .setXpath("/Page/ActionBarOverlayLayout[0]/FrameLayout[0]/LinearLayout[0]/ExpandableListView[0]#expand_lv/ELVG[0]/ELVC[-]/LinearLayout[0]/LinearLayout[0]/TextView[0]#tv_name")
                        .setTextValue("三妹")
                        .setIndex(2)
                        .build(),

                new ViewElementEvent.Builder()
                        .setPath("/ExpandableListViewActivity")
                        .setXpath("/Page/ActionBarOverlayLayout[0]/FrameLayout[0]/LinearLayout[0]/ExpandableListView[0]#expand_lv/ELVG[-]/RelativeLayout[0]")
                        .setTextValue("")
                        .setIndex(2)
                        .build()

        ));
        ActivityScenario<ExpandableListViewActivity> scenario = ActivityScenario.launch(ExpandableListViewActivity.class);
        onView(withText("headerView1")).perform(click());
        onView(withText("headerView2")).perform(click());

        onView(withText("footerView1")).perform(click());
        onView(withText("footerView2")).perform(click());

        onView(withText("我的家人")).perform(click());
        onView(withText("二妹 Remark")).perform(click());
        onView(withText("三妹")).perform(click());

        onView(withText("黑名单")).perform(click());

        Awaiter.untilTrue(receivedEvent);
        scenario.close();
    }

    @Test
    public void expandableListActivityClickEventTest() {
        final AtomicBoolean receivedEvent = new AtomicBoolean(false);
        getEventsApiServer().setOnReceivedEventListener(new OnReceivedViewClickEventsListener(
                receivedEvent,
                new ViewElementEvent.Builder()
                        .setPath("/ExpandableListSubActivity")
                        .setXpath("/Page/ActionBarOverlayLayout[0]/FrameLayout[0]/ExpandableListView[0]/ELVG[0]/ELVC[-]/LinearLayout[0]")
                        .setTextValue("")
                        .setIndex(1)
                        .build(),
                new ViewElementEvent.Builder()
                        .setPath("/ExpandableListSubActivity")
                        .setXpath("/Page/ActionBarOverlayLayout[0]/FrameLayout[0]/ExpandableListView[0]/ELVG[0]/ELVC[-]/LinearLayout[0]/LinearLayout[0]/TextView[0]#tv_name")
                        .setTextValue("三妹")
                        .setIndex(2)
                        .build()

        ));
        ActivityScenario<ExpandableListSubActivity> scenario = ActivityScenario.launch(ExpandableListSubActivity.class);
        onView(withText("我的家人")).perform(click());
        onView(withText("二妹 Remark")).perform(click());
        onView(withText("三妹")).perform(click());
        Awaiter.untilTrue(receivedEvent);
        scenario.close();
    }

    // 不再支持 activityMenuItemClickEnabled 的无埋点
    //@Test
    public void optionsMenuClickEventTest() {
        final AtomicBoolean receivedEvent = new AtomicBoolean(false);
        getEventsApiServer().setOnReceivedEventListener(new OnReceivedViewClickEventsListener(
                receivedEvent,
                new ViewElementEvent.Builder()
                        .setPath("/ClickTestActivity")
                        .setXpath("/Page/MenuView/MenuItem#navigation_home")
                        .setTextValue("Home")
                        .setIndex(-1)
                        .build(),
                new ViewElementEvent.Builder()
                        .setPath("/ClickTestActivity")
                        .setXpath("/Page/MenuView/MenuItem#navigation_dashboard")
                        .setTextValue("Dashboard")
                        .setIndex(-1)
                        .build(),
                new ViewElementEvent.Builder()
                        .setPath("/ClickTestActivity")
                        .setXpath("/Page/MenuView/MenuItem#navigation_notifications")
                        .setTextValue("Notifications")
                        .setIndex(-1)
                        .build()

        ));
        ActivityScenario<ClickTestActivity> scenario = ActivityScenario.launch(ClickTestActivity.class);
        openActionBarOverflowOrOptionsMenu(ApplicationProvider.getApplicationContext());
        onView(withText("Home")).perform(click());
        openActionBarOverflowOrOptionsMenu(ApplicationProvider.getApplicationContext());
        onView(withText("Dashboard")).perform(click());
        openActionBarOverflowOrOptionsMenu(ApplicationProvider.getApplicationContext());
        onView(withText("Notifications")).perform(click());
        Awaiter.untilTrue(receivedEvent);

        scenario.close();
    }

    @Test
    public void customIdClickEventTest() {
        final AtomicBoolean receivedEvent = new AtomicBoolean(false);
        getEventsApiServer().setOnReceivedEventListener(new OnReceivedViewClickEventsListener(
                receivedEvent,
                new ViewElementEvent.Builder()
                        .setPath("/ClickTestActivity")
                        .setXpath("/testCustomId")
                        .setTextValue("测试Button点击")
                        .setIndex(-1)
                        .build()

        ));
        ActivityLifecycleMonitorRegistry.getInstance().addLifecycleCallback((activity, stage) -> {
            if (activity.getClass() == ClickTestActivity.class && stage == Stage.CREATED) {
                View button = activity.findViewById(R.id.btn_test_click);
                GrowingAutotracker.get().setUniqueTag(button, "testCustomId");
            }
        });
        ActivityScenario<ClickTestActivity> scenario = ActivityScenario.launch(ClickTestActivity.class);
        onView(withId(R.id.btn_test_click)).perform(click());
        Awaiter.untilTrue(receivedEvent);
        scenario.close();
    }

    @Test
    public void viewClickEventInFragment() {
        final AtomicBoolean receivedEvent = new AtomicBoolean(false);
        getEventsApiServer().setOnReceivedEventListener(new OnReceivedViewClickEventsListener(
                receivedEvent,
                new ViewElementEvent.Builder()
                        .setPath("*/GreenFragment[fragment1]/OrangeFragment[TestTag]/RedFragment[small]")
                        .setXpath("/Page/TextView[0]#fragment_title")
                        .setTextValue("small RedFragment")
                        .setIndex(-1)
                        .build()

        ));
        ActivityScenario<NestedFragmentActivity> scenario = ActivityScenario.launch(NestedFragmentActivity.class);
        onView(withText("small RedFragment")).perform(click());
        Awaiter.untilTrue(receivedEvent);
        scenario.close();
    }

    @Test
    public void viewClickEventInIgnoreSelfFragment() {
        final AtomicBoolean receivedEvent = new AtomicBoolean(false);
        getEventsApiServer().setOnReceivedEventListener(new OnReceivedViewClickEventsListener(
                receivedEvent,
                new ViewElementEvent.Builder()
                        .setPath("/NestedFragmentActivity/GreenFragment[fragment1]/OrangeFragment[TestTag]")
                        .setXpath("/Page/RedFragment[small]/TextView[0]#fragment_title")
                        .setTextValue("small RedFragment")
                        .setIndex(-1)
                        .build()

        ));
        FragmentLifecycleMonitor.get().addLifecycleCallback((fragment, stage) -> {
            if (fragment.getClass() == RedFragment.class && stage == FragmentLifecycleCallback.Stage.CREATED) {
                GrowingAutotracker.get().ignorePageSupport(fragment, IgnorePolicy.IGNORE_SELF);
            }
        });
        ActivityScenario<NestedFragmentActivity> scenario = ActivityScenario.launch(NestedFragmentActivity.class);
        onView(withText("small RedFragment")).perform(click());
        Awaiter.untilTrue(receivedEvent);
        scenario.close();
    }

    @Test
    public void viewClickEventInIgnoreChildFragments() {
        final AtomicBoolean receivedEvent = new AtomicBoolean(false);
        getEventsApiServer().setOnReceivedEventListener(new OnReceivedViewClickEventsListener(
                receivedEvent,
                new ViewElementEvent.Builder()
                        .setPath("/NestedFragmentActivity/GreenFragment[fragment1]")
                        .setXpath("/Page/OrangeFragment[TestTag]/RedFragment[small]/TextView[0]#fragment_title")
                        .setTextValue("small RedFragment")
                        .setIndex(-1)
                        .build()

        ));
        FragmentLifecycleMonitor.get().addLifecycleCallback((fragment, stage) -> {
            if (fragment.getClass() == GreenFragment.class && stage == FragmentLifecycleCallback.Stage.CREATED) {
                GrowingAutotracker.get().ignorePageSupport(fragment, IgnorePolicy.IGNORE_CHILD);
            }
        });
        ActivityScenario<NestedFragmentActivity> scenario = ActivityScenario.launch(NestedFragmentActivity.class);
        onView(withText("small RedFragment")).perform(click());
        Awaiter.untilTrue(receivedEvent);
        scenario.close();
    }

    @Test
    public void viewClickEventInIgnoreChildActivity() {
        final AtomicBoolean receivedEvent = new AtomicBoolean(false);
        getEventsApiServer().setOnReceivedEventListener(new OnReceivedViewClickEventsListener(
                receivedEvent,
                new ViewElementEvent.Builder()
                        .setPath("/NestedFragmentActivity")
                        .setXpath("/Page/GreenFragment[fragment1]/OrangeFragment[TestTag]/RedFragment[small]/TextView[0]#fragment_title")
                        .setTextValue("small RedFragment")
                        .setIndex(-1)
                        .build()

        ));
        ActivityLifecycleMonitorRegistry.getInstance().addLifecycleCallback((activity, stage) -> {
            if (activity.getClass() == NestedFragmentActivity.class && stage == Stage.CREATED) {
                GrowingAutotracker.get().ignorePage(activity, IgnorePolicy.IGNORE_CHILD);
            }
        });
        ActivityScenario<NestedFragmentActivity> scenario = ActivityScenario.launch(NestedFragmentActivity.class);
        onView(withText("small RedFragment")).perform(click());
        Awaiter.untilTrue(receivedEvent);
        scenario.close();
    }

    @Test
    public void buttonClickEventOfActivityIgnorePageTest() {
        final AtomicBoolean receivedEvent = new AtomicBoolean(false);
        getEventsApiServer().setOnReceivedEventListener(new OnReceivedViewClickEventsListener(
                receivedEvent,
                new ViewElementEvent.Builder()
                        .setPath("/ClickTestActivity")
                        .setXpath("/IgnorePage/ActionBarOverlayLayout[0]/FrameLayout[0]/LinearLayout[0]#content_parent/Button[0]#btn_test_click")
                        .setTextValue("测试Button点击")
                        .setIndex(-1)
                        .build()

        ));
        ActivityLifecycleMonitorRegistry.getInstance().addLifecycleCallback((activity, stage) -> {
            if (activity.getClass() == ClickTestActivity.class && stage == Stage.CREATED) {
                GrowingAutotracker.get().ignorePage(activity, IgnorePolicy.IGNORE_SELF);
            }
        });
        ActivityScenario<ClickTestActivity> scenario = ActivityScenario.launch(ClickTestActivity.class);
        onView(withId(R.id.btn_test_click)).perform(click());
        Awaiter.untilTrue(receivedEvent);
        scenario.close();
    }

    @Test
    public void viewClickEventInCustomIdViewTest() {
        final AtomicBoolean receivedEvent = new AtomicBoolean(false);
        getEventsApiServer().setOnReceivedEventListener(new OnReceivedViewClickEventsListener(
                receivedEvent,
                new ViewElementEvent.Builder()
                        .setPath("*/GreenFragment[fragment1]/OrangeFragment[TestTag]/RedFragment[small]")
                        .setXpath("/testRedFragmentView/TextView[0]#fragment_title")
                        .setTextValue("small RedFragment")
                        .setIndex(-1)
                        .build()

        ));
        FragmentLifecycleMonitor.get().addLifecycleCallback((fragment, stage) -> {
            if (fragment.getClass() == RedFragment.class && stage == FragmentLifecycleCallback.Stage.RESUMED) {
                GrowingAutotracker.get().setUniqueTag(fragment.getView(), "testRedFragmentView");
            }
        });
        ActivityScenario<NestedFragmentActivity> scenario = ActivityScenario.launch(NestedFragmentActivity.class);
        onView(withText("small RedFragment")).perform(click());
        Awaiter.untilTrue(receivedEvent);
        scenario.close();
    }

    @Test
    public void alertDialogClickEventTest() {
        final AtomicBoolean receivedEvent = new AtomicBoolean(false);
        getEventsApiServer().setOnReceivedEventListener(new OnReceivedViewClickEventsListener(
                receivedEvent,
                new ViewElementEvent.Builder()
                        .setPath("/DialogTestActivity")
                        .setXpath("/AlertDialog/AlertDialog Title/BUTTON_POSITIVE")
                        .setTextValue("OK")
                        .setIndex(-1)
                        .build()

        ));
        ActivityScenario<DialogTestActivity> scenario = ActivityScenario.launch(DialogTestActivity.class);
        onView(withId(R.id.btn_show_alert_dialog)).perform(click());
        onView(withText("OK")).perform(click());
        Awaiter.untilTrue(receivedEvent);
        scenario.close();
    }

    @Test
    public void noTitleAlertDialogClickEventTest() {
        String xpath = "/AlertDialog/这是一个没有标题的AlertDialog/BUTTON_NEGATIVE";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            xpath = "/AlertDialog/BUTTON_NEGATIVE";
        }
        final AtomicBoolean receivedEvent = new AtomicBoolean(false);
        getEventsApiServer().setOnReceivedEventListener(new OnReceivedViewClickEventsListener(
                receivedEvent,
                new ViewElementEvent.Builder()
                        .setPath("/DialogTestActivity")
                        .setXpath(xpath)
                        .setTextValue("Cancel")
                        .setIndex(-1)
                        .build()

        ));
        ActivityScenario<DialogTestActivity> scenario = ActivityScenario.launch(DialogTestActivity.class);
        onView(withId(R.id.btn_show_no_title_alert_dialog)).perform(click());
        onView(withText("Cancel")).perform(click());
        Awaiter.untilTrue(receivedEvent);
        scenario.close();
    }

    @Test
    public void popupWindowClickEventTest() {
        final AtomicBoolean receivedEvent = new AtomicBoolean(false);
        getEventsApiServer().setOnReceivedEventListener(new OnReceivedViewClickEventsListener(
                receivedEvent,
                new ViewElementEvent.Builder()
                        .setPath("/DialogTestActivity")
                        .setXpath("/PopupWindow/PopupDecorView/LinearLayout[0]/Button[0]#btn_preview")
                        .setTextValue("测试预览")
                        .setIndex(-1)
                        .build(),
                new ViewElementEvent.Builder()
                        .setPath("/DialogTestActivity")
                        .setXpath("/AlertDialog/AlertDialog Title/BUTTON_POSITIVE")
                        .setTextValue("OK")
                        .setIndex(-1)
                        .build()

        ));
        ActivityScenario<DialogTestActivity> scenario = ActivityScenario.launch(DialogTestActivity.class);
        onView(withId(R.id.btn_show_popup_window)).perform(click());
        onView(withText("测试预览")).perform(click());
        onView(withText("OK")).perform(click());
        Awaiter.untilTrue(receivedEvent);
        scenario.close();
    }

    @Test
    public void parentIgnoreSelfViewClickEventTest() {
        final AtomicBoolean receivedEvent = new AtomicBoolean(false);
        getEventsApiServer().setOnReceivedEventListener(new OnReceivedViewClickEventsListener(
                receivedEvent,
                new ViewElementEvent.Builder()
                        .setPath("*/GreenFragment[fragment1]/OrangeFragment[TestTag]/RedFragment[small]")
                        .setXpath("/Page/TextView[0]#fragment_title")
                        .setTextValue("small RedFragment")
                        .setIndex(-1)
                        .build()

        ));
        FragmentLifecycleMonitor.get().addLifecycleCallback((fragment, stage) -> {
            if (fragment.getClass() == RedFragment.class && stage == FragmentLifecycleCallback.Stage.RESUMED) {
                GrowingAutotracker.get().ignoreView(fragment.getView(), IgnorePolicy.IGNORE_SELF);
            }
        });
        ActivityScenario<NestedFragmentActivity> scenario = ActivityScenario.launch(NestedFragmentActivity.class);
        onView(withText("small RedFragment")).perform(click());
        Awaiter.untilTrue(receivedEvent);
        scenario.close();
    }

    @Test
    public void parentIgnoreChildViewClickEventTest() {
        getEventsApiServer().setOnReceivedEventListener(new StopReceivedViewClickEventsListener());
        FragmentLifecycleMonitor.get().addLifecycleCallback((fragment, stage) -> {
            if (fragment.getClass() == RedFragment.class && stage == FragmentLifecycleCallback.Stage.RESUMED) {
                GrowingAutotracker.get().ignoreView(fragment.getView(), IgnorePolicy.IGNORE_CHILD);
            }
        });
        ActivityScenario<NestedFragmentActivity> scenario = ActivityScenario.launch(NestedFragmentActivity.class);
        onView(withText("small RedFragment")).perform(click());
        TrackHelper.waitForIdleSync();
        scenario.close();
    }

    @Test
    public void parentIgnoreAllViewClickEventTest() {
        getEventsApiServer().setOnReceivedEventListener(new StopReceivedViewClickEventsListener());
        FragmentLifecycleMonitor.get().addLifecycleCallback((fragment, stage) -> {
            if (fragment.getClass() == RedFragment.class && stage == FragmentLifecycleCallback.Stage.RESUMED) {
                GrowingAutotracker.get().ignoreView(fragment.getView(), IgnorePolicy.IGNORE_ALL);
            }
        });
        ActivityScenario<NestedFragmentActivity> scenario = ActivityScenario.launch(NestedFragmentActivity.class);
        onView(withText("small RedFragment")).perform(click());
        TrackHelper.waitForIdleSync();
        scenario.close();
    }

    @Test
    public void ignoreSelfViewClickEventTest() {
        getEventsApiServer().setOnReceivedEventListener(new StopReceivedViewClickEventsListener());
        FragmentLifecycleMonitor.get().addLifecycleCallback((fragment, stage) -> {
            if (fragment.getClass() == RedFragment.class && stage == FragmentLifecycleCallback.Stage.RESUMED) {
                GrowingAutotracker.get().ignoreView(fragment.getView().findViewById(R.id.fragment_title), IgnorePolicy.IGNORE_SELF);
            }
        });
        ActivityScenario<NestedFragmentActivity> scenario = ActivityScenario.launch(NestedFragmentActivity.class);
        onView(withText("small RedFragment")).perform(click());
        TrackHelper.waitForIdleSync();
        scenario.close();
    }

    @Test
    @SdkSuppress(minSdkVersion = Build.VERSION_CODES.LOLLIPOP)
    public void toolBarClickTest() {
        final AtomicBoolean receivedEvent = new AtomicBoolean(false);
        getEventsApiServer().setOnReceivedEventListener(new OnReceivedViewClickEventsListener(
                receivedEvent,
                new ViewElementEvent.Builder()
                        .setPath("/ToolBarActivity")
                        .setXpath("/Page/MenuView/MenuItem#navigation_home")
                        .setTextValue("Home")
                        .setIndex(-1)
                        .build(),
                new ViewElementEvent.Builder()
                        .setPath("/ToolBarActivity")
                        .setXpath("/Page/MenuView/MenuItem#navigation_dashboard")
                        .setTextValue("Dashboard")
                        .setIndex(-1)
                        .build(),
                new ViewElementEvent.Builder()
                        .setPath("/ToolBarActivity")
                        .setXpath("/Page/MenuView/MenuItem#navigation_notifications")
                        .setTextValue("Notifications")
                        .setIndex(-1)
                        .build()

        ));
        ActivityScenario<ToolBarActivity> scenario = ActivityScenario.launch(ToolBarActivity.class);
        openActionBarOverflowOrOptionsMenu(ApplicationProvider.getApplicationContext());
        onView(withText("Home")).perform(click());
        openActionBarOverflowOrOptionsMenu(ApplicationProvider.getApplicationContext());
        onView(withText("Dashboard")).perform(click());
        openActionBarOverflowOrOptionsMenu(ApplicationProvider.getApplicationContext());
        onView(withText("Notifications")).perform(click());
        Awaiter.untilTrue(receivedEvent);

        scenario.close();
    }

    @Test
    @SdkSuppress(minSdkVersion = Build.VERSION_CODES.LOLLIPOP)
    public void actionMenuViewClickTest() {
        final AtomicBoolean receivedEvent = new AtomicBoolean(false);
        getEventsApiServer().setOnReceivedEventListener(new OnReceivedViewClickEventsListener(
                receivedEvent,
                new ViewElementEvent.Builder()
                        .setPath("/ActionMenuViewActivity")
                        .setXpath("/Page/MenuView/MenuItem#navigation_home")
                        .setTextValue("Home")
                        .setIndex(-1)
                        .build(),
                new ViewElementEvent.Builder()
                        .setPath("/ActionMenuViewActivity")
                        .setXpath("/Page/MenuView/MenuItem#navigation_dashboard")
                        .setTextValue("Dashboard")
                        .setIndex(-1)
                        .build(),
                new ViewElementEvent.Builder()
                        .setPath("/ActionMenuViewActivity")
                        .setXpath("/Page/MenuView/MenuItem#navigation_notifications")
                        .setTextValue("Notifications")
                        .setIndex(-1)
                        .build()

        ));
        ActivityScenario<ActionMenuViewActivity> scenario = ActivityScenario.launch(ActionMenuViewActivity.class);
        openActionBarOverflowOrOptionsMenu(ApplicationProvider.getApplicationContext());
        onView(withText("Home")).perform(click());
        openActionBarOverflowOrOptionsMenu(ApplicationProvider.getApplicationContext());
        onView(withText("Dashboard")).perform(click());
        openActionBarOverflowOrOptionsMenu(ApplicationProvider.getApplicationContext());
        onView(withText("Notifications")).perform(click());
        Awaiter.untilTrue(receivedEvent);

        scenario.close();
    }

    @Test
    public void popupMenuClickTest() {
        final AtomicBoolean receivedEvent = new AtomicBoolean(false);
        getEventsApiServer().setOnReceivedEventListener(new OnReceivedViewClickEventsListener(
                receivedEvent,
                new ViewElementEvent.Builder()
                        .setPath("/DialogTestActivity")
                        .setXpath("/Page/MenuView/MenuItem#navigation_home")
                        .setTextValue("Home")
                        .setIndex(-1)
                        .build(),
                new ViewElementEvent.Builder()
                        .setPath("/DialogTestActivity")
                        .setXpath("/Page/MenuView/MenuItem#navigation_dashboard")
                        .setTextValue("Dashboard")
                        .setIndex(-1)
                        .build(),
                new ViewElementEvent.Builder()
                        .setPath("/DialogTestActivity")
                        .setXpath("/Page/MenuView/MenuItem#navigation_notifications")
                        .setTextValue("Notifications")
                        .setIndex(-1)
                        .build()

        ));
        ActivityScenario<DialogTestActivity> scenario = ActivityScenario.launch(DialogTestActivity.class);
        onView(withId(R.id.btn_show_popup_menu)).perform(click());
        onView(withText("Home")).perform(click());
        onView(withId(R.id.btn_show_popup_menu)).perform(click());
        onView(withText("Dashboard")).perform(click());
        onView(withId(R.id.btn_show_popup_menu)).perform(click());
        onView(withText("Notifications")).perform(click());
        Awaiter.untilTrue(receivedEvent);

        scenario.close();
    }

    private static final class StopReceivedViewClickEventsListener extends MockEventsApiServer.OnReceivedEventListener {
        @Override
        protected void onReceivedViewClickEvents(JSONArray jsonArray) throws JSONException {
            Truth.assertWithMessage("Received View Click Events").fail();
        }
    }

    private static final class OnReceivedViewClickEventsListener extends MockEventsApiServer.OnReceivedEventListener {
        private final Map<String, Long> mReceivedPages = new HashMap<>();
        private final List<ViewElementEvent> mExpectReceivedClicks;
        private final AtomicBoolean mReceivedEvent;

        OnReceivedViewClickEventsListener(AtomicBoolean receivedEvents, ViewElementEvent... viewClickEvent) {
            mReceivedEvent = receivedEvents;
            mReceivedEvent.set(false);
            mExpectReceivedClicks = new ArrayList<>(Arrays.asList(viewClickEvent));
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
        protected void onReceivedViewClickEvents(JSONArray jsonArray) throws JSONException {
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                for (int j = 0; j < mExpectReceivedClicks.size(); j++) {
                    String path = jsonObject.getString("path");
                    ViewElementEvent viewElementEvent = mExpectReceivedClicks.get(j);
                    if (path.equals(viewElementEvent.getPath())
                            && jsonObject.getString("xpath").equals(viewElementEvent.getXpath())
                            && jsonObject.optString("textValue").equals(viewElementEvent.getTextValue())
                            && jsonObject.getInt("index") == viewElementEvent.getIndex()
                            && (viewElementEvent.getXpath().startsWith("/IgnorePage/") || jsonObject.getLong("pageShowTimestamp") == mReceivedPages.get(path))) {
                        mExpectReceivedClicks.remove(j);
                        break;
                    }
                }
                if (mExpectReceivedClicks.isEmpty()) {
                    mReceivedEvent.set(true);
                }
            }
        }
    }
}
