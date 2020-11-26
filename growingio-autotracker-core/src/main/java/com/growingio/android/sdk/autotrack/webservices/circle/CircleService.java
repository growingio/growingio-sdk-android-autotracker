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
import com.growingio.android.sdk.track.ContextProvider;
import com.growingio.android.sdk.track.async.Callback;
import com.growingio.android.sdk.track.async.Disposable;
import com.growingio.android.sdk.track.log.Logger;
import com.growingio.android.sdk.track.providers.ActivityStateProvider;
import com.growingio.android.sdk.track.utils.SystemUtil;
import com.growingio.android.sdk.track.utils.ThreadUtils;
import com.growingio.android.sdk.track.webservices.BaseWebSocketService;
import com.growingio.android.sdk.track.webservices.log.MobileLogService;

import org.json.JSONException;
import org.json.JSONObject;

public class CircleService extends BaseWebSocketService implements ScreenshotProvider.OnScreenshotRefreshedListener {
    private static final String TAG = "CircleService";

    public static final String SERVICE_TYPE = "circle";

    private long mSnapshotKey = 0;
    private Disposable mCircleScreenshotDisposable;
    private MobileLogService mMobileLogService;

    @Override
    protected void onReady() {
        registerScreenshotRefreshedListener();
        ThreadUtils.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mTipView.setContent(R.string.growing_tracker_is_circling);
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
        new AlertDialog.Builder(activity)
                .setTitle(R.string.growing_autotracker_circle)
                .setMessage(R.string.growing_autotracker_exit_circle)
                .setPositiveButton(R.string.growing_autotracker_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        exit();
                    }
                })
                .setNegativeButton(R.string.growing_autotracker_cancel, null)
                .create()
                .show();
    }

    private void exit() {
        sendQuitMessage();
        SystemUtil.killAppProcess(ContextProvider.getApplicationContext());
    }

    @Override
    protected void onFailed() {
        super.onFailed();
        Logger.e(TAG, "Start CircleService Failed");
    }

    @Override
    protected void onQuited() {
        end();
    }

    @Override
    protected void onMessage(String text) {
        try {
            JSONObject message = new JSONObject(text);
            String msgType = message.optString("msgType");
            if (MobileLogService.SERVICE_TYPE.equals(msgType)) {
                mMobileLogService = new MobileLogService();
                // TODO: 2020/10/13 在圈选中启动mobileLog
                mMobileLogService.start("xxx");
            }
        } catch (JSONException e) {
            Logger.e(TAG, e);
        }
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
        if (mMobileLogService != null) {
            mMobileLogService.end();
        }
        ScreenshotProvider.get().unregisterScreenshotRefreshedListener(this);
    }
}
