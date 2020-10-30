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

package com.growingio.android.sdk.autotrack.hybrid.event;

import com.growingio.android.sdk.autotrack.events.ViewElementEvent;

import org.json.JSONException;
import org.json.JSONObject;

public final class HybridViewElementEvent extends ViewElementEvent {
    private static final long serialVersionUID = 1L;

    private final String mQuery;
    private final String mHyperlink;

    protected HybridViewElementEvent(Builder eventBuilder) {
        super(eventBuilder);
        mQuery = eventBuilder.mQuery;
        mHyperlink = eventBuilder.mHyperlink;
    }

    public String getQuery() {
        return mQuery;
    }

    public String getHyperlink() {
        return mHyperlink;
    }

    @Override
    public JSONObject toJSONObject() {
        JSONObject json = super.toJSONObject();
        try {
            json.put("query", mQuery);
            json.put("hyperlink", mHyperlink);
        } catch (JSONException ignored) {
        }
        return json;
    }

    public final static class Builder extends ViewElementEvent.Builder {
        private String mQuery;
        private String mHyperlink;

        public Builder() {
            super();
        }

        @Override
        public String getEventType() {
            return mEventType;
        }

        public Builder setEventType(String eventType) {
            mEventType = eventType;
            return this;
        }

        @Override
        public HybridViewElementEvent build() {
            return new HybridViewElementEvent(this);
        }

        public Builder setQuery(String query) {
            mQuery = query;
            return this;
        }

        public Builder setHyperlink(String hyperlink) {
            mHyperlink = hyperlink;
            return this;
        }

        public Builder setDomain(String domain) {
            mDomain = domain;
            return this;
        }

        @Override
        public Builder setPageName(String pageName) {
            super.setPageName(pageName);
            return this;
        }

        @Override
        public Builder setPageShowTimestamp(long pageShowTimestamp) {
            super.setPageShowTimestamp(pageShowTimestamp);
            return this;
        }

        @Override
        public Builder setTextValue(String textValue) {
            super.setTextValue(textValue);
            return this;
        }

        @Override
        public Builder setXpath(String xpath) {
            super.setXpath(xpath);
            return this;
        }

        @Override
        public Builder setIndex(int index) {
            super.setIndex(index);
            return this;
        }
    }
}
