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

import java.io.InputStream;

public class EventResponse {
    private final boolean succeeded;
    private final InputStream stream; //it's useless
    private final long usedBytes; //it's useless

    public EventResponse(boolean succeeded) {
        this.succeeded = succeeded;
        usedBytes = 0L;
        stream = null;
    }

    public EventResponse(boolean succeeded, InputStream stream, long usedBytes) {
        this.succeeded = succeeded;
        this.usedBytes = usedBytes;
        this.stream = stream;
    }

    public EventResponse(boolean succeeded, InputStream stream) {
        this.succeeded = succeeded;
        usedBytes = 0L;
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
}
