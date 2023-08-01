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
package com.growingio.android.sdk.track.middleware;

import androidx.annotation.IntDef;

import java.io.Serializable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public abstract class GEvent implements Serializable {
    // 即时发送策略
    public static final byte SEND_POLICY_INSTANT = 1;
    // 移动网络流量发送
    public static final byte SEND_POLICY_MOBILE_DATA = 2;
    // WIFI情况下发送
    public static final byte SEND_POLICY_WIFI = 3;

    public abstract String getEventType();

    // 默认所有数据4G下批量发送
    @SendPolicy
    public int getSendPolicy() {
        return SEND_POLICY_MOBILE_DATA;
    }

    @IntDef({SEND_POLICY_INSTANT, SEND_POLICY_MOBILE_DATA, SEND_POLICY_WIFI})
    @Retention(RetentionPolicy.SOURCE)
    public @interface SendPolicy {
    }
}
