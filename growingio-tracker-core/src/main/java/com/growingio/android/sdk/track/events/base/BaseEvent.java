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

package com.growingio.android.sdk.track.events.base;


import android.content.Context;
import android.support.annotation.CallSuper;
import android.text.TextUtils;

import com.growingio.android.sdk.track.ContextProvider;
import com.growingio.android.sdk.track.SDKConfig;
import com.growingio.android.sdk.track.data.EventSequenceId;
import com.growingio.android.sdk.track.data.PersistentDataProvider;
import com.growingio.android.sdk.track.interfaces.TrackThread;
import com.growingio.android.sdk.track.middleware.GEvent;
import com.growingio.android.sdk.track.providers.ActivityStateProvider;
import com.growingio.android.sdk.track.providers.AppInfoProvider;
import com.growingio.android.sdk.track.providers.ConfigurationProvider;
import com.growingio.android.sdk.track.providers.DeviceInfoProvider;
import com.growingio.android.sdk.track.providers.SessionProvider;
import com.growingio.android.sdk.track.providers.UserInfoProvider;
import com.growingio.android.sdk.track.utils.ConstantPool;
import com.growingio.android.sdk.track.utils.NetworkUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public abstract class BaseEvent extends GEvent {
    private static final String APP_STATE_FOREGROUND = "FOREGROUND";
    private static final String APP_STATE_BACKGROUND = "BACKGROUND";

    private final String mPlatform;
    private final String mPlatformVersion;
    private final String mDeviceId;
    private final String mUserId;
    private final String mSessionId;
    private final String mEventType;
    private final long mTimestamp;
    private final String mDomain;
    private final String mUrlScheme;
    private final String mAppState;
    private final long mGlobalSequenceId;
    private final long mEventSequenceId;
    private final Map<String, String> mExtraParams;

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
    private final String mSdkVersion;

    protected BaseEvent(BaseBuilder<?> eventBuilder) {
        mPlatform = eventBuilder.mPlatform;
        mPlatformVersion = eventBuilder.mPlatformVersion;
        mDeviceId = eventBuilder.mDeviceId;
        mUserId = eventBuilder.mUserId;
        mSessionId = eventBuilder.mSessionId;
        mEventType = eventBuilder.mEventType;
        mTimestamp = eventBuilder.mTimestamp;
        mDomain = eventBuilder.mDomain;
        mUrlScheme = eventBuilder.mUrlScheme;
        mAppState = eventBuilder.mAppState;
        mGlobalSequenceId = eventBuilder.mGlobalSequenceId;
        mEventSequenceId = eventBuilder.mEventSequenceId;
        mExtraParams = eventBuilder.mExtraParams;

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
        mSdkVersion = eventBuilder.mSdkVersion;
    }

    public static String getAppStateForeground() {
        return APP_STATE_FOREGROUND;
    }

    public static String getAppStateBackground() {
        return APP_STATE_BACKGROUND;
    }

    public String getDeviceId() {
        return mDeviceId;
    }

    public String getUserId() {
        return mUserId;
    }

    public String getSessionId() {
        return mSessionId;
    }

    public long getTimestamp() {
        return mTimestamp;
    }

    public String getDomain() {
        return mDomain;
    }

    public String getUrlScheme() {
        return mUrlScheme;
    }

    public String getAppState() {
        return mAppState;
    }

    public long getGlobalSequenceId() {
        return mGlobalSequenceId;
    }

    public long getEventSequenceId() {
        return mEventSequenceId;
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

    public String getSdkVersion() {
        return mSdkVersion;
    }

    @Override
    public String getEventType() {
        return mEventType;
    }

    public JSONObject toJSONObject() {
        JSONObject json;
        if (!mExtraParams.isEmpty()) {
            json = new JSONObject(mExtraParams);
        } else {
            json = new JSONObject();
        }
        try {
            json.put("platform", mPlatform);
            json.put("platformVersion", mPlatformVersion);
            json.put("deviceId", mDeviceId);
            if (!TextUtils.isEmpty(mUserId)) {
                json.put("userId", mUserId);
            }
            json.put("sessionId", mSessionId);
            json.put("eventType", mEventType);
            json.put("timestamp", mTimestamp);
            json.put("domain", mDomain);
            json.put("urlScheme", mUrlScheme);
            json.put("appState", mAppState);
            json.put("globalSequenceId", mGlobalSequenceId);
            json.put("eventSequenceId", mEventSequenceId);

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
            json.put("sdkVersion", mSdkVersion);

        } catch (JSONException ignored) {
        }
        return json;
    }

    public static abstract class BaseBuilder<T extends BaseEvent> {
        private String mPlatform;
        private String mPlatformVersion;
        private String mDeviceId;
        private String mUserId;
        protected String mSessionId;
        protected String mEventType;
        protected long mTimestamp;
        protected String mDomain;
        private final String mUrlScheme;
        private final String mAppState;
        private long mGlobalSequenceId;
        private long mEventSequenceId;
        private final Map<String, String> mExtraParams = new HashMap<>();

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
        private String mSdkVersion;

        protected BaseBuilder() {
            mPlatform = ConstantPool.ANDROID;
            mPlatformVersion = DeviceInfoProvider.get().getOperatingSystemVersion();
            mTimestamp = System.currentTimeMillis();
            mEventType = getEventType();
            mAppState = ActivityStateProvider.get().getForegroundActivity() != null ? APP_STATE_FOREGROUND : APP_STATE_BACKGROUND;
            mDomain = AppInfoProvider.get().getPackageName();
            mUrlScheme = ConfigurationProvider.get().getTrackConfiguration().getUrlScheme();
        }

        @TrackThread
        @CallSuper
        public void readPropertyInTrackThread() {
            mDeviceId = DeviceInfoProvider.get().getDeviceId();
            mSessionId = SessionProvider.get().getSessionId();
            mUserId = UserInfoProvider.get().getLoginUserId();
            EventSequenceId sequenceId = PersistentDataProvider.get().getAndIncrement(mEventType);
            mGlobalSequenceId = sequenceId.getGlobalId();
            mEventSequenceId = sequenceId.getEventTypeId();

            Context context = ContextProvider.getApplicationContext();
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

            SessionProvider session = SessionProvider.get();
            mLatitude = session.getLatitude();
            mLongitude = session.getLongitude();

            mSdkVersion = SDKConfig.SDK_VERSION;
            mLanguage = Locale.getDefault().getLanguage();
        }

        public BaseBuilder<?> addExtraParam(String key, String value) {
            mExtraParams.put(key, value);
            return this;
        }

        public abstract String getEventType();

        public abstract T build();
    }

}
