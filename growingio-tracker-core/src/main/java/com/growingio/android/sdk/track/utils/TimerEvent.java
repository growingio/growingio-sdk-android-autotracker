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
package com.growingio.android.sdk.track.utils;

public class TimerEvent {
    private final String eventName;
    private long startTime = 0;
    private long elapsedTime = 0;

    public TimerEvent(long startTime, final String eventName) {
        this.startTime = startTime;
        this.eventName = eventName;
    }

    public String getEventName() {
        return this.eventName;
    }

    public boolean isResume() {
        return startTime != 0;
    }

    public void updateState(long currentTime, boolean isResume) {
        if (isResume() == isResume) {
            return;
        }

        if (isResume) {
            startTime = currentTime;
        } else {
            elapsedTime += currentTime - startTime;
            startTime = 0;
        }
    }

    public void resetStartTimeBeforeEnterForeground(long currentTime) {
        if (isResume()) {
            this.startTime = currentTime;
        }
    }

    public void computeElapsedTimeBeforeEnterBackground(long currentTime) {
        if (isResume()) {
            this.elapsedTime += currentTime - startTime;
            this.startTime = currentTime;
        }
    }

    public String computeElapsedTime(long currentTime) {
        if (isResume()) {
            elapsedTime += currentTime - startTime;
        }
        if (elapsedTime < 0 || elapsedTime > 24 * 60 * 60 * 1000L) {
            return "0";
        }

        return String.valueOf(elapsedTime / 1000F);
    }
}
