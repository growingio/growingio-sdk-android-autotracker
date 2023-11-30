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

    public String getAbTestServerHost() {
        return abTestServerHost;
    }

    public void setAbTestServerHost(String abTestServerHost) {
        this.abTestServerHost = checkHost(abTestServerHost);
    }

    public long getAbTestExpired() {
        return abTestExpired;
    }

    public void setAbTestExpired(long duration, TimeUnit unit) {
        this.abTestExpired = checkDuration("ABTestExpired", duration, unit);
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

    private long checkDuration(String name, long duration, TimeUnit unit) {
        if (duration < 0) throw new IllegalArgumentException(name + " < 0");
        if (unit == null) throw new NullPointerException("unit == null");
        long millis = unit.toMillis(duration);
        if (millis > 3600 * 1000 * 24) throw new IllegalArgumentException(name + " too large.");
        if (millis < 5 * 60 * 1000) throw new IllegalArgumentException(name + " too small.");
        return millis;
    }
}
