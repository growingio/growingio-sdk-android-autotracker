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

import com.bun.miitmdid.core.ErrorCode;
import com.bun.miitmdid.core.MdidSdkHelper;
import com.bun.miitmdid.interfaces.IIdentifierListener;
import com.bun.miitmdid.interfaces.IdSupplier;
import com.growingio.android.sdk.track.log.Logger;

public class OaidHelper1025 implements IOaidHelper {
    private static final String TAG = "OaidHelper1025";
    private final IIdentifierListener mListener = new IdentifierListener();
    private volatile boolean mPreloading = false;
    private String mOaid;

    private class IdentifierListener implements IIdentifierListener {
        @Override
        public void OnSupport(boolean isSupport, IdSupplier idSupplier) {
            try {
                if (isSupport && idSupplier != null) {
                    OaidHelper1025.this.mOaid = idSupplier.getOAID();
                    Logger.d(TAG, "getOAID: " + OaidHelper1025.this.mOaid);
                } else {
                    Logger.d(TAG, "oaid not support, and isSupport: " + isSupport + ", idSupplier:" + idSupplier);
                }
            } catch (Throwable throwable) {
                Logger.e(TAG, "getOAID failed: " + throwable.getMessage());
            }

            synchronized (OaidHelper1025.this) {
                OaidHelper1025.this.mPreloading = true;
                OaidHelper1025.this.notifyAll();
            }
        }
    }

    @Override
    public void preloadOaid(Context context) {
        synchronized (this) {
            try {
                this.mPreloading = true;
                int initCode = MdidSdkHelper.InitSdk(context, true, mListener);
                switch (initCode) {
                    case ErrorCode.INIT_ERROR_RESULT_DELAY:
                    case ErrorCode.INIT_ERROR_BEGIN:
                        Logger.d(TAG, "InitSdk success: and returnCode: " + initCode);
                        break;
                    case ErrorCode.INIT_ERROR_DEVICE_NOSUPPORT:
                    case ErrorCode.INIT_ERROR_LOAD_CONFIGFILE:
                    case ErrorCode.INIT_ERROR_MANUFACTURER_NOSUPPORT:
                    case ErrorCode.INIT_HELPER_CALL_ERROR:
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
}
