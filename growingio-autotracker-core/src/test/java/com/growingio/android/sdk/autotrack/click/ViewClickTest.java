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
package com.growingio.android.sdk.autotrack.click;

import static android.widget.LinearLayout.VERTICAL;

import android.app.AlertDialog;
import android.app.Application;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.test.core.app.ApplicationProvider;

import com.google.common.truth.Truth;
import com.growingio.android.sdk.Configurable;
import com.growingio.android.sdk.CoreConfiguration;
import com.growingio.android.sdk.autotrack.AutotrackConfig;
import com.growingio.android.sdk.autotrack.Autotracker;
import com.growingio.android.sdk.autotrack.RobolectricActivity;
import com.growingio.android.sdk.autotrack.TrackMainThreadShadow;
import com.growingio.android.sdk.autotrack.inject.DialogInjector;
import com.growingio.android.sdk.autotrack.inject.ViewClickInjector;
import com.growingio.android.sdk.track.events.ViewElementEvent;
import com.growingio.android.sdk.track.providers.TrackerLifecycleProviderFactory;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.HashMap;
import java.util.Map;

@RunWith(RobolectricTestRunner.class)
@Config(shadows = {TrackMainThreadShadow.class})
public class ViewClickTest {

    private Autotracker autotracker;
    private RobolectricActivity activity;
    private Application application = ApplicationProvider.getApplicationContext();

    @Before
    public void setup() {
        CoreConfiguration coreConfiguration = new CoreConfiguration("ViewClickTest", "growingio://apm");
        AutotrackConfig autotrackConfig = new AutotrackConfig();
        Map<Class<? extends Configurable>, Configurable> map = new HashMap<>();
        map.put(AutotrackConfig.class, autotrackConfig);
        TrackerLifecycleProviderFactory.create()
                .createConfigurationProviderWithConfig(
                        coreConfiguration,
                        map);
        autotracker = new Autotracker(application);
        activity = Robolectric.buildActivity(RobolectricActivity.class).create().resume().get();
    }

    @Test
    public void injectTest() {
        TrackMainThreadShadow.callback = (event) -> {
            if (event.getEventType().equals("VIEW_CLICK")) {
                ViewElementEvent clickEvent = (ViewElementEvent) event;
                Truth.assertThat(clickEvent.getTextValue()).isEqualTo("this is cpacm");
                Truth.assertThat(clickEvent.getXpath()).isEqualTo("/RobolectricActivity/DecorView/LinearLayout/FrameLayout/FitWindowsLinearLayout/ContentFrameLayout/LinearLayout/TextView");
                Truth.assertThat(clickEvent.getXIndex()).isEqualTo("/0/0/0/0/action_bar_root/0/0/0");
            }
        };

        ViewClickInjector.viewOnClick(v -> {
        }, activity.getTextView());

        TrackMainThreadShadow.callback = (event) -> {
            if (event.getEventType().equals("VIEW_CLICK")) {
                ViewElementEvent clickEvent = (ViewElementEvent) event;
                Truth.assertThat(clickEvent.getTextValue()).isEqualTo("negative");
                Truth.assertThat(clickEvent.getXpath()).isEqualTo("/AlertDialogButtonLayout/AppCompatButton");
                Truth.assertThat(clickEvent.getXIndex()).isEqualTo("/0/0");
            }
        };

        AlertDialog testDialog = new AlertDialog.Builder(activity)
                .setPositiveButton("test", (dialog, which) -> {
                })
                .setNegativeButton("negative", (dialog, which) -> {
                })
                .create();
        testDialog.show();
        DialogInjector.alertDialogShow(testDialog);
        DialogInjector.dialogOnClick((dialog, which) -> {
        }, testDialog, -2);


        TrackMainThreadShadow.callback = null;
    }

    @Test
    public void alertDialogCustomViewTest() {
        TrackMainThreadShadow.callback = (event) -> {
            if (event.getEventType().equals("VIEW_CLICK")) {
                ViewElementEvent clickEvent = (ViewElementEvent) event;
                Truth.assertThat(clickEvent.getXpath()).isEqualTo("/RobolectricActivity/DecorView/FrameLayout/FrameLayout/LinearLayout/FrameLayout/FrameLayout/LinearLayout/Button");
                Truth.assertThat(clickEvent.getXIndex()).isEqualTo("/0/0/0/0/0/0/0/0/0");
            }
        };

        Button button = new Button(activity);
        button.setText("alertClick");
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        LinearLayout linearLayout = new LinearLayout(activity);
        linearLayout.setOrientation(VERTICAL);
        linearLayout.addView(button);

        AlertDialog customViewDialog = new AlertDialog.Builder(activity)
                .setView(linearLayout)
                .create();
        customViewDialog.show();
        DialogInjector.alertDialogShow(customViewDialog);
        ViewClickInjector.viewOnClick(v -> {
        }, button);

        TrackMainThreadShadow.callback = null;
    }
}
