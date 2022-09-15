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

import org.json.JSONObject;

public class AppClosedEvent extends BaseEvent {
    private static final long serialVersionUID = 1L;

    protected AppClosedEvent(Builder eventBuilder) {
        super(eventBuilder);
    }

    @Override
    public JSONObject toJSONObject() {
        return super.toJSONObject();
    }

    public static final class Builder extends BaseBuilder<AppClosedEvent> {

        protected Builder() {
            super(TrackEventType.APP_CLOSED);
        }

        @Override
        public AppClosedEvent build() {
            return new AppClosedEvent(this);
        }

        @Override
        public void readPropertyInTrackThread() {
            super.readPropertyInTrackThread();
        }
    }
}
