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

import okhttp3.MediaType;
import okhttp3.RequestBody;

public class DataPostRequestBuilder extends BaseRequestBuilder<DataPostRequestBuilder> {
    private static final MediaType CONTENT_TYPE = MediaType.get("application/x-www-form-urlencoded");

    private byte[] mBody = new byte[]{};

    DataPostRequestBuilder(String url) {
        super(url);
    }


    public DataPostRequestBuilder setBody(byte[] body) {
        mBody = body;
        return this;
    }

    @Override
    protected RequestBody getRequestBody() {
        return RequestBody.create(CONTENT_TYPE, mBody);
    }
}
