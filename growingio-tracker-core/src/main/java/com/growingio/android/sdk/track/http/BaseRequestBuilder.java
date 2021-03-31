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

import android.text.TextUtils;

import java.util.HashMap;
import java.util.Map;

import okhttp3.Request;
import okhttp3.RequestBody;

public abstract class BaseRequestBuilder<T extends BaseRequestBuilder> {
    private HttpUrl mUrl;
    private Map<String, String> mHeaders = new HashMap<>();
    private int mRetryTimes = 0;
    private boolean mEnableGzip = false;

    public BaseRequestBuilder(String url) {
        mUrl = new HttpUrl(url);
    }

    public T addPath(String path) {
        mUrl.addPath(path);
        return (T) this;
    }

    public T addParam(String key, String param) {
        mUrl.addParam(key, param);
        return (T) this;
    }

    public T setHeaders(Map<String, String> headers) {
        mHeaders = headers;
        return (T) this;
    }

    public T addHeader(String key, String value) {
        mHeaders.put(key, value);
        return (T) this;
    }

    public T addHeaders(Map<String, String> headers) {
        mHeaders.putAll(headers);
        return (T) this;
    }

    public HttpUrl getUrl() {
        return mUrl;
    }

    public T setRetryTimes(int retryTimes) {
        mRetryTimes = retryTimes;
        return (T) this;
    }

    public T setEnableGzip(boolean enableGzip) {
        mEnableGzip = enableGzip;
        return (T) this;
    }

    protected abstract RequestBody getRequestBody();

    public HttpRequest build() {
        RequestBody requestBody = getRequestBody();
        Request.Builder requestBuilder;
        if (requestBody == null) {
            requestBuilder = new Request.Builder()
                    .url(mUrl.toString());
        } else {
            requestBuilder = new Request.Builder()
                    .url(mUrl.toString())
                    .post(requestBody);
        }

        for (String key : mHeaders.keySet()) {
            String value = mHeaders.get(key);
            if (!TextUtils.isEmpty(value)) {
                requestBuilder.addHeader(key, value);
            }
        }
        requestBuilder.tag(RequestExtra.class, new RequestExtra(mRetryTimes, mEnableGzip));
        return new HttpRequest(requestBuilder.build());
    }
}
