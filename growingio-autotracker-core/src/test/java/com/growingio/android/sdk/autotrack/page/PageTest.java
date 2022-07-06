/*
 * Copyright (C) 2020 Beijing Yishu Technology Co., Ltd.
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
import androidx.fragment.app.Fragment;
import androidx.fragment.app.testing.FragmentScenario;
import androidx.test.core.app.ApplicationProvider;

import com.google.common.truth.Truth;
import com.growingio.android.sdk.TrackerContext;
import com.growingio.android.sdk.autotrack.IgnorePolicy;
import com.growingio.android.sdk.autotrack.RobolectricActivity;
import com.growingio.android.sdk.autotrack.inject.FragmentInjector;
import com.growingio.android.sdk.track.providers.ActivityStateProvider;
import com.growingio.android.sdk.track.providers.SessionProvider;

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
        application.registerActivityLifecycleCallbacks(ActivityStateProvider.get());
        TrackerContext.init(application);
        TrackerContext.initSuccess();
        PageProvider.get().setup();
        SessionProvider.get();
        activityController = Robolectric.buildActivity(RobolectricActivity.class);
    }

    @Test
    public void pageSample() {
        RobolectricActivity activity = activityController.create().resume().get();
        ActivityPage activityPage = (ActivityPage) PageProvider.get().findPage(activity);
        String name = activityPage.getName();
        View view = activityPage.getView();
        String tag = activityPage.getTag();
        Truth.assertThat(name).isEqualTo("RobolectricActivity");
        Truth.assertThat(view).isEqualTo(activity.getWindow().getDecorView());
        Truth.assertThat(tag).isNull();
    }

    @Test
    public void pageIgnoreTest() {
        RobolectricActivity activity = activityController.create().get();
        Fragment testFragment = new Fragment();
        SuperFragment<Fragment> fragmentX = SuperFragment.makeX(testFragment);
        PageProvider.get().addIgnoreFragment(fragmentX, IgnorePolicy.IGNORE_SELF);
        PageProvider.get().addIgnorePageClass(fragmentX.getClass(), IgnorePolicy.IGNORE_SELF);
        PageProvider.get().addIgnoreActivity(activity, IgnorePolicy.IGNORE_ALL);
        activityController.resume();
        Truth.assertThat(PageProvider.get().findPage(activity).isIgnored()).isTrue();
    }

    @Test
    public void pageViewTest() {
        RobolectricActivity activity = activityController.get();
        PageProvider.get().setActivityAlias(activity, "test");
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
        Page page = PageProvider.get().findPage(SuperFragment.make(appFragment));
        String name = page.getName();
        Truth.assertThat(name).isEqualTo("TestFragment[app]");


        FragmentInjector.systemFragmentSetUserVisibleHint(appFragment, false);
        FragmentInjector.systemFragmentOnHiddenChanged(appFragment, false);
        FragmentInjector.systemFragmentOnDestroyView(appFragment);

        Page findPage = PageProvider.get().findPage(SuperFragment.make(appFragment));
        Truth.assertThat(findPage).isNull();

    }

    @Test
    public void fragmentXInjectTest() {
        FragmentScenario<TestXFragment> fs = FragmentScenario.launch(TestXFragment.class);
        fs.onFragment(testXFragment -> {
            FragmentInjector.androidxFragmentOnResume(testXFragment);
            String name = PageProvider.get().findPage(SuperFragment.makeX(testXFragment)).getName();
            Truth.assertThat(name).isEqualTo("TestXFragment[FragmentScenario_Fragment_Tag]");

            FragmentInjector.androidxFragmentSetUserVisibleHint(testXFragment, false);
            FragmentInjector.androidxFragmentOnHiddenChanged(testXFragment, false);
            FragmentInjector.androidxFragmentOnDestroyView(testXFragment);

            Page findPage = PageProvider.get().findPage(SuperFragment.makeX(testXFragment));
            Truth.assertThat(findPage).isNull();
        });
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
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
            return new TextView(getActivity());
        }

        @Override
        public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
        }
    }


}
