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

package com.growingio.android.sdk.track.middleware;

import java.util.Arrays;

public class EventsInfo {

    private String mEventType;
    private int mPolicy;
    private byte[] mData;

    public String getEventType() {
        return mEventType;
    }

    public void setEventType(String eventType) {
        this.mEventType = eventType;
    }

    public int getPolicy() {
        return mPolicy;
    }

    public void setPolicy(int policy) {
        this.mPolicy = policy;
    }

    public byte[] getData() {
        return mData;
    }

    public void setData(byte[] data) {
        this.mData = data;
    }

    public EventsInfo(String eventType, int policy, byte[] data) {
        this.mEventType = eventType;
        this.mPolicy = policy;
        this.mData = data;
    }

    @Override
    public String toString() {
        return "EventsInfo{" +
                "eventType='" + mEventType + '\'' +
                ", policy=" + mPolicy +
                ", data=" + Arrays.toString(mData) +
                '}';
    }
}
