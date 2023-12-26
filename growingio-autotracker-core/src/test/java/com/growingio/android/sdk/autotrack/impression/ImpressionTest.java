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
package com.growingio.android.sdk.autotrack.impression;

import android.app.Application;

import androidx.test.core.app.ApplicationProvider;

import com.google.common.truth.Truth;
import com.google.common.util.concurrent.Uninterruptibles;
import com.growingio.android.sdk.Configurable;
import com.growingio.android.sdk.CoreConfiguration;
import com.growingio.android.sdk.TrackerContext;
import com.growingio.android.sdk.autotrack.AutotrackConfig;
import com.growingio.android.sdk.autotrack.Autotracker;
import com.growingio.android.sdk.autotrack.RobolectricActivity;
import com.growingio.android.sdk.track.providers.TrackerLifecycleProviderFactory;
import com.growingio.android.sdk.track.view.ViewStateChangedEvent;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RunWith(RobolectricTestRunner.class)
public class ImpressionTest {

    Application application = ApplicationProvider.getApplicationContext();
    TrackerContext context;

    @Before
    public void setup() {

        Map<Class<? extends Configurable>, Configurable> map = new HashMap<>();
        map.put(AutotrackConfig.class, new AutotrackConfig());
        TrackerLifecycleProviderFactory.create().createConfigurationProviderWithConfig(new CoreConfiguration("ImpressionTest", "growingio://impression"), map);

        Autotracker tracker = new Autotracker(application);
        context = tracker.getContext();

    }

    @Test
    public void impressionTest() {
        RobolectricActivity activity = Robolectric.buildActivity(RobolectricActivity.class).create().resume().get();
        Map<String, String> attrMap = new HashMap<>();
        attrMap.put("username", "cpacm");
        ImpressionProvider impressionProvider = context.getProvider(ImpressionProvider.class);
        impressionProvider.trackViewImpression(activity.getTextView(), "cpacm", attrMap);
        boolean result = impressionProvider.hasTrackViewImpression(activity.getTextView());
        Truth.assertThat(result).isTrue();

        ViewStateChangedEvent vscEvent = new ViewStateChangedEvent(ViewStateChangedEvent.StateType.LAYOUT_CHANGED);
        Truth.assertThat(vscEvent.getNewFocus()).isNull();
        impressionProvider.onViewStateChanged(vscEvent);
        Robolectric.flushForegroundThreadScheduler();
        Uninterruptibles.sleepUninterruptibly(1, TimeUnit.SECONDS);

        impressionProvider.stopTrackViewImpression(activity.getTextView());
        boolean result2 = impressionProvider.hasTrackViewImpression(activity.getTextView());
        Truth.assertThat(result2).isFalse();
    }
}
