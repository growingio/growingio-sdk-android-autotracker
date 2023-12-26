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

import android.text.TextUtils;

import androidx.annotation.Nullable;

import com.growingio.android.sdk.TrackerContext;
import com.growingio.android.sdk.track.events.base.BaseAttributesEvent;
import com.growingio.android.sdk.track.providers.DeviceInfoProvider;
import com.growingio.sdk.annotation.json.JsonAlias;
import com.growingio.sdk.annotation.json.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

/**
 * <p>
 *
 * @author cpacm 2022/8/3
 */
@JsonSerializer
public class ActivateEvent extends BaseAttributesEvent {

    private static final String EVENT_REENGAGE = "$app_reengage";
    private static final String EVENT_DEFER = "$app_defer";
    private static final String EVENT_ACTIVATE = "$app_activation";

    private final String eventName;

    @Nullable
    private final String oaid;
    @Nullable
    @JsonAlias(name = "googleAdvertisingId")
    private final String googleId;
    @Nullable
    private final String androidId;
    @Nullable
    private final String imei;

    protected ActivateEvent(Builder eventBuilder) {
        super(eventBuilder);
        eventName = eventBuilder.eventName;
        oaid = eventBuilder.oaid;
        googleId = eventBuilder.googleId;
        androidId = eventBuilder.androidId;
        imei = eventBuilder.imei;
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

        String eventName;
        String oaid;
        String googleId;
        String ua;
        String androidId;
        String imei;

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
        public void readPropertyInTrackThread(TrackerContext context) {
            super.readPropertyInTrackThread(context);
            DeviceInfoProvider deviceInfo = context.getDeviceInfoProvider();
            oaid = deviceInfo.getOaid();
            googleId = deviceInfo.getGoogleAdId();
            ua = deviceInfo.getUserAgent();
            androidId = deviceInfo.getAndroidId();
            imei = deviceInfo.getImei();
        }

        @Override
        public ActivateEvent build() {
            Map<String, String> map = getAttributes();
            if (map == null) map = new HashMap<>();
            if (!TextUtils.isEmpty(ua)) map.put("userAgent", ua);
            if (!TextUtils.isEmpty(classification)) map.put("deep_type", classification);
            if (!TextUtils.isEmpty(linkId)) map.put("deep_link_id", linkId);
            if (!TextUtils.isEmpty(clickId)) map.put("deep_click_id", clickId);
            if (!TextUtils.isEmpty(clickTm)) map.put("deep_click_time", clickTm);
            if (params != null) map.put("deep_params", params);
            setAttributes(map);
            return new ActivateEvent(this);
        }
    }
}
