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
import com.growingio.android.sdk.track.events.AutotrackEventType;
import com.growingio.android.sdk.track.events.ViewElementEvent;
import com.growingio.android.sdk.autotrack.page.Page;
import com.growingio.android.sdk.autotrack.page.PageProvider;
import com.growingio.android.sdk.autotrack.view.ViewHelper;
import com.growingio.android.sdk.autotrack.view.ViewNode;
import com.growingio.android.sdk.track.TrackMainThread;
import com.growingio.android.sdk.track.log.Logger;
import com.growingio.android.sdk.track.providers.ActivityStateProvider;
import com.growingio.android.sdk.track.providers.ConfigurationProvider;

class ViewClickProvider {
    private static final String TAG = "ViewClickProvider";

    private ViewClickProvider() {
    }

    public static void adapterViewItemClick(View view) {
        AutotrackConfig config = ConfigurationProvider.get().getConfiguration(AutotrackConfig.class);
        if (config != null && !config.getAutotrackOptions().isAdapterViewItemClickEnabled()) {
            Logger.i(TAG, "AutotrackOptions: adapter view item click enable is false");
            return;
        }
        viewOnClick(view);
    }

    public static void spinnerViewOnClick(View view) {
        AutotrackConfig config = ConfigurationProvider.get().getConfiguration(AutotrackConfig.class);
        if (config != null && !config.getAutotrackOptions().isSpinnerItemClickSelectEnabled()) {
            Logger.i(TAG, "AutotrackOptions: spinner item click select is false");
            return;
        }
        viewOnClick(view);
    }

    public static void expandableListViewOnGroupClick(View view) {
        AutotrackConfig config = ConfigurationProvider.get().getConfiguration(AutotrackConfig.class);
        if (config != null && !config.getAutotrackOptions().isExpandableListGroupClickEnabled()) {
            Logger.i(TAG, "AutotrackOptions: expandable list group click enable is false");
            return;
        }
        viewOnClick(view);
    }

    public static void expandableListViewOnChildClick(View view) {
        AutotrackConfig config = ConfigurationProvider.get().getConfiguration(AutotrackConfig.class);
        if (config != null && !config.getAutotrackOptions().isExpandableListChildClickEnabled()) {
            Logger.i(TAG, "AutotrackOptions: expandable list child click enable is false");
            return;
        }
        viewOnClick(view);
    }

    public static void radioGroupOnCheck(View view) {
        AutotrackConfig config = ConfigurationProvider.get().getConfiguration(AutotrackConfig.class);
        if (config != null && !config.getAutotrackOptions().isRadioGroupCheckEnabled()) {
            Logger.i(TAG, "AutotrackOptions: radio group check enable is false");
            return;
        }
        viewOnClick(view);
    }

    public static void materialButtonToggleGroupOnButtonCheck(View view) {
        AutotrackConfig config = ConfigurationProvider.get().getConfiguration(AutotrackConfig.class);
        if (config != null && !config.getAutotrackOptions().isMaterialToggleGroupButtonCheckEnabled()) {
            Logger.i(TAG, "AutotrackOptions: material toggle group check enable is false");
            return;
        }
        viewOnClick(view);
    }

    public static void tabLayoutOnTabSelected(TabLayout.Tab tab) {
        AutotrackConfig config = ConfigurationProvider.get().getConfiguration(AutotrackConfig.class);
        if (config != null && !config.getAutotrackOptions().isTabLayoutTabSelectedEnabled()) {
            Logger.i(TAG, "AutotrackOptions: tablayout tab select enable is false");
            return;
        }
        viewOnClick(tab.view, tab.getText() == null ? null : tab.getText().toString());
    }

    public static void compoundButtonOnCheck(CompoundButton button) {
        AutotrackConfig config = ConfigurationProvider.get().getConfiguration(AutotrackConfig.class);
        if (config != null && !config.getAutotrackOptions().isCompoundButtonCheckEnabled()) {
            Logger.i(TAG, "AutotrackOptions: compound button check enable is false");
            return;
        }
        String content = button.getText().toString();
        if (content == null || content.isEmpty()) content = String.valueOf(button.isChecked());
        viewOnClick(button, content);
    }

    public static void viewOnClickListener(View view) {
        AutotrackConfig config = ConfigurationProvider.get().getConfiguration(AutotrackConfig.class);
        if (config != null && !config.getAutotrackOptions().isViewClickEnabled()) {
            Logger.i(TAG, "AutotrackOptions: view click enable is false");
            return;
        }
        viewOnClick(view);
    }


