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

package com.growingio.android.circler;

import android.app.Activity;
import android.app.Application;
import android.graphics.Rect;
import android.view.View;
import android.view.WindowManager;

import androidx.test.core.app.ApplicationProvider;

import com.google.common.truth.Truth;
import com.growingio.android.circler.screenshot.CircleScreenshot;
import com.growingio.android.circler.screenshot.ScreenshotProvider;
import com.growingio.android.circler.shadow.ShadowHandler;
import com.growingio.android.circler.shadow.ShadowWH;
import com.growingio.android.hybrid.HybridLibraryGioModule;
import com.growingio.android.sdk.TrackerContext;
import com.growingio.android.sdk.track.async.Callback;
import com.growingio.android.sdk.track.providers.ActivityStateProvider;
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

    @Before
    public void setup() {
        TrackerContext.init(application);
        HybridLibraryGioModule module = new HybridLibraryGioModule();
        module.registerComponents(application, TrackerContext.get().getRegistry());
    }

    @Test
    public void dispatchTest() {
        Activity activity = Robolectric.buildActivity(RobolectricActivity.class).create().resume().get();
        ActivityStateProvider.get().onActivityResumed(activity);
        ShadowWH.activity = activity;
        ScreenshotProvider.OnScreenshotRefreshedListener dispatchListener = screenshot -> {
            System.out.println(screenshot.toJSONObject());
        };
        ScreenshotProvider.get().registerScreenshotRefreshedListener(dispatchListener);
        ScreenshotProvider.get().refreshScreenshot();

        ScreenshotProvider.get().unregisterScreenshotRefreshedListener(dispatchListener);
    }

    @Test
    public void circlerScreenShotTest() {
        ScreenshotProvider.get().sendScreenshotRefreshed("this test base64", 100);
        ScreenshotProvider.OnScreenshotRefreshedListener baseListener = new ScreenshotProvider.OnScreenshotRefreshedListener() {
            @Override
            public void onScreenshotRefreshed(CircleScreenshot screenshot) {
                Truth.assertThat(screenshot.toJSONObject().toString()).isEqualTo(
                        "{\"screenWidth\":320,\"screenHeight\":470,\"scale\":100,\"screenshot\":\"this is test base64\",\"msgType\":\"refreshScreenshot\",\"snapshotKey\":0,\"elements\":[{\"xpath\":\"\\/CustomWindow\\/DecorView\\/ActionBarOverlayLayout[0]\\/FrameLayout[0]\\/LinearLayout[0]\\/TextView[0]\",\"left\":0,\"top\":0,\"width\":0,\"height\":0,\"nodeType\":\"TEXT\",\"content\":\"this is cpacm\",\"page\":\"\\/RobolectricActivity\",\"zLevel\":0}],\"pages\":[{\"path\":\"\\/RobolectricActivity\",\"left\":0,\"top\":0,\"width\":0,\"height\":0,\"isIgnored\":false}]}"
                );
            }
        };
        ScreenshotProvider.get().registerScreenshotRefreshedListener(baseListener);

        new CircleScreenshot.Builder()
                .setScale(100)
                .setScreenshot("this is test base64")
                .setSnapshotKey(0)
                .build(getAllWindowDecorViews(), new Callback<CircleScreenshot>() {
                    @Override
                    public void onSuccess(CircleScreenshot result) {
                        ScreenshotProvider.get().sendScreenshot(result);
                    }

                    @Override
                    public void onFailed() {

                    }
                });
        ScreenshotProvider.get().unregisterScreenshotRefreshedListener(baseListener);
    }

    public List<DecorView> getAllWindowDecorViews() {
        Activity activity = Robolectric.buildActivity(RobolectricActivity.class).create().resume().get();
        ActivityStateProvider.get().onActivityCreated(activity, null);
        ActivityStateProvider.get().onActivityResumed(activity);
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
