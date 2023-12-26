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
package com.growingio.android.circler;

import android.app.Activity;
import android.app.Application;
import android.graphics.Rect;
import android.view.View;
import android.view.WindowManager;

import androidx.test.core.app.ApplicationProvider;

import com.google.common.truth.Truth;
import com.growingio.android.circler.shadow.ShadowHandler;
import com.growingio.android.circler.shadow.ShadowWH;
import com.growingio.android.hybrid.HybridLibraryGioModule;
import com.growingio.android.sdk.Tracker;
import com.growingio.android.sdk.TrackerContext;
import com.growingio.android.sdk.autotrack.view.ViewNodeProvider;
import com.growingio.android.sdk.track.listener.Callback;
import com.growingio.android.sdk.track.view.DecorView;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.List;

@Config(manifest = Config.NONE, shadows = {ShadowHandler.class, ShadowWH.class})
@RunWith(RobolectricTestRunner.class)
public class ScreenShotTest {

    Application application = ApplicationProvider.getApplicationContext();
    private TrackerContext context;

    @Before
    public void setup() {
        Tracker tracker = new Tracker(application);
        context = tracker.getContext();
        HybridLibraryGioModule module = new HybridLibraryGioModule();
        tracker.registerComponent(module);

        CirclerLibraryGioModule cModule = new CirclerLibraryGioModule();
        tracker.registerComponent(cModule);
    }

    @Test
    public void dispatchTest() {
        ScreenshotProvider screenshotProvider = context.getProvider(ScreenshotProvider.class);
        Activity activity = Robolectric.buildActivity(RobolectricActivity.class).create().resume().get();
        ShadowWH.activity = activity;
        ScreenshotProvider.OnScreenshotRefreshedListener dispatchListener = screenshot -> {
            Truth.assertThat(screenshot).isNotNull();
        };
        screenshotProvider.registerScreenshotRefreshedListener(dispatchListener);

        screenshotProvider.unregisterScreenshotRefreshedListener();
    }

    @Test
    public void circlerScreenShotTest() {
        ScreenshotProvider screenshotProvider = context.getProvider(ScreenshotProvider.class);
        ViewNodeProvider viewNodeProvider = context.getProvider(ViewNodeProvider.class);
        Activity activity = Robolectric.buildActivity(RobolectricActivity.class).create().resume().get();
        ShadowWH.activity = activity;

        ScreenshotProvider.OnScreenshotRefreshedListener baseListener = screenshot -> {
            Truth.assertThat(screenshot.toJSONObject().toString()).isEqualTo(
                    "{\"screenWidth\":320,\"screenHeight\":470,\"scale\":100,\"screenshot\":\"this is test base64\",\"msgType\":\"refreshScreenshot\",\"snapshotKey\":0,\"elements\":[],\"pages\":[]}"
            );
        };
        screenshotProvider.registerScreenshotRefreshedListener(baseListener);

        new CircleScreenshot.Builder(1920, 1680)
                .setScale(100)
                .setScreenshot("this is test base64")
                .setSnapshotKey(0)
                .build(getAllWindowDecorViews(), viewNodeProvider, new Callback<CircleScreenshot>() {
                    @Override
                    public void onSuccess(CircleScreenshot result) {
                        screenshotProvider.sendScreenshot(result);
                    }

                    @Override
                    public void onFailed() {

                    }
                });
        screenshotProvider.unregisterScreenshotRefreshedListener();
    }

    public List<DecorView> getAllWindowDecorViews() {
        Activity activity = Robolectric.buildActivity(RobolectricActivity.class).create().resume().get();
        View view = activity.getWindow().getDecorView();

        List<DecorView> decorViews = new ArrayList<>();
        int[] location = new int[2];
        view.getLocationOnScreen(location);
        int x = location[0];
        int y = location[1];

        Rect area = new Rect(x, y, x + view.getWidth(), y + view.getHeight());
        WindowManager.LayoutParams wlp = new WindowManager.LayoutParams();
        decorViews.add(new DecorView(view, area, wlp));

        return decorViews;
    }
}
