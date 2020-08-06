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

import com.growingio.android.sdk.track.events.EventType;
import com.growingio.android.sdk.track.events.base.BaseAttributesEvent;

import org.json.JSONException;
import org.json.JSONObject;

public abstract class BasePageAttributesEvent extends BaseAttributesEvent {
    private final String mPageName;
    private final long mPageShowTimestamp;

    protected BasePageAttributesEvent(EventBuilder<?> eventBuilder) {
        super(eventBuilder);
        mPageName = eventBuilder.mPageName;
        mPageShowTimestamp = eventBuilder.mPageShowTimestamp;
    }

    public String getPageName() {
        return mPageName;
    }

    public long getPageShowTimestamp() {
        return mPageShowTimestamp;
    }

    @Override
    public JSONObject toJSONObject() {
        JSONObject json = super.toJSONObject();
        try {
            json.put("mPageName", mPageName);
            json.put("mPageShowTimestamp", mPageShowTimestamp);
        } catch (JSONException ignored) {
        }
        return json;
    }

    public abstract static class EventBuilder<T extends BasePageAttributesEvent> extends BaseAttributesEvent.EventBuilder<T> {
        private String mPageName;
        private long mPageShowTimestamp;

        public EventBuilder() {
            super();
        }

        public String getPageName() {
            return mPageName;
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

        @Override
        public EventType getEventType() {
            return EventType.PAGE_ATTRIBUTES;
        }
    }
}
