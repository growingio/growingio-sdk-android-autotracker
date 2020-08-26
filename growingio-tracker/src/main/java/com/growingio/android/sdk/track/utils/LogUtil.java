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

package com.growingio.android.sdk.track.utils;

import android.support.annotation.AnyThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * sdk中大部分操作都不发生在ui线程，性能对卡顿的影响较小
 * 若发现需要在ui进程中大量打印日志，可以在LogUtil中增加线程控制或各个Util控制单独线程
 * <p>
 * 未设置tag默认使用LogUtil的TAG，tag为null不打印日志
 * UTILS作用作为所有util的代理，在有必要的情况下可以退化为单个util
 */
public class LogUtil {
    private static final String TAG = "GIO.LogUtil";
    private static final String LINE_SEPARATOR = System.getProperty("line.separator");
    /**
     * volatile仅对于数组和对象来说是地址可见性，保证多线程需要在添加util后更新数组地址
     */
    private static final LogUtil.Util[] UTILS_ARRAY_EMPTY = new LogUtil.Util[0];
    private static final List<Util> UTILS_LIST = new ArrayList<>();
    static volatile LogUtil.Util[] sUtilsArray = UTILS_ARRAY_EMPTY;
    private static final LogUtil.Util UTILS = new LogUtil.Util() {
        @Override
        public void v(String message, Object... args) {
            LogUtil.Util[] utils = sUtilsArray;
            for (LogUtil.Util util : utils) {
                util.v(message, args);
            }
        }

        @Override
        public void v(String tag, String message, Object... args) {
            LogUtil.Util[] utils = sUtilsArray;
            for (LogUtil.Util util : utils) {
                util.v(tag, message, args);
            }
        }

        @Override
        public void v(Throwable t, String message, Object... args) {
            LogUtil.Util[] utils = sUtilsArray;
            for (LogUtil.Util util : utils) {
                util.v(t, message, args);
            }
        }

        @Override
        public void v(String tag, Throwable t, String message, Object... args) {
            LogUtil.Util[] utils = sUtilsArray;
            for (LogUtil.Util util : utils) {
                util.v(tag, t, message, args);
            }
        }

        @Override
        public void v(Throwable t) {
            LogUtil.Util[] utils = sUtilsArray;
            for (LogUtil.Util util : utils) {
                util.v(t);
            }
        }

        @Override
        public void v(String tag, Throwable t) {
            LogUtil.Util[] utils = sUtilsArray;
            for (LogUtil.Util util : utils) {
                util.v(tag, t);
            }
        }

        @Override
        public void d(String message, Object... args) {
            LogUtil.Util[] utils = sUtilsArray;
            for (LogUtil.Util util : utils) {
                util.d(message, args);
            }
        }

        @Override
        public void d(String tag, String message, Object... args) {
            LogUtil.Util[] utils = sUtilsArray;
            for (LogUtil.Util util : utils) {
                util.d(tag, message, args);
            }
        }

        @Override
        public void d(Throwable t, String message, Object... args) {
            LogUtil.Util[] utils = sUtilsArray;
            for (LogUtil.Util util : utils) {
                util.d(t, message, args);
            }
        }

        @Override
        public void d(String tag, Throwable t, String message, Object... args) {
            LogUtil.Util[] utils = sUtilsArray;
            for (LogUtil.Util util : utils) {
                util.d(tag, t, message, args);
            }
        }

        @Override
        public void d(Throwable t) {
            LogUtil.Util[] utils = sUtilsArray;
            for (LogUtil.Util util : utils) {
                util.d(t);
            }
        }

        @Override
        public void d(String tag, Throwable t) {
            LogUtil.Util[] utils = sUtilsArray;
            for (LogUtil.Util util : utils) {
                util.d(tag, t);
            }
        }

        @Override
        public void i(String message, Object... args) {
            LogUtil.Util[] utils = sUtilsArray;
            for (LogUtil.Util util : utils) {
                util.i(message, args);
            }
        }

        @Override
        public void i(String tag, String message, Object... args) {
            LogUtil.Util[] utils = sUtilsArray;
            for (LogUtil.Util util : utils) {
                util.i(tag, message, args);
            }
        }

        @Override
        public void i(Throwable t, String message, Object... args) {
            LogUtil.Util[] utils = sUtilsArray;
            for (LogUtil.Util util : utils) {
                util.i(t, message, args);
            }
        }

        @Override
        public void i(String tag, Throwable t, String message, Object... args) {
            LogUtil.Util[] utils = sUtilsArray;
            for (LogUtil.Util util : utils) {
                util.i(tag, t, message, args);
            }
        }

        @Override
        public void i(Throwable t) {
            LogUtil.Util[] utils = sUtilsArray;
            for (LogUtil.Util util : utils) {
                util.i(t);
            }
        }

        @Override
        public void i(String tag, Throwable t) {
            LogUtil.Util[] utils = sUtilsArray;
            for (LogUtil.Util util : utils) {
                util.i(tag, t);
            }
        }

        @Override
        public void w(String message, Object... args) {
            LogUtil.Util[] utils = sUtilsArray;
            for (LogUtil.Util util : utils) {
                util.w(message, args);
            }
        }

        @Override
        public void w(String tag, String message, Object... args) {
            LogUtil.Util[] utils = sUtilsArray;
            for (LogUtil.Util util : utils) {
                util.w(tag, message, args);
            }
        }

        @Override
        public void w(Throwable t, String message, Object... args) {
            LogUtil.Util[] utils = sUtilsArray;
            for (LogUtil.Util util : utils) {
                util.w(t, message, args);
            }
        }

        @Override
        public void w(String tag, Throwable t, String message, Object... args) {
            LogUtil.Util[] utils = sUtilsArray;
            for (LogUtil.Util util : utils) {
                util.w(tag, t, message, args);
            }
        }

        @Override
        public void w(Throwable t) {
            LogUtil.Util[] utils = sUtilsArray;
            for (LogUtil.Util util : utils) {
                util.w(t);
            }
        }

        @Override
        public void w(String tag, Throwable t) {
            LogUtil.Util[] utils = sUtilsArray;
            for (LogUtil.Util util : utils) {
                util.w(tag, t);
            }
        }

        @Override
        public void e(String message, Object... args) {
            LogUtil.Util[] utils = sUtilsArray;
            for (LogUtil.Util util : utils) {
                util.e(message, args);
            }
        }

        @Override
        public void e(String tag, String message, Object... args) {
            LogUtil.Util[] utils = sUtilsArray;
            for (LogUtil.Util util : utils) {
                util.e(tag, message, args);
            }
        }

        @Override
        public void e(Throwable t, String message, Object... args) {
            LogUtil.Util[] utils = sUtilsArray;
            for (LogUtil.Util util : utils) {
                util.e(t, message, args);
            }
        }

        @Override
        public void e(String tag, Throwable t, String message, Object... args) {
            LogUtil.Util[] utils = sUtilsArray;
            for (LogUtil.Util util : utils) {
                util.e(tag, t, message, args);
            }
        }

        @Override
        public void e(Throwable t) {
            LogUtil.Util[] utils = sUtilsArray;
            for (LogUtil.Util util : utils) {
                util.e(t);
            }
        }

        @Override
        public void e(String tag, Throwable t) {
            LogUtil.Util[] utils = sUtilsArray;
            for (LogUtil.Util util : utils) {
                util.e(tag, t);
            }
        }

        @Override
        public void wtf(String message, Object... args) {
            LogUtil.Util[] utils = sUtilsArray;
            for (LogUtil.Util util : utils) {
                util.wtf(message, args);
            }
        }

        @Override
        public void wtf(String tag, String message, Object... args) {
            LogUtil.Util[] utils = sUtilsArray;
            for (LogUtil.Util util : utils) {
                util.wtf(tag, message, args);
            }
        }

        @Override
        public void wtf(Throwable t, String message, Object... args) {
            LogUtil.Util[] utils = sUtilsArray;
            for (LogUtil.Util util : utils) {
                util.wtf(t, message, args);
            }
        }

        @Override
        public void wtf(String tag, Throwable t, String message, Object... args) {
            LogUtil.Util[] utils = sUtilsArray;
            for (LogUtil.Util util : utils) {
                util.wtf(tag, t, message, args);
            }
        }

        @Override
        public void wtf(Throwable t) {
            LogUtil.Util[] utils = sUtilsArray;
            for (LogUtil.Util util : utils) {
                util.wtf(t);
            }
        }

        @Override
        public void wtf(String tag, Throwable t) {
            LogUtil.Util[] utils = sUtilsArray;
            for (LogUtil.Util util : utils) {
                util.wtf(t);
            }
        }

        @Override
        public void custom(int priority, String message, Object... args) {
            LogUtil.Util[] utils = sUtilsArray;
            for (LogUtil.Util util : utils) {
                util.custom(priority, message, args);
            }
        }

        @Override
        public void custom(int priority, String tag, String message, Object... args) {
            LogUtil.Util[] utils = sUtilsArray;
            for (LogUtil.Util util : utils) {
                util.custom(priority, tag, message, args);
            }
        }

        @Override
        public void custom(int priority, Throwable t, String message, Object... args) {
            LogUtil.Util[] utils = sUtilsArray;
            for (LogUtil.Util util : utils) {
                util.custom(priority, t, message, args);
            }
        }

        @Override
        public void custom(int priority, String tag, Throwable t, String message, Object... args) {
            LogUtil.Util[] utils = sUtilsArray;
            for (LogUtil.Util util : utils) {
                util.custom(priority, tag, t, message, args);
            }
        }

        @Override
        public void custom(int priority, Throwable t) {
            LogUtil.Util[] utils = sUtilsArray;
            for (LogUtil.Util util : utils) {
                util.custom(priority, t);
            }
        }

        @Override
        public void custom(int priority, String tag, Throwable t) {
            LogUtil.Util[] utils = sUtilsArray;
            for (LogUtil.Util util : utils) {
                util.custom(priority, tag, t);
            }
        }

        @Override
        protected void log(int priority, @Nullable String tag, @NonNull String message, @Nullable Throwable t) {
            // DO NOTHING
        }
    };

