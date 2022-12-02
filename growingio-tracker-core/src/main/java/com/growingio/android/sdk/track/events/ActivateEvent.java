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

package com.growingio.android.sdk.track.events;

import android.text.TextUtils;

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

    private static final String EVENT_REENGAGE = "$app_reengage";
    private static final String EVENT_DEFER = "$app_defer";
    private static final String EVENT_ACTIVATE = "$app_activation";

    private final String eventName;

    private final String oaid;
    private final String googleId;
    private final String ua;
    private final String androidId;
    private final String imei;

    private final String linkId;
    private final String clickId;
    private final String clickTm;
    private final String params;
    private final String classification; // cl 类别

    protected ActivateEvent(Builder eventBuilder) {
        super(eventBuilder);
        eventName = eventBuilder.eventName;
        oaid = eventBuilder.oaid;
        googleId = eventBuilder.googleId;
        ua = eventBuilder.ua;
        androidId = eventBuilder.androidId;
        imei = eventBuilder.imei;

        linkId = eventBuilder.linkId;
        clickId = eventBuilder.clickId;
        clickTm = eventBuilder.clickTm;
        params = eventBuilder.params;
        classification = eventBuilder.classification;
    }

    @Override
    public Map<String, String> getAttributes() {
        HashMap<String, String> map = new HashMap<>();
        if (!TextUtils.isEmpty(ua)) map.put("userAgent", ua);
        if (!TextUtils.isEmpty(classification)) map.put("deep_type", classification);
        if (!TextUtils.isEmpty(linkId)) map.put("deep_link_id", linkId);
        if (!TextUtils.isEmpty(clickId)) map.put("deep_click_id", clickId);
        if (!TextUtils.isEmpty(clickTm)) map.put("deep_click_time", clickTm);
        if (params != null) map.put("deep_params", params);
        return map;
    }

    @Override
    public JSONObject toJSONObject() {
        JSONObject json = super.toJSONObject();
        try {
            json.put("eventName", eventName);
            if (!TextUtils.isEmpty(googleId)) {
                json.put("googleAdvertisingId", googleId);
            }
            if (!TextUtils.isEmpty(oaid)) {
                json.put("oaid", oaid);
            }
            if (!TextUtils.isEmpty(imei)) {
                json.put("imei", imei);
            }
            if (!TextUtils.isEmpty(androidId)) {
                json.put("androidId", androidId);
            }
        } catch (JSONException ignored) {
        }
        return json;
    }

    public String getOaid() {
        return checkValueSafe(oaid);
    }

    public String getGoogleId() {
        return checkValueSafe(googleId);
    }

    public String getAndroidId() {
        return checkValueSafe(androidId);
    }

    public String getImei() {
        return checkValueSafe(imei);
    }

    public String getEventName() {
        return eventName;
    }

    public static final class Builder extends BaseAttributesEvent.Builder<ActivateEvent> {

        private String eventName;

        private String oaid;
        private String googleId;
        private String ua;
        private String androidId;
        private String imei;

        private String linkId;
        private String clickId;
        private String clickTm;
        private String classification; // cl 类别 defer||inapp
        private String params;


        public Builder() {
            super(TrackEventType.ACTIVATE);
            eventName = EVENT_ACTIVATE;
        }

        public Builder reengage(boolean isInApp) {
            if (isInApp) this.classification = "inapp";
            this.eventName = EVENT_REENGAGE;
            return this;
        }

        public Builder activate() {
            this.eventName = EVENT_ACTIVATE;
            return this;
        }

        public Builder clipDefer() {
            this.eventName = EVENT_DEFER;
            return this;
        }

        public Builder setAdvertData(String linkId, String clickId, String clickTm, String params) {
            this.linkId = linkId;
            this.clickId = clickId;
            this.clickTm = clickTm;
            this.params = params;
            return this;
        }

        @Override
        public void readPropertyInTrackThread() {
            super.readPropertyInTrackThread();
            DeviceInfoProvider deviceInfo = DeviceInfoProvider.get();
            oaid = deviceInfo.getOaid();
            googleId = deviceInfo.getGoogleAdId();
            ua = DeviceInfoProvider.get().getUserAgent();
            androidId = deviceInfo.getAndroidId();
            imei = deviceInfo.getImei();
        }

        @Override
        public ActivateEvent build() {
            return new ActivateEvent(this);
        }
    }
}
