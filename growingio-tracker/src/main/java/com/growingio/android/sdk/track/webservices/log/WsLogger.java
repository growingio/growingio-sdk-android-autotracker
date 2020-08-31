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

package com.growingio.android.sdk.track.webservices.log;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.growingio.android.sdk.track.log.BaseLogger;

public class WsLogger extends BaseLogger {
    public static final String TYPE = "WsLogger";

    private final CircularFifoQueue<LoggerDataMessage.LogItem> mCacheLogs = new CircularFifoQueue<>(100);
    private Callback mCallback;

    public void setCallback(Callback callback) {
        mCallback = callback;
    }

    @Override
    protected void print(int priority, @NonNull String tag, @NonNull String message, @Nullable Throwable t) {
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
            case Log.ASSERT:
                state = "ALARM";
                break;
            default:
                state = "OTHER";
        }

        if (mCallback != null) {
            if (!mCacheLogs.isEmpty()) {
                mCallback.disposeLog(LoggerDataMessage.createMessage(mCacheLogs));
                mCacheLogs.clear();
            }
            mCallback.disposeLog(LoggerDataMessage.
                    createMessage(state,
                            "xxx",
                            message,
                            String.valueOf(System.currentTimeMillis())));
        } else {
            mCacheLogs.add(LoggerDataMessage.createLogItem(state,
                    "xxx",
                    message,
                    String.valueOf(System.currentTimeMillis())));
        }
    }

    @Override
    public String getType() {
        return TYPE;
    }

    interface Callback {
        void disposeLog(LoggerDataMessage logMessage);
    }
}
