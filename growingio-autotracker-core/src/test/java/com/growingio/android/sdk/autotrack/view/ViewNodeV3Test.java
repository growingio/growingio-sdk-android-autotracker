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
package com.growingio.android.sdk.autotrack.view;


import android.app.Application;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RatingBar;
import android.widget.SeekBar;

import androidx.recyclerview.widget.RecyclerView;
import androidx.test.core.app.ApplicationProvider;

import com.google.common.truth.Truth;
import com.growingio.android.sdk.Configurable;
import com.growingio.android.sdk.CoreConfiguration;
import com.growingio.android.sdk.TrackerContext;
import com.growingio.android.sdk.autotrack.AutotrackConfig;
import com.growingio.android.sdk.autotrack.Autotracker;
import com.growingio.android.sdk.autotrack.IgnorePolicy;
import com.growingio.android.sdk.autotrack.RobolectricActivity;
import com.growingio.android.sdk.autotrack.page.ActivityPage;
import com.growingio.android.sdk.track.providers.TrackerLifecycleProviderFactory;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.fakes.RoboMenuItem;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Config(manifest = Config.NONE)
@RunWith(RobolectricTestRunner.class)
public class ViewNodeV3Test {

    Application application = ApplicationProvider.getApplicationContext();
    private TrackerContext context;

    @Before
    public void setup() {
        CoreConfiguration coreConfiguration = new CoreConfiguration("ViewNodeV3Test", "growingio://nodeV3");
        AutotrackConfig autotrackConfig = new AutotrackConfig();
        autotrackConfig.getAutotrackOptions().setActivityPageEnabled(true);
        autotrackConfig.getAutotrackOptions().setFragmentPageEnabled(true);
        Map<Class<? extends Configurable>, Configurable> map = new HashMap<>();
        map.put(AutotrackConfig.class, autotrackConfig);
        TrackerLifecycleProviderFactory.create()
                .createConfigurationProviderWithConfig(
                        coreConfiguration,
                        map);
        Autotracker autotracker = new Autotracker(application);
        context = autotracker.getContext();
    }

    @Test
    public void utilTest() {
        RobolectricActivity activity = Robolectric.buildActivity(RobolectricActivity.class).create().start().resume().get();
        Truth.assertThat(ViewUtil.canCircle(activity.getTextView())).isFalse();
        activity.getTextView().setOnClickListener(v -> {
        });
        Truth.assertThat(ViewUtil.canCircle(activity.getTextView())).isTrue();
        Truth.assertThat(ViewUtil.canCircle(activity.getRecyclerView())).isFalse();

        RecyclerView recyclerView = activity.getRecyclerView();
        View itemView = Objects.requireNonNull(recyclerView.findViewHolderForAdapterPosition(0)).itemView;
        Truth.assertThat(ViewUtil.canCircle(itemView)).isTrue();
    }

    @Test
    public void viewNodeTest() {
        RobolectricActivity activity = Robolectric.buildActivity(RobolectricActivity.class).create().start().resume().get();

        ViewNodeProvider viewNodeProvider = context.getProvider(ViewNodeProvider.class);
        ViewNodeV3Renderer renderer = new ViewNodeV3Renderer(viewNodeProvider);

        ViewNodeV3 viewNode = renderer.renderViewNode(activity.getTextView());
        Truth.assertThat(viewNode.getXPath()).endsWith("/DecorView/LinearLayout[0]/FrameLayout[0]/LinearLayout[0]/TextView[0]");
        Truth.assertThat(viewNode.getViewContent()).isEqualTo("this is cpacm");
        Truth.assertThat(viewNode.getNodeType()).isEqualTo("TEXT");
        Truth.assertThat(viewNode.getClickableParentXPath()).isNull();
        Truth.assertThat(viewNode.getIndex()).isEqualTo(-1);
        ViewNodeV3 newNode = viewNode.append(activity.getImageView(), 0, true);
        Truth.assertThat(newNode.getXPath()).endsWith("/DecorView/LinearLayout[0]/FrameLayout[0]/LinearLayout[0]/TextView[0]/ImageView[0]");

        RecyclerView recyclerView = activity.getRecyclerView();
        View itemView = Objects.requireNonNull(recyclerView.findViewHolderForAdapterPosition(2)).itemView;
        ViewNodeV3 listItemNode = renderer.renderViewNode(itemView);
        Truth.assertThat(listItemNode.getXPath()).endsWith("/DecorView/LinearLayout[0]/FrameLayout[0]/LinearLayout[0]/RecyclerView[0]/TextView[-]");
    }

