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

package com.growingio.android.sdk.autotrack.click;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

import com.growingio.android.sdk.autotrack.AutotrackConfiguration;
import com.growingio.android.sdk.autotrack.events.AutotrackEventType;
import com.growingio.android.sdk.autotrack.events.ViewElementEvent;
import com.growingio.android.sdk.autotrack.page.Page;
import com.growingio.android.sdk.autotrack.page.PageProvider;
import com.growingio.android.sdk.autotrack.shadow.AlertControllerShadow;
import com.growingio.android.sdk.autotrack.util.ClassUtil;
import com.growingio.android.sdk.autotrack.view.ViewAttributeUtil;
import com.growingio.android.sdk.autotrack.view.ViewHelper;
import com.growingio.android.sdk.autotrack.view.ViewNode;
import com.growingio.android.sdk.track.TrackMainThread;
import com.growingio.android.sdk.track.log.Logger;
import com.growingio.android.sdk.track.providers.ActivityStateProvider;
import com.growingio.android.sdk.track.utils.ThreadUtils;

class ViewClickProvider {
    private static final String TAG = "ViewClickProvider";
    private static final int[] DIALOG_BUTTON_IDS = new int[]{DialogInterface.BUTTON_NEUTRAL, DialogInterface.BUTTON_NEGATIVE, DialogInterface.BUTTON_POSITIVE};
    private static final String[] DIALOG_BUTTON_NAMES = new String[]{"BUTTON_NEUTRAL", "BUTTON_NEGATIVE", "BUTTON_POSITIVE"};

    private ViewClickProvider() {
    }

    public static void alertDialogOnClick(AlertDialog dialog, int which) {
        Logger.d(TAG, "alertDialogOnClick: which = " + which);
        if (which < 0) {
            Button button = dialog.getButton(which);
            if (button != null) {
                viewOnClick(button);
            }
        } else {
            ListView listView = dialog.getListView();
            if (listView != null) {
                viewOnClick(listView.getChildAt(which - listView.getFirstVisiblePosition()));
            }
        }
    }

    /**
     * 由于Android的不同版本的AlertDialog实现机制不一样，导致view xpath也不一样，如
     * Android 7.0之前     /DialogWindow/DecorView/FrameLayout[0]/FrameLayout[0]/LinearLayout[0]/LinearLayout[2]/LinearLayout[0]/Button[2]
     * Android 7，0及其之后 /DialogWindow/DecorView/FrameLayout[0]/FrameLayout[0]/AlertDialogLayout[0]/ScrollView[0]/ButtonBarLayout[0]/Button[2]
     * 所以这里人为的给view定义一个id
     */
    public static void alertDialogShow(AlertDialog dialog) {
        if (!AutotrackConfiguration.initializedSuccessfully()) {
            Logger.e(TAG, "Autotracker do not initialized successfully");
            return;
        }

        if (dialog == null) {
            Logger.d(TAG, "alertDialogShow: dialog is NULL");
            return;
        }

        Logger.d(TAG, "alertDialogShow: " + dialog);
        for (int i = 0; i < DIALOG_BUTTON_IDS.length; i++) {
            Button button = dialog.getButton(DIALOG_BUTTON_IDS[i]);
            if (button != null && TextUtils.isEmpty(ViewAttributeUtil.getCustomId(button))) {
                String dialogButtonName = DIALOG_BUTTON_NAMES[i];
                ThreadUtils.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ViewAttributeUtil.setCustomId(button, getAlertDialogName(dialog) + "/" + dialogButtonName);
                    }
                });
            }
        }

        // TODO: 2020/10/10 list dialog等也要处理
    }

    private static String getAlertDialogName(AlertDialog dialog) {
        String className = ClassUtil.getSimpleClassName(dialog.getClass());
        try {
            AlertControllerShadow alertControllerShadow = new AlertControllerShadow(dialog);
            CharSequence title = alertControllerShadow.getTitle();
            if (!TextUtils.isEmpty(title)) {
                return className + "/" + title;
            }

            CharSequence message = alertControllerShadow.getMessage();
            if (!TextUtils.isEmpty(message)) {
                return className + "/" + message;
            }
        } catch (Exception e) {
            Logger.e(TAG, e);
        }
        return className;
    }

    public static void viewOnClick(View view) {
        if (!AutotrackConfiguration.initializedSuccessfully()) {
            Logger.e(TAG, "Autotracker do not initialized successfully");
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
        if (!AutotrackConfiguration.initializedSuccessfully()) {
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
        if (!AutotrackConfiguration.initializedSuccessfully()) {
            Logger.e(TAG, "Autotracker do not initialized successfully");
            return;
        }

        Activity activity = ActivityStateProvider.get().getForegroundActivity();
        menuItemOnClick(activity, menuItem);
    }

    private static void sendClickEvent(Page<?> page, ViewNode viewNode) {
        TrackMainThread.trackMain().postEventToTrackMain(
                new ViewElementEvent.Builder()
                        .setEventType(AutotrackEventType.VIEW_CLICK)
                        .setPageName(page.path())
                        .setPageShowTimestamp(page.getShowTimestamp())
                        .setXpath(viewNode.getXPath())
                        .setIndex(viewNode.getIndex())
                        .setTextValue(viewNode.getViewContent())
        );
    }
}
