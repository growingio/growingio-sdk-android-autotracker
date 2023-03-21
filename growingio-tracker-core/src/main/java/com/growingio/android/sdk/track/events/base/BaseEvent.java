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

import com.growingio.android.sdk.track.SDKConfig;
import com.growingio.android.sdk.TrackerContext;
import com.growingio.android.sdk.track.ipc.EventSequenceId;
import com.growingio.android.sdk.track.ipc.PersistentDataProvider;
import com.growingio.android.sdk.track.listener.TrackThread;
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

    private static final long serialVersionUID = -6563998911329703050L;

    private static final String APP_STATE_FOREGROUND = "FOREGROUND";
    private static final String APP_STATE_BACKGROUND = "BACKGROUND";

    private final String mPlatform;
    private final String mPlatformVersion;
    private final String mDeviceId;
    private final String mUserKey;
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
        mUserKey = eventBuilder.mUserKey;
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

    public String getDeviceId() {
        return checkValueSafe(mDeviceId);
    }

    public String getUserKey() {
        return checkValueSafe(mUserKey);
    }

    public String getUserId() {
        return checkValueSafe(mUserId);
    }

    public String getSessionId() {
        return checkValueSafe(mSessionId);
    }

    public long getTimestamp() {
        return mTimestamp;
    }

    public String getDomain() {
        return checkValueSafe(mDomain);
    }

    public String getUrlScheme() {
        return checkValueSafe(mUrlScheme);
    }

    public String getAppState() {
        return checkValueSafe(mAppState);
    }

    public long getGlobalSequenceId() {
        return mGlobalSequenceId;
    }

    public long getEventSequenceId() {
        return mEventSequenceId;
    }

    public String getNetworkState() {
        return checkValueSafe(mNetworkState);
    }

    public String getAppChannel() {
        return checkValueSafe(mAppChannel);
    }

    public int getScreenHeight() {
        return mScreenHeight;
    }

    public int getScreenWidth() {
        return mScreenWidth;
    }

    public String getDeviceBrand() {
        return checkValueSafe(mDeviceBrand);
    }

    public String getDeviceModel() {
        return checkValueSafe(mDeviceModel);
    }

    public String getDeviceType() {
        return checkValueSafe(mDeviceType);
    }

    public String getAppName() {
        return checkValueSafe(mAppName);
    }

    public String getAppVersion() {
        return checkValueSafe(mAppVersion);
    }

    public String getLanguage() {
        return checkValueSafe(mLanguage);
    }

    public double getLatitude() {
        return mLatitude;
    }

    public double getLongitude() {
        return mLongitude;
    }

    public String getSdkVersion() {
        return checkValueSafe(mSdkVersion);
    }

    public String getPlatform() {
        return checkValueSafe(mPlatform);
    }

    public String getPlatformVersion() {
        return checkValueSafe(mPlatformVersion);
    }

    public Map<String, String> getExtraParams() {
        return mExtraParams;
    }

    @Override
    public String getEventType() {
        return checkValueSafe(mEventType);
    }

    protected String checkValueSafe(String value) {
        return TextUtils.isEmpty(value) ? "" : value;
    }

    public JSONObject toJSONObject() {
        JSONObject json;
        if (!mExtraParams.isEmpty()) {
            json = new JSONObject(mExtraParams);
        } else {
            json = new JSONObject();
        }
        try {
            json.put(BaseField.PLATFORM, mPlatform);
            json.put(BaseField.PLATFORM_VERSION, mPlatformVersion);
            json.put(BaseField.DEVICE_ID, getDeviceId());
            if (!TextUtils.isEmpty(getUserKey())) {
                json.put(BaseField.USER_KEY, getUserKey());
            }
            if (!TextUtils.isEmpty(getUserId())) {
                json.put(BaseField.USER_ID, getUserId());
            }
            json.put(BaseField.SESSION_ID, getSessionId());
            json.put(BaseField.EVENT_TYPE, getEventType());
            json.put(BaseField.TIMESTAMP, getTimestamp());
            json.put(BaseField.GSID, getGlobalSequenceId());
            json.put(BaseField.ESID, getEventSequenceId());

            json.put(BaseField.DOMAIN, getDomain());
            json.put(BaseField.URL_SCHEME, getUrlScheme());

            if (!TextUtils.isEmpty(getAppState())) {
                json.put(BaseField.APP_STATE, getAppState());
            }
            if (!TextUtils.isEmpty(getNetworkState())) {
                json.put(BaseField.NETWORK_STATE, getNetworkState());
            }
            if (!TextUtils.isEmpty(getAppChannel())) {
                json.put(BaseField.APP_CHANNEL, getAppChannel());
            }
            if (getScreenHeight() > 0) {
                json.put(BaseField.SCREEN_HEIGHT, getScreenHeight());
            }
            if (getScreenWidth() > 0) {
                json.put(BaseField.SCREEN_WIDTH, getScreenWidth());
            }
            if (!TextUtils.isEmpty(getDeviceBrand())) {
                json.put(BaseField.DEVICE_BRAND, getDeviceBrand());
            }
            if (!TextUtils.isEmpty(getDeviceModel())) {
                json.put(BaseField.DEVICE_MODEL, getDeviceModel());
            }
            if (!TextUtils.isEmpty(getDeviceType())) {
                json.put(BaseField.DEVICE_TYPE, getDeviceType());
            }
            if (!TextUtils.isEmpty(getAppName())) {
                json.put(BaseField.APP_NAME, getAppName());
            }
            if (!TextUtils.isEmpty(getAppVersion())) {
                json.put(BaseField.APP_VERSION, getAppVersion());
            }
            if (!TextUtils.isEmpty(getLanguage())) {
                json.put(BaseField.LANGUAGE, getLanguage());
            }
            if (getLatitude() != 0 || getLongitude() != 0) {
                json.put(BaseField.LATITUDE, getLatitude());
                json.put(BaseField.LONGITUDE, getLongitude());
            }
            if (!TextUtils.isEmpty(getSdkVersion())) {
                json.put(BaseField.SDK_VERSION, getSdkVersion());
            }
        } catch (JSONException ignored) {
        }
        return json;
    }

    public static abstract class BaseBuilder<T extends BaseEvent> {
        private String mPlatform;
        private String mPlatformVersion;
        private String mDeviceId;
        private String mUserKey;
        private String mUserId;
        protected String mSessionId;
        protected String mEventType;
        protected long mTimestamp;
        protected String mDomain;
        private String mUrlScheme;
        private String mAppState;
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

        protected BaseBuilder(String eventType) {
            mEventType = eventType;
            mPlatform = ConstantPool.ANDROID;
            mPlatformVersion = DeviceInfoProvider.get().getOperatingSystemVersion();
        }

        @Deprecated
        protected BaseBuilder() {
            mPlatform = ConstantPool.ANDROID;
            mPlatformVersion = DeviceInfoProvider.get().getOperatingSystemVersion();
        }

        protected Map<String, Boolean> mFilterField = new HashMap<>();

        @TrackThread
        public void filterFieldProperty(Map<String, Boolean> filterField) {
            if (filterField == null) mFilterField.clear();
            else {
                mFilterField = filterField;
            }
        }

        public Map<String, Boolean> getFilterMap() {
            mFilterField.put(BaseField.APP_STATE, true);
            mFilterField.put(BaseField.NETWORK_STATE, true);
            mFilterField.put(BaseField.SCREEN_HEIGHT, true);
            mFilterField.put(BaseField.SCREEN_WIDTH, true);
            mFilterField.put(BaseField.DEVICE_BRAND, true);
            mFilterField.put(BaseField.DEVICE_MODEL, true);
            mFilterField.put(BaseField.DEVICE_TYPE, true);
            mFilterField.put(BaseField.APP_CHANNEL, true);
            mFilterField.put(BaseField.APP_NAME, true);
            mFilterField.put(BaseField.APP_VERSION, true);
            mFilterField.put(BaseField.LANGUAGE, true);
            mFilterField.put(BaseField.LATITUDE, true);
            mFilterField.put(BaseField.LONGITUDE, true);
            mFilterField.put(BaseField.SDK_VERSION, true);
            return mFilterField;
        }

        @TrackThread
        @CallSuper
        public void readPropertyInTrackThread() {
            if (mEventType == null) mEventType = getEventType();

            mAppState = ActivityStateProvider.get().getForegroundActivity() != null ? APP_STATE_FOREGROUND : APP_STATE_BACKGROUND;
            mDomain = AppInfoProvider.get().getPackageName();
            mUrlScheme = ConfigurationProvider.core().getUrlScheme();

            mTimestamp = (mTimestamp != 0) ? mTimestamp : System.currentTimeMillis();
            mDeviceId = DeviceInfoProvider.get().getDeviceId();
            mSessionId = PersistentDataProvider.get().getSessionId();
            mUserKey = UserInfoProvider.get().getLoginUserKey();
            mUserId = UserInfoProvider.get().getLoginUserId();
            EventSequenceId sequenceId = PersistentDataProvider.get().getAndIncrement(mEventType);
            mGlobalSequenceId = sequenceId.getGlobalId();
            mEventSequenceId = sequenceId.getEventTypeId();

            String mDataSourceId = ConfigurationProvider.core().getDataSourceId();
            if (!TextUtils.isEmpty(mDataSourceId)) {
                addExtraParam(BaseField.DATA_SOURCE_ID, mDataSourceId);
                String mLatestGioId = PersistentDataProvider.get().getGioId();
                if (!TextUtils.isEmpty(mLatestGioId)) {
                    addExtraParam(BaseField.GIO_ID, mLatestGioId);
                }
            }

            // filter field area
            if (!getFieldDefault(BaseField.APP_STATE)) {
                mAppState = null;
            }

            Context context = TrackerContext.get().getApplicationContext();
            mNetworkState = getFieldDefault(BaseField.NETWORK_STATE) ? NetworkUtil.getActiveNetworkState(context).getNetworkName() : null;

            DeviceInfoProvider deviceInfo = DeviceInfoProvider.get();
            mScreenHeight = getFieldDefault(BaseField.SCREEN_HEIGHT) ? deviceInfo.getScreenHeight() : 0;
            mScreenWidth = getFieldDefault(BaseField.SCREEN_WIDTH) ? deviceInfo.getScreenWidth() : 0;
            mDeviceBrand = getFieldDefault(BaseField.DEVICE_BRAND) ? deviceInfo.getDeviceBrand() : null;
            mDeviceModel = getFieldDefault(BaseField.DEVICE_MODEL) ? deviceInfo.getDeviceModel() : null;
            mDeviceType = getFieldDefault(BaseField.DEVICE_TYPE) ? deviceInfo.getDeviceType() : null;

            AppInfoProvider appInfo = AppInfoProvider.get();
            mAppChannel = getFieldDefault(BaseField.APP_CHANNEL) ? appInfo.getAppChannel() : null;
            mAppName = getFieldDefault(BaseField.APP_NAME) ? appInfo.getAppName() : null;
            mAppVersion = getFieldDefault(BaseField.APP_VERSION) ? appInfo.getAppVersion() : null;

            SessionProvider session = SessionProvider.get();
            mLatitude = getFieldDefault(BaseField.LATITUDE) ? session.getLatitude() : 0;
            mLongitude = getFieldDefault(BaseField.LONGITUDE) ? session.getLongitude() : 0;

            mSdkVersion = getFieldDefault(BaseField.SDK_VERSION) ? SDKConfig.SDK_VERSION : null;
            mLanguage = getFieldDefault(BaseField.LANGUAGE) ? Locale.getDefault().getLanguage() : null;
        }

        public BaseBuilder<?> addExtraParam(String key, String value) {
            mExtraParams.put(key, value);
            return this;
        }

        protected Boolean getFieldDefault(String key) {
            if (mFilterField.containsKey(key)) {
                return mFilterField.get(key);
            }
            return true;
        }

        public String getEventType() {
            return mEventType;
        }

        public abstract T build();
    }

}
