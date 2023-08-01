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
package com.growingio.android.sdk.track.view;

import android.app.Activity;
import android.app.Application;
import android.view.View;

import androidx.test.core.app.ApplicationProvider;

import com.google.common.truth.Truth;
import com.growingio.android.sdk.Tracker;
import com.growingio.android.sdk.TrackerContext;
import com.growingio.android.sdk.track.providers.ActivityStateProvider;
import com.growingio.android.sdk.track.providers.RobolectricActivity;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.io.IOException;
import java.util.List;
import java.util.function.Consumer;


@Config(manifest = Config.NONE)
@RunWith(RobolectricTestRunner.class)
public class ViewTest {

    private TrackerContext context;

    @Before
    public void setup() {
        Application application = ApplicationProvider.getApplicationContext();
        Tracker tracker = new Tracker(application);
        context = tracker.getContext();
    }

    @Test
    public void windowHelperTest() {
        ActivityStateProvider activityStateProvider = context.getActivityStateProvider();
        Activity activity = Robolectric.buildActivity(RobolectricActivity.class).create().resume().get();

        Truth.assertThat(WindowHelper.get().isDecorView(activity.getWindow().getDecorView())).isTrue();

        activityStateProvider.onActivityResumed(activity);
        Truth.assertThat(WindowHelper.get().getTopActivityDecorView()).isEqualTo(activity.getWindow().getDecorView());
        List<DecorView> views = WindowHelper.get().getTopActivityViews();
        views.forEach(new Consumer<DecorView>() {
            @Override
            public void accept(DecorView decorView) {
                Truth.assertThat(decorView.getLayoutParams().packageName).isEqualTo("com.cpacm.test");
            }
        });
    }

    @Test
    public void windowManagerShadow() throws Exception {
        WindowManagerShadow shadow = new WindowManagerShadow(FakeWindowManagerGlobal.class.getName());
        View[] array = shadow.getAllWindowViews();
        Truth.assertThat(array.length).isEqualTo(0);
    }

    Application application = ApplicationProvider.getApplicationContext();

    @Test
    public void screenshotTest() throws Exception {
        ActivityStateProvider activityStateProvider = context.getActivityStateProvider();
        Activity activity = Robolectric.buildActivity(RobolectricActivity.class).create().resume().get();
        activityStateProvider.onActivityResumed(activity);

        ScreenshotUtil.getScreenshotBitmap(1, bitmap -> {
            Truth.assertThat(bitmap).isNotNull();
            try {
                String base64 = ScreenshotUtil.getScreenshotBase64(bitmap);
                Truth.assertThat(base64).startsWith("data:image/jpeg;base64,");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

}