    private LogUtil() {
        throw new AssertionError("No instances.");
    }

    @AnyThread
    public static void v(String message, Object... args) {
        UTILS.v(message, args);
    }

    @AnyThread
    public static void v(String tag, String message, Object... args) {
        UTILS.v(tag, message, args);
    }

    @AnyThread
    public static void v(Throwable t, String message, Object... args) {
        UTILS.v(t, message, args);
    }

    @AnyThread
    public static void v(String tag, Throwable t, String message, Object... args) {
        UTILS.v(tag, t, message, args);
    }

    @AnyThread
    public static void v(Throwable t) {
        UTILS.v(t);
    }

    @AnyThread
    public static void v(String tag, Throwable t) {
        UTILS.v(tag, t);
    }

    @AnyThread
    public static void d(String message, Object... args) {
        UTILS.d(message, args);
    }

    @AnyThread
    public static void d(String tag, String message, Object... args) {
        UTILS.d(tag, message, args);
    }

    @AnyThread
    public static void d(Throwable t, String message, Object... args) {
        UTILS.d(t, message, args);
    }

    @AnyThread
    public static void d(String tag, Throwable t, String message, Object... args) {
        UTILS.d(tag, t, message, args);
    }

    @AnyThread
    public static void d(Throwable t) {
        UTILS.d(t);
    }

