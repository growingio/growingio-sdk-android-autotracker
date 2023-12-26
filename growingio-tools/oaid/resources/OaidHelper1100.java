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
package com.growingio.android.oaid;

import android.content.Context;

import com.bun.miitmdid.core.InfoCode;
import com.bun.miitmdid.core.MdidSdkHelper;
import com.bun.miitmdid.interfaces.IIdentifierListener;
import com.bun.miitmdid.interfaces.IdSupplier;
import com.growingio.android.sdk.track.log.Logger;

public class OaidHelper1100 implements IIdentifierListener, IOaidHelper {
    private static final String TAG = "OaidHelper1100";
    private volatile boolean mPreloading = false;
    private String mOaid;
    private boolean isCertInit = false;
    private final String mCert;

    static {
        //1.1.0的oaid_sdk
        loadLibrary("msaoaidsec");
        // 适配1.0.29的oaid_sdk
        loadLibrary("nllvm1632808251147706677");
        // 适配1.0.27的oaid_sdk
        loadLibrary("nllvm1630571663641560568");
        // 适配1.0.26的oaid_sdk
        loadLibrary("nllvm1623827671");
    }

    private static void loadLibrary(String libName) {
        try {
            System.loadLibrary(libName);
        } catch (Throwable ignored) {
        }
    }


    public OaidHelper1100(String cert) {
        this.mCert = cert;
    }

    @Override
    public void onSupport(IdSupplier idSupplier) {
        try {
            if (idSupplier != null && idSupplier.isSupported()) {
                this.mOaid = idSupplier.getOAID();
                //String oaid = idSupplier.getOAID();
                //String vaid = idSupplier.getVAID();
                //String aaid = idSupplier.getAAID();
                Logger.d(TAG, "getOAID: " + OaidHelper1100.this.mOaid);
            } else {
                Logger.d(TAG, "oaid not support, and idSupplier:" + idSupplier);
            }
        } catch (Throwable throwable) {
            Logger.e(TAG, "getOAID failed: " + throwable.getMessage());
        }

        synchronized (OaidHelper1100.this) {
            mPreloading = false;
            this.notifyAll();
        }
    }

    public void preloadOaid(Context context) {
        synchronized (this) {
            try {
                this.mPreloading = true;
                // 初始化SDK证书
                if (!this.isCertInit) { // 证书只需初始化一次
                    // 证书为PEM文件中的所有文本内容（包括首尾行、换行符）
                    this.isCertInit = MdidSdkHelper.InitCert(context, this.mCert);
                    if (!this.isCertInit) {
                        Logger.w(TAG, "preloadOaid: cert init failed");
                    }
                }

                int initCode = MdidSdkHelper.InitSdk(context, true, this);
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
}
