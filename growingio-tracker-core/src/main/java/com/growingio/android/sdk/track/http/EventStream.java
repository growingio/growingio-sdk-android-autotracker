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

/**
 * <p>
 *
 * @author cpacm 5/13/21
 */
public class EventStream {
    private final byte[] bodyData;
    private String mediaType = "multipart/form-data";

    public EventStream(byte[] bodyData) {
        this.bodyData = bodyData;
    }

    public EventStream(byte[] bodyData, String mediaType) {
        this.bodyData = bodyData;
        this.mediaType = mediaType;
    }

    public byte[] getBodyData() {
        return bodyData;
    }

    public String getMediaType() {
        return mediaType;
    }
}
