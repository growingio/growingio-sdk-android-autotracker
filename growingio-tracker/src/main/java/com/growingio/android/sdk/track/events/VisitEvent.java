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
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;

import com.growingio.android.sdk.track.CoreAppState;
import com.growingio.android.sdk.track.GConfig;
import com.growingio.android.sdk.track.events.base.BaseEventWithSequenceId;
import com.growingio.android.sdk.track.providers.DeviceInfoProvider;
import com.growingio.android.sdk.track.providers.LocationProvider;
import com.growingio.android.sdk.track.providers.ProjectInfoProvider;
import com.growingio.android.sdk.track.utils.rom.RomChecker;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Locale;
import java.util.Map;

public final class VisitEvent extends BaseEventWithSequenceId {
    private static final long serialVersionUID = 1L;

    private final String mAppChannel;
    private final int mScreenHeight;
    private final int mScreenWidth;
    private final String mDeviceBrand;
    private final String mDeviceModel;
    private final boolean mIsPhone;
    private final String mOperatingSystem;
    private final String mOperatingSystemVersion;
    private final String mSdkVersion;
    private final String mAppName;
    private final String mAppVersion;
    private final String mUrlScheme;
    private final String mLanguage;
    private final double mLatitude;
    private final double mLongitude;
    private final String mImei;
    private final String mAndroidId;
    private final String mGoogleAdvertisingId;
    private final Map<String, String> mFeaturesVersion;

    protected VisitEvent(EventBuilder eventBuilder) {
        super(eventBuilder);
        mAppChannel = eventBuilder.mAppChannel;
        mScreenHeight = eventBuilder.mScreenHeight;
        mScreenWidth = eventBuilder.mScreenWidth;
        mDeviceBrand = eventBuilder.mDeviceBrand;
        mDeviceModel = eventBuilder.mDeviceModel;
        mIsPhone = eventBuilder.mIsPhone;
        mOperatingSystem = eventBuilder.mOperatingSystem;
        mOperatingSystemVersion = eventBuilder.mOperatingSystemVersion;
        mSdkVersion = eventBuilder.mSdkVersion;
        mAppName = eventBuilder.mAppName;
        mAppVersion = eventBuilder.mAppVersion;
        mUrlScheme = eventBuilder.mUrlScheme;
        mLanguage = eventBuilder.mLanguage;
        mLatitude = eventBuilder.mLatitude;
        mLongitude = eventBuilder.mLongitude;
        mImei = eventBuilder.mImei;
        mAndroidId = eventBuilder.mAndroidId;
        mGoogleAdvertisingId = eventBuilder.mGoogleAdvertisingId;
        mFeaturesVersion = eventBuilder.mFeaturesVersion;
    }

