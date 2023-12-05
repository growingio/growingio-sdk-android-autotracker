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
package com.growingio.android.abtest;

import com.growingio.android.sdk.Configurable;

import java.util.concurrent.TimeUnit;

/**
 * <p>
 *
 * @author cpacm 2023/11/24
 */
public class ABTestConfig implements Configurable {
    private String abTestServerHost = "https://ab.growingio.com";
    private long abTestExpired = 300_000L;
    private long abTestTimeout = 5_000L;

    public String getAbTestServerHost() {
        return abTestServerHost;
    }

    public ABTestConfig setAbTestServerHost(String abTestServerHost) {
        this.abTestServerHost = checkHost(abTestServerHost);
        return this;
    }

    public long getAbTestExpired() {
        return abTestExpired;
    }

    /**
     * set abTest expired: 1m ~ 24h, default is 5m.
     */
    public ABTestConfig setAbTestExpired(long duration, TimeUnit unit) {
        this.abTestExpired = checkDuration(duration, unit);
        return this;
    }

    public long getAbTestTimeout() {
        return abTestTimeout;
    }

    /**
     * set abTest timeout: at least 1000ms, default is 5s.
     */
    public ABTestConfig setAbTestTimeout(long duration, TimeUnit unit) {
        this.abTestTimeout = checkTimeoutDuration(duration, unit);
        return this;
    }

    private String checkHost(String host) {
        if (host == null) throw new NullPointerException("host == null");
        if (host.isEmpty()) throw new IllegalArgumentException("host.length() == 0");
        if (host != null && host.endsWith("/")) host = host.substring(0, host.length() - 1);
        if (!host.startsWith("http")) {
            host = "https://" + host;
        }
        return host;
    }

    private long checkDuration(long duration, TimeUnit unit) {
        if (duration < 0) throw new IllegalArgumentException("ABTestExpired" + " < 0");
        if (unit == null) throw new NullPointerException("unit == null");
        long millis = unit.toMillis(duration);
        if (millis > 3600 * 1000 * 24) throw new IllegalArgumentException("ABTestExpired" + " too large.");
        if (millis < 60 * 1000) throw new IllegalArgumentException("ABTestExpired" + " too small.");
        return millis;
    }

    private long checkTimeoutDuration(long duration, TimeUnit unit) {
        if (duration < 0) throw new IllegalArgumentException("ABTestTimeout" + " < 0");
        if (unit == null) throw new NullPointerException("unit == null");
        long millis = unit.toMillis(duration);
        if (millis > Integer.MAX_VALUE) throw new IllegalArgumentException("ABTestTimeout" + " too large.");
        if (millis < 1000) throw new IllegalArgumentException("ABTestTimeout" + " too small.");
        return millis;
    }
}
