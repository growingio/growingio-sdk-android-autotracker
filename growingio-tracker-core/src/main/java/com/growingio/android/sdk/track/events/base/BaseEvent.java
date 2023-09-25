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
package com.growingio.android.sdk.track.events.base;

import androidx.annotation.FloatRange;
import androidx.annotation.IntRange;
import androidx.annotation.Nullable;

import com.growingio.android.sdk.track.SDKConfig;
import com.growingio.android.sdk.TrackerContext;
import com.growingio.android.sdk.track.events.helper.DefaultEventFilterInterceptor;
import com.growingio.android.sdk.track.providers.PersistentDataProvider;
import com.growingio.android.sdk.track.listener.TrackThread;
import com.growingio.android.sdk.track.middleware.GEvent;
import com.growingio.android.sdk.track.providers.ActivityStateProvider;
import com.growingio.android.sdk.track.providers.AppInfoProvider;
import com.growingio.android.sdk.track.providers.ConfigurationProvider;
import com.growingio.android.sdk.track.providers.DeviceInfoProvider;
import com.growingio.android.sdk.track.providers.UserInfoProvider;
import com.growingio.android.sdk.track.utils.ConstantPool;
import com.growingio.android.sdk.track.utils.NetworkUtil;
import com.growingio.sdk.annotation.json.JsonSerializer;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@JsonSerializer(builder = "BaseBuilder")
public abstract class BaseEvent extends GEvent {

    private static final long serialVersionUID = -6563998911329703050L;

    private static final String APP_STATE_FOREGROUND = "FOREGROUND";
    private static final String APP_STATE_BACKGROUND = "BACKGROUND";

    public final static String EVENT_TYPE = "eventType";

    private final String platform;
    private final String platformVersion;
    private final String deviceId;
    @Nullable
    private final String userKey;
    @Nullable
    private final String userId;
    private final String sessionId;
    private final String eventType;
    private final long timestamp;
    private final String domain;
    private final String urlScheme;
    @Nullable
    private final String appState;

    private final long eventSequenceId;
    @Nullable
    private final String dataSourceId;
    @Nullable
    private final String networkState;
    @Nullable
    private final String appChannel;
    @IntRange(from = 0)
    private final int screenHeight;
    @IntRange(from = 0)
    private final int screenWidth;
    @Nullable
    private final String deviceBrand;
    @Nullable
    private final String deviceModel;
    @Nullable
    private final String deviceType;
    @Nullable
    private final String appName;
    @Nullable
    private final String appVersion;
    @Nullable
    private final String language;
    @FloatRange(from = 0, to = 0)//!=0
    private final double latitude;
    @FloatRange(from = 0, to = 0)//!=0
    private final double longitude;
    @Nullable
    private final String sdkVersion;

    @Nullable
    private final String timezoneOffset;

    protected BaseEvent(BaseBuilder<?> eventBuilder) {
        platform = eventBuilder.platform;
        platformVersion = eventBuilder.platformVersion;
        deviceId = eventBuilder.deviceId;
        userKey = eventBuilder.userKey;
        userId = eventBuilder.userId;
        sessionId = eventBuilder.sessionId;
        eventType = eventBuilder.eventType;
        timestamp = eventBuilder.timestamp;
        domain = eventBuilder.domain;
        urlScheme = eventBuilder.urlScheme;
        appState = eventBuilder.appState;
        eventSequenceId = eventBuilder.eventSequenceId;
        dataSourceId = eventBuilder.dataSourceId;

        networkState = eventBuilder.networkState;
        appChannel = eventBuilder.appChannel;
        screenHeight = eventBuilder.screenHeight;
        screenWidth = eventBuilder.screenWidth;
        deviceBrand = eventBuilder.deviceBrand;
        deviceModel = eventBuilder.deviceModel;
        deviceType = eventBuilder.deviceType;
        appName = eventBuilder.appName;
        appVersion = eventBuilder.appVersion;
        language = eventBuilder.language;
        latitude = eventBuilder.latitude;
        longitude = eventBuilder.longitude;
        sdkVersion = eventBuilder.sdkVersion;
        timezoneOffset = eventBuilder.timezoneOffset;
    }

    public String getDeviceId() {
        return checkValueSafe(deviceId);
    }

    public String getUserKey() {
        return checkValueSafe(userKey);
    }

    public String getUserId() {
        return checkValueSafe(userId);
    }