    @AnyThread
    public static void d(String tag, Throwable t) {
        UTILS.d(tag, t);
    }

    @AnyThread
    public static void i(String message, Object... args) {
        UTILS.i(message, args);
    }

    @AnyThread
    public static void i(String tag, String message, Object... args) {
        UTILS.i(tag, message, args);
    }

    @AnyThread
    public static void i(Throwable t, String message, Object... args) {
        UTILS.i(t, message, args);
    }

    @AnyThread
    public static void i(String tag, Throwable t, String message, Object... args) {
        UTILS.i(tag, t, message, args);
    }

    @AnyThread
    public static void i(Throwable t) {
        UTILS.i(t);
    }

    @AnyThread
    public static void i(String tag, Throwable t) {
        UTILS.i(tag, t);
    }

    @AnyThread
    public static void w(String message, Object... args) {
        UTILS.w(message, args);
    }

    @AnyThread
    public static void w(String tag, String message, Object... args) {
        UTILS.w(tag, message, args);
    }

    @AnyThread
    public static void w(Throwable t, String message, Object... args) {
        UTILS.w(t, message, args);
    }

    @AnyThread
    public static void w(String tag, Throwable t, String message, Object... args) {
        UTILS.w(tag, t, message, args);
    }

    @AnyThread
    public static void w(Throwable t) {
        UTILS.w(t);
    }

