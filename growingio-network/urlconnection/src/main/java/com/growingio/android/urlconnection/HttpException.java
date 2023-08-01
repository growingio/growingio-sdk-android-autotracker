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
package com.growingio.android.urlconnection;

import java.io.IOException;

/**
 * Thrown when an http request fails.
 *
 * <p>Exposes the specific status code or {@link #UNKNOWN} via {@link #getStatusCode()} so users may
 * attempt to retry or otherwise uniformly handle certain types of errors regardless of the
 * underlying http library.
 */
// Public API.
@SuppressWarnings({"WeakerAccess", "unused"})
public final class HttpException extends IOException {
    private static final long serialVersionUID = 1L;

    public static final int UNKNOWN = -1;
    private final int statusCode;

    public HttpException(int statusCode) {
        this("Http request failed", statusCode);
    }

    public HttpException(String message, int statusCode) {
        this(message, statusCode, null /*cause*/);
    }

    public HttpException(String message, int statusCode, Throwable cause) {
        super(message + ", status code: " + statusCode, cause);
        this.statusCode = statusCode;
    }

    /**
     * Returns the http status code, or {@link #UNKNOWN} if the request failed without providing a
     * status code.
     */
    public int getStatusCode() {
        return statusCode;
    }
}
