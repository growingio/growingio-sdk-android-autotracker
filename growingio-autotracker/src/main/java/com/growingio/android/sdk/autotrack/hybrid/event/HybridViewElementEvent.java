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

import com.growingio.android.sdk.autotrack.events.base.BaseViewElementEvent;
import com.growingio.android.sdk.track.CoreAppState;
import com.growingio.android.sdk.track.events.EventType;

import org.json.JSONException;
import org.json.JSONObject;

public final class HybridViewElementEvent extends BaseViewElementEvent {
    private static final long serialVersionUID = 1L;

    private final String mQueryParameters;

    protected HybridViewElementEvent(EventBuilder eventBuilder) {
        super(eventBuilder);
        mQueryParameters = eventBuilder.mQueryParameters;
    }

    public String getQueryParameters() {
        return mQueryParameters;
    }

    @Override
    public JSONObject toJSONObject() {
        JSONObject json = super.toJSONObject();
        try {
            json.put("mQueryParameters", mQueryParameters);
        } catch (JSONException ignored) {
        }
        return json;
    }

    public final static class EventBuilder extends BaseViewElementEvent.EventBuilder<HybridViewElementEvent> {
        private String mQueryParameters;

        public EventBuilder(CoreAppState coreAppState) {
            super(coreAppState);
        }

        @Override
        public EventType getEventType() {
            return mEventType;
        }

        public EventBuilder setEventType(EventType eventType) {
            mEventType = eventType;
            return this;
        }

        @Override
        public HybridViewElementEvent build() {
            return new HybridViewElementEvent(this);
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
    }
}
