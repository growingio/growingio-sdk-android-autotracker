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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EventUrl {
    private final String mHost;
    private final Map<String, String> mHeaders = new HashMap<>();
    private final List<String> mPaths = new ArrayList<>();
    private final Map<String, String> mParams = new HashMap<>();
    private byte[] mBodyData;
    private final long mTime;
    private String mMediaType = "application/json"; //or "application/x-www-form-urlencoded" for data

    public EventUrl(String host, long time) {
        mHost = host;
        mTime = time;
    }

    public EventUrl addPath(String path) {
        mPaths.add(path);
        return this;
    }

    public EventUrl addParam(String key, String param) {
        mParams.put(key, param);
        return this;
    }

    public EventUrl addHeader(String key, String value) {
        mHeaders.put(key, value);
        return this;
    }

    public String getMediaType() {
        return mMediaType;
    }

    public byte[] getRequestBody() {
        return mBodyData;
    }

    public long getTime() {
        return mTime;
    }

    public EventUrl setBodyData(byte[] mBodyData) {
        this.mBodyData = mBodyData;
        return this;
    }

    public EventUrl setMediaType(String mMediaType) {
        this.mMediaType = mMediaType;
        return this;
    }

    public Map<String, String> getHeaders() {
        return mHeaders;
    }

    public String toUrl() {
        StringBuilder urlBuilder = new StringBuilder(mHost);
        for (String path : mPaths) {
            if (urlBuilder.charAt(urlBuilder.length() - 1) != '/') {
                urlBuilder.append("/");
            }
            urlBuilder.append(path);
        }

        if (!mParams.isEmpty()) {
            urlBuilder.append("?");
            for (String key : mParams.keySet()) {
                urlBuilder.append(key).append("=").append(mParams.get(key)).append("&");
            }
            urlBuilder.deleteCharAt(urlBuilder.length() - 1);
        }

        return urlBuilder.toString();
    }

    @Override
    public String toString() {
        StringBuilder urlBuilder = new StringBuilder(mHost);
        for (String path : mPaths) {
            if (urlBuilder.charAt(urlBuilder.length() - 1) != '/') {
                urlBuilder.append("/");
            }
            urlBuilder.append(path);
        }

        if (!mParams.isEmpty()) {
            urlBuilder.append("?");
            for (String key : mParams.keySet()) {
                urlBuilder.append(key).append("=").append(mParams.get(key)).append("&");
            }
            urlBuilder.deleteCharAt(urlBuilder.length() - 1);
        }

        return urlBuilder.toString();
    }
}