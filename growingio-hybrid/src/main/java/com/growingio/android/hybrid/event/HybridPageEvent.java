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

package com.growingio.android.hybrid.event;

import com.growingio.android.sdk.track.events.PageEvent;

import org.json.JSONException;
import org.json.JSONObject;

public class HybridPageEvent extends PageEvent {
    private static final long serialVersionUID = 1L;

    private final String mProtocolType;
    private final String mQuery;

    protected HybridPageEvent(Builder eventBuilder) {
        super(eventBuilder);
        mProtocolType = eventBuilder.mProtocolType;
        mQuery = eventBuilder.mQuery;
    }

    public String getQuery() {
        return mQuery;
    }

    public String getProtocolType() {
        return mProtocolType;
    }

    @Override
    public JSONObject toJSONObject() {
        JSONObject json = super.toJSONObject();
        try {
            json.put("protocolType", mProtocolType);
            json.put("query", mQuery);
        } catch (JSONException ignored) {
        }
        return json;
    }

    public static class Builder extends PageEvent.Builder {
        private String mProtocolType;
        private String mQuery;

        public Builder() {
            super();
        }

        public Builder setProtocolType(String protocolType) {
            mProtocolType = protocolType;
            return this;
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
        public HybridPageEvent build() {
            return new HybridPageEvent(this);
        }

        @Override
        public Builder setPath(String path) {
            super.setPath(path);
            return this;
        }

        @Override
        public Builder setTitle(String title) {
            super.setTitle(title);
            return this;
        }

        @Override
        public Builder setReferralPage(String referralPage) {
            super.setReferralPage(referralPage);
            return this;
        }

        @Override
        public Builder setTimestamp(long timestamp) {
            super.setTimestamp(timestamp);
            return this;
        }

        @Override
        public Builder setOrientation(String orientation) {
            super.setOrientation(orientation);
            return this;
        }
    }
}
