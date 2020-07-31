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

import android.util.Log;

import java.util.List;

public class SDKStaticsEvent {

    private long mId;
    private String mHourTime;
    private int mSavedNum;
    private int mSentSuccessNum;
    private int mBytesSent;
    private int mFailedNum;
    private int mInvalidNum;

    public static boolean sendEvents(List<SDKStaticsEvent> events) {
        for (SDKStaticsEvent event : events) {
            Log.d("LDK", "uploadEvent: " + event);
        }
        return true;
    }

    public long getId() {
        return mId;
    }

    public SDKStaticsEvent setId(long id) {
        this.mId = id;
        return this;
    }

    public String getHourTime() {
        return mHourTime;
    }

    public SDKStaticsEvent setHourTime(String hourTime) {
        this.mHourTime = hourTime;
        return this;
    }

    public int getSavedNum() {
        return mSavedNum;
    }

    public SDKStaticsEvent setSavedNum(int savedNum) {
        this.mSavedNum = savedNum;
        return this;
    }

    public int getSentSuccessNum() {
        return mSentSuccessNum;
    }

    public SDKStaticsEvent setSentSuccessNum(int sentSuccessNum) {
        this.mSentSuccessNum = sentSuccessNum;
        return this;
    }

    public int getBytesSent() {
        return mBytesSent;
    }

    public SDKStaticsEvent setBytesSent(int bytesSent) {
        this.mBytesSent = bytesSent;
        return this;
    }

    public int getFailedNum() {
        return mFailedNum;
    }

    public SDKStaticsEvent setFailedNum(int failedNum) {
        this.mFailedNum = failedNum;
        return this;
    }

    public int getInvalidNum() {
        return mInvalidNum;
    }

    public SDKStaticsEvent setInvalidNum(int invalidNum) {
        this.mInvalidNum = invalidNum;
        return this;
    }

    @Override
    public String toString() {
        return "SDKStaticsEvent{" +
                "id=" + mId +
                ", hourTime='" + mHourTime + '\'' +
                ", savedNum=" + mSavedNum +
                ", sentSuccessNum=" + mSentSuccessNum +
                ", bytesSent=" + mBytesSent +
                ", failedNum=" + mFailedNum +
                ", invalidNum=" + mInvalidNum +
                '}';
    }
}
