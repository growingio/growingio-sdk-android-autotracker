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
import android.text.TextUtils;
import android.util.Log;

import java.io.PrintWriter;
import java.io.StringWriter;

public abstract class BaseLogger implements ILogger {
    @Override
    public void v(String tag, String message, Object... args) {
        prepareLog(Log.VERBOSE, tag, null, message, args);
    }

    @Override
    public void v(String tag, Throwable t, String message, Object... args) {
        prepareLog(Log.VERBOSE, tag, t, message, args);
    }

    @Override
    public void v(String tag, Throwable t) {
        prepareLog(Log.VERBOSE, tag, t, null);
    }

    @Override
    public void d(String tag, String message, Object... args) {
        prepareLog(Log.DEBUG, tag, null, message, args);
    }

    @Override
    public void d(String tag, Throwable t, String message, Object... args) {
        prepareLog(Log.DEBUG, tag, t, message, args);
    }

    @Override
    public void d(String tag, Throwable t) {
        prepareLog(Log.DEBUG, tag, t, null);
    }

    @Override
    public void i(String tag, String message, Object... args) {
        prepareLog(Log.INFO, tag, null, message, args);
    }

    @Override
    public void i(String tag, Throwable t, String message, Object... args) {
        prepareLog(Log.INFO, tag, t, message, args);
    }

    @Override
    public void i(String tag, Throwable t) {
        prepareLog(Log.INFO, tag, t, null);
    }

    @Override
    public void w(String tag, String message, Object... args) {
        prepareLog(Log.WARN, tag, null, message, args);
    }

    @Override
    public void w(String tag, Throwable t, String message, Object... args) {
        prepareLog(Log.WARN, tag, t, message, args);
    }

    @Override
    public void w(String tag, Throwable t) {
        prepareLog(Log.WARN, tag, t, null);
    }

    @Override
    public void e(String tag, String message, Object... args) {
        prepareLog(Log.ERROR, tag, null, message, args);
    }

    @Override
    public void e(String tag, Throwable t, String message, Object... args) {
        prepareLog(Log.ERROR, tag, t, message, args);
    }

    @Override
    public void e(String tag, Throwable t) {
        prepareLog(Log.ERROR, tag, t, null);
    }

    @Override
    public void wtf(String tag, String message, Object... args) {
        prepareLog(Log.ASSERT, tag, null, message, args);
    }

    @Override
    public void wtf(String tag, Throwable t, String message, Object... args) {
        prepareLog(Log.ASSERT, tag, t, message, args);
    }

    @Override
    public void wtf(String tag, Throwable t) {
        prepareLog(Log.ASSERT, tag, t, null);
    }

    private void prepareLog(int priority, String tag, Throwable t, String message, Object... args) {
        if (TextUtils.isEmpty(tag)) {
            return;
        }

        if (message != null && message.length() == 0) {
            message = null;
        }
        if (message == null) {
            if (t == null) {
                return;
            }
            message = getStackTraceString(t);
        } else {
            if (args != null && args.length > 0) {
                message = formatMessage(message, args);
            }
            if (t != null) {
                message += "\n" + getStackTraceString(t);
            }
        }

        print(priority, tag, message, t);
    }

    private String formatMessage(@NonNull String message, @NonNull Object[] args) {
        try {
            return String.format(message, args);
        } catch (Exception ignored) {
        }

        return message;
    }

    private String getStackTraceString(Throwable t) {
        StringWriter sw = new StringWriter(256);
        PrintWriter pw = new PrintWriter(sw, false);
        t.printStackTrace(pw);
        pw.flush();
        return sw.toString();
    }

    protected abstract void print(int priority, @NonNull String tag, @NonNull String message,
                                  @Nullable Throwable t);
}
