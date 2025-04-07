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
package com.growingio.android.circler;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;

import com.growingio.android.sdk.track.SDKConfig;
import com.growingio.android.sdk.track.TrackMainThread;
import com.growingio.android.sdk.track.log.Logger;
import com.growingio.android.sdk.track.providers.ActivityStateProvider;
import com.growingio.android.sdk.track.view.TipView;

/**
 * <p>
 * Thread safe circle tipview
 *
 * @author cpacm 2021/10/22
 */
public class ThreadSafeTipView {
    private static final String TAG = "ThreadSafeTipView";
    private TipView tipView;
    private final Context context;

    private final String appVersion;
    private final ActivityStateProvider activityStateProvider;

    private void initView() {
        if (tipView == null) {
            tipView = new TipView(context);
            tipView.setContent(R.string.growing_circler_connecting_to_web);
        }
    }

    public ThreadSafeTipView(Context context, ActivityStateProvider activityStateProvider, String appVersion) {
        this.appVersion = appVersion;
        this.context = context;
        this.activityStateProvider = activityStateProvider;
    }

    public void enableShow() {
        runOnUiThread(() -> {
            initView();
            tipView.show(this.activityStateProvider.getResumedActivity());
        });
    }

    public void dismiss() {
        runOnUiThread(() -> {
            if (tipView != null) {
                tipView.dismiss();
                tipView = null;
            }
        });
    }

    public void removeOnly() {
        runOnUiThread(() -> {
            if (tipView != null) {
                tipView.remove();
            }
        });
    }

    public void onReady(OnExitListener listener) {
        runOnUiThread(() -> {
            initView();
            tipView.setContent(R.string.growing_circler_progress);
            tipView.setOnClickListener(v -> showExitDialog(listener));
        });
    }

    public void setErrorMessage(int resid) {
        runOnUiThread(() -> {
            tipView.setErrorMessage(context.getResources().getText(resid));
        });
    }

    public void show(Activity activity) {
        runOnUiThread(() -> {
            initView();
            tipView.ready(activity);
        });
    }

    protected void showExitDialog(OnExitListener listener) {
        Activity activity = activityStateProvider.getForegroundActivity();
        if (activity == null) {
            Logger.e(TAG, "showExitDialog: ForegroundActivity is NULL");
            return;
        }
        String message = activity.getString(R.string.growing_circler_app_version)
                + appVersion
                + activity.getString(R.string.growing_circler_sdk_version)
                + SDKConfig.SDK_VERSION;
        new AlertDialog.Builder(activity)
                .setTitle(R.string.growing_circler_progress)
                .setMessage(message)
                .setPositiveButton(R.string.growing_circler_exit, (dialog, which) -> listener.onExitCircler())
                .setNegativeButton(R.string.growing_circler_continue, null)
                .create()
                .show();
    }

    public void showQuitedDialog(OnExitListener listener) {
        Activity activity = activityStateProvider.getForegroundActivity();
        if (activity == null) {
            Logger.e(TAG, "showQuitedDialog: ForegroundActivity is NULL");
            return;
        }
        new AlertDialog.Builder(activity)
                .setTitle(R.string.growing_circler_device_unconnected)
                .setMessage(R.string.growing_circler_unconnected)
                .setPositiveButton(R.string.growing_circler_exit, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        listener.onExitCircler();
                    }
                })
                .setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        listener.onExitCircler();
                    }
                })
                .setCancelable(false)
                .create()
                .show();
    }

    private void runOnUiThread(Runnable r) {
        TrackMainThread.trackMain().runOnUiThread(r);
    }

    public interface OnExitListener {
        void onExitCircler();
    }

}
