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

package com.growingio.android.sdk.autotrack;


import android.app.Application;
import android.app.Fragment;

import androidx.test.core.app.ApplicationProvider;

import com.google.common.truth.Truth;
import com.growingio.android.sdk.Configurable;
import com.growingio.android.sdk.CoreConfiguration;
import com.growingio.android.sdk.autotrack.impression.ImpressionProvider;
import com.growingio.android.sdk.track.providers.ConfigurationProvider;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.HashMap;
import java.util.Map;

@Config(manifest = Config.NONE, shadows = {ShadowThreadUtils.class})
@RunWith(RobolectricTestRunner.class)
public class AutotrackTest {
    private Autotracker autotracker;

    @Before
    public void setup() {
        Application application = ApplicationProvider.getApplicationContext();
        autotracker = new Autotracker(application);
        Map<Class<? extends Configurable>, Configurable> modules = new HashMap<>();
        modules.put(AutotrackConfig.class, new AutotrackConfig().setImpressionScale(0.5f));
        ConfigurationProvider.initWithConfig(new CoreConfiguration("AutotrackTest", "growingio://autotrack"), modules);
    }

    @Test
    public void apiTest() {
        RobolectricActivity activity = Robolectric.buildActivity(RobolectricActivity.class).create().resume().get();
        autotracker.setUniqueTag(activity.getTextView(), "");
        Truth.assertThat(activity.getTextView().getTag(R.id.growing_tracker_view_custom_id)).isNull();
        autotracker.setUniqueTag(activity.getTextView(), "test");
        Truth.assertThat(activity.getTextView().getTag(R.id.growing_tracker_view_custom_id)).isEqualTo("test");

        Map<String, String> testMap = new HashMap<>();
        testMap.put("data", "test");
        testMap.put("name", "cpacm");
        autotracker.setPageAttributes(activity, testMap);
        autotracker.setPageAttributes(new Fragment(), testMap);
        autotracker.setPageAttributesX(new androidx.fragment.app.Fragment(), testMap);

        autotracker.trackViewImpression(activity.getImageView(), "testImp");
        Truth.assertThat(ImpressionProvider.get().hasTrackViewImpression(activity.getImageView())).isTrue();
        autotracker.stopTrackViewImpression(activity.getImageView());
        Truth.assertThat(ImpressionProvider.get().hasTrackViewImpression(activity.getImageView())).isFalse();

        autotracker.setPageAlias(activity, "TestActivity");
        autotracker.setPageAlias(new Fragment(), "TestFragment");
        autotracker.setPageAliasX(new androidx.fragment.app.Fragment(), "TestFragmentX");
        autotracker.ignorePage(activity, IgnorePolicy.IGNORE_SELF);
        autotracker.ignorePage(new Fragment(), IgnorePolicy.IGNORE_SELF);
        autotracker.ignorePageX(new androidx.fragment.app.Fragment(), IgnorePolicy.IGNORE_SELF);
        autotracker.ignoreView(activity.getTextView(), IgnorePolicy.IGNORE_SELF);

    }
}
