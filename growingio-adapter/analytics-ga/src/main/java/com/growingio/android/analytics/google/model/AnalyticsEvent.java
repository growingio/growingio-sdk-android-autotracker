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

package com.growingio.android.analytics.google.model;

import android.content.Context;
import android.text.TextUtils;

import com.growingio.android.sdk.TrackerContext;
import com.growingio.android.sdk.track.SDKConfig;
import com.growingio.android.sdk.track.events.TrackEventType;
import com.growingio.android.sdk.track.events.base.BaseEvent;
import com.growingio.android.sdk.track.events.helper.FieldIgnoreFilter;
import com.growingio.android.sdk.track.providers.AppInfoProvider;
import com.growingio.android.sdk.track.providers.DeviceInfoProvider;
import com.growingio.android.sdk.track.providers.SessionProvider;
import com.growingio.android.sdk.track.utils.NetworkUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Locale;
import java.util.Map;

/**
 * 当前仅支持 JSON模块上报，PB模块不依赖 toJSONObject方法
 */
public class AnalyticsEvent extends BaseEvent {
    private static final String REPLACE_USER_KEY = "userKey";
    private static final String REPLACE_SESSION_ID = "sessionId";
    private static final String REPLACE_DATASOURCE_ID = "dataSourceId";
    private static final String REPLACE_USER_ID = "userId";
    private static final String REPLACE_GIO_ID = "gioId";
    private static final String REPLACE_ATTRIBUTES = "attributes";
    private static final String REPLACE_GLOBAL_SEQUENCE_ID = "globalSequenceId";
    private static final String REPLACE_EVENT_SEQUENCE_ID = "eventSequenceId";

    private static final String READ_DEVICE_ID = "deviceId";
    private static final String READ_NETWORK_STATE = "networkState";
    private static final String READ_SCREEN_HEIGHT = "screenHeight";
    private static final String READ_SCREEN_WIDTH = "screenWidth";
    private static final String READ_DEVICE_BRAND = "deviceBrand";
    private static final String READ_DEVICE_MODEL = "deviceModel";
    private static final String READ_DEVICE_TYPE = "deviceType";
    private static final String READ_APP_CHANNEL = "appChannel";
    private static final String READ_APP_NAME = "appName";
    private static final String READ_APP_VERSION = "appVersion";
    private static final String READ_LATITUDE = "latitude";
    private static final String READ_LONGITUDE = "longitude";
    private static final String READ_SDK_VERSION = "sdkVersion";
    private static final String READ_LANGUAGE = "language";

    private static final String READ_VISIT_IMEI = "imei";
    private static final String READ_VISIT_ANDROID_ID = "androidId";
    private static final String READ_VISIT_OAID = "oaid";
    private static final String READ_VISIT_GOOGLE_ADVERTISING_ID = "googleAdvertisingId";

    private static final String READ_AND_REPLACE_TIMESTAMP = "timestamp";

    private final TrackerInfo mTrackerInfo;
    private final long mTimestamp;
    private final boolean mReadProperty;

    private final String mEventType;
    private final int mSendPolicy;

    private JSONObject mJsonObject;

    public AnalyticsEvent(final BaseEvent baseEvent, final TrackerInfo trackerInfo) {
        this(baseEvent, trackerInfo, 0L, false);
    }

    public AnalyticsEvent(final BaseEvent baseEvent, final TrackerInfo trackerInfo, final boolean readProperty) {
        this(baseEvent, trackerInfo, 0L, readProperty);
    }

    public AnalyticsEvent(final BaseEvent baseEvent, final TrackerInfo trackerInfo, final long timestamp) {
        this(baseEvent, trackerInfo, timestamp, false);
    }

    public AnalyticsEvent(final BaseEvent baseEvent, final TrackerInfo trackerInfo, final long timestamp, final boolean readProperty) {
        // 空实现
        super(new BaseBuilder<BaseEvent>() {
            @Override
            public String getEventType() {
                return null;
            }

            @Override
            public BaseEvent build() {
                return null;
            }
        });

        this.mTrackerInfo = trackerInfo;
        this.mTimestamp = timestamp;
        this.mReadProperty = readProperty;

        this.mEventType = baseEvent.getEventType();
        this.mSendPolicy = baseEvent.getSendPolicy();
        this.mJsonObject = baseEvent.toJSONObject();

        if (mReadProperty) {
           readProperty();
        }
    }

    private void readProperty() {
        try {
            readBaseProperty();

            if (TrackEventType.VISIT.equals(mEventType)) {
                DeviceInfoProvider deviceInfo = DeviceInfoProvider.get();
                String imei = deviceInfo.getImei();
                if (!TextUtils.isEmpty(imei)) {
                    mJsonObject.put(READ_VISIT_IMEI, imei);
                }
                String androidId = deviceInfo.getAndroidId();
                if (!TextUtils.isEmpty(androidId)) {
                    mJsonObject.put(READ_VISIT_ANDROID_ID, androidId);
                }
                String oaid = deviceInfo.getOaid();
                if (!TextUtils.isEmpty(oaid)) {
                    mJsonObject.put(READ_VISIT_OAID, oaid);
                }
//                String googleAdvertisingId = "";
//                if (!TextUtils.isEmpty(googleAdvertisingId)) {
//                    mJsonObject.put(READ_VISIT_GOOGLE_ADVERTISING_ID, googleAdvertisingId);
//                }
            }
        }  catch (JSONException ignored) {

        }
    }

