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

package com.growingio.android.sdk.track.timer;

public class Timer {
    private final String mEventName;
    private long mStartTime = 0;
    private long mElapsedTime = 0;

    public Timer(long startTime, final String eventName) {
        this.mStartTime = startTime;
        this.mEventName = eventName;
    }

    public String getEventName() {
        return this.mEventName;
    }

    public boolean isResume() {
        return mStartTime != 0;
    }

    public void updateState(long currentTime, boolean isResume) {
        if (isResume() == isResume) {
            return;
        }

        if (isResume) {
            mStartTime = currentTime;
        } else {
            mElapsedTime += currentTime - mStartTime;
            mStartTime = 0;
        }
    }

    public void resetStartTimeBeforeEnterForeground(long currentTime) {
        if (isResume()) {
            this.mStartTime = currentTime;
        }
    }

    public void computeElapsedTimeBeforeEnterBackground(long currentTime) {
        if (isResume()) {
            this.mElapsedTime += currentTime - mStartTime;
            this.mStartTime = currentTime;
        }
    }

    public String computeElapsedTime(long currentTime) {
        if (isResume()) {
            mElapsedTime += currentTime - mStartTime;
        }
        if (mElapsedTime < 0 || mElapsedTime > 24 * 60 * 60 * 1000L) {
            return "0";
        }

        return String.valueOf(mElapsedTime / 1000F);
    }
}
