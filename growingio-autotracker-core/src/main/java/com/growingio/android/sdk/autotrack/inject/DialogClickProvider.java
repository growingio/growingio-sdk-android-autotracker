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

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewParent;
import android.widget.Button;
import android.widget.ListView;

import com.growingio.android.sdk.TrackerContext;
import com.growingio.android.sdk.autotrack.AutotrackConfig;
import com.growingio.android.sdk.autotrack.shadow.AlertControllerShadow;
import com.growingio.android.sdk.autotrack.util.ClassUtil;
import com.growingio.android.sdk.autotrack.view.ViewAttributeUtil;
import com.growingio.android.sdk.track.TrackMainThread;
import com.growingio.android.sdk.track.log.Logger;
import com.growingio.android.sdk.track.providers.TrackerLifecycleProvider;

class DialogClickProvider implements TrackerLifecycleProvider {
    private static final String TAG = "DialogClickProvider";
    private static final int[] DIALOG_BUTTON_IDS = new int[]{DialogInterface.BUTTON_NEUTRAL, DialogInterface.BUTTON_NEGATIVE, DialogInterface.BUTTON_POSITIVE};
    private static final String[] DIALOG_BUTTON_NAMES = new String[]{"BUTTON_NEUTRAL", "BUTTON_NEGATIVE", "BUTTON_POSITIVE"};

    private ViewClickProvider viewClickProvider;
    private AutotrackConfig autotrackConfig;

    DialogClickProvider() {
    }

    @Override
    public void setup(TrackerContext context) {
        viewClickProvider = context.getProvider(ViewClickProvider.class);
        autotrackConfig = context.getConfigurationProvider().getConfiguration(AutotrackConfig.class);
    }

    @Override
    public void shutdown() {

    }

    /**
     * 同版本下appcompat 包下的xpath应该也会一样。
     */
    public void alertDialogXOnClick(androidx.appcompat.app.AlertDialog dialog, int which) {
        Logger.d(TAG, "alertDialogXOnClick: which = " + which);
        if (dialog == null) return;
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

    public void alertDialogSupportOnClick(android.support.v7.app.AlertDialog dialog, int which) {
        Logger.d(TAG, "alertDialogSupportOnClick: which = " + which);
        if (dialog == null) return;
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

    public void alertDialogOnClick(AlertDialog dialog, int which) {
        Logger.d(TAG, "alertDialogOnClick: which = " + which);
        if (dialog == null) return;
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

    private void viewOnClick(View view) {
        if (autotrackConfig != null && !autotrackConfig.getAutotrackOptions().isDialogClickEnabled()) {
            Logger.i(TAG, "AutotrackOptions: dialog click enable is false");
            return;
        }
        if (viewClickProvider != null) {
            viewClickProvider.viewOnClick(view);
        }
    }

    /**
     * 由于Android的不同版本的AlertDialog实现机制不一样，导致view xpath也不一样，如
     * Android 7.0之前     /DialogWindow/DecorView/FrameLayout[0]/FrameLayout[0]/LinearLayout[0]/LinearLayout[2]/LinearLayout[0]/Button[2]
     * Android 7，0及其之后 /DialogWindow/DecorView/FrameLayout[0]/FrameLayout[0]/AlertDialogLayout[0]/ScrollView[0]/ButtonBarLayout[0]/Button[2]
     * 所以这里人为的给view定义一个id
     */
    public void alertDialogShow(AlertDialog dialog) {
        if (!TrackerContext.initializedSuccessfully()) {
            Logger.e(TAG, "Autotracker do not initialized successfully");
            return;
        }

        if (dialog == null) {
            Logger.w(TAG, "alertDialogShow: dialog is NULL");
            return;
        }

        if (autotrackConfig != null && autotrackConfig.isDowngrade()) {
            alertDialogShowV3(dialog);
        } else {
            alertDialogShowV4(dialog);
        }
    }

    private void alertDialogShowV3(AlertDialog dialog) {
        Logger.d(TAG, "alertDialogShowV3: " + dialog);
        for (int i = 0; i < DIALOG_BUTTON_IDS.length; i++) {
            Button button = dialog.getButton(DIALOG_BUTTON_IDS[i]);
            if (button != null && ViewAttributeUtil.getCustomId(button) == null) {
                String dialogButtonName = DIALOG_BUTTON_NAMES[i];
                TrackMainThread.trackMain().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ViewAttributeUtil.setCustomId(button, getAlertDialogName(dialog) + "/" + dialogButtonName);
                    }
                });
            }
        }
    }

    private void alertDialogShowV4(AlertDialog dialog) {
        Logger.d(TAG, "alertDialogShowV4: " + dialog);
        for (int dialogButtonId : DIALOG_BUTTON_IDS) {
            Button button = dialog.getButton(dialogButtonId);
            if (button != null) {
                ViewParent viewParent = button.getParent();
                if (viewParent instanceof View) {
                    View parent = (View) viewParent;
                    if (ViewAttributeUtil.getCustomId(parent) == null) {
                        TrackMainThread.trackMain().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                ViewAttributeUtil.setCustomId(parent, getAlertDialogName(dialog) + "ButtonLayout");
                            }
                        });
                    }
                }
            }
        }

        ListView listView = dialog.getListView();
        if (listView != null && ViewAttributeUtil.getCustomId(listView) == null) {
            TrackMainThread.trackMain().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ViewAttributeUtil.setCustomId(listView, getAlertDialogName(dialog) + "ListView");
                }
            });
        }

        // customView
    }

    private String getAlertDialogName(AlertDialog dialog) {
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
