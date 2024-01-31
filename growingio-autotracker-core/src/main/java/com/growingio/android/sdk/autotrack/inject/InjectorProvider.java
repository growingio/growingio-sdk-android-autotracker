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
package com.growingio.android.sdk.autotrack.inject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.SeekBar;

import com.google.android.material.slider.RangeSlider;
import com.google.android.material.slider.Slider;
import com.google.android.material.tabs.TabLayout;
import com.growingio.android.sdk.TrackerContext;
import com.growingio.android.sdk.autotrack.AutotrackConfig;
import com.growingio.android.sdk.autotrack.view.ViewAttributeUtil;
import com.growingio.android.sdk.track.log.Logger;
import com.growingio.android.sdk.track.middleware.hybrid.HybridBridge;
import com.growingio.android.sdk.track.modelloader.ModelLoader;
import com.growingio.android.sdk.track.modelloader.TrackerRegistry;
import com.growingio.android.sdk.track.providers.ActivityStateProvider;
import com.growingio.android.sdk.track.providers.ConfigurationProvider;
import com.growingio.android.sdk.track.providers.TrackerLifecycleProvider;

import java.util.Map;

/**
 * <p>
 *
 * @author cpacm 2023/7/30
 */
public class InjectorProvider implements TrackerLifecycleProvider {

    private final static String TAG = "InjectorProvider";

    private static class SingleInstance {
        private static final InjectorProvider INSTANCE = new InjectorProvider();
    }

    public static InjectorProvider get() {
        return InjectorProvider.SingleInstance.INSTANCE;
    }

    public InjectorProvider() {

    }

    private ActivityStateProvider activityStateProvider;
    private ViewClickProvider viewClickProvider;
    private ViewChangeProvider viewChangeProvider;
    private DialogClickProvider dialogClickProvider;
    private ConfigurationProvider configurationProvider;

    private TrackerRegistry registry;

    @Override
    public void setup(TrackerContext context) {
        activityStateProvider = context.getActivityStateProvider();
        viewClickProvider = context.getProvider(ViewClickProvider.class);
        viewChangeProvider = context.getProvider(ViewChangeProvider.class);
        dialogClickProvider = context.getProvider(DialogClickProvider.class);
        configurationProvider = context.getConfigurationProvider();

        registry = context.getRegistry();
    }

    public void onActivityNewIntent(Activity activity, Intent intent) {
        if (activityStateProvider != null) {
            activityStateProvider.onActivityNewIntent(activity, intent);
        }
    }

    public void activityOptionsItemOnClick(Activity activity, MenuItem menuItem) {
        if (viewClickProvider != null) {
            viewClickProvider.activityOptionsItemOnClick(activity, menuItem);
        }
    }

    public void bridgeForWebView(View view) {
        AutotrackConfig config = configurationProvider.getConfiguration(AutotrackConfig.class);
        boolean webViewBridgeEnabled = config == null || config.isWebViewBridgeEnabled();
        boolean ignoredView = ViewAttributeUtil.isIgnoredView(view);
        if (!webViewBridgeEnabled || ignoredView) {
            Logger.w(TAG, "Autotracker webViewBridgeEnabled: " + webViewBridgeEnabled + ", isIgnoredView: " + ignoredView);
            return;
        }

        boolean result = false;
        if (registry != null) {
            ModelLoader<HybridBridge, Boolean> modelLoader = registry.getModelLoader(HybridBridge.class, Boolean.class);
            if (modelLoader != null) {
                result = modelLoader.buildLoadData(new HybridBridge(view)).fetcher.executeData();
            }
        }
        Logger.d(TAG, "bridgeForWebView: webView = " + view.getClass().getName() + ", result = " + result);
    }

    public void alertDialogShow(AlertDialog dialog) {
        if (dialogClickProvider != null) {
            dialogClickProvider.alertDialogShow(dialog);
        }
    }

    public void alertDialogXOnClick(androidx.appcompat.app.AlertDialog dialog, int which) {
        if (dialogClickProvider != null) {
            dialogClickProvider.alertDialogXOnClick(dialog, which);
        }
    }

    public void alertDialogSupportOnClick(android.support.v7.app.AlertDialog dialog, int which) {
        if (dialogClickProvider != null) {
            dialogClickProvider.alertDialogSupportOnClick(dialog, which);
        }
    }

