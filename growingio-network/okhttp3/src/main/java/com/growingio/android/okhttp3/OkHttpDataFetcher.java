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

package com.growingio.android.okhttp3;

import com.growingio.android.sdk.track.http.EventResponse;
import com.growingio.android.sdk.track.http.EventUrl;
import com.growingio.android.sdk.track.log.Logger;
import com.growingio.android.sdk.track.modelloader.DataFetcher;

import java.io.IOException;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * <p>
 *
 * @author cpacm 2021/3/31
 */
public class OkHttpDataFetcher implements DataFetcher<EventResponse>, Callback {
    private static final String TAG = "OkHttpDataFetcher";

    private final Call.Factory client;
    private final EventUrl eventUrl;
    private DataCallback<? super EventResponse> callback;
    private ResponseBody responseBody;
    private volatile Call call;

    public OkHttpDataFetcher(Call.Factory client, EventUrl eventUrl) {
        this.client = client;
        this.eventUrl = eventUrl;
    }

    @Override
    public void loadData(DataCallback<? super EventResponse> callback) {
        Request.Builder requestBuilder = new Request.Builder().url(eventUrl.toUrl());
        if (eventUrl.getRequestBody() != null) {
            requestBuilder.post(RequestBody.create(MediaType.parse(eventUrl.getMediaType()), eventUrl.getRequestBody()));
        }
        for (Map.Entry<String, String> headerEntry : eventUrl.getHeaders().entrySet()) {
            String key = headerEntry.getKey();
            requestBuilder.addHeader(key, headerEntry.getValue());
        }
        this.callback = callback;
        Request request = requestBuilder.build();
        call = client.newCall(request);
        call.enqueue(this);
    }

    @Override
    public EventResponse executeData() {
        Request.Builder requestBuilder = new Request.Builder().url(eventUrl.toUrl());
        for (Map.Entry<String, String> headerEntry : eventUrl.getHeaders().entrySet()) {
            String key = headerEntry.getKey();
            requestBuilder.addHeader(key, headerEntry.getValue());
        }
        if (eventUrl.getRequestBody() != null) {
            requestBuilder.post(RequestBody.create(MediaType.parse(eventUrl.getMediaType()), eventUrl.getRequestBody()));
        }
        Request request = requestBuilder.build();
        try {
            call = client.newCall(request);
            Response response = call.execute();
            responseBody = response.body();
            if (response.isSuccessful()) {
                boolean successed = true;
                long contentLength = responseBody.contentLength();
                return new EventResponse(successed, responseBody.byteStream(), contentLength);
            } else {
                return new EventResponse(false);
            }
        } catch (IOException e) {
            Logger.e(TAG, e);
            return new EventResponse(false);
        } catch (NullPointerException e) {
            Logger.e(TAG, e);
            return new EventResponse(false);
        }
    }

    @Override
    public void cleanup() {
        if (responseBody != null) {
            responseBody.close();
        }
        callback = null;
    }

    @Override
    public void cancel() {
        Call local = call;
        if (local != null) {
            local.cancel();
        }
    }

    @Override
    public Class<EventResponse> getDataClass() {
        return EventResponse.class;
    }

    @Override
    public void onFailure(Call call, IOException e) {
        callback.onLoadFailed(e);
        Logger.e(TAG, e);
    }

    @Override
    public void onResponse(Call call, Response response) {
        responseBody = response.body();
        if (response.isSuccessful()) {
            if (responseBody == null) {
                throw new IllegalArgumentException("Must not be null or empty");
            }
            long contentLength = responseBody.contentLength();
            EventResponse eventResponse = new EventResponse(true, responseBody.byteStream(), contentLength);
            callback.onDataReady(eventResponse);
        } else {
            callback.onLoadFailed(new Exception(response.message()));
        }
    }
}
