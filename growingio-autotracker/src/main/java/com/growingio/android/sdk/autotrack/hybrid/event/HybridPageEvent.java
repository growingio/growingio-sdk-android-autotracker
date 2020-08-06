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

import com.growingio.android.sdk.autotrack.events.base.BasePageEvent;

import org.json.JSONException;
import org.json.JSONObject;

public class HybridPageEvent extends BasePageEvent {
    private static final long serialVersionUID = 1L;

    private final String mProtocolType;
    private final String mQueryParameters;

    protected HybridPageEvent(EventBuilder eventBuilder) {
        super(eventBuilder);
        mProtocolType = eventBuilder.mProtocolType;
        mQueryParameters = eventBuilder.mQueryParameters;
    }

    public String getQueryParameters() {
        return mQueryParameters;
    }

    public String getProtocolType() {
        return mProtocolType;
    }

    @Override
    public JSONObject toJSONObject() {
        JSONObject json = super.toJSONObject();
        try {
            json.put("mProtocolType", mProtocolType);
            json.put("mQueryParameters", mQueryParameters);
        } catch (JSONException ignored) {
        }
        return json;
    }

    public static class EventBuilder extends BasePageEvent.EventBuilder<HybridPageEvent> {
        private String mProtocolType;
        private String mQueryParameters;

        public EventBuilder() {
            super();
        }

        public String getProtocolType() {
            return mProtocolType;
        }

        public EventBuilder setProtocolType(String protocolType) {
            mProtocolType = protocolType;
            return this;
        }

        public String getQueryParameters() {
            return mQueryParameters;
        }

        public EventBuilder setQueryParameters(String queryParameters) {
            mQueryParameters = queryParameters;
            return this;
        }

        public EventBuilder setDomain(String domain) {
            mDomain = domain;
            return this;
        }

        @Override
        public HybridPageEvent build() {
            return new HybridPageEvent(this);
        }
    }
}
