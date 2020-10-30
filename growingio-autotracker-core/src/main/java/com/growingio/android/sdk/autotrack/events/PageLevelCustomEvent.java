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

import com.growingio.android.sdk.track.events.CustomEvent;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

public class PageLevelCustomEvent extends CustomEvent {
    private static final long serialVersionUID = 1L;

    private final String mPageName;
    private final long mPageShowTimestamp;

    protected PageLevelCustomEvent(Builder eventBuilder) {
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
            json.put("pageName", mPageName);
            json.put("pageShowTimestamp", mPageShowTimestamp);
        } catch (JSONException ignored) {
        }
        return json;
    }

    public static class Builder extends CustomEvent.Builder {
        private String mPageName;
        private long mPageShowTimestamp;

        public Builder() {
        }

        public Builder setPageName(String pageName) {
            mPageName = pageName;
            return this;
        }

        public Builder setPageShowTimestamp(long pageShowTimestamp) {
            mPageShowTimestamp = pageShowTimestamp;
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
        public PageLevelCustomEvent build() {
            return new PageLevelCustomEvent(this);
        }
    }
}
