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
package com.growingio.android.sdk.track.providers;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.webkit.WebSettings;

import com.growingio.android.sdk.TrackerContext;
import com.growingio.android.sdk.track.listener.TrackThread;
import com.growingio.android.sdk.track.log.Logger;
import com.growingio.android.sdk.track.middleware.platform.PlatformHelper;
import com.growingio.android.sdk.track.middleware.platform.PlatformInfo;
import com.growingio.android.sdk.track.modelloader.TrackerRegistry;
import com.growingio.android.sdk.track.utils.ConstantPool;
import com.growingio.android.sdk.track.utils.DeviceUtil;
import com.growingio.android.sdk.track.middleware.OaidHelper;
import com.growingio.android.sdk.track.utils.PermissionUtil;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;

public class DeviceInfoProvider implements TrackerLifecycleProvider {
    private static final String TAG = "DeviceInfoProvider";

    private static final String DEVICE_TYPE_PHONE = "PHONE";
    private static final String DEVICE_TYPE_PAD = "PAD";
    private static final String DEVICE_TYPE_FOLD = "FOLD";
    private static final List<String> MAGIC_ANDROID_IDS = new ArrayList<String>() {
        {
            add("9774d56d682e549c");
            add("0123456789abcdef");
            add("0000000000000000");
        }
    };

    private String mDeviceBrand;
    private String mDeviceModel;
    private String mDeviceType;
    private int mScreenHeight;
    private int mScreenWidth;

    private String mUserAgent;
    private String mAndroidId;
    private String mImei;
    private String mOaid;
    private String mGoogleAdId;
    private String mFirebaseId;
    private String mDeviceId;
    private String mPlatform;
    private String mPlatformVersion;
    private int mTimezoneOffset = Integer.MIN_VALUE;

    private double mLatitude = 0;
    private double mLongitude = 0;

    private Context context;
    private TrackerRegistry registry;
    private ConfigurationProvider configurationProvider;
    private PersistentDataProvider persistentDataProvider;

    DeviceInfoProvider() {
    }

    @Override
    public void setup(TrackerContext context) {
        this.context = context.getBaseContext();
        this.registry = context.getRegistry();
        this.configurationProvider = context.getConfigurationProvider();
        this.persistentDataProvider = context.getProvider(PersistentDataProvider.class);

        this.mPlatform = ConstantPool.ANDROID;
        this.mPlatformVersion = Build.VERSION.RELEASE == null ? ConstantPool.UNKNOWN : Build.VERSION.RELEASE;

        loadPlatformInfo();
    }

    @Override
    public void shutdown() {

    }

    private void loadPlatformInfo() {
        PlatformInfo platformInfo = registry.executeData(null, PlatformHelper.class, PlatformInfo.class);
        if (platformInfo != null) {
            if (platformInfo.getPlatform() != null) {
                this.mPlatform = platformInfo.getPlatform();
            }
            if (platformInfo.getPlatformVersion() != null) {
                this.mPlatformVersion = platformInfo.getPlatformVersion();
            }
            if (platformInfo.getDeviceType() != null) {
                this.mDeviceType = platformInfo.getDeviceType();
            }
            mGoogleAdId = platformInfo.getGmsId();
            mFirebaseId = platformInfo.getFirebaseId();
        }
    }

    public void updateFoldScreenSize() {
        if (getDeviceType().equalsIgnoreCase(DEVICE_TYPE_FOLD)) {
            DisplayMetrics metrics = DeviceUtil.getDisplayMetrics(context);
            mScreenWidth = metrics.widthPixels;
            mScreenHeight = metrics.heightPixels;
        }
    }

    public String getPlatformVersion() {
        return mPlatformVersion;
    }

    public String getPlatform() {
        return mPlatform;
    }

    public String getDeviceBrand() {
        if (TextUtils.isEmpty(mDeviceBrand)) {
            mDeviceBrand = Build.BRAND == null ? ConstantPool.UNKNOWN : Build.BRAND;
        }
        return mDeviceBrand;
    }

    public String getDeviceModel() {
        if (TextUtils.isEmpty(mDeviceModel)) {
            mDeviceModel = Build.MODEL == null ? ConstantPool.UNKNOWN : Build.MODEL;
        }
        return mDeviceModel;
    }

    public String getDeviceType() {
        if (TextUtils.isEmpty(mDeviceType)) {
            mDeviceType = DeviceUtil.isPhone(context) ? DEVICE_TYPE_PHONE : DEVICE_TYPE_PAD;
        }
        return mDeviceType;
    }


    public int getScreenHeight() {
        if (mScreenHeight <= 0) {
            DisplayMetrics metrics = DeviceUtil.getDisplayMetrics(context);
            mScreenHeight = metrics.heightPixels;
        }
        return mScreenHeight;
    }

