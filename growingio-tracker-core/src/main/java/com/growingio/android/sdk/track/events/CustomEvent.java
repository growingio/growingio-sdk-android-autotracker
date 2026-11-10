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

import androidx.annotation.Nullable;

import com.growingio.android.sdk.track.events.base.BaseAttributesEvent;
import com.growingio.sdk.annotation.json.JsonSerializer;

@JsonSerializer
public class CustomEvent extends BaseAttributesEvent {
    private static final long serialVersionUID = 1L;

    private final String eventName;
    @Nullable
    private final String referralPage;

    protected CustomEvent(Builder eventBuilder) {
        super(eventBuilder);
        eventName = eventBuilder.eventName;
        referralPage = eventBuilder.referralPage;
    }

    public String getEventName() {
        return checkValueSafe(eventName);
    }

    public String getReferralPage() {
        return checkValueSafe(referralPage);
    }

    public static class Builder extends BaseAttributesEvent.Builder<CustomEvent> {
        private String eventName;
        private String referralPage;

        public Builder() {
            super(TrackEventType.CUSTOM);
        }

        public Builder setEventName(String eventName) {
            this.eventName = eventName;
            return this;
        }

        public Builder setReferralPage(String referralPage) {
            this.referralPage = referralPage;
            return this;
        }

        public String getEventName() {
            return eventName;
        }

        public String getReferralPage() {
            return referralPage;
        }

        @Override
        public CustomEvent build() {
            return new CustomEvent(this);
        }
    }
}
