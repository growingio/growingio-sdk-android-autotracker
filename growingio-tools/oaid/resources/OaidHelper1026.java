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

package com.growingio.android.oaid;

import android.content.Context;

import com.bun.miitmdid.core.InfoCode;
import com.bun.miitmdid.core.MdidSdkHelper;
import com.bun.miitmdid.interfaces.IIdentifierListener;
import com.bun.miitmdid.interfaces.IdSupplier;
import com.growingio.android.sdk.track.log.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class OaidHelper1026 implements IOaidHelper {
    private static final String TAG = "OaidHelper1026";
    private final IIdentifierListener mListener = new IdentifierListener();
    private volatile boolean mPreloading = false;
    private String mOaid;
    private boolean isCertInit = false;
    private boolean isDeviceSupported = false;

    static {
        initSDKLibrary();
    }

    private class IdentifierListener implements  IIdentifierListener {
        @Override
        public void onSupport(IdSupplier idSupplier) {
            try {
                if (idSupplier != null && idSupplier.isSupported() && !idSupplier.isLimited()) {

                    // 临时修复1.0.26版本BUG-001: 部分品牌机型 isSupport=true 后，大约5s后会再次回调 isSupport=false
                    if (idSupplier.isSupported()) {
                        OaidHelper1026.this.isDeviceSupported = true;
                    } else if (OaidHelper1026.this.isDeviceSupported) {
                        return;
                    }

                    OaidHelper1026.this.mOaid = idSupplier.getOAID();
                    Logger.d(TAG, "getOAID: " + OaidHelper1026.this.mOaid);
                } else {
                    Logger.d(TAG, "oaid not support, and idSupplier:" + idSupplier);
                }
            } catch (Throwable throwable) {
                Logger.e(TAG, "getOAID failed: " + throwable.getMessage());
            }

            synchronized (OaidHelper1026.this) {
                OaidHelper1026.this.mPreloading = false;
                OaidHelper1026.this.notifyAll();
            }
        }
    }

    @Override
    public void preloadOaid(Context context) {
        synchronized (this) {
            try {
                this.mPreloading = true;
                // 初始化SDK证书
                if (!this.isCertInit) { // 证书只需初始化一次
                    // 证书为PEM文件中的所有文本内容（包括首尾行、换行符）
                    this.isCertInit = MdidSdkHelper.InitCert(context, loadPemFromAssetFile(context));
                    if (!this.isCertInit) {
                        Logger.w(TAG, "getDeviceIds: cert init failed");
                    }
                }
                int initCode = MdidSdkHelper.InitSdk(context, true, mListener);
                switch (initCode) {
                    case InfoCode.INIT_INFO_RESULT_DELAY:
                    case InfoCode.INIT_INFO_RESULT_OK:
                        Logger.d(TAG, "InitSdk success: and returnCode: " + initCode);
                        break;
                    case InfoCode.INIT_ERROR_CERT_ERROR:
                    case InfoCode.INIT_ERROR_DEVICE_NOSUPPORT:
                    case InfoCode.INIT_ERROR_LOAD_CONFIGFILE:
                    case InfoCode.INIT_ERROR_MANUFACTURER_NOSUPPORT:
                    case InfoCode.INIT_ERROR_SDK_CALL_ERROR:
                        Logger.e(TAG, "MdidSdkHelper.InitSdk failed, and returnCode: " + initCode);
                        this.mPreloading = false;
                        break;
                    default:
                        Logger.d(TAG, "other error code: " + initCode);
                }
            } catch (Throwable throwable) {
                Logger.e(TAG, "preloadOaid: " + throwable.getMessage());
                this.mPreloading = false;
            }
        }
    }

    @Override
    public String getOaid() {
        synchronized (this) {
            while (this.mPreloading) {
                try {
                    this.wait(3000L);
                } catch (InterruptedException e) {
                    Logger.e(TAG, e, "waitCompleteAndGetOaid interrupted");
                    Thread.currentThread().interrupt();
                }

                if (this.mPreloading) {
                    Logger.d(TAG, "it's too long to get oaid, and reject get oaid");
                    break;
                }
            }
            this.mPreloading = false;
        }
        return this.mOaid;
    }

    private static void initSDKLibrary() {
        try {
            System.loadLibrary("nllvm1623827671");
        } catch (Throwable var1) {
        }
    }

    /**
     * 从asset文件读取证书内容
     *
     * @param context
     * @return 证书字符串
     */
    private static String loadPemFromAssetFile(Context context) {
        BufferedReader in = null;

        try {
            in = new BufferedReader(new InputStreamReader(context.getAssets().open(context.getPackageName() + ".cert.pem")));
            StringBuilder builder = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null) {
                builder.append(line);
                builder.append('\n');
            }
            return builder.toString();
        } catch (IOException ignored) {
            Logger.e(TAG, "loadPemFromAssetFile failed");
            return "";
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException ignored) {
                }
            }

        }
    }
}
