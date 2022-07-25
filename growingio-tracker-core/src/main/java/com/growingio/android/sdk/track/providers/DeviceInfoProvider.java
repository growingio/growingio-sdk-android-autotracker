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

package com.growingio.android.sdk.track.providers;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;

import com.growingio.android.sdk.TrackerContext;
import com.growingio.android.sdk.track.ipc.PersistentDataProvider;
import com.growingio.android.sdk.track.log.Logger;
import com.growingio.android.sdk.track.utils.ConstantPool;
import com.growingio.android.sdk.track.utils.DeviceUtil;
import com.growingio.android.sdk.track.utils.OaidHelper;
import com.growingio.android.sdk.track.utils.PermissionUtil;

import java.nio.charset.Charset;
import java.util.UUID;

public class DeviceInfoProvider {
    private static final String TAG = "DeviceInfoProvider";

    private static final String DEVICE_TYPE_PHONE = "PHONE";
    private static final String DEVICE_TYPE_PAD = "PAD";

    private static final String MAGIC_ANDROID_ID = "9774d56d682e549c"; // Error AndroidID

    private final Context mContext;

    private String mOperatingSystemVersion;
    private String mDeviceBrand;
    private String mDeviceModel;
    private String mDeviceType;
    private int mScreenHeight;
    private int mScreenWidth;

    private String mAndroidId;
    private String mImei;
    private String mOaid;
    private String mGoogleAdId;
    private String mDeviceId;

    private static class SingleInstance {
        private static final DeviceInfoProvider INSTANCE = new DeviceInfoProvider();
    }

    private DeviceInfoProvider() {
        mContext = TrackerContext.get().getApplicationContext();
    }

    public static DeviceInfoProvider get() {
        return SingleInstance.INSTANCE;
    }

    public String getOperatingSystemVersion() {
        if (TextUtils.isEmpty(mOperatingSystemVersion)) {
            mOperatingSystemVersion = Build.VERSION.RELEASE == null ? ConstantPool.UNKNOWN : Build.VERSION.RELEASE;
        }
        return mOperatingSystemVersion;
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
            mDeviceType = DeviceUtil.isPhone(mContext) ? DEVICE_TYPE_PHONE : DEVICE_TYPE_PAD;
        }
        return mDeviceType;
    }

    public int getScreenHeight() {
        if (mScreenHeight <= 0) {
            DisplayMetrics metrics = DeviceUtil.getDisplayMetrics(mContext);
            mScreenHeight = metrics.heightPixels;
        }
        return mScreenHeight;
    }

    public int getScreenWidth() {
        if (mScreenWidth <= 0) {
            DisplayMetrics metrics = DeviceUtil.getDisplayMetrics(mContext);
            mScreenWidth = metrics.widthPixels;
        }
        return mScreenWidth;
    }

    @SuppressLint("MissingPermission")
    public String getImei() {
        if (TextUtils.isEmpty(mImei)) {
            if (PermissionUtil.checkReadPhoneStatePermission()) {
                try {
                    TelephonyManager tm = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        mImei = tm.getImei();
                    } else {
                        mImei = tm.getDeviceId();
                    }
                } catch (Throwable e) {
                    Logger.d(TAG, "don't have permission android.permission.READ_PHONE_STATE,initIMEI failed ");
                }
            }
        }
        return null;
    }

    public String getAndroidId() {
        // ensure androidid call once in a process
        if (TextUtils.isEmpty(mAndroidId)) {
            try {
//                mAndroidId = Settings.System.getString(mContext.getContentResolver(), Settings.System.ANDROID_ID);
                mAndroidId = null;
                if (TextUtils.isEmpty(mAndroidId) || MAGIC_ANDROID_ID.equals(mAndroidId)) {
                    Logger.e(TAG, "get AndroidId error");
                    mAndroidId = MAGIC_ANDROID_ID;
                }
            } catch (Throwable e) {
                Logger.e(TAG, "get AndroidId error");
                mAndroidId = MAGIC_ANDROID_ID;
            }
        }
        return mAndroidId.equals(MAGIC_ANDROID_ID) ? null : mAndroidId;
    }

    public String getOaid() {
        if (TextUtils.isEmpty(mOaid)) {
            mOaid = TrackerContext.get().executeData(null, OaidHelper.class, String.class);
        }
        return mOaid;
    }

    public String getGoogleAdId() {
        return mGoogleAdId;
    }

    public String getDeviceId() {
        if (TextUtils.isEmpty(mDeviceId)) {
            mDeviceId = PersistentDataProvider.get().getDeviceId();
            if (TextUtils.isEmpty(mDeviceId)) {
                mDeviceId = calculateDeviceId();
            }
        }
        return mDeviceId;
    }

//    public void setDeviceId(String deviceId) {
//        this.mDeviceId = deviceId;
//    }

    private String calculateDeviceId() {
        Logger.d(TAG, "first time calculate deviceId");
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
            result = UUID.randomUUID().toString();
        }
        if (result != null && result.length() != 0) {
            PersistentDataProvider.get().setDeviceId(result);
        }
        return result;
    }
}