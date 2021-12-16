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

package com.growingio.android.debugger;

import android.app.Activity;
import android.app.Application;

import androidx.test.core.app.ApplicationProvider;

import com.google.common.truth.Truth;
import com.growingio.android.debugger.shadow.ShadowHandler;
import com.growingio.android.debugger.shadow.ShadowWH;
import com.growingio.android.hybrid.HybridLibraryGioModule;
import com.growingio.android.sdk.TrackerContext;
import com.growingio.android.sdk.track.providers.ActivityStateProvider;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

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
            Truth.assertThat(screenshot).isNotNull();
        };
        ScreenshotProvider.get().registerScreenshotRefreshedListener(dispatchListener);
        ScreenshotProvider.get().refreshScreenshot();

        ScreenshotProvider.get().unregisterScreenshotRefreshedListener(dispatchListener);
    }

    @Test
    public void debuggerScreenShotTest() {
        ScreenshotProvider.get().registerScreenshotRefreshedListener(new ScreenshotProvider.OnScreenshotRefreshedListener() {
            @Override
            public void onScreenshotRefreshed(DebuggerScreenshot screenshot) {
                Truth.assertThat(screenshot.toJSONObject().toString()).isEqualTo(
                        "{\"screenWidth\":320,\"screenHeight\":470,\"scale\":100,\"screenshot\":\"this test base64\",\"msgType\":\"refreshScreenshot\",\"snapshotKey\":0}");
            }
        });
        ScreenshotProvider.get().sendScreenshotRefreshed("this test base64", 100);
    }

}