    @AnyThread
    public static void w(String tag, Throwable t) {
        UTILS.w(tag, t);
    }

    @AnyThread
    public static void e(String message, Object... args) {
        UTILS.e(message, args);
    }

    @AnyThread
    public static void e(String tag, String message, Object... args) {
        UTILS.e(tag, message, args);
    }

    @AnyThread
    public static void e(Throwable t, String message, Object... args) {
        UTILS.e(t, message, args);
    }

    @AnyThread
    public static void e(String tag, Throwable t, String message, Object... args) {
        UTILS.e(tag, t, message, args);
    }

    @AnyThread
    public static void e(Throwable t) {
        UTILS.e(t);
    }

    @AnyThread
    public static void e(String tag, Throwable t) {
        UTILS.e(tag, t);
    }

    @AnyThread
    public static void wtf(String message, Object... args) {
        UTILS.wtf(message, args);
    }

    @AnyThread
    public static void wtf(String tag, String message, Object... args) {
        UTILS.wtf(tag, message, args);
    }

    @AnyThread
    public static void wtf(Throwable t, String message, Object... args) {
        UTILS.wtf(t, message, args);
    }

    @AnyThread
    public static void wtf(String tag, Throwable t, String message, Object... args) {
        UTILS.wtf(tag, t, message, args);
    }

    @AnyThread
    public static void wtf(Throwable t) {
        UTILS.wtf(t);
    }

    @AnyThread
    public static void wtf(String tag, Throwable t) {
        UTILS.wtf(tag, t);
    }

    @AnyThread
    public static void custom(int priority, String message, Object... args) {
        UTILS.custom(priority, message, args);
    }

    @AnyThread
    public static void custom(int priority, String tag, String message, Object... args) {
        UTILS.custom(priority, tag, message, args);
    }

    @AnyThread
    public static void custom(int priority, Throwable t, String message, Object... args) {
        UTILS.custom(priority, t, message, args);
    }

    @AnyThread
    public static void custom(int priority, String tag, Throwable t, String message, Object... args) {
        UTILS.custom(priority, tag, t, message, args);
    }

    @AnyThread
    public static void custom(int priority, Throwable t) {
        UTILS.custom(priority, t);
    }

    @AnyThread
    public static void custom(int priority, String tag, Throwable t) {
        UTILS.custom(priority, tag, t);
    }

    @AnyThread
    public static void add(@NonNull LogUtil.Util util) {
        if (util == null || util == UTILS) {
            return;
        }

        synchronized (UTILS_LIST) {
            if (!UTILS_LIST.contains(util)) {
                UTILS_LIST.add(util);
                sUtilsArray = UTILS_LIST.toArray(new LogUtil.Util[UTILS_LIST.size()]);
            }
        }
    }

    @AnyThread
    public static void add(@NonNull LogUtil.Util... utils) {
        if (utils == null) {
            return;
        }
        for (LogUtil.Util util : utils) {
            if (util == null || util == UTILS) {
                return;
            }
        }
        synchronized (UTILS_LIST) {
            for (LogUtil.Util util : utils) {
                if (!UTILS_LIST.contains(util)) {
                    UTILS_LIST.add(util);
                }
            }
            sUtilsArray = UTILS_LIST.toArray(new LogUtil.Util[UTILS_LIST.size()]);
        }
    }

