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
package com.growingio.android.sdk.track.log;

public class LogItem {
    private final int mPriority;
    private final String mTag;
    private final String mMessage;
    private final Throwable mThrowable;
    private final long mTimeStamp;

    private LogItem(int priority, String tag, String message, Throwable throwable, long timeStamp) {
        mPriority = priority;
        mTag = tag;
        mMessage = message;
        mThrowable = throwable;
        mTimeStamp = timeStamp;
    }

    public int getPriority() {
        return mPriority;
    }

    public String getTag() {
        return mTag;
    }

    public String getMessage() {
        return mMessage;
    }

    public Throwable getThrowable() {
        return mThrowable;
    }

    public long getTimeStamp() {
        return mTimeStamp;
    }

    public static class Builder {
        private int mPriority;
        private String mTag;
        private String mMessage;
        private Throwable mThrowable;
        private long mTimeStamp;

        public Builder() {
        }

        public Builder setPriority(int priority) {
            mPriority = priority;
            return this;
        }

        public Builder setTag(String tag) {
            mTag = tag;
            return this;
        }

        public Builder setMessage(String message) {
            mMessage = message;
            return this;
        }

        public Builder setThrowable(Throwable throwable) {
            mThrowable = throwable;
            return this;
        }

        public Builder setTimeStamp(long timeStamp) {
            mTimeStamp = timeStamp;
            return this;
        }

        public LogItem build() {
            return new LogItem(mPriority, mTag, mMessage, mThrowable, mTimeStamp);
        }
    }
}
