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

package com.growingio.android.sdk.track.log;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.concurrent.atomic.AtomicBoolean;

public class DebugLogger extends BaseLogger {
    private static final String TYPE = "Logcat";
    private static final String TAG_PREFIX = "TRACK.";
    private static final int MAX_LOG_LENGTH = 4000;

    private AtomicBoolean mFirstInit = new AtomicBoolean(true);

    @Override
    protected void print(int priority, @NonNull String tag, @NonNull String message, @Nullable Throwable t) {
        if (mFirstInit.compareAndSet(true, false)) {
            ILogger cacheLogger = Logger.getLogger(CacheLogger.TYPE);
            if (cacheLogger instanceof CacheLogger) {
                for (LogItem item : ((CacheLogger) cacheLogger).getCacheLogs()) {
                    printWithoutCache(item.getPriority(), item.getTag(), item.getMessage(), item.getThrowable());
                }
            }
        }
        printWithoutCache(priority, tag, message, t);
    }

    protected void printWithoutCache(int priority, @NonNull String tag, @NonNull String message, @Nullable Throwable t) {
        if (priority > Log.ASSERT) {
            return;
        }
        tag = TAG_PREFIX + tag;

        if (message.length() < MAX_LOG_LENGTH) {
            if (priority == Log.ASSERT) {
                Log.wtf(tag, message);
            } else {
                Log.println(priority, tag, message);
            }
            return;
        }

        for (int i = 0, length = message.length(); i < length; i++) {
            int newline = message.indexOf('\n', i);
            newline = newline != -1 ? newline : length;
            do {
                int end = Math.min(newline, i + MAX_LOG_LENGTH);
                String part = message.substring(i, end);
                if (priority == Log.ASSERT) {
                    Log.wtf(tag, part);
                } else {
                    Log.println(priority, tag, part);
                }
                i = end;
            } while (i < newline);
        }
    }

    @Override
    public String getType() {
        return TYPE;
    }
}