    @AnyThread
    public static void remove(@NonNull LogUtil.Util util) {
        if (util == null || util == UTILS) {
            return;
        }

        synchronized (UTILS_LIST) {
            if (UTILS_LIST.remove(util)) {
                sUtilsArray = UTILS_LIST.toArray(new LogUtil.Util[UTILS_LIST.size()]);
            }
        }
    }

    @AnyThread
    public static void removeAll() {
        synchronized (UTILS_LIST) {
            UTILS_LIST.clear();
            sUtilsArray = UTILS_ARRAY_EMPTY;
        }
    }

    @AnyThread
    public static int utilCount() {
        synchronized (UTILS_LIST) {
            return UTILS_LIST.size();
        }
    }

    private static void printLine(String tag, boolean isTop) {
        if (isTop) {
            d(tag, "╔═══════════════════════════════════════════════════════════════════════════════════════");
        } else {
            d(tag, "╚═══════════════════════════════════════════════════════════════════════════════════════");
        }
    }

    public static void printJson(String tag, String headString, String jsonStr) {

        String message;
        try {
            if (jsonStr.startsWith("{")) {
                JSONObject jsonObject = new JSONObject(jsonStr);
                message = jsonObject.toString(2); //最重要的方法，就一行，返回格式化的json字符串，其中的数字4是缩进字符数
            } else if (jsonStr.startsWith("[")) {
                JSONArray jsonArray = new JSONArray(jsonStr);
                message = jsonArray.toString(2);
            } else {
                message = jsonStr;
            }
        } catch (JSONException e) {
            message = jsonStr;
        }
        printLine(tag, true);
        message = headString + LINE_SEPARATOR + message;
        String[] lines = message.split(LINE_SEPARATOR);
        for (String line : lines) {
            d(tag, "║ " + line);
        }
        printLine(tag, false);
    }

    public static abstract class Util {
        public void v(String message, Object... args) {
            this.prepareLog(Log.VERBOSE, null, message, args);
        }

        public void v(String tag, String message, Object... args) {
            this.prepareLog(Log.VERBOSE, tag, null, message, args);
        }

        public void v(Throwable t, String message, Object... args) {
            this.prepareLog(Log.VERBOSE, t, message, args);
        }

        public void v(String tag, Throwable t, String message, Object... args) {
            this.prepareLog(Log.VERBOSE, tag, t, message, args);
        }

        public void v(Throwable t) {
            this.prepareLog(Log.VERBOSE, t, null);
        }

        public void v(String tag, Throwable t) {
            this.prepareLog(Log.VERBOSE, tag, t, null);
        }

        public void d(String message, Object... args) {
            this.prepareLog(Log.DEBUG, null, message, args);
        }

        public void d(String tag, String message, Object... args) {
            this.prepareLog(Log.DEBUG, tag, null, message, args);
        }

        public void d(Throwable t, String message, Object... args) {
            this.prepareLog(Log.DEBUG, t, message, args);
        }

        public void d(String tag, Throwable t, String message, Object... args) {
            this.prepareLog(Log.DEBUG, tag, t, message, args);
        }

        public void d(Throwable t) {
            this.prepareLog(Log.DEBUG, t, null);
        }

        public void d(String tag, Throwable t) {
            this.prepareLog(Log.DEBUG, tag, t, null);
        }

        public void i(String message, Object... args) {
            this.prepareLog(Log.INFO, null, message, args);
        }

        public void i(String tag, String message, Object... args) {
            this.prepareLog(Log.INFO, tag, null, message, args);
        }

        public void i(Throwable t, String message, Object... args) {
            this.prepareLog(Log.INFO, t, message, args);
        }

        public void i(String tag, Throwable t, String message, Object... args) {
            this.prepareLog(Log.INFO, tag, t, message, args);
        }

        public void i(Throwable t) {
            this.prepareLog(Log.INFO, t, null);
        }

        public void i(String tag, Throwable t) {
            this.prepareLog(Log.INFO, tag, t, null);
        }

        public void w(String message, Object... args) {
            prepareLog(Log.WARN, null, message, args);
        }

        public void w(String tag, String message, Object... args) {
            prepareLog(Log.WARN, tag, null, message, args);
        }

