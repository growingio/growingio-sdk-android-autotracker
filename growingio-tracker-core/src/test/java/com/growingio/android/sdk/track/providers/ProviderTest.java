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

package com.growingio.android.sdk.track.providers;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.os.Bundle;

import androidx.test.core.app.ApplicationProvider;

import com.google.common.truth.Truth;
import com.growingio.android.sdk.TrackerContext;
import com.growingio.android.sdk.track.listener.IActivityLifecycle;
import com.growingio.android.sdk.track.listener.event.ActivityLifecycleEvent;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.Config;

@Config(manifest = Config.NONE)
@RunWith(RobolectricTestRunner.class)
public class ProviderTest {

    private final Application application = ApplicationProvider.getApplicationContext();

    @Before
    public void setup() {
        TrackerContext.init(application);
    }

    @Test
    public void activityStateProvider() {
        ActivityController<RobolectricActivity> activityController = Robolectric.buildActivity(RobolectricActivity.class);
        RobolectricActivity activity = activityController.get();
        application.registerActivityLifecycleCallbacks(ActivityStateProvider.get());

        Bundle testBundle = new Bundle();

        IActivityLifecycle iActivityLifecycle = new IActivityLifecycle() {
            @Override
            public void onActivityLifecycle(ActivityLifecycleEvent event) {
                Truth.assertThat(event.getActivity()).isEqualTo(activity);
                Truth.assertThat(event.eventType).isEqualTo(activity.state);
                if (event.getBundle() != null) {
                    Truth.assertThat(event.getBundle()).isEqualTo(testBundle);
                }
                if (event.getIntent() != null) {
                    Truth.assertThat(event.getIntent()).isEqualTo(activity.getIntent());
                }
            }
        };
        ActivityStateProvider.get().register(iActivityLifecycle);

        activityController.create(testBundle);
        activityController.resume();
        activityController.pause();
        activityController.stop();
        activityController.destroy();
        ActivityStateProvider.get().unregisterActivityLifecycleListener(iActivityLifecycle);
    }

    @Test
    public void appInfoProvider(){

    }

}
