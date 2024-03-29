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

import com.growingio.android.sdk.Configurable;

import java.util.concurrent.TimeUnit;

public class UrlConnectionConfig implements Configurable {

    public static final int DEFAULT_URL_CONNECTION_TIMEOUT = 15_000;
    private int connectTimeout = DEFAULT_URL_CONNECTION_TIMEOUT;
    private int readTimeout = DEFAULT_URL_CONNECTION_TIMEOUT;

    public UrlConnectionConfig setConnectTimeout(long connectTimeout, TimeUnit unit) {
        this.connectTimeout = checkDuration("connectTimeout", connectTimeout, unit);
        return this;
    }

    public UrlConnectionConfig setReadTimeout(long readTimeout, TimeUnit unit) {
        this.readTimeout = checkDuration("readTimeout", readTimeout, unit);
        return this;
    }

    int getConnectTimeout() {
        return connectTimeout;
    }

    int getReadTimeout() {
        return readTimeout;
    }

    private static int checkDuration(String name, long duration, TimeUnit unit) {
        if (duration <= 0) throw new IllegalArgumentException(name + " <= 0");
        if (unit == null) throw new NullPointerException("unit == null");
        long millis = unit.toMillis(duration);
        if (millis > Integer.MAX_VALUE) throw new IllegalArgumentException(name + " too large.");
        if (millis == 0 && duration > 0) throw new IllegalArgumentException(name + " too small.");
        return (int) millis;
    }
}
