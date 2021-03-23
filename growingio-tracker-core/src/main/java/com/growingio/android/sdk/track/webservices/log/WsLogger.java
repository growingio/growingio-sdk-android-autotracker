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

import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.growingio.android.sdk.track.log.BaseLogger;
import com.growingio.android.sdk.track.log.CircularFifoQueue;
import com.growingio.android.sdk.track.log.Logger;

/**
 * 设置缓存机制，防止socket在同一时间发送过多log导致关键性信息（pagerefresh和 debuggerevent）无法被服务器及时收到
 */
public class WsLogger extends BaseLogger {
    public static final String TYPE = "WsLogger";

    private volatile Callback mCallback;

    private final CircularFifoQueue<LoggerDataMessage.LogItem> mCacheLogs = new CircularFifoQueue<>(100);
    private static Handler mLogHandler;

    public WsLogger() {
    }

    public void setCallback(Callback callback) {
        mCallback = callback;
    }

    public void openLog() {
        if (mLogHandler == null) {
            HandlerThread logHt = new HandlerThread("WsLogger");
            logHt.start();
            mLogHandler = new Handler(logHt.getLooper());
        }
        //send log 2 times per 1 second
        mLogHandler.postDelayed(mLogRunnable, 500);
        Logger.addLogger(this);
    }

    public void closeLog() {
        if (mLogHandler != null) {
            mLogHandler.removeCallbacks(mLogRunnable);
        }
        Logger.removeLogger(this);
    }

    private final Runnable mLogRunnable = this::printOut;

    @Override
    protected void print(int priority, @NonNull String tag, @NonNull String message, @Nullable Throwable t) {
        String state = priorityToState(priority);
        mCacheLogs.add(LoggerDataMessage.createLogItem(state, "subType", message, System.currentTimeMillis()));
    }

    public void printOut() {
        if (mCacheLogs.size() == 0) return;
        if (mCallback != null) {
            String loggerData = LoggerDataMessage.createMessage(mCacheLogs).toJSONObject().toString();
            mCacheLogs.clear();
            mCallback.disposeLog(loggerData);
        }
        mLogHandler.removeCallbacks(mLogRunnable);
        mLogHandler.postDelayed(mLogRunnable, 500);
    }

    @Override
    public String getType() {
        return TYPE;
    }

    private String priorityToState(int priority) {
        switch (priority) {
            case Log.VERBOSE:
                return "VERBOSE";
            case Log.DEBUG:
                return "DEBUG";
            case Log.INFO:
                return "INFO";
            case Log.WARN:
                return "WARN";
            case Log.ERROR:
                return "ERROR";
            case Log.ASSERT:
                return "ALARM";
            default:
                return "OTHER";
        }
    }

    public interface Callback {
        void disposeLog(String logMessage);
    }
}
