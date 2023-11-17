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
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;

import com.google.android.material.tabs.TabLayout;
import com.growingio.android.sdk.TrackerContext;
import com.growingio.android.sdk.autotrack.AutotrackConfig;
import com.growingio.android.sdk.autotrack.util.ClassUtil;
import com.growingio.android.sdk.autotrack.view.ViewAttributeUtil;
import com.growingio.android.sdk.autotrack.view.ViewNodeProvider;
import com.growingio.android.sdk.track.TrackMainThread;
import com.growingio.android.sdk.track.log.Logger;
import com.growingio.android.sdk.track.providers.TrackerLifecycleProvider;

class ViewClickProvider implements TrackerLifecycleProvider {
    private static final String TAG = "ViewClickProvider";

    ViewClickProvider() {
    }

    private AutotrackConfig autotrackConfig;
    private ViewNodeProvider viewNodeProvider;

    @Override
    public void setup(TrackerContext context) {
        autotrackConfig = context.getConfigurationProvider().getConfiguration(AutotrackConfig.class);
        viewNodeProvider = context.getProvider(ViewNodeProvider.class);
    }

    @Override
    public void shutdown() {

    }

    public void adapterViewItemClick(View view) {
        if (autotrackConfig != null && !autotrackConfig.getAutotrackOptions().isAdapterViewItemClickEnabled()) {
            Logger.i(TAG, "AutotrackOptions: adapter view item click enable is false");
            return;
        }
        viewOnClick(view);
    }

    public void spinnerViewOnClick(View view) {
        if (autotrackConfig != null && !autotrackConfig.getAutotrackOptions().isSpinnerItemClickSelectEnabled()) {
            Logger.i(TAG, "AutotrackOptions: spinner item click select is false");
            return;
        }
        viewOnClick(view);
    }

    public void expandableListViewOnGroupClick(View view) {
        if (autotrackConfig != null && !autotrackConfig.getAutotrackOptions().isExpandableListGroupClickEnabled()) {
            Logger.i(TAG, "AutotrackOptions: expandable list group click enable is false");
            return;
        }
        viewOnClick(view);
    }

    public void expandableListViewOnChildClick(View view) {
        if (autotrackConfig != null && !autotrackConfig.getAutotrackOptions().isExpandableListChildClickEnabled()) {
            Logger.i(TAG, "AutotrackOptions: expandable list child click enable is false");
            return;
        }
        viewOnClick(view);
    }

    public void radioGroupOnCheck(View view) {
        if (autotrackConfig != null && !autotrackConfig.getAutotrackOptions().isRadioGroupCheckEnabled()) {
            Logger.i(TAG, "AutotrackOptions: radio group check enable is false");
            return;
        }
        viewOnClick(view);
    }

    public void materialButtonToggleGroupOnButtonCheck(View view) {
        if (autotrackConfig != null && !autotrackConfig.getAutotrackOptions().isMaterialToggleGroupButtonCheckEnabled()) {
            Logger.i(TAG, "AutotrackOptions: material toggle group check enable is false");
            return;
        }
        viewOnClick(view);
    }

    public void tabLayoutOnTabSelected(TabLayout.Tab tab) {
        if (autotrackConfig != null && !autotrackConfig.getAutotrackOptions().isTabLayoutTabSelectedEnabled()) {
            Logger.i(TAG, "AutotrackOptions: tablayout tab select enable is false");
            return;
        }
        if (tab != null) {
            viewOnClick(tab.view);
        }
    }

    public void compoundButtonOnCheck(CompoundButton button) {
        if (autotrackConfig != null && !autotrackConfig.getAutotrackOptions().isCompoundButtonCheckEnabled()) {
            Logger.i(TAG, "AutotrackOptions: compound button check enable is false");
            return;
        }
        viewOnClick(button);
    }

    public void viewOnClickListener(View view) {
        if (autotrackConfig != null && !autotrackConfig.getAutotrackOptions().isViewClickEnabled()) {
            Logger.i(TAG, "AutotrackOptions: view click enable is false");
            return;
        }
        viewOnClick(view);
    }


    public void activityOptionsItemOnClick(Activity activity, MenuItem menuItem) {
        if (autotrackConfig != null && !autotrackConfig.getAutotrackOptions().isActivityMenuItemClickEnabled()) {
            Logger.i(TAG, "AutotrackOptions: activityMenuItemClickEnabled is false");
            return;
        }
        menuItemOnClick(activity, menuItem);
    }


    public void toolbarMenuItemOnClick(MenuItem menuItem) {
        if (autotrackConfig != null && !autotrackConfig.getAutotrackOptions().isToolbarMenuItemClickEnabled()) {
            Logger.i(TAG, "AutotrackOptions: toolbarMenuItemClickEnabled is false");
            return;
        }
        menuItemOnClick(menuItem);
    }

    public void actionMenuItemOnClick(MenuItem menuItem) {
        if (autotrackConfig != null && !autotrackConfig.getAutotrackOptions().isActionMenuItemClickEnabled()) {
            Logger.i(TAG, "AutotrackOptions: actionMenuItemClickEnabled is false");
            return;
        }
        menuItemOnClick(menuItem);
    }

    public void popupMenuItemOnClick(MenuItem menuItem) {
        if (autotrackConfig != null && !autotrackConfig.getAutotrackOptions().isPopupMenuItemClickEnabled()) {
            Logger.i(TAG, "AutotrackOptions: popupMenuItemClickEnabled is false");
            return;
        }
        menuItemOnClick(menuItem);
    }

    public void contextMenuItemOnClick(MenuItem menuItem) {
        if (autotrackConfig != null && !autotrackConfig.getAutotrackOptions().isContextMenuItemClickEnabled()) {
            Logger.i(TAG, "AutotrackOptions: contextMenuItemClickEnabled is false");
            return;
        }
        menuItemOnClick(menuItem);
    }

    private void menuItemOnClick(MenuItem menuItem) {
        if (!TrackerContext.initializedSuccessfully()) {
            Logger.e(TAG, "Autotracker do not initialized successfully");
            return;
        }

        Activity activity = TrackMainThread.trackMain().getForegroundActivity();
        menuItemOnClick(activity, menuItem);
    }

    public void viewOnClick(View view) {
        if (!TrackerContext.initializedSuccessfully()) {
            Logger.e(TAG, "Autotracker do not initialized successfully");
            return;
        }

        if (view == null) {
            Logger.e(TAG, "viewOnClick: view is NULL");
            return;
        }

        if (ViewAttributeUtil.isIgnoreViewClick(view)) {
            return;
        }

        // 为了防止click事件重复发送
        if (ClassUtil.isDuplicateClick(view)) {
            Logger.w(TAG, "Duplicate Click");
            return;
        }
        if (viewNodeProvider != null) {
            viewNodeProvider.generateViewClickEvent(view);
        }
    }

    public void menuItemOnClick(Activity activity, MenuItem menuItem) {
        if (!TrackerContext.initializedSuccessfully()) {
            Logger.e(TAG, "Autotracker do not initialized successfully");
            return;
        }

        if (activity == null || menuItem == null) {
            Logger.e(TAG, "menuItemOnClick: activity or menuItem is NULL");
            return;
        }

        if (viewNodeProvider != null) {
            viewNodeProvider.generateMenuItemEvent(activity, menuItem);
        }
    }

}
