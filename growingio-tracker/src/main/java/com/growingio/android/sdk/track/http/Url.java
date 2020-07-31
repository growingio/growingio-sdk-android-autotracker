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

public class Url {
    private final String mHost;
    private final List<String> mPaths = new ArrayList<>();
    private final Map<String, String> mParams = new HashMap<>();

    Url(String host) {
        mHost = host;
    }

    Url addPath(String path) {
        mPaths.add(path);
        return this;
    }

    Url addParam(String key, String param) {
        mParams.put(key, param);
        return this;
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