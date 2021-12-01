/*
 *   Copyright (C) 2020 Beijing Yishu Technology Co., Ltd.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package com.growingio.android.sdk.track.events.hybrid;

import com.growingio.android.sdk.track.events.PageLevelCustomEvent;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

public final class HybridCustomEvent extends PageLevelCustomEvent {
    private static final long serialVersionUID = 1L;

    private final String mQuery;

    protected HybridCustomEvent(Builder eventBuilder) {
        super(eventBuilder);
        mQuery = eventBuilder.mQuery;
    }

    public String getQuery() {
        return checkValueSafe(mQuery);
    }

    @Override
    public JSONObject toJSONObject() {
        JSONObject json = super.toJSONObject();
        try {
            json.put("query", mQuery);
        } catch (JSONException ignored) {
        }
        return json;
    }

    public static class Builder extends PageLevelCustomEvent.Builder {
        private String mQuery;

        public Builder() {
            super();
        }

        public Builder setQuery(String query) {
            mQuery = query;
            return this;
        }

        public Builder setDomain(String domain) {
            mDomain = domain;
            return this;
        }

        @Override
        public Builder setPath(String path) {
            super.setPath(path);
            return this;
        }

        @Override
        public Builder setPageShowTimestamp(long pageShowTimestamp) {
            super.setPageShowTimestamp(pageShowTimestamp);
            return this;
        }

        @Override
        public Builder setEventName(String eventName) {
            super.setEventName(eventName);
            return this;
        }

        @Override
        public Builder setAttributes(Map<String, String> attributes) {
            super.setAttributes(attributes);
            return this;
        }

        @Override
        public HybridCustomEvent build() {
            return new HybridCustomEvent(this);
        }
    }
}
