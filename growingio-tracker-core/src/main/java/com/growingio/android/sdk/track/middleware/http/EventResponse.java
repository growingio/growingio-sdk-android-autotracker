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
package com.growingio.android.sdk.track.middleware.http;

import java.io.InputStream;

public class EventResponse {
    private final boolean succeeded;
    private final InputStream stream;
    private final long usedBytes; //it's useless
    private final int responseCode;

    public EventResponse(int code) {
        this.responseCode = code;
        this.succeeded = responseCode >= 200 && responseCode < 300;
        usedBytes = 0L;
        stream = null;
    }

    public EventResponse(boolean succeeded) {
        this.succeeded = succeeded;
        this.responseCode = 0;
        usedBytes = 0L;
        stream = null;
    }

    public EventResponse(boolean succeeded, InputStream stream, long usedBytes) {
        this.succeeded = succeeded;
        this.usedBytes = usedBytes;
        this.stream = stream;
        this.responseCode = 204;
    }

    public EventResponse(int responseCode, InputStream stream, long usedBytes) {
        this.responseCode = responseCode;
        this.succeeded = responseCode >= 200 && responseCode < 300;
        this.usedBytes = usedBytes;
        this.stream = stream;
    }

    public boolean isSucceeded() {
        return succeeded;
    }

    public long getUsedBytes() {
        return usedBytes;
    }

    public InputStream getStream() {
        return stream;
    }

    public int getResponseCode() {
        return responseCode;
    }
}
