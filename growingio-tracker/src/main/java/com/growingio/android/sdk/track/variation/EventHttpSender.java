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
import com.growingio.android.sdk.track.middleware.IEventSender;
import com.growingio.android.sdk.track.providers.ConfigurationProvider;
import com.growingio.android.sdk.track.snappy.Snappy;

import org.json.JSONArray;
import org.json.JSONObject;

import java.nio.charset.Charset;
import java.util.List;

public class EventHttpSender implements IEventSender {
    private static final String TAG = "EventHttpSender";

    private final EventMarshaller<JSONObject, JSONArray> mEventMarshaller;

    public EventHttpSender(EventMarshaller<JSONObject, JSONArray> eventMarshaller) {
        mEventMarshaller = eventMarshaller;
    }

    /**
     * 数据需要压缩+加密才可以上传
     * https://codes.growingio.com/w/api_v3_interface/
     */
    @Override
    public boolean send(List<GEvent> events) {
        if (events == null || events.isEmpty()) {
            return true;
        }
//        for (GEvent event : events) {
//            if (event instanceof BaseEvent) {
//                Logger.printJson(TAG, "send: event, type is " + ((BaseEvent) event).getEventType(), ((BaseEvent) event).toJSONObject().toString());
//            }
//        }

        BaseEvent event;
        if (events.get(0) instanceof BaseEvent) {
            event = (BaseEvent) events.get(0);
        } else {
            return false;
        }

        ConfigurationProvider infoProvider = ConfigurationProvider.get();
//        String projectId = infoProvider.getProjectId();
//        String url = EventUrlProvider.EventUrlPolicy.get().getUrl(projectId, event.getEventType());
        String data = mEventMarshaller.marshall(events).toString();
//        Logger.printJson(TAG + "Marshaller", "send: event marshall", data);
        byte[] compressData = Snappy.compress(data.getBytes(Charset.forName("UTF-8")));

//        Response response = HttpRequest.postData("https://demo6984138.mockable.io/trackApi")
//                .setBody(compressData)
//                .addHeader("X-Compress-Codec", "2")
//                .build()
//                .execute();
//        return response.isSuccessful();

        return true;
    }
}
