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

import android.text.TextUtils;

import com.growingio.android.sdk.track.events.base.BaseEvent;
import com.growingio.android.sdk.track.providers.DeviceInfoProvider;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

public final class VisitEvent extends BaseEvent {
    private static final long serialVersionUID = 1L;

    private final String mImei;
    private final String mAndroidId;
    private final String mOaid;
    private final String mGoogleAdvertisingId;
    private final Map<String, String> mExtraSdk;

    protected VisitEvent(Builder eventBuilder) {
        super(eventBuilder);
        mImei = eventBuilder.mImei;
        mAndroidId = eventBuilder.mAndroidId;
        mOaid = eventBuilder.mOaid;
        mGoogleAdvertisingId = eventBuilder.mGoogleAdvertisingId;
        mExtraSdk = eventBuilder.mExtraSdk;
    }

    @Override
    public int getSendPolicy() {
        return SEND_POLICY_INSTANT;
    }

    @Override
    public JSONObject toJSONObject() {
        JSONObject json = super.toJSONObject();
        try {
            if (!TextUtils.isEmpty(getImei())) {
                json.put("imei", getImei());
            }
            if (!TextUtils.isEmpty(getAndroidId())) {
                json.put("androidId", getAndroidId());
            }
            if (!TextUtils.isEmpty(getOaid())) {
                json.put("oaid", getOaid());
            }
            if (!TextUtils.isEmpty(getGoogleAdvertisingId())) {
                json.put("googleAdvertisingId", getGoogleAdvertisingId());
            }

            if (getExtraSdk() != null && !getExtraSdk().isEmpty()) {
                json.put("extraSdk", getExtraSdk());
            }
        } catch (JSONException ignored) {
        }
        return json;
    }

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public String getImei() {
        return checkValueSafe(mImei);
    }

    public String getAndroidId() {
        return checkValueSafe(mAndroidId);
    }

    public String getOaid() {
        return checkValueSafe(mOaid);
    }

    public String getGoogleAdvertisingId() {
        return checkValueSafe(mGoogleAdvertisingId);
    }

    public Map<String, String> getExtraSdk() {
        return mExtraSdk;
    }

    public static final class Builder extends BaseBuilder<VisitEvent> {
        private String mImei;
        private String mAndroidId;
        private String mOaid;
        private String mGoogleAdvertisingId;
        private Map<String, String> mExtraSdk;

        public Builder() {
            super(TrackEventType.VISIT);
        }

        @Override
        public void readPropertyInTrackThread() {
            super.readPropertyInTrackThread();

            DeviceInfoProvider deviceInfo = DeviceInfoProvider.get();
            mImei = deviceInfo.getImei();
            mAndroidId = deviceInfo.getAndroidId();
            mOaid = deviceInfo.getOaid();
            mGoogleAdvertisingId = "";
        }

        public Builder setExtraSdk(Map<String, String> extraSdk) {
            this.mExtraSdk = extraSdk;
            return this;
        }

        public Builder setTimestamp(long timestamp) {
            mTimestamp = timestamp;
            return this;
        }

        public Builder setSessionId(String sessionId) {
            mSessionId = sessionId;
            return this;
        }

        @Override
        public VisitEvent build() {
            return new VisitEvent(this);
        }
    }
}
