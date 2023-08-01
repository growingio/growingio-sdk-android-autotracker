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
package com.growingio.android.debugger;

import android.util.Log;

import com.growingio.android.sdk.track.SDKConfig;
import com.growingio.android.sdk.track.log.LogItem;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class LoggerDataMessage {

    public static final String MSG_TYPE = "logger_data";

    private final List<DebuggerLogItem> mLogs;
    private final String mMsgType;

    private LoggerDataMessage(List<DebuggerLogItem> logs) {
        mMsgType = MSG_TYPE;
        mLogs = logs;
    }

    public static LoggerDataMessage createTrackMessage(List<LogItem> logs) {
        List<DebuggerLogItem> list = new ArrayList<>(logs.size());
        for (int i = 0; i < logs.size(); i++) {
            LogItem logItem = logs.get(i);
            list.add(DebuggerLogItem.create(priorityToState(logItem.getPriority()), "subType", logItem.getMessage(), logItem.getTimeStamp()));
        }
        return new LoggerDataMessage(list);
    }

    public JSONObject toJSONObject() {
        JSONObject json = new JSONObject();
        try {
            json.put("msgType", mMsgType);
            json.put("sdkVersion", SDKConfig.SDK_VERSION);
            JSONArray logs = new JSONArray();
            for (DebuggerLogItem logItem : mLogs) {
                logs.put(logItem.toJSONObject());
            }
            json.put("data", logs);
        } catch (JSONException ignored) {
        }
        return json;
    }

    private static String priorityToState(int priority) {
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

    public static class DebuggerLogItem {

        private final String mType;
        private final String mSubType;
        private final String mMessage;
        private final long mTime;

        public DebuggerLogItem(String type, String subType, String message, long time) {
            mType = type;
            mSubType = subType;
            mMessage = message;
            mTime = time;
        }

        public static DebuggerLogItem create(String type, String subType, String message, long time) {
            return new DebuggerLogItem(type, subType, message, time);
        }

        public JSONObject toJSONObject() {
            JSONObject json = new JSONObject();
            try {
                json.put("type", mType);
                json.put("subType", mSubType);
                json.put("message", mMessage);
                json.put("time", mTime);
            } catch (JSONException ignored) {
            }
            return json;
        }
    }
}
