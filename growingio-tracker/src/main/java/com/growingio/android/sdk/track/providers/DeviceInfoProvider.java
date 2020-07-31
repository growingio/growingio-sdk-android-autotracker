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

import android.content.Context;
import android.provider.Settings;
import android.support.annotation.AnyThread;
import android.support.annotation.NonNull;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import com.growingio.android.sdk.track.GConfig;
import com.growingio.android.sdk.track.interfaces.GMainThread;
import com.growingio.android.sdk.track.interfaces.ResultCallback;
import com.growingio.android.sdk.track.utils.GIOProviders;
import com.growingio.android.sdk.track.utils.LogUtil;
import com.growingio.android.sdk.track.utils.PermissionUtil;
import com.growingio.android.sdk.track.utils.PersistUtil;
import com.growingio.android.sdk.track.utils.ThreadUtils;

import java.nio.charset.Charset;
import java.util.UUID;

/**
 * 用于获取设备信息的DeviceInfo
 */
public interface DeviceInfoProvider {
    /**
     * @return 获取设备imei信息
     */
    @AnyThread
    String getImei();

    /**
     * @return 获取设备AndroidId值
     */
    @AnyThread
    String getAndroidId();

    /**
     * @return 获取设备oaid值
     */
    @GMainThread
    String getOaid();

    /**
     * @return 获取GoogleAdId
     */
    @GMainThread
    String getGoogleAdId();

    @GMainThread
    String getDeviceId();

    void setDeviceId(String deviceId);

    @AnyThread
    void getDeviceId(@NonNull ResultCallback<String> callback);

    class DeviceInfoPolicy implements DeviceInfoProvider {

        private static final String TAG = "GIO.DeviceInfo";
        private Context mContext;
        private String mAndroidId;
        private String mImei;
        private String mOaid;
        private String mGoogleAdId;
        private String mDeviceId;
        public DeviceInfoPolicy(Context context) {
            this.mContext = context.getApplicationContext();
            PermissionUtil.init(this.mContext);
        }

        public static DeviceInfoPolicy get(final Context context) {
            return GIOProviders.provider(DeviceInfoPolicy.class, new GIOProviders.DefaultCallback<DeviceInfoPolicy>() {
                @Override
                public DeviceInfoPolicy value() {
                    return new DeviceInfoPolicy(context);
                }
            });
        }

        @Override
        public String getImei() {
            if (TextUtils.isEmpty(mImei) && GConfig.getInstance().isEnableDataCollect()) {
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

        @Override
        public String getAndroidId() {
            if (TextUtils.isEmpty(mAndroidId) && GConfig.getInstance().isEnableDataCollect()) {
                mAndroidId = Settings.System.getString(mContext.getContentResolver(), Settings.System.ANDROID_ID);
            }
            return mAndroidId;
        }

        @Override
        public String getOaid() {
            return mOaid;
        }

        @Override
        public String getGoogleAdId() {
            return mGoogleAdId;
        }

        @Override
        public String getDeviceId() {
            if (TextUtils.isEmpty(mDeviceId) && GConfig.getInstance().isEnableDataCollect()) {
                mDeviceId = PersistUtil.fetchDeviceId();
                if (TextUtils.isEmpty(mDeviceId)) {
                    mDeviceId = calculateDeviceId();
                }
            }
            return mDeviceId;
        }

        @Override
        public void setDeviceId(String deviceId) {
            this.mDeviceId = deviceId;
        }

        @Override
        public void getDeviceId(@NonNull final ResultCallback<String> callback) {
            if (!TextUtils.isEmpty(mDeviceId)) {
                callback.onResult(mDeviceId);
                return;
            }
            ThreadUtils.postOnGIOMainThread(new Runnable() {
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
            if (!TextUtils.isEmpty(adId) && !"9774d56d682e549c".equals(adId)) {
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
            // Write the value out to the prefs file
            PersistUtil.saveDeviceId(result);
            return result;
        }
    }
}
