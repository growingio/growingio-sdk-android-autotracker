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
package com.growingio.android.sdk.track.events;

import com.growingio.android.sdk.track.events.base.BaseAttributesEvent;
import com.growingio.android.sdk.track.utils.ConstantPool;
import com.growingio.sdk.annotation.json.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

@JsonSerializer
public class CustomEvent extends BaseAttributesEvent {
    private static final long serialVersionUID = 1L;

    private final String eventName;

    protected CustomEvent(Builder eventBuilder) {
        super(eventBuilder);
        eventName = eventBuilder.eventName;
    }

    public String getEventName() {
        return checkValueSafe(eventName);
    }

    public static class Builder extends BaseAttributesEvent.Builder<CustomEvent> {
        private String eventName;
        private int customEventType = ConstantPool.CUSTOM_TYPE_SYSTEM;

        public Builder() {
            super(TrackEventType.CUSTOM);
        }

        public Builder setEventName(String eventName) {
            this.eventName = eventName;
            return this;
        }

        public String getEventName() {
            return eventName;
        }

        public Builder setCustomEventType(int customEventType) {
            this.customEventType = customEventType;
            return this;
        }

        public Builder setGeneralProps(Map<String, String> generalProps) {
            if (customEventType == ConstantPool.CUSTOM_TYPE_USER) {
                if (generalProps != null && generalProps.size() > 0) {
                    Map<String, String> attributes = getAttributes();
                    if (attributes == null) attributes = new HashMap<>();
                    for (String key : generalProps.keySet()) {
                        if (attributes.containsKey(key)) continue;
                        String value = generalProps.get(key);
                        attributes.put(key, value);
                    }
                    setAttributes(attributes);
                }
            }
            return this;
        }

        @Override
        public CustomEvent build() {
            return new CustomEvent(this);
        }
    }
}
