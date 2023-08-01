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

import com.growingio.android.sdk.TrackerContext;
import com.growingio.android.sdk.track.events.base.BaseEvent;
import com.growingio.android.sdk.track.providers.DeviceInfoProvider;
import com.growingio.sdk.annotation.json.JsonSerializer;

import java.util.Map;

@JsonSerializer
public final class VisitEvent extends BaseEvent {
    private static final long serialVersionUID = 1L;

    @Nullable
    private final String imei;
    @Nullable
    private final String androidId;
    @Nullable
    private final String oaid;
    @Nullable
    private final String googleAdvertisingId;
    @Nullable
    private final Map<String, String> extraSdk;

    VisitEvent(Builder eventBuilder) {
        super(eventBuilder);
        imei = eventBuilder.imei;
        androidId = eventBuilder.androidId;
        oaid = eventBuilder.oaid;
        googleAdvertisingId = eventBuilder.googleAdvertisingId;
        extraSdk = eventBuilder.extraSdk;
    }

    @Override
    public int getSendPolicy() {
        return SEND_POLICY_INSTANT;
    }

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public String getImei() {
        return checkValueSafe(imei);
    }

    public String getAndroidId() {
        return checkValueSafe(androidId);
    }

    public String getOaid() {
        return checkValueSafe(oaid);
    }

    public String getGoogleAdvertisingId() {
        return checkValueSafe(googleAdvertisingId);
    }

    @Nullable
    public Map<String, String> getExtraSdk() {
        return extraSdk;
    }

    public static final class Builder extends BaseBuilder<VisitEvent> {
        String imei;
        String androidId;
        String oaid;
        String googleAdvertisingId;
        private Map<String, String> extraSdk;

        public Builder() {
            super(TrackEventType.VISIT);
        }

        @Override
        public void readPropertyInTrackThread(TrackerContext context) {
            super.readPropertyInTrackThread(context);

            DeviceInfoProvider deviceInfo = context.getDeviceInfoProvider();
            imei = deviceInfo.getImei();
            androidId = deviceInfo.getAndroidId();
            oaid = deviceInfo.getOaid();
            googleAdvertisingId = "";
        }

        public Builder setExtraSdk(Map<String, String> extraSdk) {
            this.extraSdk = extraSdk;
            return this;
        }

        @Override
        public VisitEvent build() {
            return new VisitEvent(this);
        }
    }
}
