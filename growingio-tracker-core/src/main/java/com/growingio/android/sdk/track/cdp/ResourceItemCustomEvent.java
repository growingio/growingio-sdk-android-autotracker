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

package com.growingio.android.sdk.track.cdp;

import com.growingio.android.sdk.track.events.CustomEvent;
import com.growingio.android.sdk.track.events.TrackEventType;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

public class ResourceItemCustomEvent extends CustomEvent {
    private static final long serialVersionUID = 1L;

    private final ResourceItem mResourceItem;

    protected ResourceItemCustomEvent(Builder eventBuilder) {
        super(eventBuilder);
        mResourceItem = eventBuilder.mResourceItem;
    }

    @Override
    public JSONObject toJSONObject() {
        JSONObject jsonObject = super.toJSONObject();
        try {
            if (mResourceItem != null) {
                jsonObject.put("resourceItem", mResourceItem.toJSONObject());
            }
        } catch (JSONException ignored) {
        }
        return jsonObject;
    }

    public static class Builder extends CustomEvent.Builder {
        private ResourceItem mResourceItem;

        public Builder() {
            super();
        }

        @Override
        public String getEventType() {
            return TrackEventType.CUSTOM;
        }

        public Builder setEventName(String eventName) {
            super.setEventName(eventName);
            return this;
        }

        @Override
        public Builder setAttributes(Map<String, String> attributes) {
            super.setAttributes(attributes);
            return this;
        }

        public Builder setResourceItem(ResourceItem resourceItem) {
            mResourceItem = resourceItem;
            return this;
        }

        @Override
        public ResourceItemCustomEvent build() {
            return new ResourceItemCustomEvent(this);
        }
    }
}
