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

import com.growingio.android.sdk.track.log.CacheLogger;
import com.growingio.android.sdk.track.log.ILogger;
import com.growingio.android.sdk.track.log.LogItem;
import com.growingio.android.sdk.track.log.Logger;

import java.util.List;

/**
 * 利用CacheLog缓存机制，防止socket在同一时间发送过多log导致关键性信息（pagerefresh和 debuggerevent）无法被服务器及时收到
 */
public class WsLogger {
    public static final String TYPE = "WsLogger";

    private volatile Callback mCallback;
    private static Handler sLogHandler;
    private CacheLogger cacheLogger;

    public WsLogger() {
        ILogger cacheLogger = Logger.getLogger(CacheLogger.TYPE);
        if (cacheLogger instanceof CacheLogger) {
            this.cacheLogger = ((CacheLogger) cacheLogger);
        }
    }

    public void setCallback(Callback callback) {
        mCallback = callback;
    }

    public void openLog() {
        if (sLogHandler == null) {
            HandlerThread logHt = new HandlerThread("WsLogger");
            logHt.start();
            sLogHandler = new Handler(logHt.getLooper());
        }
        //send log 2 times per 1 second
        sLogHandler.postDelayed(mLogRunnable, 500);
    }

    public void closeLog() {
        if (sLogHandler != null) {
            sLogHandler.removeCallbacks(mLogRunnable);
        }
    }

    private final Runnable mLogRunnable = this::printOut;


    public void printOut() {
        if (cacheLogger != null) {
            List<LogItem> mLoggers = cacheLogger.getCacheLogsAndClear();
            if (mLoggers == null || mLoggers.size() == 0) return;
            if (mCallback != null) {
                String loggerData = LoggerDataMessage.createTrackMessage(mLoggers).toJSONObject().toString();
                mCallback.disposeLog(loggerData);
            }
        }
        sLogHandler.removeCallbacks(mLogRunnable);
        sLogHandler.postDelayed(mLogRunnable, 500);
    }

    public interface Callback {
        void disposeLog(String logMessage);
    }
}
