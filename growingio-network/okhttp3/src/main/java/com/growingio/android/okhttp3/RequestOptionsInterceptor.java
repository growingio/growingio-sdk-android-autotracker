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

import com.growingio.android.sdk.track.log.Logger;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;


class RequestOptionsInterceptor implements Interceptor {
    private static final String TAG = "RequestOptionsInterceptor";

    public static final String PREVIEW_OPTIONS = "previewOptions";
    private final Map<String, Boolean> previewOptionsMap = new HashMap<>();

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        String preview = request.tag(String.class);
        String host = request.url().host();
        boolean needPreviewOptions = preview != null
                && preview.equals(PREVIEW_OPTIONS)
                && host != null && !previewOptionsMap.containsKey(host);
        if (needPreviewOptions) {
            Request optionsRequest = request.newBuilder()
                    //.get()
                    .method("OPTIONS", null)
                    .build();
            Response optionsResponse = chain.proceed(optionsRequest);
            if (optionsResponse.isSuccessful()) {
                previewOptionsMap.put(host, true);
                return chain.proceed(request);
            }
            Logger.e(TAG, "Failed to get preview options for host: " + host);
            return optionsResponse.newBuilder().code(451).build();
            // return optionsResponse;
        }

        return chain.proceed(chain.request());
    }
}
