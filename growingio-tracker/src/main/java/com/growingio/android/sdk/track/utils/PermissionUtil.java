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

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;

import com.growingio.android.sdk.track.ContextProvider;
import com.growingio.android.sdk.track.log.Logger;

public class PermissionUtil {
    private static final String TAG = "permission";

    private static final int FLAG_INTERNET = 1;
    private static final int FLAG_ACCESS_NETWORK_STATE = 1 << 1;
    private static final int FLAG_EXTERNAL_STORAGE = 1 << 2;
    private static final int FLAG_READ_PHONE_STATE = 1 << 3;
    private static PackageManager sPackageManager;
    private static String sPackageName;
    private static int sPermissionFlags = 0;

    private PermissionUtil(PackageManager packageManager, String packageName) {
        sPackageManager = packageManager;
        sPackageName = packageName;
    }

    public static boolean hasInternetPermission() {
        return checkPermission(Manifest.permission.INTERNET, FLAG_INTERNET);
    }

    public static boolean hasAccessNetworkStatePermission() {
        return checkPermission(Manifest.permission.ACCESS_NETWORK_STATE, FLAG_ACCESS_NETWORK_STATE);
    }

    public static boolean hasWriteExternalPermission() {
        return checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, FLAG_EXTERNAL_STORAGE);
    }

    public static boolean checkReadPhoneStatePermission() {
        return checkPermission(Manifest.permission.READ_PHONE_STATE, FLAG_READ_PHONE_STATE);
    }

    private static boolean checkPermission(String permissionName, int flag) {
        if (sPackageManager == null) {
            Context context = ContextProvider.getApplicationContext();
            sPackageManager = context.getPackageManager();
            sPackageName = context.getPackageName();
        }

        if ((sPermissionFlags & flag) != 0) {
            return true;
        }
        boolean hasPermission;
        try {
            hasPermission = PackageManager.PERMISSION_GRANTED == sPackageManager.checkPermission(permissionName, sPackageName);
        } catch (Throwable e) {
            hasPermission = false;
            Logger.d(TAG, e, "checkPermission failed");
        }
        if (hasPermission) {
            sPermissionFlags |= flag;
            return true;
        }
        return false;
    }
}
