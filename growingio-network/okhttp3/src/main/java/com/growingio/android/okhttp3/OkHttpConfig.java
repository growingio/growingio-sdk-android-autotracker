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

import com.growingio.android.sdk.Configurable;

import java.util.concurrent.TimeUnit;

public class OkHttpConfig implements Configurable {
    private static final int DEFAULT_OKHTTP_TIMEOUT = 10;
    private int callTimeout = 0;

    private int connectTimeout = checkDuration("connectTimeout", DEFAULT_OKHTTP_TIMEOUT, TimeUnit.SECONDS);
    private int readTimeout = checkDuration("readTimeout", DEFAULT_OKHTTP_TIMEOUT, TimeUnit.SECONDS);
    private int writeTimeout = checkDuration("writeTimeout", DEFAULT_OKHTTP_TIMEOUT, TimeUnit.SECONDS);

    /**
     * Sets the default timeout for complete calls. A value of 0 means no timeout, otherwise values
     * must be between 1 and {@link Integer#MAX_VALUE} when converted to milliseconds.
     *
     * <p>The call timeout spans the entire call: resolving DNS, connecting, writing the request
     * body, server processing, and reading the response body. If the call requires redirects or
     * retries all must complete within one timeout period.
     *
     * <p>Default: connectTimeout + readTimeout + writeTimeout = 30s
     */
    public OkHttpConfig setRequestTimeout(long timeout, TimeUnit unit) {
        callTimeout = checkDuration("timeout", timeout, unit);
        return this;
    }

    /**
     * Sets the default timeout for complete calls.
     *
     * <p>The call timeout spans the entire call: resolving DNS, connecting, writing the request
     * body, server processing, and reading the response body. If the call requires redirects or
     * retries all must complete within one timeout period.
     *
     * <p>Default: connectTimeout + readTimeout + writeTimeout = 30s
     */
    public OkHttpConfig setRequestDetailTimeout(long connectTimeout, long readTimeout, long writeTimeout, TimeUnit unit) {
        this.callTimeout = 0;
        this.connectTimeout = checkDuration("callTimeout", connectTimeout, unit);
        this.readTimeout = checkDuration("readTimeout", readTimeout, unit);
        this.writeTimeout = checkDuration("writeTimeout", writeTimeout, unit);
        return this;
    }

    private static int checkDuration(String name, long duration, TimeUnit unit) {
        if (duration < 0) throw new IllegalArgumentException(name + " < 0");
        if (unit == null) throw new NullPointerException("unit == null");
        long millis = unit.toMillis(duration);
        if (millis > Integer.MAX_VALUE) throw new IllegalArgumentException(name + " too large.");
        if (millis == 0 && duration > 0) throw new IllegalArgumentException(name + " too small.");
        return (int) millis;
    }

    public int getHttpCallTimeout() {
        return callTimeout;
    }

    int getConnectTimeout() {
        return connectTimeout;
    }

    int getReadTimeout() {
        return readTimeout;
    }

    int getWriteTimeout() {
        return writeTimeout;
    }
}