    public int getScreenWidth() {
        if (mScreenWidth <= 0) {
            DisplayMetrics metrics = DeviceUtil.getDisplayMetrics(context);
            mScreenWidth = metrics.widthPixels;
        }
        return mScreenWidth;
    }

    @SuppressLint({"MissingPermission", "HardwareIds", "WrongConstant"})
    public String getImei() {
        if (TextUtils.isEmpty(mImei) && configurationProvider.core().isImeiEnabled()) {
            if (PermissionUtil.checkReadPhoneStatePermission()) {
                try {
                    TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        mImei = tm.getImei();
                    } else {
                        mImei = tm.getDeviceId();
                    }
                } catch (Throwable e) {
                    Logger.w(TAG, "sdk doesn't have permission - android.permission.READ_PHONE_STATE, initIMEI failed and skip");
                }
            }
        }
        return mImei;
    }

    public String getAndroidId() {
        // ensure androidid call once in a process
        if (TextUtils.isEmpty(mAndroidId) && configurationProvider.core().isAndroidIdEnabled()) {
            try {
                mAndroidId = Settings.System.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
                if (TextUtils.isEmpty(mAndroidId) || MAGIC_ANDROID_IDS.contains(mAndroidId)) {
                    mAndroidId = MAGIC_ANDROID_IDS.get(0);
                } else {
                    Logger.i(TAG, "get AndroidId success, and androidId is " + mAndroidId);
                }
            } catch (Throwable e) {
                mAndroidId = MAGIC_ANDROID_IDS.get(0);
            }
        }
        return MAGIC_ANDROID_IDS.contains(mAndroidId) ? null : mAndroidId;
    }

    public String getOaid() {
        if (TextUtils.isEmpty(mOaid)) {
            mOaid = registry.executeData(null, OaidHelper.class, String.class);
        }
        return mOaid;
    }

    public String getGoogleAdId() {
        return mGoogleAdId;
    }

    public String getFirebaseId() {
        return mFirebaseId;
    }

    public String getDeviceId() {
        if (TextUtils.isEmpty(mDeviceId)) {
            mDeviceId = persistentDataProvider.getDeviceId();
            if (TextUtils.isEmpty(mDeviceId)) {
                mDeviceId = calculateDeviceId();
            }
        }
        return mDeviceId;
    }

    private String calculateDeviceId() {
        String adId = getAndroidId();
        String result = null;
        if (!TextUtils.isEmpty(adId)) {
            result = UUID.nameUUIDFromBytes(adId.getBytes(Charset.forName("UTF-8"))).toString();
        } else {
            String imi = getImei();
            if (!TextUtils.isEmpty(imi)) {
                result = UUID.nameUUIDFromBytes(imi.getBytes(Charset.forName("UTF-8"))).toString();
            }
        }

        if (TextUtils.isEmpty(result)) {
            Logger.w(TAG, "try get AndroidId and fail, sdk generate random uuid as AndroidId");
            result = UUID.randomUUID().toString();
        }
        if (result != null && !result.isEmpty()) {
            persistentDataProvider.setDeviceId(result);
        }
        return result;
    }


    public String getUserAgent() {
        if (!TextUtils.isEmpty(mUserAgent)) return mUserAgent;
        if (TextUtils.isEmpty(mUserAgent)) {
            try {
                mUserAgent = WebSettings.getDefaultUserAgent(context);
            } catch (Exception badException) {
                Logger.e(TAG, badException.getMessage());
                mUserAgent = System.getProperty("http.agent");
            }
        }
        return mUserAgent;
    }

    @TrackThread
    public void setLocation(double latitude, double longitude) {
        double eps = 1e-5;
        if (Math.abs(latitude) < eps && Math.abs(longitude) < eps) {
            Logger.w(TAG, "invalid latitude and longitude, and return");
            return;
        }

        mLatitude = latitude;
        mLongitude = longitude;
        Logger.d(TAG, "set location with " + mLatitude + "-" + mLongitude);
    }

    @TrackThread
    public void cleanLocation() {
        Logger.d(TAG, "clean location by User, Doesn't send visit event.");
        mLatitude = 0;
        mLongitude = 0;
    }

    @TrackThread
    public double getLatitude() {
        return mLatitude;
    }

    @TrackThread
    public double getLongitude() {
        return mLongitude;
    }

    public int getTimezoneOffset() {
        if (mTimezoneOffset == Integer.MIN_VALUE) {
            Calendar calendar = Calendar.getInstance();
            mTimezoneOffset = -(calendar.get(Calendar.ZONE_OFFSET) + calendar.get(Calendar.DST_OFFSET)) / (60 * 1000);
        }
        return mTimezoneOffset;
    }
}