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

package com.growingio.android.sdk.autotrack.webservices;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.growingio.android.sdk.autotrack.webservices.message.LoggerDataMessage;
import com.growingio.android.sdk.track.crash.CrashUtil;
import com.growingio.android.sdk.track.utils.LogUtil;

import org.json.JSONObject;

public class WSLogCacheUtil extends LogUtil.Util {
    private CircularFifoQueue<LoggerDataMessage.LogItem> mCacheLogs;
    private ActionCallback mActionCallback;

    private WSLogCacheUtil() {
        mCacheLogs = new CircularFifoQueue<>(100);
    }

    public static WSLogCacheUtil getInstance() {
        return SingleInstance.INSTANCE;
    }

    public void setmActionCallback(ActionCallback mActionCallback) {
        this.mActionCallback = mActionCallback;
    }

    @Override
    protected void log(int priority, @Nullable String tag, @NonNull String message, @Nullable Throwable t) {

        String state;
        switch (priority) {
            case Log.VERBOSE:
                state = "VERBOSE";
                break;
            case Log.DEBUG:
                state = "DEBUG";
                break;
            case Log.INFO:
                state = "INFO";
                break;
            case Log.WARN:
                state = "WARN";
                break;
            case Log.ERROR:
                state = "ERROR";
                break;
            case CrashUtil.ALARM:
                state = "ALARM";
                break;
            default:
                state = "OTHER";
        }

        if (mActionCallback != null) {
            if (!mCacheLogs.isEmpty()) {
                mActionCallback.disposeLog(LoggerDataMessage.createMessage(mCacheLogs).toJSONObject());
                mCacheLogs.clear();
            }
            mActionCallback.disposeLog(LoggerDataMessage.
                    createMessage(state,
                            "xxx",
                            message,
                            String.valueOf(System.currentTimeMillis()))
                    .toJSONObject());
        } else {
            mCacheLogs.add(LoggerDataMessage.createLogItem(state,
                    "xxx",
                    message,
                    String.valueOf(System.currentTimeMillis())));
        }
    }

    private static class SingleInstance {
        private static final WSLogCacheUtil INSTANCE = new WSLogCacheUtil();

        private SingleInstance() {
        }
    }

    public interface ActionCallback {
        void disposeLog(JSONObject logMessage);
    }
}
