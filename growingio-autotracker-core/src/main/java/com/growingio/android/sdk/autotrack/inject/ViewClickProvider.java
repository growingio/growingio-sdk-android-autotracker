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

import com.growingio.android.sdk.TrackerContext;
import com.growingio.android.sdk.autotrack.util.ClassUtil;
import com.growingio.android.sdk.track.events.AutotrackEventType;
import com.growingio.android.sdk.track.events.ViewElementEvent;
import com.growingio.android.sdk.autotrack.page.Page;
import com.growingio.android.sdk.autotrack.page.PageProvider;
import com.growingio.android.sdk.autotrack.view.ViewHelper;
import com.growingio.android.sdk.autotrack.view.ViewNode;
import com.growingio.android.sdk.track.TrackMainThread;
import com.growingio.android.sdk.track.log.Logger;
import com.growingio.android.sdk.track.providers.ActivityStateProvider;

class ViewClickProvider {
    private static final String TAG = "ViewClickProvider";

    private ViewClickProvider() {
    }

    public static void viewOnClick(View view) {
        if (!TrackerContext.initializedSuccessfully()) {
            Logger.e(TAG, "Autotracker do not initialized successfully");
            return;
        }

        // 为了防止click事件重复发送
        if (ClassUtil.isDuplicateClick(view)) {
            view.hasOnClickListeners();
            Logger.e(TAG, "Duplicate Click");
            return;
        }

        ViewNode viewNode = ViewHelper.getClickViewNode(view);
        if (viewNode != null) {
            Page<?> page = PageProvider.get().findPage(view);
            sendClickEvent(page, viewNode);
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
            sendClickEvent(page, viewNode);
        } else {
            Logger.e(TAG, "MenuItem ViewNode is NULL");
        }
    }

    public static void menuItemOnClick(MenuItem menuItem) {
        if (!TrackerContext.initializedSuccessfully()) {
            Logger.e(TAG, "Autotracker do not initialized successfully");
            return;
        }

        Activity activity = ActivityStateProvider.get().getForegroundActivity();
        menuItemOnClick(activity, menuItem);
    }

    private static void sendClickEvent(Page<?> page, ViewNode viewNode) {
        if (page == null) {
            Logger.e(TAG, "sendClickEvent page Activity is NULL");
            return;
        }
        TrackMainThread.trackMain().postEventToTrackMain(
                new ViewElementEvent.Builder()
                        .setEventType(AutotrackEventType.VIEW_CLICK)
                        .setPath(page.path())
                        .setPageShowTimestamp(page.getShowTimestamp())
                        .setXpath(viewNode.getXPath())
                        .setIndex(viewNode.getIndex())
                        .setTextValue(viewNode.getViewContent())
        );
    }
}
