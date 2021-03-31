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

package com.growingio.android.sdk.autotrack.webservices.debugger;

import android.app.Activity;
import android.app.AlertDialog;

import com.growingio.android.sdk.autotrack.R;
import com.growingio.android.sdk.track.SDKConfig;
import com.growingio.android.sdk.track.log.Logger;
import com.growingio.android.sdk.track.providers.ActivityStateProvider;
import com.growingio.android.sdk.track.providers.AppInfoProvider;
import com.growingio.android.sdk.track.utils.ThreadUtils;
import com.growingio.android.sdk.track.webservices.BaseWebSocketService;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * <p>
 * debugger service
 */
public class DebuggerService extends BaseWebSocketService {
    private static final String TAG = "DebuggerService";
    public static final String SERVICE_TYPE = "debugger";


    public DebuggerService() {
        DebuggerEventWrapper.get().registerDebuggerEventListener(this::sendMessage);
    }


    @Override
    protected void onReady() {
        super.onReady();
        DebuggerEventWrapper.get().ready();
        ThreadUtils.runOnUiThread(() -> {
            mTipView.setContent(R.string.growing_autotracker_is_debugger);
            mTipView.setOnClickListener(v -> showExitDialog());
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
                .setPositiveButton(R.string.growing_autotracker_exit_debugger, (dialog, which) -> exitDebugger())
                .setNegativeButton(R.string.growing_autotracker_continue_debugger, null)
                .create()
                .show();
    }

    @Override
    protected void onMessage(String text) {
        try {
            JSONObject message = new JSONObject(text);
            String msgType = message.optString("msgType");
            if (DebuggerEventWrapper.SERVICE_LOGGER_OPEN.equals(msgType)) {
                DebuggerEventWrapper.get().openLogger();
            } else if (DebuggerEventWrapper.SERVICE_LOGGER_CLOSE.equals(msgType)) {
                DebuggerEventWrapper.get().closeLogger();
            }
        } catch (JSONException e) {
            Logger.e(TAG, e);
        }
    }


    private void exitDebugger() {
        sendQuitMessage();
        end();
    }

    @Override
    protected void onFailed() {
        if (getSocketState() >= SOCKET_STATE_CLOSED) {
            return;
        }
        super.onFailed();
        Logger.e(TAG, "Start DebuggerService Failed");
        ThreadUtils.runOnUiThread(this::showQuitedDialog);
    }

    @Override
    protected void onQuited() {
        if (getSocketState() >= SOCKET_STATE_CLOSED) {
            return;
        }
        end();
        super.onQuited();
        ThreadUtils.runOnUiThread(this::showQuitedDialog);
    }

    private void showQuitedDialog() {
        Activity activity = ActivityStateProvider.get().getForegroundActivity();
        if (activity == null) {
            Logger.e(TAG, "showQuitedDialog: ForegroundActivity is NULL");
            return;
        }
        new AlertDialog.Builder(activity)
                .setTitle(R.string.growing_tracker_device_unconnected)
                .setMessage(R.string.growing_autotracker_debugger_unconnected)
                .setPositiveButton(R.string.growing_autotracker_exit_debugger, null)
                .setOnDismissListener(dialog -> exitDebugger())
                .setCancelable(false)
                .create()
                .show();
    }

    @Override
    public void end() {
        super.end();
        mTipView.dismiss();
        DebuggerEventWrapper.get().end();
    }
}