    public static void activityOptionsItemOnClick(Activity activity, MenuItem menuItem) {
        AutotrackConfig config = ConfigurationProvider.get().getConfiguration(AutotrackConfig.class);
        if (config != null && !config.getAutotrackOptions().isActivityMenuItemClickEnabled()) {
            Logger.i(TAG, "AutotrackOptions: activityMenuItemClickEnabled is false");
            return;
        }
        menuItemOnClick(activity, menuItem);
    }


    public static void toolbarMenuItemOnClick(MenuItem menuItem) {
        AutotrackConfig config = ConfigurationProvider.get().getConfiguration(AutotrackConfig.class);
        if (config != null && !config.getAutotrackOptions().isToolbarMenuItemClickEnabled()) {
            Logger.i(TAG, "AutotrackOptions: toolbarMenuItemClickEnabled is false");
            return;
        }
        menuItemOnClick(menuItem);
    }

    public static void actionMenuItemOnClick(MenuItem menuItem) {
        AutotrackConfig config = ConfigurationProvider.get().getConfiguration(AutotrackConfig.class);
        if (config != null && !config.getAutotrackOptions().isActionMenuItemClickEnabled()) {
            Logger.i(TAG, "AutotrackOptions: actionMenuItemClickEnabled is false");
            return;
        }
        menuItemOnClick(menuItem);
    }

    public static void popupMenuItemOnClick(MenuItem menuItem) {
        AutotrackConfig config = ConfigurationProvider.get().getConfiguration(AutotrackConfig.class);
        if (config != null && !config.getAutotrackOptions().isPopupMenuItemClickEnabled()) {
            Logger.i(TAG, "AutotrackOptions: popupMenuItemClickEnabled is false");
            return;
        }
        menuItemOnClick(menuItem);
    }

    public static void contextMenuItemOnClick(MenuItem menuItem) {
        AutotrackConfig config = ConfigurationProvider.get().getConfiguration(AutotrackConfig.class);
        if (config != null && !config.getAutotrackOptions().isContextMenuItemClickEnabled()) {
            Logger.i(TAG, "AutotrackOptions: contextMenuItemClickEnabled is false");
            return;
        }
        menuItemOnClick(menuItem);
    }

    private static void menuItemOnClick(MenuItem menuItem) {
        if (!TrackerContext.initializedSuccessfully()) {
            Logger.e(TAG, "Autotracker do not initialized successfully");
            return;
        }

        Activity activity = ActivityStateProvider.get().getForegroundActivity();
        menuItemOnClick(activity, menuItem);
    }

    public static void viewOnClick(View view) {
        viewOnClick(view, null);
    }

    private static void viewOnClick(View view, String content) {
        if (!TrackerContext.initializedSuccessfully()) {
            Logger.e(TAG, "Autotracker do not initialized successfully");
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

        ViewNode viewNode = ViewHelper.getClickViewNode(view);
        if (viewNode != null) {
            Page<?> page = PageProvider.get().findPage(view);
            sendClickEvent(page, viewNode, content);
        } else {
            Logger.e(TAG, "ViewNode is NULL");
        }
    }

    public static void menuItemOnClick(Activity activity, MenuItem menuItem) {
        if (!TrackerContext.initializedSuccessfully()) {
            Logger.e(TAG, "Autotracker do not initialized successfully");
            return;
        }

        if (activity == null || menuItem == null) {
            Logger.e(TAG, "menuItemOnClick: activity or menuItem is NULL");
            return;
        }

        Page<?> page = PageProvider.get().findPage(activity);
        ViewNode viewNode = ViewHelper.getMenuItemViewNode(page, menuItem);
        if (viewNode != null) {
            sendClickEvent(page, viewNode, null);
        } else {
            Logger.e(TAG, "MenuItem ViewNode is NULL");
        }
    }

    private static void sendClickEvent(Page<?> page, ViewNode viewNode, String content) {
        if (page == null) {
            Logger.w(TAG, "sendClickEvent page Activity is NULL");
            return;
        }
        TrackMainThread.trackMain().postEventToTrackMain(
                new ViewElementEvent.Builder(AutotrackEventType.VIEW_CLICK)
                        .setPath(page.path())
                        .setPageShowTimestamp(page.getShowTimestamp())
                        .setXpath(viewNode.getXPath())
                        .setIndex(viewNode.getIndex())
                        .setTextValue(content == null ? viewNode.getViewContent() : content)
        );
    }
}