        public void w(Throwable t, String message, Object... args) {
            prepareLog(Log.WARN, t, message, args);
        }

        public void w(String tag, Throwable t, String message, Object... args) {
            prepareLog(Log.WARN, tag, t, message, args);
        }

        public void w(Throwable t) {
            prepareLog(Log.WARN, t, null);
        }

        public void w(String tag, Throwable t) {
            prepareLog(Log.WARN, tag, t, null);
        }

        public void e(String message, Object... args) {
            prepareLog(Log.ERROR, null, message, args);
        }

        public void e(String tag, String message, Object... args) {
            prepareLog(Log.ERROR, tag, null, message, args);
        }

        public void e(Throwable t, String message, Object... args) {
            prepareLog(Log.ERROR, t, message, args);
        }

        public void e(String tag, Throwable t, String message, Object... args) {
            prepareLog(Log.ERROR, tag, t, message, args);
        }

        public void e(Throwable t) {
            prepareLog(Log.ERROR, t, null);
        }

        public void e(String tag, Throwable t) {
            prepareLog(Log.ERROR, tag, t, null);
        }

        public void wtf(String message, Object... args) {
            prepareLog(Log.ASSERT, null, message, args);
        }

        public void wtf(String tag, String message, Object... args) {
            prepareLog(Log.ASSERT, tag, null, message, args);
        }

        public void wtf(Throwable t, String message, Object... args) {
            prepareLog(Log.ASSERT, t, message, args);
        }

        public void wtf(String tag, Throwable t, String message, Object... args) {
            prepareLog(Log.ASSERT, tag, t, message, args);
        }

        public void wtf(Throwable t) {
            prepareLog(Log.ASSERT, t, null);
        }

        public void wtf(String tag, Throwable t) {
            prepareLog(Log.ASSERT, tag, t, null);
        }

        public void custom(int priority, String message, Object... args) {
            prepareLog(priority, null, message, args);
        }

        public void custom(int priority, String tag, String message, Object... args) {
            prepareLog(priority, tag, null, message, args);
        }

        public void custom(int priority, Throwable t, String message, Object... args) {
            prepareLog(priority, t, message, args);
        }

        public void custom(int priority, String tag, Throwable t, String message, Object... args) {
            prepareLog(priority, tag, t, message, args);
        }

        public void custom(int priority, Throwable t) {
            prepareLog(priority, t, null);
        }

        public void custom(int priority, String tag, Throwable t) {
            prepareLog(priority, tag, t, null);
        }

        private void prepareLog(int priority, Throwable t, String message, Object... args) {
            prepareLog(priority, TAG, t, message, args);
        }

        private void prepareLog(int priority, String tag, Throwable t, String message, Object... args) {
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

            log(priority, tag, message, t);
        }

        protected String formatMessage(@NonNull String message, @NonNull Object[] args) {
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

        protected abstract void log(int priority, @Nullable String tag, @NonNull String message,
                                    @Nullable Throwable t);
    }

    public static class DebugUtil extends LogUtil.Util {
        private static final int MAX_LOG_LENGTH = 4000;

        private DebugUtil() {
        }

        public static DebugUtil getInstance() {
            return SingleInstance.INSTANCE;
        }

        @Override
        protected void log(int priority, String tag, @NonNull String message, Throwable t) {
            if (priority > Log.ASSERT) {
                return;
            }

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

        private static class SingleInstance {
            private static final DebugUtil INSTANCE = new DebugUtil();

            private SingleInstance() {
            }
        }
    }

    public static class ReleaseUitl extends DebugUtil {
        private ReleaseUitl() {
        }

        public static ReleaseUitl getInstance() {
            return SingleInstance.INSTANCE;
        }

        @Override
        protected void log(int priority, String tag, @NonNull String message, Throwable t) {
            if (priority < Log.ERROR) {
                return;
            }

            super.log(priority, tag, message, t);
        }

        private static class SingleInstance {
            private static final ReleaseUitl INSTANCE = new ReleaseUitl();

            private SingleInstance() {
            }
        }
    }
}
