/*
 * Copyright (C) 2023 Beijing Yishu Technology Co., Ltd.
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

import androidx.annotation.Nullable;

import com.growingio.sdk.annotation.json.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

@JsonSerializer
public abstract class BaseAttributesEvent extends BaseEvent {
    @Nullable
    private final Map<String, String> attributes;

    protected BaseAttributesEvent(Builder<?> eventBuilder) {
        super(eventBuilder);
        attributes = eventBuilder.attributes;
    }

    @Nullable
    public Map<String, String> getAttributes() {
        return attributes;
    }

    public abstract static class Builder<T extends BaseAttributesEvent> extends BaseBuilder<T> {
        private Map<String, String> attributes;

        protected Builder(String eventType) {
            super(eventType);
        }

        public Builder<T> setGeneralProps(Map<String, String> generalProps) {
            if (generalProps != null && !generalProps.isEmpty()) {
                Map<String, String> newAttributes = new HashMap<>();
                if (this.attributes != null) newAttributes.putAll(this.attributes);
                for (String key : generalProps.keySet()) {
                    if (newAttributes.containsKey(key)) continue;
                    String value = generalProps.get(key);
                    newAttributes.put(key, value);
                }
                setAttributes(newAttributes);
            }
            return this;
        }

        public Builder<T> setAttributes(Map<String, String> attributes) {
            this.attributes = attributes;
            return this;
        }

        protected Map<String, String> getAttributes() {
            return attributes;
        }
    }
}
