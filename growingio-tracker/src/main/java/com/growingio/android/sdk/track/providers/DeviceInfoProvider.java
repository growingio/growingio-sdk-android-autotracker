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
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import com.growingio.android.sdk.track.ContextProvider;
import com.growingio.android.sdk.track.TrackMainThread;
import com.growingio.android.sdk.track.data.PersistentDataProvider;
import com.growingio.android.sdk.track.interfaces.ResultCallback;
import com.growingio.android.sdk.track.ipc.GrowingIOIPC;
import com.growingio.android.sdk.track.utils.LogUtil;
import com.growingio.android.sdk.track.utils.PermissionUtil;

import java.nio.charset.Charset;
import java.util.UUID;

public class DeviceInfoProvider {
    private static final String TAG = "DeviceInfoProvider";

    private static final String MAGIC_ANDROID_ID = "9774d56d682e549c";

    private final Context mContext;
    private final GrowingIOIPC mIPC;

    private String mAndroidId;
    private String mImei;
    private String mOaid;
    private String mGoogleAdId;
    private String mDeviceId;

    private static class SingleInstance {
        private static final DeviceInfoProvider INSTANCE = new DeviceInfoProvider();
    }

    private DeviceInfoProvider() {
        mContext = ContextProvider.getApplicationContext();
        mIPC = PersistentDataProvider.get().getIPC();
    }

    public static DeviceInfoProvider get() {
        return SingleInstance.INSTANCE;
    }

    @SuppressLint({"MissingPermission", "HardwareIds"})
    public String getImei() {
        if (TextUtils.isEmpty(mImei) && ConfigurationProvider.get().isDataCollectionEnabled()) {
            if (PermissionUtil.checkReadPhoneStatePermission()) {
                try {
                    TelephonyManager tm = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
                    mImei = tm.getDeviceId();
                } catch (Throwable e) {
                    LogUtil.d(TAG, "don't have permission android.permission.READ_PHONE_STATE,initIMEI failed ");
                }
            }
        }
        return null;
    }

    public String getAndroidId() {
        if (TextUtils.isEmpty(mAndroidId) && ConfigurationProvider.get().isDataCollectionEnabled()) {
            mAndroidId = Settings.System.getString(mContext.getContentResolver(), Settings.System.ANDROID_ID);
        }
        return mAndroidId;
    }

    public String getOaid() {
        return mOaid;
    }

    public String getGoogleAdId() {
        return mGoogleAdId;
    }

    public String getDeviceId() {
        if (TextUtils.isEmpty(mDeviceId) && ConfigurationProvider.get().isDataCollectionEnabled()) {
            mDeviceId = mIPC.getDeviceId();
            if (TextUtils.isEmpty(mDeviceId)) {
                mDeviceId = calculateDeviceId();
            }
        }
        return mDeviceId;
    }

//    public void setDeviceId(String deviceId) {
//        this.mDeviceId = deviceId;
//    }

    public void getDeviceId(@NonNull final ResultCallback<String> callback) {
        if (!TextUtils.isEmpty(mDeviceId)) {
            callback.onResult(mDeviceId);
            return;
        }
        TrackMainThread.trackMain().postActionToTrackMain(new Runnable() {
            @Override
            public void run() {
                callback.onResult(getDeviceId());
            }
        });
    }

    private String calculateDeviceId() {
        LogUtil.d(TAG, "first time calculate deviceId");
        String adId = getAndroidId();
        String result = null;
        if (!TextUtils.isEmpty(adId) && !MAGIC_ANDROID_ID.equals(adId)) {
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
        mIPC.setDeviceId(result);
        return result;
    }
}