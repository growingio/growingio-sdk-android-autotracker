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

package com.growingio.android.sdk.track.variation;

import com.growingio.android.sdk.track.events.base.BaseEvent;
import com.growingio.android.sdk.track.events.marshaller.EventMarshaller;
import com.growingio.android.sdk.track.middleware.GEvent;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

public class TrackEventJsonMarshaller implements EventMarshaller<JSONObject, JSONArray> {

    @Override
    public JSONObject marshall(GEvent event) {
        if (event instanceof BaseEvent) {
            return ((BaseEvent) event).toJSONObject();
        }
        return null;
    }

    @Override
    public JSONArray marshall(List<GEvent> events) {
        JSONArray jsonArray = new JSONArray();
        if (events == null || events.isEmpty()) {
            return jsonArray;
        }

        for (GEvent event : events) {
            JSONObject eventJson = marshall(event);
            if (eventJson != null) {
                jsonArray.put(marshall(event));
            }
        }
        return jsonArray;
    }
}
