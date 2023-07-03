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

package com.growingio.android.sdk.track.events;

import android.text.TextUtils;

import com.growingio.android.sdk.track.events.base.BaseAttributesEvent;

import org.json.JSONException;
import org.json.JSONObject;

public class ViewElementEvent extends BaseAttributesEvent {
    private static final long serialVersionUID = 1L;

    private final String mPath;
    private final String mTextValue;

    private final long mPageShowTimestamp;
    private final String mXpath;
    private final int mIndex;

    protected ViewElementEvent(Builder eventBuilder) {
        super(eventBuilder);
        mPath = eventBuilder.mPath;
        mTextValue = eventBuilder.mTextValue;
        mPageShowTimestamp = eventBuilder.mPageShowTimestamp;
        mXpath = eventBuilder.mXpath;
        mIndex = eventBuilder.mIndex;
    }

    public String getPath() {
        return checkValueSafe(mPath);
    }

    public String getTextValue() {
        return checkValueSafe(mTextValue);
    }

    public long getPageShowTimestamp() {
        return mPageShowTimestamp;
    }

    public String getXpath() {
        return checkValueSafe(mXpath);
    }

    public int getIndex() {
        return mIndex;
    }

    @Override
    public JSONObject toJSONObject() {
        JSONObject json = super.toJSONObject();
        try {
            json.put("path", getPath());
            if (!TextUtils.isEmpty(getTextValue())) {
                json.put("textValue", getTextValue());
            }
            json.put("pageShowTimestamp", getPageShowTimestamp());
            json.put("xpath", getXpath());
            json.put("index", getIndex());
        } catch (JSONException ignored) {
        }
        return json;
    }

    public static class Builder extends BaseAttributesEvent.Builder<ViewElementEvent> {
        private String mPath;
        private String mTextValue;
        private String mXpath;
        private int mIndex = -1;

        private long mPageShowTimestamp;

        public Builder() {
            super(AutotrackEventType.VIEW_CLICK);
        }


        public Builder(String eventType) {
            super(eventType);
        }

        public Builder setPageShowTimestamp(long pageShowTimestamp) {
            mPageShowTimestamp = pageShowTimestamp;
            return this;
        }

        public Builder setEventType(String eventType) {
            mEventType = eventType;
            return this;
        }

        public Builder setPath(String path) {
            mPath = path;
            return this;
        }

        public Builder setTextValue(String textValue) {
            mTextValue = textValue;
            return this;
        }

        public Builder setXpath(String xpath) {
            mXpath = xpath;
            return this;
        }

        public Builder setIndex(int index) {
            mIndex = index;
            return this;
        }

        public String getPath() {
            return mPath;
        }

        @Override
        public ViewElementEvent build() {
            return new ViewElementEvent(this);
        }
    }
}
