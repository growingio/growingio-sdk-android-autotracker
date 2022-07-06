/*
 *   Copyright (C) 2020 Beijing Yishu Technology Co., Ltd.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package com.growingio.android.sdk.autotrack.click;

import android.app.AlertDialog;
import android.app.Application;
import android.view.View;

import androidx.test.core.app.ApplicationProvider;

import com.google.common.truth.Truth;
import com.growingio.android.sdk.TrackerContext;
import com.growingio.android.sdk.autotrack.RobolectricActivity;
import com.growingio.android.sdk.autotrack.inject.DialogInjector;
import com.growingio.android.sdk.autotrack.inject.ViewClickInjector;
import com.growingio.android.sdk.track.events.ViewElementEvent;
import com.growingio.android.sdk.track.providers.ActivityStateProvider;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
@Config(shadows = {TrackMainThreadShadow.class})
public class ViewClickTest {


    private RobolectricActivity activity;
    private Application application = ApplicationProvider.getApplicationContext();

    @Before
    public void setup() {
        TrackerContext.init(application);
        TrackerContext.initSuccess();
        activity = Robolectric.buildActivity(RobolectricActivity.class).create().resume().get();
        ActivityStateProvider.get().onActivityResumed(activity);
    }

    @Test
    public void injectTest() {
        TrackMainThreadShadow.callback = (event) -> {
            if (event.getEventType().equals("VIEW_CLICK")) {
                ViewElementEvent clickEvent = (ViewElementEvent) event;
                Truth.assertThat(clickEvent.getTextValue()).isEqualTo("this is cpacm");
                Truth.assertThat(clickEvent.getXpath()).isEqualTo("/MainWindow/DecorView/ActionBarOverlayLayout[0]/FrameLayout[0]/LinearLayout[0]/TextView[0]");
            }
        };

        ViewClickInjector.viewOnClick((View.OnClickListener) v -> {
        }, activity.getTextView());

        TrackMainThreadShadow.callback = (event) -> {
            System.out.println(event.toJSONObject());
            if (event.getEventType().equals("VIEW_CLICK")) {
                ViewElementEvent clickEvent = (ViewElementEvent) event;
                Truth.assertThat(clickEvent.getTextValue()).isEqualTo("test");
                Truth.assertThat(clickEvent.getXpath()).isEqualTo("/AlertDialog/BUTTON_POSITIVE");
            }
        };

        AlertDialog testDialog = new AlertDialog.Builder(activity)
                .setPositiveButton("test", (dialog, which) -> {
                })
                .create();
        testDialog.show();
        DialogInjector.alertDialogShow(testDialog);
        DialogInjector.dialogOnClick((dialog, which) -> {
        }, testDialog, -1);
    }
}
