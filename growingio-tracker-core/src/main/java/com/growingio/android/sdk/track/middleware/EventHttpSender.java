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
package com.growingio.android.sdk.track.middleware;

import android.text.TextUtils;

import com.growingio.android.sdk.TrackerContext;
import com.growingio.android.sdk.track.listener.TrackThread;
import com.growingio.android.sdk.track.middleware.http.EventEncoder;
import com.growingio.android.sdk.track.middleware.http.EventResponse;
import com.growingio.android.sdk.track.middleware.http.EventUrl;
import com.growingio.android.sdk.track.log.Logger;
import com.growingio.android.sdk.track.modelloader.ModelLoader;
import com.growingio.android.sdk.track.modelloader.TrackerRegistry;
import com.growingio.android.sdk.track.providers.ConfigurationProvider;

public class EventHttpSender implements IEventNetSender {
    private static final String TAG = "EventHttpSender";

    private final String mProjectId;
    private final String mServerHost;
    private final TrackerRegistry trackerRegistry;
    private final boolean defaultPreflight;

    private boolean requestPreflightChecked = false;

    public EventHttpSender(TrackerContext context) {
        ConfigurationProvider configurationProvider = context.getConfigurationProvider();
        this.trackerRegistry = context.getRegistry();
        mProjectId = configurationProvider.core().getProjectId();
        mServerHost = configurationProvider.core().getDataCollectionServerHost();
        defaultPreflight = false;
    }

    private boolean isPreflightChecked() {
        if (defaultPreflight) return requestPreflightChecked;
        else return true;
    }

    private ModelLoader<EventUrl, EventResponse> getNetworkModelLoader() {
        return trackerRegistry.getModelLoader(EventUrl.class, EventResponse.class);
    }

    private EventResponse requestPreflight(long time) {
        EventUrl eventUrl = new EventUrl(mServerHost, time)
                .setRequestMethod(EventUrl.OPTIONS)
                .addPath("v3")
                .addPath("projects")
                .addPath(mProjectId)
                .addPath("collect")
                .addHeader("Access-Control-Request-Method", "POST")
                .addHeader("Origin", mServerHost)
                .addParam("stm", String.valueOf(time));
        ModelLoader.LoadData<EventResponse> loadData = getNetworkModelLoader().buildLoadData(eventUrl);
        if (!loadData.fetcher.getDataClass().isAssignableFrom(EventResponse.class)) {
            Logger.e(TAG, new IllegalArgumentException("illegal data class for http response."));
            return new EventResponse(0);
        }
        EventResponse response = loadData.fetcher.executeData();
        if (response.isSucceeded()) {
            requestPreflightChecked = true;
        }
        return response;
    }

    @TrackThread
    @Override
    public SendResponse send(byte[] events, String mediaType) {
        if (events == null || events.length == 0) {
            return new SendResponse(0, 0);
        }
        if (getNetworkModelLoader() == null) {
            Logger.e(TAG, "please register http request component first");
            return new SendResponse(0, 0);
        }
        long time = System.currentTimeMillis();
        EventUrl eventUrl = new EventUrl(mServerHost, time)
                .addPath("v3")
                .addPath("projects")
                .addPath(mProjectId)
                .addPath("collect")
                .addParam("stm", String.valueOf(time))
                .setBodyData(events);
        if (!TextUtils.isEmpty(mediaType)) eventUrl.setMediaType(mediaType);
        //data encoder - https://codes.growingio.com/w/api_v3_interface/
        EventEncoder encoder = trackerRegistry.executeData(new EventEncoder(eventUrl), EventEncoder.class, EventEncoder.class);
        if (encoder != null) {
            eventUrl = encoder.getEventUrl();
        }

        byte[] data = eventUrl.getRequestBody();
        Logger.d(TAG, "send event to url: " + eventUrl.toString());

        ModelLoader.LoadData<EventResponse> loadData = getNetworkModelLoader().buildLoadData(eventUrl);
        if (!loadData.fetcher.getDataClass().isAssignableFrom(EventResponse.class)) {
            Logger.e(TAG, new IllegalArgumentException("illegal data class for http response."));
            return new SendResponse(0, 0);
        }
        EventResponse response = loadData.fetcher.executeData();
        int responseCode = response != null ? response.getResponseCode() : 0;
        if (responseCode >= 200 && responseCode < 300) {
            requestPreflightChecked = true;
        } else if (responseCode == 403) {
            requestPreflightChecked = false;
        }
        long totalUsed = data == null ? 0L : data.length;
        return new SendResponse(responseCode, totalUsed);
    }
}
