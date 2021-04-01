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
import android.text.TextUtils;

import com.growingio.android.sdk.track.TrackerContext;
import com.growingio.android.sdk.track.SDKConfig;
import com.growingio.android.sdk.track.events.base.BaseEvent;
import com.growingio.android.sdk.track.providers.AppInfoProvider;
import com.growingio.android.sdk.track.providers.DeviceInfoProvider;
import com.growingio.android.sdk.track.utils.NetworkUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Locale;
import java.util.Map;

public final class VisitEvent extends BaseEvent {
    private static final long serialVersionUID = 1L;

    private final String mNetworkState;
    private final String mAppChannel;
    private final int mScreenHeight;
    private final int mScreenWidth;
    private final String mDeviceBrand;
    private final String mDeviceModel;
    private final String mDeviceType;
    private final String mAppName;
    private final String mAppVersion;
    private final String mLanguage;
    private final double mLatitude;
    private final double mLongitude;
    private final String mImei;
    private final String mAndroidId;
    private final String mOaid;
    private final String mGoogleAdvertisingId;
    private final String mSdkVersion;
    private final Map<String, String> mExtraSdk;

    protected VisitEvent(Builder eventBuilder) {
        super(eventBuilder);
        mNetworkState = eventBuilder.mNetworkState;
        mAppChannel = eventBuilder.mAppChannel;
        mScreenHeight = eventBuilder.mScreenHeight;
        mScreenWidth = eventBuilder.mScreenWidth;
        mDeviceBrand = eventBuilder.mDeviceBrand;
        mDeviceModel = eventBuilder.mDeviceModel;
        mDeviceType = eventBuilder.mDeviceType;
        mAppName = eventBuilder.mAppName;
        mAppVersion = eventBuilder.mAppVersion;
        mLanguage = eventBuilder.mLanguage;
        mLatitude = eventBuilder.mLatitude;
        mLongitude = eventBuilder.mLongitude;
        mImei = eventBuilder.mImei;
        mAndroidId = eventBuilder.mAndroidId;
        mOaid = eventBuilder.mOaid;
        mGoogleAdvertisingId = eventBuilder.mGoogleAdvertisingId;
        mSdkVersion = eventBuilder.mSdkVersion;
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
            json.put("networkState", mNetworkState);
            if (!TextUtils.isEmpty(mAppChannel)) {
                json.put("appChannel", mAppChannel);
            }
            json.put("screenHeight", mScreenHeight);
            json.put("screenWidth", mScreenWidth);
            json.put("deviceBrand", mDeviceBrand);
            json.put("deviceModel", mDeviceModel);
            json.put("deviceType", mDeviceType);
            json.put("appName", mAppName);
            json.put("appVersion", mAppVersion);
            json.put("language", mLanguage);

            if (mLatitude != 0 || mLongitude != 0) {
                json.put("latitude", mLatitude);
                json.put("longitude", mLongitude);
            }

            if (!TextUtils.isEmpty(mImei)) {
                json.put("imei", mImei);
            }
            if (!TextUtils.isEmpty(mAndroidId)) {
                json.put("androidId", mAndroidId);
            }
            if (!TextUtils.isEmpty(mOaid)) {
                json.put("oaid", mOaid);
            }
            if (!TextUtils.isEmpty(mGoogleAdvertisingId)) {
                json.put("googleAdvertisingId", mGoogleAdvertisingId);
            }
            json.put("sdkVersion", mSdkVersion);
            if (mExtraSdk != null && !mExtraSdk.isEmpty()) {
                json.put("extraSdk", mExtraSdk);
            }
        } catch (JSONException ignored) {
        }
        return json;
    }

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public String getNetworkState() {
        return mNetworkState;
    }

    public String getAppChannel() {
        return mAppChannel;
    }

    public int getScreenHeight() {
        return mScreenHeight;
    }

    public int getScreenWidth() {
        return mScreenWidth;
    }

    public String getDeviceBrand() {
        return mDeviceBrand;
    }

    public String getDeviceModel() {
        return mDeviceModel;
    }

    public String getDeviceType() {
        return mDeviceType;
    }

    public String getAppName() {
        return mAppName;
    }

    public String getAppVersion() {
        return mAppVersion;
    }

    public String getLanguage() {
        return mLanguage;
    }

    public double getLatitude() {
        return mLatitude;
    }

    public double getLongitude() {
        return mLongitude;
    }

    public String getImei() {
        return mImei;
    }

    public String getAndroidId() {
        return mAndroidId;
    }

    public String getOaid() {
        return mOaid;
    }

    public String getGoogleAdvertisingId() {
        return mGoogleAdvertisingId;
    }

    public String getSdkVersion() {
        return mSdkVersion;
    }

    public Map<String, String> getExtraSdk() {
        return mExtraSdk;
    }

    public static final class Builder extends BaseBuilder<VisitEvent> {
        private String mNetworkState;
        private String mAppChannel;
        private int mScreenHeight;
        private int mScreenWidth;
        private String mDeviceBrand;
        private String mDeviceModel;
        private String mDeviceType;
        private String mAppName;
        private String mAppVersion;
        private String mLanguage;
        private double mLatitude;
        private double mLongitude;
        private String mImei;
        private String mAndroidId;
        private String mOaid;
        private String mGoogleAdvertisingId;
        private String mSdkVersion;
        private Map<String, String> mExtraSdk;

        Builder() {
            super();
        }

        @Override
        public String getEventType() {
            return TrackEventType.VISIT;
        }

        @Override
        public void readPropertyInTrackThread() {
            super.readPropertyInTrackThread();

            Context context = TrackerContext.get().getApplicationContext();
            mNetworkState = NetworkUtil.getActiveNetworkState(context).getNetworkName();

            DeviceInfoProvider deviceInfo = DeviceInfoProvider.get();
            mScreenHeight = deviceInfo.getScreenHeight();
            mScreenWidth = deviceInfo.getScreenWidth();
            mDeviceBrand = deviceInfo.getDeviceBrand();
            mDeviceModel = deviceInfo.getDeviceModel();
            mDeviceType = deviceInfo.getDeviceType();

            AppInfoProvider appInfo = AppInfoProvider.get();
            mAppChannel = appInfo.getAppChannel();
            mAppName = appInfo.getAppName();
            mAppVersion = appInfo.getAppVersion();

            mSdkVersion = SDKConfig.SDK_VERSION;
            mLanguage = Locale.getDefault().getLanguage();

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

        public Builder setLatitude(double latitude) {
            mLatitude = latitude;
            return this;
        }

        public Builder setLongitude(double longitude) {
            mLongitude = longitude;
            return this;
        }

        @Override
        public VisitEvent build() {
            return new VisitEvent(this);
        }
    }
}
