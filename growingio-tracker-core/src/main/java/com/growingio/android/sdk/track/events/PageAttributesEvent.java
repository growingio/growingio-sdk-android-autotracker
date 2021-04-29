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

import com.growingio.android.sdk.track.events.base.BaseAttributesEvent;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

public class PageAttributesEvent extends BaseAttributesEvent {
    private static final long serialVersionUID = 1L;

    private final String mPath;
    private final long mPageShowTimestamp;

    protected PageAttributesEvent(Builder eventBuilder) {
        super(eventBuilder);
        mPath = eventBuilder.mPath;
        mPageShowTimestamp = eventBuilder.mPageShowTimestamp;
    }

    public String getPath() {
        return mPath;
    }

    public long getPageShowTimestamp() {
        return mPageShowTimestamp;
    }

    @Override
    public JSONObject toJSONObject() {
        JSONObject json = super.toJSONObject();
        try {
            json.put("path", mPath);
            json.put("pageShowTimestamp", mPageShowTimestamp);
        } catch (JSONException ignored) {
        }
        return json;
    }

    public static class Builder extends BaseAttributesEvent.Builder<PageAttributesEvent> {
        private String mPath;
        private long mPageShowTimestamp;

        public Builder() {
            super();
        }

        public Builder setPath(String path) {
            mPath = path;
            return this;
        }

        public Builder setPageShowTimestamp(long pageShowTimestamp) {
            mPageShowTimestamp = pageShowTimestamp;
            return this;
        }

        @Override
        public Builder setAttributes(Map<String, String> attributes) {
            super.setAttributes(attributes);
            return this;
        }

        @Override
        public String getEventType() {
            return AutotrackEventType.PAGE_ATTRIBUTES;
        }

        @Override
        public PageAttributesEvent build() {
            return new PageAttributesEvent(this);
        }
    }
}
