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

package com.growingio.android.json;

import android.os.Build;

import com.growingio.android.sdk.track.events.base.BaseEvent;
import com.growingio.android.sdk.track.log.Logger;
import com.growingio.android.sdk.track.middleware.format.EventFormatData;
import com.growingio.android.sdk.track.middleware.format.EventByteArray;
import com.growingio.android.sdk.track.middleware.GEvent;
import com.growingio.android.sdk.track.middleware.format.FormatDataFetcher;

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
            JSONObject eventJson = ((BaseEvent) gEvent).toJSONObject();
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
        Logger.d(TAG, "----- merge json data size:" + length + " ----");
        for (byte[] data : events) {
            length -= 1;
            if (data.length > 0) {
                String event = new String(data);
                if (event.startsWith("{") && event.endsWith("}")) { //ensure json format
                    sb.append(event);
                    if (length > 0) {
                        sb.append(",");
                    }
                }
            }
        }
        sb.append("]");
        return sb.toString();
    }

    private void assertCondition(boolean condition, String msg) throws IllegalArgumentException {
        if (!condition) throw new IllegalArgumentException(msg);
    }

    @Override
    public Class<EventByteArray> getDataClass() {
        return EventByteArray.class;
    }

}
