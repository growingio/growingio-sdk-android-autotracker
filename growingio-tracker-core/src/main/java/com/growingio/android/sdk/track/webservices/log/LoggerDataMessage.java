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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayDeque;
import java.util.Queue;

public class LoggerDataMessage {

    public static final String MSG_TYPE = "logger_data";

    private Queue<LogItem> mLogs;
    private final String mMsgType;

    private LoggerDataMessage(Queue<LogItem> logs) {
        mMsgType = MSG_TYPE;
        mLogs = logs;
    }

    public static LogItem createLogItem(String type, String subType, String message, String time) {
        return LogItem.create(type, subType, message, time);
    }

    public static LoggerDataMessage createMessage(Queue<LogItem> logs) {
        return new LoggerDataMessage(logs);
    }

    public static LoggerDataMessage createMessage(String type, String subType, String message, String time) {
        Queue<LogItem> logs = new ArrayDeque<>(1);
        logs.add(createLogItem(type, subType, message, time));
        return new LoggerDataMessage(logs);
    }

    public JSONObject toJSONObject() {
        JSONObject json = new JSONObject();
        try {
            json.put("msgType", mMsgType);
            JSONArray logs = new JSONArray();
            for (LogItem logItem : mLogs) {
                logs.put(logItem.toJSONObject());
            }
            json.put("logs", logs);
        } catch (JSONException ignored) {
        }
        return json;
    }

    public static class LogItem {

        private final String mType;
        private final String mSubType;
        private final String mMessage;
        private final String mTime;

        public LogItem(String type, String subType, String message, String time) {
            mType = type;
            mSubType = subType;
            mMessage = message;
            mTime = time;
        }

        public static LogItem create(String type, String subType, String message, String time) {
            return new LogItem(type, subType, message, time);
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
