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
package com.growingio.android.sdk.track.providers;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.text.TextUtils;

import com.growingio.android.sdk.TrackerContext;
import com.growingio.android.sdk.track.log.Logger;

public class AppInfoProvider implements TrackerLifecycleProvider {
    private static final String TAG = "AppInfoProvider";

    private String packageName;
    private String appName;
    private String appVersion;

    private Context context;

    AppInfoProvider() {
    }

    @Override
    public void setup(TrackerContext context) {
        this.context = context.getBaseContext();
    }

    @Override
    public void shutdown() {

    }

    public String getPackageName() {
        if (TextUtils.isEmpty(packageName)) {
            packageName = context.getPackageName();
        }
        return packageName;
    }

    public String getAppName() {
        if (TextUtils.isEmpty(appName)) {
            try {
                PackageManager packageManager = context.getPackageManager();
                appName = packageManager.getApplicationLabel(context.getApplicationInfo()).toString();
            } catch (Exception e) {
                Logger.e(TAG, e);
            }
        }
        return appName;
    }

    public String getAppVersion() {
        if (TextUtils.isEmpty(appVersion)) {
            PackageManager packageManager = context.getPackageManager();
            try {
                PackageInfo info = packageManager.getPackageInfo(context.getPackageName(), 0);
                appVersion = info.versionName;
            } catch (PackageManager.NameNotFoundException e) {
                Logger.e(TAG, e);
            }
        }
        return appVersion;
    }
}
