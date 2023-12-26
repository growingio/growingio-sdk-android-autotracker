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

import com.growingio.android.sdk.track.TrackMainThread;
import com.growingio.android.sdk.track.log.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * <p>
 * 支持至oaid sdk 1.1.0
 * 客户提供Cert
 *
 * @author cpacm 2022/1/14
 */
public class OaidCertHelper implements IOaidHelper {

    private static final String TAG = "OaidDataLoader.Factory";
    private final OaidConfig config;
    private IOaidHelper oaidHelper;
    private volatile String oaid;

    public OaidCertHelper(Context context, OaidConfig oaidConfig) {
        this.config = oaidConfig;
        preloadOaid(context);
    }

    private boolean hasClass(String className) {
        try {
            Class.forName(className);
            return true;
        } catch (Throwable e) {
            return false;
        }
    }

    @Override
    public void preloadOaid(Context context) {
        synchronized (this) {
            TrackMainThread.trackMain().postActionToTrackMain(() -> {
                try {
                    if (hasClass("com.bun.miitmdid.core.CertChecker")) {
                        this.oaidHelper = preloadOaid1100(context);
                    } else if (hasClass("com.bun.miitmdid.interfaces.IIdentifierListener")) {
                        this.oaidHelper = preloadOaid1025();
                    } else if (hasClass("com.bun.supplier.IIdentifierListener")) {
                        this.oaidHelper = preloadOaid1013();
                    } else if (hasClass("com.bun.miitmdid.core.IIdentifierListener")) {
                        this.oaidHelper = preloadOaid1010();
                    }
                } catch (Throwable throwable) {
                    Logger.d(TAG, "not compatible with the version of oaid sdk");
                }
                if (this.oaidHelper != null) {
                    this.oaidHelper.preloadOaid(context);
                }
            });
        }
    }

    private IOaidHelper preloadOaid1100(Context context) {
        return new OaidHelper1100(getOaidCert(context));
    }

    private IOaidHelper preloadOaid1025() {
        return new OaidHelper1025();
    }

    private IOaidHelper preloadOaid1013() {
        return new OaidHelper1013();
    }

    private IOaidHelper preloadOaid1010() {
        return new OaidHelper1010();
    }

    private String getOaidCert(Context context) {
        if (config.getProvideCert() != null && !config.getProvideCert().isEmpty()) {
            return config.getProvideCert();
        }
        if (config.getProvideCertAsset() != null && !config.getProvideCertAsset().isEmpty()) {
            return loadPemFromAssetFile(context, config.getProvideCertAsset());
        }
        if (config.getProvideCertCallback() != null) {
            return config.getProvideCertCallback().provideCertJob(context);
        }
        return loadPemFromAssetFile(context, context.getPackageName() + ".cert.pem");
    }


    @Override
    public String getOaid() {
        // Check whether it is running on the main thread
        if (TrackMainThread.trackMain().runningOnUiThread()) {
            return this.oaid;
        }

        if (this.oaid == null && this.oaidHelper != null) {
            this.oaid = oaidHelper.getOaid();
        }
        return this.oaid;
    }

    /**
     * 从asset文件读取证书内容
     *
     * @return 证书字符串
     */
    private String loadPemFromAssetFile(Context context, String path) {
        BufferedReader in = null;
        try {
            in = new BufferedReader(new InputStreamReader(context.getAssets().open(path)));
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
