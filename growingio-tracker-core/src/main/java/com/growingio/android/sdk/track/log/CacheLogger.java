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
package com.growingio.android.sdk.track.log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CacheLogger extends BaseLogger {
    public static final String TYPE = "CacheLogger";

    private final CircularFifoQueue<LogItem> mCacheLogs = new CircularFifoQueue<>(100);

    @Override
    protected synchronized void print(int priority, String tag, String message, Throwable t) {
        mCacheLogs.add(new LogItem.Builder()
                .setPriority(priority)
                .setTag(tag)
                .setMessage(message)
                .setThrowable(t)
                .setTimeStamp(System.currentTimeMillis())
                .build());
    }

    @Override
    public String getType() {
        return TYPE;
    }

    public synchronized List<LogItem> getCacheLogsAndClear() {
        if (mCacheLogs.isEmpty()) {
            return Collections.emptyList();
        }

        List<LogItem> copyList = new ArrayList<>(mCacheLogs.size());
        copyList.addAll(mCacheLogs);
        mCacheLogs.clear();

        return copyList;
    }

    public synchronized List<LogItem> getCacheLogs() {
        if (mCacheLogs.isEmpty()) {
            return Collections.emptyList();
        }

        List<LogItem> copyList = new ArrayList<>(mCacheLogs.size());
        copyList.addAll(mCacheLogs);

        return copyList;
    }
}