    public void alertDialogOnClick(AlertDialog dialog, int which) {
        if (dialogClickProvider != null) {
            dialogClickProvider.alertDialogOnClick(dialog, which);
        }
    }

    public void toolbarMenuItemOnClick(MenuItem menuItem) {
        if (viewClickProvider != null) {
            viewClickProvider.toolbarMenuItemOnClick(menuItem);
        }
    }

    public void actionMenuItemOnClick(MenuItem menuItem) {
        if (viewClickProvider != null) {
            viewClickProvider.actionMenuItemOnClick(menuItem);
        }
    }

    public void popupMenuItemOnClick(MenuItem menuItem) {
        if (viewClickProvider != null) {
            viewClickProvider.popupMenuItemOnClick(menuItem);
        }
    }

    public void contextMenuItemOnClick(MenuItem menuItem) {
        if (viewClickProvider != null) {
            viewClickProvider.contextMenuItemOnClick(menuItem);
        }
    }

    public void viewOnClick(View view) {
        if (viewClickProvider != null) {
            viewClickProvider.viewOnClick(view);
        }
    }

    public void viewOnClickListener(View view) {
        if (viewClickProvider != null) {
            viewClickProvider.viewOnClickListener(view);
        }
    }

    public void adapterViewItemClick(View view) {
        if (viewClickProvider != null) {
            viewClickProvider.adapterViewItemClick(view);
        }
    }

    public void spinnerViewOnClick(View view) {
        if (viewClickProvider != null) {
            viewClickProvider.spinnerViewOnClick(view);
        }
    }

    public void expandableListViewOnGroupClick(View view) {
        if (viewClickProvider != null) {
            viewClickProvider.expandableListViewOnGroupClick(view);
        }
    }

    public void expandableListViewOnChildClick(View view) {
        if (viewClickProvider != null) {
            viewClickProvider.expandableListViewOnChildClick(view);
        }
    }

    public void radioGroupOnCheck(View view) {
        if (viewClickProvider != null) {
            viewClickProvider.radioGroupOnCheck(view);
        }
    }

    public void materialButtonToggleGroupOnButtonCheck(View view) {
        if (viewClickProvider != null) {
            viewClickProvider.materialButtonToggleGroupOnButtonCheck(view);
        }
    }

    public void tabLayoutOnTabSelected(TabLayout.Tab tab) {
        if (viewClickProvider != null) {
            viewClickProvider.tabLayoutOnTabSelected(tab);
        }
    }

    public void compoundButtonOnCheck(CompoundButton button) {
        if (viewClickProvider != null) {
            viewClickProvider.compoundButtonOnCheck(button);
        }
    }

    public void editTextOnFocusChange(View view) {
        if (viewChangeProvider != null) {
            viewChangeProvider.editTextOnFocusChange(view);
        }
    }

    public void seekBarOnProgressChange(SeekBar seekBar) {
        if (viewChangeProvider != null) {
            viewChangeProvider.seekBarOnProgressChange(seekBar);
        }
    }

    public void ratingBarOnRatingChange(View view, float rating) {
        if (viewChangeProvider != null) {
            viewChangeProvider.ratingBarOnRatingChange(view, rating);
        }
    }

    public void sliderOnStopTrackingTouch(Slider slider) {
        if (viewChangeProvider != null) {
            viewChangeProvider.sliderOnStopTrackingTouch(slider);
        }
    }

    public void rangeSliderOnStopTrackingTouch(RangeSlider slider) {
        if (viewChangeProvider != null) {
            viewChangeProvider.rangeSliderOnStopTrackingTouch(slider);
        }
    }

    @Override
    public void shutdown() {

    }

    public static void injectProviders(Map<Class<? extends TrackerLifecycleProvider>, TrackerLifecycleProvider> provideStore) {
        provideStore.put(DialogClickProvider.class, new DialogClickProvider());
        provideStore.put(ViewClickProvider.class, new ViewClickProvider());
        provideStore.put(ViewChangeProvider.class, new ViewChangeProvider());
        provideStore.put(InjectorProvider.class, InjectorProvider.get());
    }
}
