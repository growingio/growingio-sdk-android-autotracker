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

package com.growingio.android.sdk.autotrack.webservices.circle;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.view.View;

import com.growingio.android.sdk.autotrack.R;
import com.growingio.android.sdk.autotrack.webservices.ScreenshotProvider;
import com.growingio.android.sdk.autotrack.webservices.circle.entity.CircleScreenshot;
import com.growingio.android.sdk.track.SDKConfig;
import com.growingio.android.sdk.track.async.Callback;
import com.growingio.android.sdk.track.async.Disposable;
import com.growingio.android.sdk.track.log.Logger;
import com.growingio.android.sdk.track.providers.ActivityStateProvider;
import com.growingio.android.sdk.track.providers.AppInfoProvider;
import com.growingio.android.sdk.track.utils.ThreadUtils;
import com.growingio.android.sdk.track.webservices.BaseWebSocketService;

public class CircleService extends BaseWebSocketService implements ScreenshotProvider.OnScreenshotRefreshedListener {
    private static final String TAG = "CircleService";

    public static final String SERVICE_TYPE = "circle";

    private long mSnapshotKey = 0;
    private Disposable mCircleScreenshotDisposable;

    @Override
    protected void onReady() {
        super.onReady();
        registerScreenshotRefreshedListener();
        ThreadUtils.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mTipView.setContent(R.string.growing_autotracker_is_circling);
                mTipView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showExitDialog();
                    }
                });
            }
        });
    }

    private void showExitDialog() {
        Activity activity = ActivityStateProvider.get().getForegroundActivity();
        if (activity == null) {
            Logger.e(TAG, "showExitDialog: ForegroundActivity is NULL");
            return;
        }

        String message = activity.getString(R.string.growing_autotracker_app_version) + AppInfoProvider.get().getAppVersion() + "\n"
                + activity.getString(R.string.growing_autotracker_sdk_version) + SDKConfig.SDK_VERSION;
        new AlertDialog.Builder(activity)
                .setTitle(R.string.growing_autotracker_is_circling)
                .setMessage(message)
                .setPositiveButton(R.string.growing_autotracker_exit_circle, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        exitCircle();
                    }
                })
                .setNegativeButton(R.string.growing_autotracker_continue_circle, null)
                .create()
                .show();
    }

    private void exitCircle() {
        end();
        sendQuitMessage();
    }

    @Override
    protected void onFailed() {
        if (getSocketState() >= SOCKET_STATE_CLOSED) {
            return;
        }
        super.onFailed();
        Logger.e(TAG, "Start CircleService Failed");
        ThreadUtils.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                showQuitedDialog();
            }
        });
    }

    @Override
    protected void onQuited() {
        if (getSocketState() >= SOCKET_STATE_CLOSED) {
            return;
        }

        end();
        super.onQuited();
        ThreadUtils.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                showQuitedDialog();
            }
        });
    }

    private void showQuitedDialog() {
        Activity activity = ActivityStateProvider.get().getForegroundActivity();
        if (activity == null) {
            Logger.e(TAG, "showQuitedDialog: ForegroundActivity is NULL");
            return;
        }
        new AlertDialog.Builder(activity)
                .setTitle(R.string.growing_tracker_device_unconnected)
                .setMessage(R.string.growing_autotracker_circle_unconnected)
                .setPositiveButton(R.string.growing_autotracker_exit_circle, null)
                .setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        exitCircle();
                    }
                })
                .setCancelable(false)
                .create()
                .show();
    }

    private void registerScreenshotRefreshedListener() {
        ScreenshotProvider.get().registerScreenshotRefreshedListener(this);
    }

    @Override
    public void onScreenshotRefreshed(String screenshotBase64, float scale) {
        if (mCircleScreenshotDisposable != null) {
            mCircleScreenshotDisposable.dispose();
        }

        mCircleScreenshotDisposable = new CircleScreenshot.Builder()
                .setScale(scale)
                .setScreenshot(screenshotBase64)
                .setSnapshotKey(mSnapshotKey++)
                .build(new Callback<CircleScreenshot>() {
                    @Override
                    public void onSuccess(CircleScreenshot result) {
                        Logger.d(TAG, "Create circle screenshot successfully");
                        sendMessage(result.toJSONObject().toString());
                    }

                    @Override
                    public void onFailed() {
                        Logger.e(TAG, "Create circle screenshot failed");
                    }
                });
    }

    @Override
    public void end() {
        super.end();
        mTipView.dismiss();
        ScreenshotProvider.get().unregisterScreenshotRefreshedListener(this);
    }
}
