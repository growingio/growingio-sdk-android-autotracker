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
package com.growingio.android.sdk.autotrack.page;

import android.app.Application;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.testing.FragmentScenario;
import androidx.lifecycle.Lifecycle;
import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;

import com.google.common.truth.Truth;
import com.growingio.android.sdk.Configurable;
import com.growingio.android.sdk.CoreConfiguration;
import com.growingio.android.sdk.autotrack.AutotrackConfig;
import com.growingio.android.sdk.autotrack.Autotracker;
import com.growingio.android.sdk.autotrack.RobolectricActivity;
import com.growingio.android.sdk.autotrack.inject.FragmentInjector;
import com.growingio.android.sdk.track.providers.TrackerLifecycleProviderFactory;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.android.controller.FragmentController;
import org.robolectric.annotation.Config;

import java.util.HashMap;
import java.util.Map;

@Config(manifest = Config.NONE)
@RunWith(RobolectricTestRunner.class)
public class PageTest {

    Application application = ApplicationProvider.getApplicationContext();
    ActivityController<RobolectricActivity> activityController;

    @Before
    public void setup() {
        Map<Class<? extends Configurable>, Configurable> map = new HashMap<>();
        map.put(AutotrackConfig.class, new AutotrackConfig().setSupportFragmentTag(true));
        TrackerLifecycleProviderFactory.create().createConfigurationProviderWithConfig(new CoreConfiguration("PageTest", "growingio://impression"), map);

        Autotracker autotracker = new Autotracker(application);
        activityController = Robolectric.buildActivity(RobolectricActivity.class);
    }

    @Test
    public void pageSample() {
        RobolectricActivity activity = activityController.create().resume().get();
        ActivityPage activityPage = (ActivityPage) PageProvider.get().searchActivityPage(activity);
        String name = activityPage.getName();
        View view = activityPage.getView();
        String tag = activityPage.getTag();
        Truth.assertThat(name).isEqualTo("RobolectricActivity");
        Truth.assertThat(view).isEqualTo(activity.getWindow().getDecorView());
        Truth.assertThat(tag).isNull();
    }

    @Test
    public void pageViewTest() {
        RobolectricActivity activity = activityController.get();
        PageProvider.get().autotrackActivity(activity, "test", null);
        activityController.create().resume();
        Map<String, String> attrMap = new HashMap<>();
        attrMap.put("username", "cpacm");
        PageProvider.get().setPageAttributes(activity, attrMap);
        Page page = PageProvider.get().findPage(activity.getTextView());
        Truth.assertThat(page.getName()).isEqualTo("test");
        Truth.assertThat(page.getAlias()).isEqualTo("test");
        Truth.assertThat(page.getAttributes().size()).isEqualTo(1);
    }

    @Test
    public void fragmentInjectTest() {
        RobolectricActivity activity = activityController.create().resume().get();
        FragmentController<TestFragment> fc = Robolectric.buildFragment(TestFragment.class);
        TestFragment appFragment = fc.get();
        activity.attachFragment(appFragment);
        fc.create().resume();
        FragmentInjector.systemFragmentOnResume(appFragment);
        Page page = PageProvider.get().findOrCreateFragmentPage(SuperFragment.make(appFragment));
        String name = page.getName();
        Truth.assertThat(name).isEqualTo("TestFragment");
        Truth.assertThat(page.getTag()).isEqualTo("app");

        FragmentInjector.systemFragmentSetUserVisibleHint(appFragment, false);
        FragmentInjector.systemFragmentOnHiddenChanged(appFragment, false);
        FragmentInjector.systemFragmentOnDestroyView(appFragment);

        Page activityPage = PageProvider.get().findOrCreateActivityPage(activity);
        Page findPage = PageProvider.get().searchFragmentPage(SuperFragment.make(appFragment), activityPage);
        Truth.assertThat(findPage).isNull();

    }

    @Test
    public void fragmentXInjectTest() {
        FragmentScenario<TestXFragment> fs = FragmentScenario.launch(TestXFragment.class);
        fs.onFragment(testXFragment -> {
            FragmentInjector.androidxFragmentOnResume(testXFragment);
            Page findTestPage = PageProvider.get().findOrCreateFragmentPage(SuperFragment.makeX(testXFragment));
            Truth.assertThat(findTestPage.getName()).isEqualTo("TestXFragment");
            Truth.assertThat(findTestPage.getTag()).isEqualTo("FragmentScenario_Fragment_Tag");

            FragmentInjector.androidxFragmentSetUserVisibleHint(testXFragment, false);
            FragmentInjector.androidxFragmentOnHiddenChanged(testXFragment, false);
            FragmentInjector.androidxFragmentOnDestroyView(testXFragment);

            Page activityPage = PageProvider.get().findOrCreateActivityPage(testXFragment.getActivity());
            Page findPage = PageProvider.get().searchFragmentPage(SuperFragment.makeX(testXFragment), activityPage);
            Truth.assertThat(findPage).isNull();
        });
        fs.close();
    }

    @Test
    public void activityTest() {
        ActivityScenario<RobolectricActivity> as = ActivityScenario.launch(RobolectricActivity.class);
        as.moveToState(Lifecycle.State.RESUMED);
        as.onActivity(activity -> {
            Page activityPage = PageProvider.get().searchActivityPage(activity);
            String name = activityPage.getName();
            Truth.assertThat(name).isEqualTo("RobolectricActivity");
            as.moveToState(Lifecycle.State.DESTROYED);
            activityPage = PageProvider.get().searchActivityPage(activity);
            Truth.assertThat(activityPage).isNull();
        });
        as.close();
    }

    public static class TestFragment extends android.app.Fragment {
        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
        }

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
            return new TextView(getActivity());
        }

        @Override
        public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
        }
    }

    public static class TestXFragment extends androidx.fragment.app.Fragment {
        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
        }

        @Nullable
        @Override
        public View onCreateView(@Nullable LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
            return new TextView(getActivity());
        }

        @Override
        public void onViewCreated(@Nullable View view, @Nullable Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
        }
    }


}
