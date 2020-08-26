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

import com.growingio.android.sdk.track.events.base.BaseEvent;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

public final class ConversionVariablesEvent extends BaseEvent {
    private static final long serialVersionUID = 1L;

    private final Map<String, String> mVariables;

    protected ConversionVariablesEvent(Builder eventBuilder) {
        super(eventBuilder);
        mVariables = eventBuilder.mVariables;
    }

    @Override
    public JSONObject toJSONObject() {
        JSONObject json = super.toJSONObject();
        try {
            if (mVariables != null && !mVariables.isEmpty()) {
                json.put("variables", new JSONObject(mVariables));
            }
        } catch (JSONException ignored) {
        }
        return json;
    }

    public Map<String, String> getVariables() {
        return mVariables;
    }

    public static final class Builder extends BaseBuilder<ConversionVariablesEvent> {
        private Map<String, String> mVariables;

        public Builder() {
            super();
        }

        @Override
        public String getEventType() {
            return TrackEventType.CONVERSION_VARIABLES;
        }

        public Builder setVariables(Map<String, String> variables) {
            mVariables = variables;
            return this;
        }

        @Override
        public ConversionVariablesEvent build() {
            return new ConversionVariablesEvent(this);
        }
    }
}
