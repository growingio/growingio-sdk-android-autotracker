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
package com.growingio.android.json;

import android.os.Build;

import com.growingio.android.sdk.track.events.base.BaseEvent;
import com.growingio.android.sdk.track.log.Logger;
import com.growingio.android.sdk.track.middleware.format.EventFormatData;
import com.growingio.android.sdk.track.middleware.format.EventByteArray;
import com.growingio.android.sdk.track.middleware.GEvent;
import com.growingio.android.sdk.track.middleware.format.FormatDataFetcher;
import com.growingio.android.sdk.track.providers.EventBuilderProvider;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * <p>
 *
 * @author cpacm 2021/5/13
 */
public class JsonDataFetcher implements FormatDataFetcher<EventByteArray> {
    private static final String TAG = "JsonDataFetcher";

    private final EventFormatData eventData;

    public JsonDataFetcher(EventFormatData eventData) {
        this.eventData = eventData;
    }

    @Override
    public EventByteArray executeData() {
        try {
            if (eventData.getEventOp() == EventFormatData.DATA_FORMAT_EVENT) {
                assertCondition(eventData.getEvent() != null, "leak necessary event");
                return format(eventData.getEvent());
            } else if (eventData.getEventOp() == EventFormatData.DATA_FORMAT_MERGE) {
                assertCondition(eventData.getEvents() != null, "leak necessary events");
                return merge(eventData.getEvents());
            }
            return new EventByteArray(null);
        } catch (IllegalArgumentException e) {
            Logger.e(TAG, e);
            return new EventByteArray(null);
        }
    }

    @Override
    public EventByteArray format(GEvent gEvent) {
        if (gEvent instanceof BaseEvent) {
            JSONObject eventJson = EventBuilderProvider.toJson((BaseEvent) gEvent);
            return new EventByteArray(eventJson.toString().getBytes(), "application/json");
        }
        return new EventByteArray(null);
    }

    @Override
    public EventByteArray merge(List<byte[]> events) {
        String data = marshall(events);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            return new EventByteArray(data.getBytes(StandardCharsets.UTF_8), "application/json");
        } else {
            return new EventByteArray(data.getBytes(), "application/json");
        }
    }

    private String marshall(List<byte[]> events) {
        if (events == null || events.isEmpty()) {
            return "[]";
        }
        StringBuilder sb = new StringBuilder("[");
        int length = events.size();
        for (int i = 0; i < events.size(); i++) {
            byte[] data = events.get(i);
            length -= 1;
            if (data.length > 0) {
                String event = new String(data);
                if (event.startsWith("{") && event.endsWith("}")) { //ensure json format
                    if (eventData.isForceLoginPending(i)) {
                        event = replaceDeviceId(event, eventData.getPendingDeviceIdReplacement());
                    }
                    sb.append(event);
                    if (length > 0) {
                        sb.append(",");
                    }
                }
            } else {
                Logger.e(TAG, "Events in the database are not in the JSON format");
            }
        }
        sb.append("]");
        return sb.toString();
    }

    /**
     * 强制登录：只在发送副本中替换 deviceId；替换失败时按入库时的数据（自动生成的设备 ID）发送，不阻塞整批
     */
    private String replaceDeviceId(String event, String deviceId) {
        try {
            JSONObject eventJson = new JSONObject(event);
            eventJson.put("deviceId", deviceId);
            return eventJson.toString();
        } catch (JSONException e) {
            Logger.e(TAG, e, "failed to replace deviceId of force login pending event");
            return event;
        }
    }

    private void assertCondition(boolean condition, String msg) throws IllegalArgumentException {
        if (!condition) throw new IllegalArgumentException(msg);
    }

    @Override
    public Class<EventByteArray> getDataClass() {
        return EventByteArray.class;
    }

}
