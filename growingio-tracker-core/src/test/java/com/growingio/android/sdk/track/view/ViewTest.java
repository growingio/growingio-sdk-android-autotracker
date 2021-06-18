/*
 *
 *  Copyright (C) 2020 Beijing Yishu Technology Co., Ltd.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package com.growingio.android.sdk.track.view;

import android.app.Activity;
import android.app.Application;
import android.view.View;

import androidx.test.core.app.ApplicationProvider;

import com.google.common.truth.Truth;
import com.growingio.android.sdk.TrackerContext;
import com.growingio.android.sdk.track.R;
import com.growingio.android.sdk.track.providers.ActivityStateProvider;
import com.growingio.android.sdk.track.providers.RobolectricActivity;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.Config;

import java.util.List;
import java.util.function.Consumer;


@Config(manifest = Config.NONE)
@RunWith(RobolectricTestRunner.class)
public class ViewTest {

    @Test
    public void windowHelperTest() {
        Activity activity = Robolectric.buildActivity(RobolectricActivity.class).create().resume().get();

        Truth.assertThat(WindowHelper.get().isDecorView(activity.getWindow().getDecorView())).isTrue();

        ActivityStateProvider.get().onActivityResumed(activity);
        List<DecorView> views = WindowHelper.get().getTopActivityViews();
        System.out.println(views.size());
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
        TrackerContext.init(application);
        Activity activity = Robolectric.buildActivity(RobolectricActivity.class).create().resume().get();
        ActivityStateProvider.get().onActivityResumed(activity);
        String base64 = ScreenshotUtil.getScreenshotBase64(1);
        Truth.assertThat(base64).startsWith("data:image/jpeg;base64,");
    }

    @Test
    public void viewTreeStatusTest() {
        application.registerActivityLifecycleCallbacks(ActivityStateProvider.get());
        TrackerContext.init(application);
        OnViewStateChangedListener testViewStateChangeListener = new OnViewStateChangedListener() {
            @Override
            public void onViewStateChanged(ViewStateChangedEvent changedEvent) {
                ViewStateChangedEvent.StateType stateType = changedEvent.getStateType();
                Truth.assertThat(stateType).isInstanceOf(ViewStateChangedEvent.StateType.class);
            }
        };
        ViewTreeStatusProvider.get().register(testViewStateChangeListener);
        ActivityController<RobolectricActivity> controller = Robolectric.buildActivity(RobolectricActivity.class);
        RobolectricActivity activity = controller.create().resume().get();
        Truth.assertThat(activity.getWindow().getDecorView().getTag(R.id.growing_tracker_monitoring_view_tree_enabled)).isNotNull();
        activity.refresh();
        controller.pause();
        ViewTreeStatusProvider.get().unregister(testViewStateChangeListener);
    }

}
