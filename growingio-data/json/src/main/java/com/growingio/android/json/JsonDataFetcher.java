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
import com.growingio.android.sdk.track.http.EventData;
import com.growingio.android.sdk.track.http.EventStream;
import com.growingio.android.sdk.track.middleware.GEvent;
import com.growingio.android.sdk.track.modelloader.DataFetcher;

import org.json.JSONArray;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * <p>
 *
 * @author cpacm 2021/5/13
 */
public class JsonDataFetcher implements DataFetcher<EventStream> {
    private static final String TAG = "JsonDataFetcher";

    private final EventData eventData;

    public JsonDataFetcher(EventData eventData) {
        this.eventData = eventData;
    }


    @Override
    public void loadData(DataCallback<? super EventStream> callback) {
        try {
            byte[] data = marshall(eventData.getEvents());
            callback.onDataReady(new EventStream(data, "application/json"));
        } catch (Exception e) {
            callback.onLoadFailed(e);
        }
    }

    @Override
    public EventStream executeData() {
        byte[] data = marshall(eventData.getEvents());
        return new EventStream(data, "application/json");
    }

    public byte[] marshall(List<GEvent> events) {
        if (events == null || events.isEmpty()) {
            return null;
        }
        JSONArray jsonArray = new JSONArray();
        for (GEvent event : events) {
            if (event instanceof BaseEvent) {
                JSONObject eventJson = ((BaseEvent) event).toJSONObject();
                jsonArray.put(eventJson);
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            return jsonArray.toString().getBytes(StandardCharsets.UTF_8);
        } else {
            return jsonArray.toString().getBytes();
        }
    }

    @Override
    public void cleanup() {
    }

    @Override
    public void cancel() {
    }

    @Override
    public Class<EventStream> getDataClass() {
        return EventStream.class;
    }

}