    @Test
    public void viewRenderTest() {
        RobolectricActivity activity = Robolectric.buildActivity(RobolectricActivity.class).create().start().resume().get();
        ViewNodeProvider viewNodeProvider = context.getProvider(ViewNodeProvider.class);
        ViewNodeV3Renderer renderer = new ViewNodeV3Renderer(viewNodeProvider);

        RecyclerView recyclerView = activity.getRecyclerView();
        recyclerView.getWindowVisibility();
        Truth.assertThat(ViewAttributeUtil.isViewSelfVisible(activity.getWindow().getDecorView())).isFalse();
        Truth.assertThat(ViewAttributeUtil.viewVisibilityInParents(recyclerView)).isFalse();

        View itemView = Objects.requireNonNull(recyclerView.findViewHolderForAdapterPosition(0)).itemView;
        String content = ViewAttributeUtil.getViewContent(itemView);
        Truth.assertThat(content).isEqualTo("position:0");

        EditText testEt = new EditText(activity);
        testEt.setText("test edittext");
        MenuItem testItem = new RoboMenuItem().setTitle("test menu item")
                .setActionView(testEt);
        activity.onContextItemSelected(testItem);
        ViewNodeV3 pageNode = ViewNodeV3.generateMenuItemViewNode(activity, testItem);
        Truth.assertThat(pageNode.getXPath()).isEqualTo("/Page/MenuView/MenuItem#null");

        ViewNodeV3 menuNode = renderer.renderViewNode(testItem.getActionView());
        Truth.assertThat(menuNode.getXPath()).isEqualTo("/CustomWindow/EditText");

        RatingBar ratingBar = new RatingBar(activity);
        ratingBar.setRating(10f);
        ratingBar.setProgress(1);
        Truth.assertThat(ViewAttributeUtil.getViewContent(ratingBar)).isEqualTo("0.5");

        SeekBar seekBar = new SeekBar(activity);
        seekBar.setProgress(100);
        Truth.assertThat(ViewAttributeUtil.getViewContent(seekBar)).isEqualTo("100");

        RadioGroup radioGroup = new RadioGroup(activity);
        RadioButton radio1 = new RadioButton(activity);
        radio1.setText("radio1");
        radio1.setId(1);
        RadioButton radio2 = new RadioButton(activity);
        radio2.setText("radio2");
        radio2.setId(2);
        radio2.setChecked(true);
        radio2.setSelected(true);
        radioGroup.addView(radio1);
        radioGroup.addView(radio2);
        radioGroup.setSelected(true);
        Truth.assertThat(ViewAttributeUtil.getViewContent(radioGroup)).isEqualTo("radio2");
    }

    @Test
    public void viewAttrTest() {
        RobolectricActivity activity = Robolectric.buildActivity(RobolectricActivity.class).create().start().resume().get();

        ViewAttributeUtil.setCustomId(activity.getTextView(), "test id");
        Truth.assertThat(ViewAttributeUtil.getCustomId(activity.getTextView())).isEqualTo("test id");

        ViewAttributeUtil.setTrackText(activity.getImageView(), true);
        Truth.assertThat(ViewAttributeUtil.getTrackText(activity.getImageView())).isTrue();

        ViewAttributeUtil.setContent(activity.getTextView(), "test content");
        Truth.assertThat(ViewAttributeUtil.getContent(activity.getTextView())).isEqualTo("test content");

        ViewAttributeUtil.setIgnorePolicy(activity.getRecyclerView(), IgnorePolicy.IGNORE_ALL);
        Truth.assertThat(ViewAttributeUtil.getIgnorePolicy(activity.getRecyclerView())).isEqualTo(IgnorePolicy.IGNORE_ALL);

        ViewAttributeUtil.setMonitoringFocusContent(activity.getImageView(), "test focus");
        Truth.assertThat(ViewAttributeUtil.getMonitoringFocusContent(activity.getImageView())).isEqualTo("test focus");

        ViewAttributeUtil.setViewPage(activity.getTextView(), new ActivityPage(activity));
        Truth.assertThat(ViewAttributeUtil.getViewPage(activity.getTextView()).path()).isEqualTo("/RobolectricActivity");

        Truth.assertThat(PageHelper.getWindowPrefix(activity.getTextView())).isEqualTo("Page");
        Truth.assertThat(PageHelper.getMainWindowPrefix()).isEqualTo("MainWindow");
    }

}
