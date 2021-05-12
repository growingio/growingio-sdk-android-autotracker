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

import android.content.Context;

import com.growingio.android.sdk.TrackerContext;
import com.growingio.android.sdk.track.events.base.BaseEvent;
import com.growingio.android.sdk.track.utils.NetworkUtil;

import org.json.JSONException;
import org.json.JSONObject;

public class AppClosedEvent extends BaseEvent {
    private static final long serialVersionUID = 1L;

    private final String mNetworkState;

    protected AppClosedEvent(Builder eventBuilder) {
        super(eventBuilder);
        mNetworkState = eventBuilder.mNetworkState;
    }

    @Override
    public JSONObject toJSONObject() {
        JSONObject json = super.toJSONObject();
        try {
            json.put("networkState", mNetworkState);
        } catch (JSONException ignored) {
        }
        return json;
    }

    public static final class Builder extends BaseBuilder<AppClosedEvent> {
        private String mNetworkState;

        @Override
        public String getEventType() {
            return TrackEventType.APP_CLOSED;
        }

        @Override
        public AppClosedEvent build() {
            return new AppClosedEvent(this);
        }

        @Override
        public void readPropertyInTrackThread() {
            super.readPropertyInTrackThread();
            Context context = TrackerContext.get().getApplicationContext();
            mNetworkState = NetworkUtil.getActiveNetworkState(context).getNetworkName();
        }
    }
}
