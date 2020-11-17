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
package com.growingio.android.sdk.track.utils;

import android.content.Context;
import com.bun.miitmdid.core.MdidSdkHelper;
import com.bun.miitmdid.interfaces.IIdentifierListener;
import com.bun.miitmdid.interfaces.IdSupplier;
import com.growingio.android.sdk.track.log.Logger;

public class OaidHelper implements IIdentifierListener {
    private static final String TAG = "GIO.oaid";
    private String mOaid;
    private volatile boolean mComplete = false;

    public OaidHelper() {
    }

    public String getOaid(Context context) {
        Logger.d("GIO.oaid", "call getOaid");

        int ret;
        try {
            ret = MdidSdkHelper.InitSdk(context, true, this);
        } catch (Throwable throwable) {
            Logger.e("GIO.oaid", "InitSdkError: " + throwable.getMessage());
            return null;
        }

        switch (ret) {
            case 0:
            case 1008610:
            case 1008614:
                Logger.d("GIO.oaid", "InitSdk success: and returnCode: " + ret);
                break;
            case 1008611:
            case 1008612:
            case 1008613:
            case 1008615:
                Logger.e("GIO.oaid", "MdidSdkHelper.InitSdk failed, and returnCode: " + ret);
                return null;
            default:
                Logger.d("GIO.oaid", "other error code: " + ret);
        }

        if (this.mComplete) {
            return this.mOaid;
        } else {
            synchronized (this) {
                if (this.mComplete) {
                    return this.mOaid;
                }

                long start = System.currentTimeMillis();

                while (!this.mComplete) {
                    try {
                        this.wait(3000L);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    if (!this.mComplete) {
                        Logger.d("GIO.oaid", "it's too long to get oaid, and reject get oaid");
                        break;
                    }
                }
            }

            return this.mOaid;
        }
    }

    @Override
    public void OnSupport(boolean isSupport, IdSupplier idSupplier) {
        if (isSupport && idSupplier != null) {
            try {
                this.mOaid = idSupplier.getOAID();
                Logger.d("GIO.oaid", "get oaid: " + this.mOaid);
            } catch (Throwable throwable) {
                Logger.e("GIO.oaid", "getOAID failed: " + throwable.getMessage());
            }
        } else {
            Logger.d("GIO.oaid", "oaid not support, and isSupport: " + isSupport + ", idSupplier:" + idSupplier);
        }

        synchronized (this) {
            this.mComplete = true;
            this.notifyAll();
        }
    }
}
