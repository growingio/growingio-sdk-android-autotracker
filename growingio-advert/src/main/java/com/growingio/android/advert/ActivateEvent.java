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

    private final String oaid;
    private final String googleId;
    private final String ua;
    private final String androidId;
    private final String imei;

    protected ActivateEvent(Builder eventBuilder) {
        super(eventBuilder);
        oaid = eventBuilder.oaid;
        googleId = eventBuilder.googleId;
        ua = eventBuilder.ua;
        androidId = eventBuilder.androidId;
        imei = eventBuilder.imei;
    }

    @Override
    public Map<String, String> getAttributes() {
        HashMap<String, String> map = new HashMap<>();
        map.put("userAgent", ua);
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
            if (!TextUtils.isEmpty(imei)) {
                json.put("imei",imei);
            }
            if (!TextUtils.isEmpty(androidId)) {
                json.put("androidId", androidId);
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

    public String getAndroidId() {
        return androidId;
    }

    public String getImei() {
        return imei;
    }

    public static final class Builder extends BaseAttributesEvent.Builder<ActivateEvent> {
        private String oaid;
        private String googleId;
        private String ua;
        private String androidId;
        private String imei;


        public Builder() {
            super(TrackEventType.ACTIVATE);
        }

        @Override
        public void readPropertyInTrackThread() {
            super.readPropertyInTrackThread();
            DeviceInfoProvider deviceInfo = DeviceInfoProvider.get();
            oaid = deviceInfo.getOaid();
            googleId = deviceInfo.getGoogleAdId();
            ua = AdvertUtils.getUserAgent(TrackerContext.get());
            androidId = deviceInfo.getAndroidId();
            imei = deviceInfo.getImei();
        }

        @Override
        public ActivateEvent build() {
            return new ActivateEvent(this);
        }
    }
}
