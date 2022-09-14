/*
 *   Copyright (C) 2020 Beijing Yishu Technology Co., Ltd.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package com.growingio.android.advert;

import android.text.TextUtils;

import com.growingio.android.sdk.TrackerContext;
import com.growingio.android.sdk.track.events.TrackEventType;
import com.growingio.android.sdk.track.events.base.BaseAttributesEvent;
import com.growingio.android.sdk.track.providers.DeviceInfoProvider;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * <p>
 *
 * @author cpacm 2022/8/3
 */
public class ActivateEvent extends BaseAttributesEvent {

    private static final String TYPE_NAME = TrackEventType.ACTIVATE;

    private final String oaid;
    private final String googleId;
    private final String ua;

    protected ActivateEvent(Builder eventBuilder) {
        super(eventBuilder);
        oaid = eventBuilder.oaid;
        googleId = eventBuilder.googleId;
        ua = eventBuilder.ua;
    }

    @Override
    public Map<String, String> getAttributes() {
        HashMap<String, String> map = new HashMap<>();
        map.put("ua", ua);
        return map;
    }

    @Override
    public JSONObject toJSONObject() {
        JSONObject json = super.toJSONObject();
        try {

            if (!TextUtils.isEmpty(googleId)) {
                json.put("googleAdvertisingId", googleId);
            }
            if (!TextUtils.isEmpty(oaid)) {
                json.put("oaid", oaid);
            }
        } catch (JSONException ignored) {
        }
        return json;
    }

    public String getOaid() {
        return oaid;
    }

    public String getGoogleId() {
        return googleId;
    }

    public static final class Builder extends BaseAttributesEvent.Builder<ActivateEvent> {
        private String oaid;
        private String googleId;
        private String ua;

        public Builder() {
            super();
        }

        @Override
        public String getEventType() {
            return TYPE_NAME;
        }

        @Override
        public void readPropertyInTrackThread() {
            super.readPropertyInTrackThread();
            DeviceInfoProvider deviceInfo = DeviceInfoProvider.get();
            oaid = deviceInfo.getOaid();
            googleId = deviceInfo.getGoogleAdId();
            ua = AdvertUtils.getUserAgent(TrackerContext.get());
        }

        @Override
        public ActivateEvent build() {
            return new ActivateEvent(this);
        }
    }
}
