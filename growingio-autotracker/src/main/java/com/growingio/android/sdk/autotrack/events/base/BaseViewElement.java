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

package com.growingio.android.sdk.autotrack.events.base;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

public abstract class BaseViewElement implements Serializable {
    private final long mTimestamp;
    private final String mTextValue;
    private final String mXpath;
    private final int mIndex;

    private final int mGlobalSequenceId;
    private final int mEventSequenceId;

    public BaseViewElement(BaseElementBuilder<?> elementBuilder) {
        mTimestamp = elementBuilder.mTimestamp;
        mTextValue = elementBuilder.mTextValue;
        mXpath = elementBuilder.mXpath;
        mIndex = elementBuilder.mIndex;
        mGlobalSequenceId = elementBuilder.mGlobalSequenceId;
        mEventSequenceId = elementBuilder.mEventSequenceId;
    }

    public long getTimestamp() {
        return mTimestamp;
    }

    public String getTextValue() {
        return mTextValue;
    }

    public String getXpath() {
        return mXpath;
    }

    public int getIndex() {
        return mIndex;
    }

    public int getGlobalSequenceId() {
        return mGlobalSequenceId;
    }

    public int getEventSequenceId() {
        return mEventSequenceId;
    }

    public JSONObject toJSONObject() {
        JSONObject json = new JSONObject();
        try {
            json.put("mTimestamp", mTimestamp);
            json.put("mTextValue", mTextValue);
            json.put("mXpath", mXpath);
            json.put("mIndex", mIndex);
            json.put("mGlobalSequenceId", mGlobalSequenceId);
            json.put("mEventSequenceId", mEventSequenceId);
        } catch (JSONException ignored) {
        }
        return json;
    }

    public abstract static class BaseElementBuilder<T extends BaseViewElement> {
        private long mTimestamp;
        private String mTextValue;
        private String mXpath;
        private int mIndex = -1;

        private int mGlobalSequenceId;
        private int mEventSequenceId;

        public long getTimestamp() {
            return mTimestamp;
        }

        public BaseElementBuilder<T> setTimestamp(long timestamp) {
            mTimestamp = timestamp;
            return this;
        }

        public String getTextValue() {
            return mTextValue;
        }

        public BaseElementBuilder<T> setTextValue(String textValue) {
            mTextValue = textValue;
            return this;
        }

        public String getXpath() {
            return mXpath;
        }

        public BaseElementBuilder<T> setXpath(String xpath) {
            mXpath = xpath;
            return this;
        }

        public int getIndex() {
            return mIndex;
        }

        public BaseElementBuilder<T> setIndex(int index) {
            mIndex = index;
            return this;
        }

        public int getGlobalSequenceId() {
            return mGlobalSequenceId;
        }

        BaseElementBuilder<T> setGlobalSequenceId(int globalSequenceId) {
            mGlobalSequenceId = globalSequenceId;
            return this;
        }

        public int getEventSequenceId() {
            return mEventSequenceId;
        }

        BaseElementBuilder<T> setEventSequenceId(int eventSequenceId) {
            mEventSequenceId = eventSequenceId;
            return this;
        }

        public abstract T build();
    }
}
