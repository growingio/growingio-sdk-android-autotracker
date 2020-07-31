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

import com.growingio.android.sdk.track.CoreAppState;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

public abstract class BaseAttributesEvent extends BaseEventWithSequenceId {
    private final Map<String, String> mAttributes;

    protected BaseAttributesEvent(EventBuilder<?> eventBuilder) {
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
            json.put("mAttributes", mAttributes);
        } catch (JSONException ignored) {
        }
        return json;
    }

    public abstract static class EventBuilder<T extends BaseAttributesEvent> extends BaseEventWithSequenceId.EventBuilder<T> {
        private Map<String, String> mAttributes;

        protected EventBuilder(CoreAppState coreAppState) {
            super(coreAppState);
        }

        public EventBuilder<T> setAttributes(Map<String, String> attributes) {
            mAttributes = attributes;
            return this;
        }
    }
}
