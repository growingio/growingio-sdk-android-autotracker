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
package com.growingio.android.okhttp3;

import com.growingio.android.sdk.track.middleware.http.EventResponse;
import com.growingio.android.sdk.track.middleware.http.EventUrl;
import com.growingio.android.sdk.track.log.Logger;
import com.growingio.android.sdk.track.middleware.http.HttpDataFetcher;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
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
public class OkHttpDataFetcher implements HttpDataFetcher<EventResponse>, Callback {
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
        Request request = buildRequestWithEventUrl();
        this.callback = callback;
        call = client.newCall(request);
        call.enqueue(this);
    }

    private Request buildRequestWithEventUrl() {
        Request.Builder requestBuilder = new Request.Builder().url(eventUrl.toUrl());
        for (Map.Entry<String, String> headerEntry : eventUrl.getHeaders().entrySet()) {
            String key = headerEntry.getKey();
            requestBuilder.addHeader(key, headerEntry.getValue());
        }
        if (eventUrl.getRequestBody() != null) {
            requestBuilder.post(RequestBody.create(MediaType.parse(eventUrl.getMediaType()), eventUrl.getRequestBody()));
        } else {
            if (eventUrl.getRequestMethod() == EventUrl.POST) {
                RequestBody requestBody = RequestBody.create(MediaType.parse(eventUrl.getMediaType()), new byte[0]);
                requestBuilder.post(requestBody);
            } else {
                requestBuilder.get();
            }
        }
        return requestBuilder.build();
    }


    @Override
    public EventResponse executeData() {
        Request request = buildRequestWithEventUrl();
        try {
            call = client.newCall(request);
            Response response = call.execute();
            responseBody = response.body();
            if (response.isSuccessful()) {
                boolean successed = true;
                long contentLength = responseBody.contentLength();
                return new EventResponse(successed, copyResponse(responseBody.byteStream()), contentLength);
            } else {
                Logger.e(TAG, "OkHttpSender failed with code:" + response.code());
                return new EventResponse(response.code());
            }
        } catch (IOException e) {
            Logger.e(TAG, e);
            return new EventResponse(false);
        } catch (NullPointerException e) {
            Logger.e(TAG, e);
            return new EventResponse(false);
        } finally {
            cleanup();
        }
    }

    private InputStream copyResponse(InputStream input) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len;
        while ((len = input.read(buffer)) > -1) {
            baos.write(buffer, 0, len);
        }
        baos.flush();
        return new ByteArrayInputStream(baos.toByteArray());
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
        try {
            if (response.isSuccessful()) {
                responseBody = response.body();
                long contentLength = responseBody.contentLength();
                EventResponse eventResponse = new EventResponse(response.code(), responseBody.byteStream(), contentLength);
                callback.onDataReady(eventResponse);
            } else {
                callback.onDataReady(new EventResponse(response.code()));
            }
        } finally {
            cleanup();
        }
    }
}
