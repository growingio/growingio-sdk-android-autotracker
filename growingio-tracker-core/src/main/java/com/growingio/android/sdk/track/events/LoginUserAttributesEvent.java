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

import java.util.Map;

public final class LoginUserAttributesEvent extends BaseAttributesEvent {
    private static final long serialVersionUID = 1L;

    protected LoginUserAttributesEvent(Builder eventBuilder) {
        super(eventBuilder);
    }

    public static final class Builder extends BaseAttributesEvent.Builder<LoginUserAttributesEvent> {
        public Builder() {
            super();
        }

        @Override
        public String getEventType() {
            return TrackEventType.LOGIN_USER_ATTRIBUTES;
        }

        @Override
        public LoginUserAttributesEvent build() {
            return new LoginUserAttributesEvent(this);
        }

        @Override
        public Builder setAttributes(Map<String, String> attributes) {
            super.setAttributes(attributes);
            return this;
        }
    }
}
