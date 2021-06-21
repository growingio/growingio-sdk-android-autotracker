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

package com.growingio.android.sdk;


import android.app.Application;
import android.content.Context;

import androidx.test.core.app.ApplicationProvider;

import com.google.common.truth.Truth;
import com.google.common.util.concurrent.Uninterruptibles;
import com.growingio.android.sdk.track.async.Disposable;
import com.growingio.android.sdk.track.async.HandlerDisposable;
import com.growingio.android.sdk.track.async.UnsubscribedDisposable;
import com.growingio.android.sdk.track.middleware.EventsContentProvider;
import com.growingio.android.sdk.track.modelloader.TrackerRegistry;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.google.common.truth.Truth.assertThat;

@Config(manifest = Config.NONE)
@RunWith(RobolectricTestRunner.class)
public class TrackerTest {

    Application application = ApplicationProvider.getApplicationContext();

    @Test
    public void initTest() {
        Tracker nullTracker = new Tracker(null, null);
        assertThat(nullTracker.isInited).isFalse();

        try {
            Tracker errorTracker = new Tracker(application, new TrackConfiguration());
        } catch (Exception e) {
            assertThat(e).isInstanceOf(IllegalStateException.class);
        }
    }

    @Test
    public void apiTest() {
        Robolectric.buildContentProvider(EventsContentProvider.class).create();
        TrackConfiguration trackConfiguration = new TrackConfiguration("test", "test");
        trackConfiguration.setDebugEnabled(true);
        Tracker tracker = new Tracker(application, trackConfiguration);
        Map<String, String> valueMap = new HashMap<>();
        valueMap.put("user", "cpacm");
        tracker.trackCustomEvent("test");
        tracker.setConversionVariables(null);
        tracker.setConversionVariables(valueMap);
        tracker.setLoginUserId("cpacm");
        tracker.setLoginUserAttributes(null);
        tracker.setLoginUserAttributes(valueMap);
        tracker.setVisitorAttributes(null);
        tracker.setVisitorAttributes(valueMap);
        assertThat(tracker.getDeviceId()).isNotEmpty();

        tracker.setDataCollectionEnabled(true);
        tracker.setDataCollectionEnabled(false);
        tracker.cleanLocation();
        tracker.setLocation(0d, 1d);
        tracker.cleanLocation();
        tracker.onActivityNewIntent(null, null);

        tracker.registerComponent(new LibraryGioModule() {
            @Override
            public void registerComponents(Context context, TrackerRegistry registry) {
                //nothing
            }
        });
    }
}
