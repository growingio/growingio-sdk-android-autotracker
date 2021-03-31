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

import android.util.Log;

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
public class OkHttpDataFetcher implements HttpDataFetcher<String>, Callback {
    private static final String TAG = "OkHttpDataFetcher";

    private final Call.Factory client;
    private final HttpUrl httpUrl;
    private DataCallback<? super String> callback;

    public OkHttpDataFetcher(Call.Factory client, HttpUrl httpUrl) {
        this.client = client;
        this.httpUrl = httpUrl;
    }

    @Override
    public void loadData(DataCallback<? super String> callback) {
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
    public void cleanup() {

    }

    @Override
    public void cancel() {

    }

    @Override
    public Class<String> getDataClass() {
        return null;
    }

    @Override
    public void onFailure(Call call,  IOException e) {
        if (Log.isLoggable(TAG, Log.DEBUG)) {
            Log.d(TAG, "OkHttp failed to obtain result", e);
        }

        callback.onLoadFailed(e);
    }

    @Override
    public void onResponse(Call call, Response response) {
        ResponseBody responseBody = response.body();
//        if (response.isSuccessful()) {
//            long contentLength = Preconditions.checkNotNull(responseBody).contentLength();
//            stream = ContentLengthInputStream.obtain(responseBody.byteStream(), contentLength);
//            callback.onDataReady(stream);
//        } else {
//            callback.onLoadFailed(new HttpException(response.message(), response.code()));
//        }
    }
}