    @Override
    public int getSendPolicy() {
        return SEND_POLICY_INSTANT;
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

    public boolean isPhone() {
        return mIsPhone;
    }

    public String getOperatingSystem() {
        return mOperatingSystem;
    }

    public String getOperatingSystemVersion() {
        return mOperatingSystemVersion;
    }

    public String getSdkVersion() {
        return mSdkVersion;
    }

    public String getAppName() {
        return mAppName;
    }

    public String getAppVersion() {
        return mAppVersion;
    }

    public String getUrlScheme() {
        return mUrlScheme;
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

    public String getGoogleAdvertisingId() {
        return mGoogleAdvertisingId;
    }

    public Map<String, String> getFeaturesVersion() {
        return mFeaturesVersion;
    }

    @Override
    public JSONObject toJSONObject() {
        JSONObject json = super.toJSONObject();
        try {
            json.put("mAppChannel", mAppChannel);
            json.put("mScreenHeight", mScreenHeight);
            json.put("mScreenWidth", mScreenWidth);
            json.put("mDeviceBrand", mDeviceBrand);
            json.put("mDeviceModel", mDeviceModel);
            json.put("mIsPhone", mIsPhone);
            json.put("mOperatingSystem", mOperatingSystem);
            json.put("mOperatingSystemVersion", mOperatingSystemVersion);
            json.put("mSdkVersion", mSdkVersion);
            json.put("mAppName", mAppName);
            json.put("mAppVersion", mAppVersion);
            json.put("mUrlScheme", mUrlScheme);
            json.put("mLanguage", mLanguage);
            json.put("mLatitude", mLatitude);
            json.put("mLongitude", mLongitude);
            json.put("mImei", mImei);
            json.put("mAndroidId", mAndroidId);
            json.put("mGoogleAdvertisingId", mGoogleAdvertisingId);
            json.put("mFeaturesVersion", mFeaturesVersion);
        } catch (JSONException ignored) {
        }
        return json;
    }

    public static final class EventBuilder extends BaseEventWithSequenceId.EventBuilder<VisitEvent> {
        private String mAppChannel;
        private int mScreenHeight;
        private int mScreenWidth;
        private String mDeviceBrand;
        private String mDeviceModel;
        private boolean mIsPhone;
        private String mOperatingSystem;
        private String mOperatingSystemVersion;
        private String mSdkVersion;
        private String mAppName;
        private String mAppVersion;
        private String mUrlScheme;
        private String mLanguage;
        private double mLatitude;
        private double mLongitude;
        private String mImei;
        private String mAndroidId;
        private String mGoogleAdvertisingId;
        private Map<String, String> mFeaturesVersion;

        public EventBuilder(CoreAppState coreAppState) {
            super(coreAppState);
        }

        @Override
        public EventType getEventType() {
            return EventType.VISIT;
        }

        @Override
        public void readPropertyInGMain() {
            super.readPropertyInGMain();
            Context context = mCoreAppState.getGlobalContext();

            mAppChannel = ProjectInfoProvider.AccountInfoPolicy.get().getChannel(context);

            DisplayMetrics metrics = new DisplayMetrics();
            Display display = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                display.getRealMetrics(metrics);
            } else {
                display.getMetrics(metrics);
            }

            mScreenHeight = metrics.heightPixels;
            mScreenWidth = metrics.widthPixels;
            mDeviceBrand = Build.BRAND == null ? "UNKNOWN" : Build.BRAND;
            mDeviceModel = Build.MODEL == null ? "UNKNOWN" : Build.MODEL;
            mIsPhone = RomChecker.isPhone(context);
            mOperatingSystem = "Android";
            mOperatingSystemVersion = Build.VERSION.RELEASE == null ? "UNKNOWN" : Build.VERSION.RELEASE;

            ProjectInfoProvider projectInfoProvider = ProjectInfoProvider.AccountInfoPolicy.get();
            mAppName = projectInfoProvider.getPackageName(context);
            mUrlScheme = projectInfoProvider.getUrlScheme();

            PackageManager packageManager = context.getPackageManager();
            try {
                PackageInfo info = packageManager.getPackageInfo(context.getPackageName(), 0);
                mAppVersion = info.versionName;
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }

            mSdkVersion = GConfig.SDK_VERSION;
            mLanguage = Locale.getDefault().toString();

            mLatitude = LocationProvider.LocationPolicy.get(mCoreAppState).getLatitude();
            mLongitude = LocationProvider.LocationPolicy.get(mCoreAppState).getLongitude();

            DeviceInfoProvider deviceInfo = DeviceInfoProvider.DeviceInfoPolicy.get(context);
            mImei = deviceInfo.getImei();
            mAndroidId = deviceInfo.getAndroidId();

            mGoogleAdvertisingId = "";
        }

        public Map<String, String> getFeaturesVersion() {
            return mFeaturesVersion;
        }

        public EventBuilder setFeaturesVersion(Map<String, String> featuresVersion) {
            mFeaturesVersion = featuresVersion;
            return this;
        }

        public EventBuilder setTimestamp(long timestamp) {
            mTimestamp = timestamp;
            return this;
        }

        public EventBuilder setSessionId(String sessionId) {
            mSessionId = sessionId;
            return this;
        }

        @Override
        public VisitEvent build() {
            return new VisitEvent(this);
        }
    }
}
