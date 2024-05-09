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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Logger {
    private static final String LINE_SEPARATOR = System.getProperty("line.separator");

    private static final ConcurrentHashMap<String, ILogger> LOGGERS = new ConcurrentHashMap<>();

    static {
        addLogger(new ErrorLogger());
        addLogger(new CacheLogger());
    }

    private Logger() {
        throw new AssertionError("No instances.");
    }

    public static void addLogger(ILogger logger) {
        LOGGERS.put(logger.getType(), logger);
    }

    public static void removeLogger(ILogger logger) {
        LOGGERS.remove(logger.getType());
    }

    public static ILogger getLogger(String loggerType) {
        return LOGGERS.get(loggerType);
    }

    public static void v(String tag, String message, Object... args) {
        Iterator<Map.Entry<String, ILogger>> iterator = LOGGERS.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, ILogger> entry = iterator.next();
            entry.getValue().v(tag, message, args);
        }
    }

    public static void v(String tag, Throwable t, String message, Object... args) {
        Iterator<Map.Entry<String, ILogger>> iterator = LOGGERS.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, ILogger> entry = iterator.next();
            entry.getValue().v(tag, t, message, args);
        }
    }

    public static void v(String tag, Throwable t) {
        Iterator<Map.Entry<String, ILogger>> iterator = LOGGERS.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, ILogger> entry = iterator.next();
            entry.getValue().v(tag, t);
        }
    }

    public static void d(String tag, String message, Object... args) {
        Iterator<Map.Entry<String, ILogger>> iterator = LOGGERS.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, ILogger> entry = iterator.next();
            entry.getValue().d(tag, message, args);
        }
    }

    public static void d(String tag, Throwable t, String message, Object... args) {
        Iterator<Map.Entry<String, ILogger>> iterator = LOGGERS.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, ILogger> entry = iterator.next();
            entry.getValue().d(tag, t, message, args);
        }
    }

    public static void d(String tag, Throwable t) {
        Iterator<Map.Entry<String, ILogger>> iterator = LOGGERS.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, ILogger> entry = iterator.next();
            entry.getValue().d(tag, t);
        }
    }

    public static void i(String tag, String message, Object... args) {
        Iterator<Map.Entry<String, ILogger>> iterator = LOGGERS.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, ILogger> entry = iterator.next();
            entry.getValue().i(tag, message, args);
        }
    }

    public static void i(String tag, Throwable t, String message, Object... args) {
        Iterator<Map.Entry<String, ILogger>> iterator = LOGGERS.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, ILogger> entry = iterator.next();
            entry.getValue().i(tag, t, message, args);
        }
    }

    public static void i(String tag, Throwable t) {
        Iterator<Map.Entry<String, ILogger>> iterator = LOGGERS.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, ILogger> entry = iterator.next();
            entry.getValue().i(tag, t);
        }
    }

    public static void w(String tag, String message, Object... args) {
        Iterator<Map.Entry<String, ILogger>> iterator = LOGGERS.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, ILogger> entry = iterator.next();
            entry.getValue().w(tag, message, args);
        }
    }

    public static void w(String tag, Throwable t, String message, Object... args) {
        Iterator<Map.Entry<String, ILogger>> iterator = LOGGERS.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, ILogger> entry = iterator.next();
            entry.getValue().w(tag, t, message, args);
        }
    }

    public static void w(String tag, Throwable t) {
        Iterator<Map.Entry<String, ILogger>> iterator = LOGGERS.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, ILogger> entry = iterator.next();
            entry.getValue().w(tag, t);
        }
    }

    public static void e(String tag, String message, Object... args) {
        Iterator<Map.Entry<String, ILogger>> iterator = LOGGERS.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, ILogger> entry = iterator.next();
            entry.getValue().e(tag, message, args);
        }
    }

    public static void e(String tag, Throwable t, String message, Object... args) {
        Iterator<Map.Entry<String, ILogger>> iterator = LOGGERS.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, ILogger> entry = iterator.next();
            entry.getValue().e(tag, t, message, args);
        }
    }

    public static void e(String tag, Throwable t) {
        Iterator<Map.Entry<String, ILogger>> iterator = LOGGERS.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, ILogger> entry = iterator.next();
            entry.getValue().e(tag, t);
        }
    }

    public static void wtf(String tag, String message, Object... args) {
        Iterator<Map.Entry<String, ILogger>> iterator = LOGGERS.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, ILogger> entry = iterator.next();
            entry.getValue().wtf(tag, message, args);
        }
    }

    public static void wtf(String tag, Throwable t, String message, Object... args) {
        Iterator<Map.Entry<String, ILogger>> iterator = LOGGERS.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, ILogger> entry = iterator.next();
            entry.getValue().wtf(tag, t, message, args);
        }
    }

    public static void wtf(String tag, Throwable t) {
        Iterator<Map.Entry<String, ILogger>> iterator = LOGGERS.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, ILogger> entry = iterator.next();
            entry.getValue().wtf(tag, t);
        }
    }

    private static String getLine(boolean isTop) {
        if (isTop) {
            return "╔════════════════════════════════════════════════════";
        } else {
            return "╚════════════════════════════════════════════════════";
        }
    }

    public static void printJson(String tag, String headString, String jsonStr) {
        String message;
        try {
            if (jsonStr.length() > 10 * 1024) {
                message = jsonStr.substring(0, 10 * 1024) + "...(json is too long, it's length is " + jsonStr.getBytes().length + ")";
            } else {
                if (jsonStr.startsWith("{")) {
                    JSONObject jsonObject = new JSONObject(jsonStr);
                    message = jsonObject.toString(2);
                    message = message.replace("\\/", "/");
                } else if (jsonStr.startsWith("[")) {
                    JSONArray jsonArray = new JSONArray(jsonStr);
                    message = jsonArray.toString(2);
                    message = message.replace("\\/", "/");
                } else {
                    message = jsonStr;
                }
            }
        } catch (JSONException e) {
            message = jsonStr;
        }
        StringBuilder beautifulMsg = new StringBuilder(getLine(true)).append(LINE_SEPARATOR);
        String[] lines = message.split(LINE_SEPARATOR);
        for (String line : lines) {
            beautifulMsg.append("║ ").append(line).append(LINE_SEPARATOR);
        }
        beautifulMsg.append(getLine(false));

        message = headString + LINE_SEPARATOR + beautifulMsg.toString();
        d(tag, message);
    }
}