    private void readBaseProperty() throws JSONException {
        mJsonObject.put(READ_AND_REPLACE_TIMESTAMP, System.currentTimeMillis());
        mJsonObject.put(READ_DEVICE_ID, DeviceInfoProvider.get().getDeviceId());

        Context context = TrackerContext.get().getApplicationContext();
        String netWorkState = FieldIgnoreFilter.isFieldFilter("networkState") ? "" : NetworkUtil.getActiveNetworkState(context).getNetworkName();
        if (!TextUtils.isEmpty(netWorkState)) {
            mJsonObject.put(READ_NETWORK_STATE, netWorkState);
        }

        DeviceInfoProvider deviceInfo = DeviceInfoProvider.get();
        int screenHeight = FieldIgnoreFilter.isFieldFilter("screenHeight") ? 0 : deviceInfo.getScreenHeight();
        if (screenHeight > 0) {
            mJsonObject.put(READ_SCREEN_HEIGHT, screenHeight);
        }
        int screenWidth = FieldIgnoreFilter.isFieldFilter("screenWidth") ? 0 : deviceInfo.getScreenWidth();
        if (screenWidth > 0) {
            mJsonObject.put(READ_SCREEN_WIDTH, screenWidth);
        }
        String deviceBrand = FieldIgnoreFilter.isFieldFilter("deviceBrand") ? "" : deviceInfo.getDeviceBrand();
        if (!TextUtils.isEmpty(deviceBrand)) {
            mJsonObject.put(READ_DEVICE_BRAND, deviceBrand);
        }
        String deviceModel = FieldIgnoreFilter.isFieldFilter("deviceModel") ? "" : deviceInfo.getDeviceModel();
        if (!TextUtils.isEmpty(deviceModel)) {
            mJsonObject.put(READ_DEVICE_MODEL, deviceModel);
        }
        String deviceType = FieldIgnoreFilter.isFieldFilter("deviceType") ? "" : deviceInfo.getDeviceType();
        if (!TextUtils.isEmpty(deviceType)) {
            mJsonObject.put(READ_DEVICE_TYPE, deviceType);
        }

        AppInfoProvider appInfo = AppInfoProvider.get();
        String appChannel = appInfo.getAppChannel();
        if (!TextUtils.isEmpty(appChannel)) {
            mJsonObject.put(READ_APP_CHANNEL, appChannel);
        }
        mJsonObject.put(READ_APP_NAME, appInfo.getAppName());
        mJsonObject.put(READ_APP_VERSION, appInfo.getAppVersion());

        SessionProvider session = SessionProvider.get();
        double latitude = session.getLatitude();
        double longitude = session.getLongitude();
        if (latitude != 0 || longitude != 0) {
            mJsonObject.put(READ_LATITUDE, latitude);
            mJsonObject.put(READ_LONGITUDE, longitude);
        }

        mJsonObject.put(READ_SDK_VERSION, SDKConfig.SDK_VERSION);
        mJsonObject.put(READ_LANGUAGE, Locale.getDefault().getLanguage());
    }

    @Override
    public String getEventType() {
        return mEventType;
    }

    @Override
    public int getSendPolicy() {
        return mSendPolicy;
    }

    @Override
    public JSONObject toJSONObject() {
        try {
            // 如果存在则移除 userKey 字段
            mJsonObject.remove(REPLACE_USER_KEY);
            // 如果存在则移除 esid、gesid 字段
            mJsonObject.remove(REPLACE_GLOBAL_SEQUENCE_ID);
            mJsonObject.remove(REPLACE_EVENT_SEQUENCE_ID);

            // 替换 / 增加 gioId
            mJsonObject.put(REPLACE_GIO_ID, mTrackerInfo.getLastUserId());
            // 替换 / 增加 dataSourceId
            mJsonObject.put(REPLACE_DATASOURCE_ID, mTrackerInfo.getDatasourceId());
            // 替换 / 增加 sessionId
            mJsonObject.put(REPLACE_SESSION_ID, mTrackerInfo.getSessionId());
            // 替换 / 增加 userId
            mJsonObject.put(REPLACE_USER_ID, mTrackerInfo.getUserId());
            // 如果 timestamp 不为0L，替换 / 增加 timestamp
            if (mTimestamp != 0L) {
                mJsonObject.put(READ_AND_REPLACE_TIMESTAMP, mTimestamp);
            }

            // custom需要处理 通用参数
            if (TrackEventType.CUSTOM.equals(mEventType)) {
                JSONObject attributes = mJsonObject.optJSONObject(REPLACE_ATTRIBUTES);
                if (attributes == null) {
                    attributes = new JSONObject();
                }

                for (Map.Entry<String, String> attr : mTrackerInfo.getParams().entrySet()) {
                    // 如果与send设置的字段冲突，优先使用send方法中设置的字段值
                    String attrKey = attr.getKey();
                    if (attrKey != null && !attributes.has(attrKey)) {
                        attributes.put(attr.getKey(), attr.getValue());
                    }
                }
            }

            return mJsonObject;
        } catch (JSONException ignored) {
        }

        return new JSONObject();
    }
}
