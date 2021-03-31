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

package com.growingio.android.sdk.track.http;

import com.growingio.android.sdk.track.log.Logger;

import java.io.IOException;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * <p>
 *
 * @author cpacm 2021/3/31
 */
public class OkHttpDataFetcher implements HttpDataFetcher<EventResponse>, Callback {
    private static final String TAG = "OkHttpDataFetcher";

    private final Call.Factory client;
    private final HttpUrl httpUrl;
    private DataCallback<? super EventResponse> callback;
    private ResponseBody responseBody;

    public OkHttpDataFetcher(Call.Factory client, HttpUrl httpUrl) {
        this.client = client;
        this.httpUrl = httpUrl;
    }

    @Override
    public void loadData(DataCallback<? super EventResponse> callback) {
        Request.Builder requestBuilder = new Request.Builder().url(httpUrl.toUrl());
        for (Map.Entry<String, String> headerEntry : httpUrl.getHeaders().entrySet()) {
            String key = headerEntry.getKey();
            requestBuilder.addHeader(key, headerEntry.getValue());
        }
        this.callback = callback;
        Request request = requestBuilder.build();
        client.newCall(request).enqueue(this);
    }

    @Override
    public EventResponse executeData() {
        Request.Builder requestBuilder = new Request.Builder().url(httpUrl.toUrl());
        for (Map.Entry<String, String> headerEntry : httpUrl.getHeaders().entrySet()) {
            String key = headerEntry.getKey();
            requestBuilder.addHeader(key, headerEntry.getValue());
        }
        Request request = requestBuilder.build();
        try {
            Response response = client.newCall(request).execute();
            responseBody = response.body();
            if (response.isSuccessful()) {
                boolean successed = true;
                long contentLength = responseBody.contentLength();
                return new EventResponse(successed, contentLength);
            } else {
                return new EventResponse(false, 0L);
            }
        } catch (IOException e) {
            Logger.e(TAG, e);
            return new EventResponse(false, 0L);
        }
    }

    @Override
    public void cleanup() {

    }

    @Override
    public void cancel() {

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
            boolean successed = true;
            long contentLength = responseBody.contentLength();
            EventResponse eventResponse = new EventResponse(successed, contentLength);
            callback.onDataReady(eventResponse);
        } else {
            callback.onLoadFailed(new Exception(response.message()));
        }
    }
}
