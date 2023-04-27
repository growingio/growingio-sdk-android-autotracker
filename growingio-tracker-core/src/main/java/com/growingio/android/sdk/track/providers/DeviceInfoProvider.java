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
import android.webkit.WebSettings;
import android.webkit.WebView;

import com.growingio.android.sdk.TrackerContext;
import com.growingio.android.sdk.track.ipc.PersistentDataProvider;
import com.growingio.android.sdk.track.log.Logger;
import com.growingio.android.sdk.track.utils.ConstantPool;
import com.growingio.android.sdk.track.utils.DeviceUtil;
import com.growingio.android.sdk.track.middleware.OaidHelper;
import com.growingio.android.sdk.track.utils.PermissionUtil;

import java.nio.charset.Charset;
import java.util.UUID;

public class DeviceInfoProvider {
    private static final String TAG = "DeviceInfoProvider";

    private static final String DEVICE_TYPE_PHONE = "PHONE";
    private static final String DEVICE_TYPE_PAD = "PAD";

    private static final String MAGIC_ANDROID_ID = "9774d56d682e549c"; // Error AndroidID

    private String mOperatingSystemVersion;
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
    private String mDeviceId;

    private static class SingleInstance {
        private static final DeviceInfoProvider INSTANCE = new DeviceInfoProvider();
    }

    private DeviceInfoProvider() {
    }

    private Context getContext() {
        return TrackerContext.get().getApplicationContext();
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
            mDeviceType = DeviceUtil.isPhone(getContext()) ? DEVICE_TYPE_PHONE : DEVICE_TYPE_PAD;
        }
        return mDeviceType;
    }

    public int getScreenHeight() {
        if (mScreenHeight <= 0) {
            DisplayMetrics metrics = DeviceUtil.getDisplayMetrics(getContext());
            mScreenHeight = metrics.heightPixels;
        }
        return mScreenHeight;
    }

    public int getScreenWidth() {
        if (mScreenWidth <= 0) {
            DisplayMetrics metrics = DeviceUtil.getDisplayMetrics(getContext());
            mScreenWidth = metrics.widthPixels;
        }
        return mScreenWidth;
    }

    @SuppressLint({"MissingPermission", "HardwareIds", "WrongConstant"})
    public String getImei() {
        if (TextUtils.isEmpty(mImei)) {
            if (PermissionUtil.checkReadPhoneStatePermission()) {
                try {
                    TelephonyManager tm = (TelephonyManager) getContext().getSystemService(Context.TELEPHONY_SERVICE);
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
        return null;
    }

    public String getAndroidId() {
        // ensure androidid call once in a process
        if (TextUtils.isEmpty(mAndroidId)) {
            try {
                mAndroidId = Settings.System.getString(getContext().getContentResolver(), Settings.System.ANDROID_ID);
                if (TextUtils.isEmpty(mAndroidId) || MAGIC_ANDROID_ID.equals(mAndroidId)) {
                    mAndroidId = MAGIC_ANDROID_ID;
                } else {
                    Logger.i(TAG, "get AndroidId success, and androidId is" + mAndroidId);
                }
            } catch (Throwable e) {
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
        if (result != null && result.length() != 0) {
            PersistentDataProvider.get().setDeviceId(result);
        }
        return result;
    }

    public String getUserAgent() {
        if (!TextUtils.isEmpty(mUserAgent)) return mUserAgent;
        mUserAgent = System.getProperty("http.agent");
        if (TextUtils.isEmpty(mUserAgent)
                && PermissionUtil.hasInternetPermission()) {
            try {
                mUserAgent = new WebView(getContext()).getSettings().getUserAgentString();
            } catch (Exception e) {
                Logger.e(TAG, e.getMessage());
                try {
                    mUserAgent = WebSettings.getDefaultUserAgent(getContext());
                } catch (Exception badException) {
                    Logger.e(TAG, badException.getMessage());
                }
            }
        }
        return mUserAgent;
    }
}