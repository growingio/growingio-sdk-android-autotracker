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

package com.growingio.android.sdk.autotrack.events;

import android.text.TextUtils;

import com.growingio.android.sdk.track.events.base.BaseEvent;

import org.json.JSONException;
import org.json.JSONObject;

public class ViewElementEvent extends BaseEvent {
    private static final long serialVersionUID = 1L;

    private final String mPageName;
    private final long mPageShowTimestamp;
    private final String mTextValue;
    private final String mXpath;
    private final int mIndex;

    protected ViewElementEvent(Builder eventBuilder) {
        super(eventBuilder);
        mPageName = eventBuilder.mPageName;
        mPageShowTimestamp = eventBuilder.mPageShowTimestamp;
        mTextValue = eventBuilder.mTextValue;
        mXpath = eventBuilder.mXpath;
        mIndex = eventBuilder.mIndex;
    }

    public String getPageName() {
        return mPageName;
    }

    public long getPageShowTimestamp() {
        return mPageShowTimestamp;
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

    @Override
    public JSONObject toJSONObject() {
        JSONObject json = super.toJSONObject();
        try {
            json.put("pageName", mPageName);
            json.put("pageShowTimestamp", mPageShowTimestamp);
            if (!TextUtils.isEmpty(mTextValue)) {
                json.put("textValue", mTextValue);
            }
            json.put("xpath", mXpath);
            json.put("index", mIndex);
        } catch (JSONException ignored) {
        }
        return json;
    }

    public static class Builder extends BaseBuilder<ViewElementEvent> {
        private String mPageName;
        private long mPageShowTimestamp;
        private String mTextValue;
        private String mXpath;
        private int mIndex = -1;

        public Builder() {
            super();
        }

        @Override
        public String getEventType() {
            return null;
        }

        public Builder setEventType(String eventType) {
            mEventType = eventType;
            return this;
        }

        public Builder setPageName(String pageName) {
            mPageName = pageName;
            return this;
        }

        public Builder setPageShowTimestamp(long pageShowTimestamp) {
            mPageShowTimestamp = pageShowTimestamp;
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

        @Override
        public ViewElementEvent build() {
            return new ViewElementEvent(this);
        }
    }
}
