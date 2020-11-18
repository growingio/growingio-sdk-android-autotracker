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
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.text.TextUtils;

import com.growingio.android.sdk.track.ContextProvider;
import com.growingio.android.sdk.track.log.Logger;

public class AppInfoProvider {
    private static final String TAG = "AppInfoProvider";

    private final Context mContext;

    private String mPackageName;
    private String mAppName;
    private String mAppVersion;
    private String mAppChannel;

    private static class SingleInstance {
        private static final AppInfoProvider INSTANCE = new AppInfoProvider();
    }

    private AppInfoProvider() {
        mContext = ContextProvider.getApplicationContext();
    }

    public static AppInfoProvider get() {
        return SingleInstance.INSTANCE;
    }

    public String getPackageName() {
        if (TextUtils.isEmpty(mPackageName)) {
            mPackageName = mContext.getPackageName();
        }
        return mPackageName;
    }

    public String getAppName() {
        if (TextUtils.isEmpty(mAppName)) {
            try {
                PackageManager packageManager = mContext.getPackageManager();
                mAppName = packageManager.getApplicationLabel(mContext.getApplicationInfo()).toString();
            } catch (Exception e) {
                Logger.e(TAG, e);
            }
        }
        return mAppName;
    }

    public String getAppVersion() {
        if (TextUtils.isEmpty(mAppVersion)) {
            PackageManager packageManager = mContext.getPackageManager();
            try {
                PackageInfo info = packageManager.getPackageInfo(mContext.getPackageName(), 0);
                mAppVersion = info.versionName;
            } catch (PackageManager.NameNotFoundException e) {
                Logger.e(TAG, e);
            }
        }
        return mAppVersion;
    }

    public String getAppChannel() {
        if (TextUtils.isEmpty(mAppChannel)) {
            mAppChannel = ConfigurationProvider.get().getTrackConfiguration().getChannel();
        }
        return mAppChannel;
    }
}
