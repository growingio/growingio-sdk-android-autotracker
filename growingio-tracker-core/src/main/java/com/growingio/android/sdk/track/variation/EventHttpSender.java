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

import com.growingio.android.sdk.track.TrackerContext;
import com.growingio.android.sdk.track.TrackConfiguration;
import com.growingio.android.sdk.track.events.base.BaseEvent;
import com.growingio.android.sdk.track.events.marshaller.EventMarshaller;
import com.growingio.android.sdk.track.http.EventResponse;
import com.growingio.android.sdk.track.http.EventUrl;
import com.growingio.android.sdk.track.log.Logger;
import com.growingio.android.sdk.track.middleware.GEvent;
import com.growingio.android.sdk.track.middleware.IEventNetSender;
import com.growingio.android.sdk.track.middleware.SendResponse;
import com.growingio.android.sdk.track.modelloader.ModelLoader;
import com.growingio.android.sdk.track.providers.ConfigurationProvider;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

import okhttp3.Response;

public class EventHttpSender implements IEventNetSender {
    private static final String TAG = "EventHttpSender";

    private final EventMarshaller<JSONObject, JSONArray> mEventMarshaller;
    private final String mProjectId;
    private final String mServerHost;
    private ModelLoader<EventUrl, EventResponse> mHttpLoader;

    public EventHttpSender(EventMarshaller<JSONObject, JSONArray> eventMarshaller) {
        mEventMarshaller = eventMarshaller;
        TrackConfiguration configuration = ConfigurationProvider.get().getTrackConfiguration();
        mProjectId = configuration.getProjectId();
        mServerHost = configuration.getDataCollectionServerHost();
    }

    private ModelLoader<EventUrl, EventResponse> getModelLoader() {
        if (mHttpLoader == null) {
            mHttpLoader = TrackerContext.get().getRegistry().getModelLoader(EventUrl.class, EventResponse.class);
        }
        return mHttpLoader;
    }

    /**
     * 数据可以选择压缩+加密
     * https://codes.growingio.com/w/api_v3_interface/
     */
    @Override
    public SendResponse send(List<GEvent> events) {
        if (events == null || events.isEmpty()) {
            return new SendResponse(true, 0);
        }
        if (getModelLoader() == null) {
            Logger.e(TAG, "please register http request component first");
            return new SendResponse(false, 0);
        }

        if (!(events.get(0) instanceof BaseEvent)) {
            return new SendResponse(true, 0);
        }

        String data = mEventMarshaller.marshall(events).toString();
        EventUrl eventUrl = new EventUrl(mServerHost)
                .addPath("v3")
                .addPath("projects")
                .addPath(mProjectId)
                .addPath("collect")
                .addParam("stm", String.valueOf(System.currentTimeMillis()))
                .setBodyData(data)
                .setMediaType("application/json");
        ModelLoader.LoadData<EventResponse> loadData = getModelLoader().buildLoadData(eventUrl);
        if (loadData.fetcher.getDataClass() != EventResponse.class) {
            Logger.e(TAG, new IllegalArgumentException("illegal data class for http response."));
            return new SendResponse(true, 0);
        }
        EventResponse response = loadData.fetcher.executeData();

        boolean successful = response != null && response.isSucceeded();
        if (successful) {
            Logger.d(TAG, "Send events successfully");
        } else {
            Logger.d(TAG, "Send events failed, response = " + response);
        }

        return new SendResponse(successful, data.getBytes().length);
    }
}
