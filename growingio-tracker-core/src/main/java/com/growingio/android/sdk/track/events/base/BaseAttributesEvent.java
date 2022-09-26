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

package com.growingio.android.sdk.track.events.base;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

public abstract class BaseAttributesEvent extends BaseEvent {
    private final Map<String, String> mAttributes;

    protected BaseAttributesEvent(Builder<?> eventBuilder) {
        super(eventBuilder);
        mAttributes = eventBuilder.mAttributes;
    }

    public Map<String, String> getAttributes() {
        return mAttributes;
    }

    @Override
    public JSONObject toJSONObject() {
        JSONObject json = super.toJSONObject();
        try {
            if (getAttributes() != null && !getAttributes().isEmpty()) {
                json.put("attributes", new JSONObject(getAttributes()));
            }
        } catch (JSONException ignored) {
        }
        return json;
    }

    public abstract static class Builder<T extends BaseAttributesEvent> extends BaseBuilder<T> {
        private Map<String, String> mAttributes;

        protected Builder(String eventType) {
            super(eventType);
        }

        public Builder<T> setAttributes(Map<String, String> attributes) {
            mAttributes = attributes;
            return this;
        }
    }
}