    public String getSessionId() {
        return checkValueSafe(sessionId);
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getDomain() {
        return checkValueSafe(domain);
    }

    public String getUrlScheme() {
        return checkValueSafe(urlScheme);
    }

    public String getAppState() {
        return checkValueSafe(appState);
    }

    public long getEventSequenceId() {
        return eventSequenceId;
    }

    public String getNetworkState() {
        return checkValueSafe(networkState);
    }

    public String getAppChannel() {
        return checkValueSafe(appChannel);
    }

    public int getScreenHeight() {
        return screenHeight;
    }

    public int getScreenWidth() {
        return screenWidth;
    }

    public String getDeviceBrand() {
        return checkValueSafe(deviceBrand);
    }

    public String getDeviceModel() {
        return checkValueSafe(deviceModel);
    }

    public String getDeviceType() {
        return checkValueSafe(deviceType);
    }

    public String getAppName() {
        return checkValueSafe(appName);
    }

    public String getAppVersion() {
        return checkValueSafe(appVersion);
    }

    public String getLanguage() {
        return checkValueSafe(language);
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public String getSdkVersion() {
        return checkValueSafe(sdkVersion);
    }

    public String getPlatform() {
        return checkValueSafe(platform);
    }

    public String getPlatformVersion() {
        return checkValueSafe(platformVersion);
    }

    public String getDataSourceId() {
        return checkValueSafe(dataSourceId);
    }

    @Override
    public String getEventType() {
        return checkValueSafe(eventType);
    }

    public String getTimezoneOffset() {
        return timezoneOffset;
    }

    protected String checkValueSafe(String value) {
        return value == null ? "" : value;
    }

    public static abstract class BaseBuilder<T extends BaseEvent> {
        protected String platform;
        protected String platformVersion;
        protected String deviceId;
        protected String userKey;
        protected String userId;
        protected String sessionId;
        protected String eventType;
        protected long timestamp;
        protected String domain;
        protected String urlScheme;
        protected String appState;
        protected long eventSequenceId;
        protected String dataSourceId;

        protected String networkState;
        protected String appChannel;
        protected int screenHeight;
        protected int screenWidth;
        protected String deviceBrand;
        protected String deviceModel;
        protected String deviceType;
        protected String appName;
        protected String appVersion;
        protected String language;
        protected double latitude;
        protected double longitude;
        protected String sdkVersion;
        protected String timezoneOffset;

        protected BaseBuilder(String eventType) {
            this.eventType = eventType;
            platform = ConstantPool.ANDROID;
        }

        @Deprecated
        protected BaseBuilder() {
            platform = ConstantPool.ANDROID;
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

        private boolean isEventSequenceIdType(String type) {
            if (DefaultEventFilterInterceptor.FilterEventType.VISIT.equals(type)) return true;
            if (DefaultEventFilterInterceptor.FilterEventType.CUSTOM.equals(type)) return true;
            if (DefaultEventFilterInterceptor.FilterEventType.PAGE.equals(type)) return true;
            if (DefaultEventFilterInterceptor.FilterEventType.VIEW_CLICK.equals(type)) return true;
            if (DefaultEventFilterInterceptor.FilterEventType.VIEW_CHANGE.equals(type)) return true;
            return false;
        }

        @TrackThread
        public void readPropertyInTrackThread(TrackerContext context) {
            if (eventType == null) eventType = getEventType();

            ActivityStateProvider activityStateProvider = context.getActivityStateProvider();
            appState = activityStateProvider.getForegroundActivity() != null ? APP_STATE_FOREGROUND : APP_STATE_BACKGROUND;
            // filter field area
            if (!getFieldDefault(BaseField.APP_STATE)) {
                appState = null;
            }

            PersistentDataProvider persistentDataProvider = context.getProvider(PersistentDataProvider.class);
            sessionId = persistentDataProvider.getSessionId();
            if (isEventSequenceIdType(eventType)) {
                eventSequenceId = persistentDataProvider.getGlobalEventSequenceIdAndIncrement();
            } else {
                eventSequenceId = 0L;
            }

            UserInfoProvider userInfoProvider = context.getUserInfoProvider();
            userKey = userInfoProvider.getLoginUserKey();
            userId = userInfoProvider.getLoginUserId();

            ConfigurationProvider configurationProvider = context.getConfigurationProvider();
            urlScheme = configurationProvider.core().getUrlScheme();
            dataSourceId = configurationProvider.core().getDataSourceId();
            appChannel = getFieldDefault(BaseField.APP_CHANNEL) ? configurationProvider.core().getChannel() : null;

            DeviceInfoProvider deviceInfo = context.getDeviceInfoProvider();
            platformVersion = deviceInfo.getOperatingSystemVersion();
            deviceId = deviceInfo.getDeviceId();
            screenHeight = getFieldDefault(BaseField.SCREEN_HEIGHT) ? deviceInfo.getScreenHeight() : 0;
            screenWidth = getFieldDefault(BaseField.SCREEN_WIDTH) ? deviceInfo.getScreenWidth() : 0;
            deviceBrand = getFieldDefault(BaseField.DEVICE_BRAND) ? deviceInfo.getDeviceBrand() : null;
            deviceModel = getFieldDefault(BaseField.DEVICE_MODEL) ? deviceInfo.getDeviceModel() : null;
            deviceType = getFieldDefault(BaseField.DEVICE_TYPE) ? deviceInfo.getDeviceType() : null;
            latitude = getFieldDefault(BaseField.LATITUDE) ? deviceInfo.getLatitude() : 0;
            longitude = getFieldDefault(BaseField.LONGITUDE) ? deviceInfo.getLongitude() : 0;
            timezoneOffset = getFieldDefault(BaseField.TIMEZONE_OFFSET) ? String.valueOf(deviceInfo.getTimezoneOffset()) : null;

            AppInfoProvider appInfo = context.getProvider(AppInfoProvider.class);
            appName = getFieldDefault(BaseField.APP_NAME) ? appInfo.getAppName() : null;
            appVersion = getFieldDefault(BaseField.APP_VERSION) ? appInfo.getAppVersion() : null;
            if (domain == null || domain.isEmpty()) {
                // default is packageName
                domain = appInfo.getPackageName();
            }

            timestamp = (timestamp != 0) ? timestamp : System.currentTimeMillis();
            networkState = getFieldDefault(BaseField.NETWORK_STATE) ? NetworkUtil.getActiveNetworkState(context).getNetworkName() : null;
            sdkVersion = getFieldDefault(BaseField.SDK_VERSION) ? SDKConfig.SDK_VERSION : null;
            language = getFieldDefault(BaseField.LANGUAGE) ? Locale.getDefault().getLanguage() : null;
            platform = ConstantPool.ANDROID;
        }

        protected Boolean getFieldDefault(String key) {
            if (mFilterField.containsKey(key)) {
                return mFilterField.get(key);
            }
            return true;
        }

        public String getEventType() {
            return eventType;
        }

        public abstract T build();
    }

}
