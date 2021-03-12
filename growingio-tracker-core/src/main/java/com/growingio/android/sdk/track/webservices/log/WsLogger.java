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
import com.growingio.android.sdk.track.log.CacheLogger;
import com.growingio.android.sdk.track.log.ILogger;
import com.growingio.android.sdk.track.log.LogItem;
import com.growingio.android.sdk.track.log.Logger;

import java.util.ArrayDeque;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicBoolean;

public class WsLogger extends BaseLogger {
    public static final String TYPE = "WsLogger";

    private volatile Callback mCallback;
    private AtomicBoolean mFirstInit = new AtomicBoolean(true);

    public void setCallback(Callback callback) {
        mCallback = callback;
    }

    @Override
    protected void print(int priority, @NonNull String tag, @NonNull String message, @Nullable Throwable t) {
          String state = priorityToState(priority);
          if (mCallback != null) {
              if (mFirstInit.compareAndSet(true, false)) {
                  ILogger cacheLogger = Logger.getLogger(CacheLogger.TYPE);
                  if (cacheLogger instanceof CacheLogger) {
                      List<LogItem> cacheLogs = ((CacheLogger) cacheLogger).getCacheLogs();
                      Queue<LoggerDataMessage.LogItem> queue = new ArrayDeque<>(cacheLogs.size());
                      for (LogItem item : cacheLogs) {
                          queue.add(LoggerDataMessage.createLogItem(
                                  priorityToState(item.getPriority()),
                                  "subType",
                                  item.getMessage(),
                                  item.getTimeStamp()));
                      }
                      mCallback.disposeLog(LoggerDataMessage.createMessage(queue));
                  }
              }
              mCallback.disposeLog(LoggerDataMessage.
                      createMessage(state,
                              "subType",
                              message,
                              System.currentTimeMillis()));
          }
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
        void disposeLog(LoggerDataMessage logMessage);
    }
}
