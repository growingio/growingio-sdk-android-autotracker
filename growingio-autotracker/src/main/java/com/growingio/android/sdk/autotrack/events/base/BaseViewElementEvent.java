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

import com.growingio.android.sdk.track.events.base.BaseEvent;

import org.json.JSONException;
import org.json.JSONObject;

public abstract class BaseViewElementEvent extends BaseEvent {
    private final String mPageName;
    private final long mPageShowTimestamp;
    private final String mTextValue;
    private final String mXpath;
    private final int mIndex;

    protected BaseViewElementEvent(EventBuilder<?> eventBuilder) {
        super(eventBuilder);
        mPageName = eventBuilder.mPageName;
        mPageShowTimestamp = eventBuilder.mPageShowTimestamp;
        mTextValue = eventBuilder.mTextValue;
        mXpath = eventBuilder.mXpath;
        mIndex = eventBuilder.mIndex;
    }

    @Override
    public JSONObject toJSONObject() {
        JSONObject json = super.toJSONObject();
        try {
            json.put("pageName", mPageName);
            json.put("pageShowTimestamp", mPageShowTimestamp);
            json.put("textValue", mTextValue);
            json.put("xpath", mXpath);
            json.put("index", mIndex);
        } catch (JSONException ignored) {
        }
        return json;
    }

    public abstract static class EventBuilder<T extends BaseViewElementEvent> extends BaseEvent.BaseEventBuilder<T> {
        private String mPageName;
        private long mPageShowTimestamp;
        private String mTextValue;
        private String mXpath;
        private int mIndex;

        protected EventBuilder() {
            super();
        }

        public EventBuilder<T> setPageName(String pageName) {
            mPageName = pageName;
            return this;
        }

        public long getPageShowTimestamp() {
            return mPageShowTimestamp;
        }

        public EventBuilder<T> setPageShowTimestamp(long pageShowTimestamp) {
            mPageShowTimestamp = pageShowTimestamp;
            return this;
        }

        public EventBuilder<T> setTextValue(String textValue) {
            mTextValue = textValue;
            return this;
        }

        public EventBuilder<T> setXpath(String xpath) {
            mXpath = xpath;
            return this;
        }

        public EventBuilder<T> setIndex(int index) {
            mIndex = index;
            return this;
        }
    }
}
