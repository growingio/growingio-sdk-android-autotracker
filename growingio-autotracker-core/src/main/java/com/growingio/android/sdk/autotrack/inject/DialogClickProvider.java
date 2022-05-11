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

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.ListView;

import com.growingio.android.sdk.TrackerContext;
import com.growingio.android.sdk.autotrack.shadow.AlertControllerShadow;
import com.growingio.android.sdk.autotrack.util.ClassUtil;
import com.growingio.android.sdk.autotrack.view.ViewAttributeUtil;
import com.growingio.android.sdk.track.log.Logger;
import com.growingio.android.sdk.track.utils.ThreadUtils;

class DialogClickProvider {
    private static final String TAG = "DialogClickProvider";
    private static final int[] DIALOG_BUTTON_IDS = new int[]{DialogInterface.BUTTON_NEUTRAL, DialogInterface.BUTTON_NEGATIVE, DialogInterface.BUTTON_POSITIVE};
    private static final String[] DIALOG_BUTTON_NAMES = new String[]{"BUTTON_NEUTRAL", "BUTTON_NEGATIVE", "BUTTON_POSITIVE"};

    private DialogClickProvider() {
    }

    /**
     * 同版本下appcompat 包下的xpath应该也会一样。
     */
    public static void alertDialogXOnClick(androidx.appcompat.app.AlertDialog dialog, int which) {
        Logger.d(TAG, "alertDialogXOnClick: which = " + which);
        if (which < 0) {
            Button button = dialog.getButton(which);
            if (button != null) {
                ViewClickProvider.viewOnClick(button);
            }
        } else {
            ListView listView = dialog.getListView();
            if (listView != null) {
                ViewClickProvider.viewOnClick(listView.getChildAt(which - listView.getFirstVisiblePosition()));
            }
        }
    }

    public static void alertDialogSupportOnClick(android.support.v7.app.AlertDialog dialog, int which) {
        Logger.d(TAG, "alertDialogSupportOnClick: which = " + which);
        if (which < 0) {
            Button button = dialog.getButton(which);
            if (button != null) {
                ViewClickProvider.viewOnClick(button);
            }
        } else {
            ListView listView = dialog.getListView();
            if (listView != null) {
                ViewClickProvider.viewOnClick(listView.getChildAt(which - listView.getFirstVisiblePosition()));
            }
        }
    }

    public static void alertDialogOnClick(AlertDialog dialog, int which) {
        Logger.d(TAG, "alertDialogOnClick: which = " + which);
        if (which < 0) {
            Button button = dialog.getButton(which);
            if (button != null) {
                ViewClickProvider.viewOnClick(button);
            }
        } else {
            ListView listView = dialog.getListView();
            if (listView != null) {
                ViewClickProvider.viewOnClick(listView.getChildAt(which - listView.getFirstVisiblePosition()));
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
        if (!TrackerContext.initializedSuccessfully()) {
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
}
