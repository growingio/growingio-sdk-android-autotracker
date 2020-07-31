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

package com.growingio.android.sdk.track.utils.rom;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;

import com.growingio.android.sdk.track.utils.LogUtil;

public class MiUiChecker extends RomPermissionChecker {
    private static final String TAG  = "MiUiChecker";

    public MiUiChecker(Activity activity) {
        super(activity);
    }

    private static boolean isIntentAvailable(Intent intent, Context context) {
        if (intent == null) {
            return false;
        }
        return context.getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY).size() > 0;
    }

    @Override
    public boolean check() {
        final int version = Build.VERSION.SDK_INT;
        if (version >= 19) {
            return checkOp(24); //OP_SYSTEM_ALERT_WINDOW = 24;
        } else {
            return true;
        }
    }

    @Override
    public Intent getApplyPermissionIntent() {
        int versionCode = RomChecker.getMiuiVersion();

        Intent intent = null;
        if (versionCode == 5) {
            String packageName = mContext.getPackageName();
            intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            Uri uri = Uri.fromParts("package", packageName, null);
            intent.setData(uri);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        } else if (versionCode == 6 || versionCode == 7) {
            intent = new Intent("miui.intent.action.APP_PERM_EDITOR");
            intent.setClassName("com.miui.securitycenter", "com.miui.permcenter.permissions.AppPermissionsEditorActivity");
            intent.putExtra("extra_pkgname", mContext.getPackageName());
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        } else if (versionCode == 8) {
            intent = new Intent("miui.intent.action.APP_PERM_EDITOR");
            intent.setClassName("com.miui.securitycenter", "com.miui.permcenter.permissions.PermissionsEditorActivity");
            intent.putExtra("extra_pkgname", mContext.getPackageName());
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            if (isIntentAvailable(intent, mContext)) {
                mContext.startActivity(intent);
            } else {
                intent = new Intent("miui.intent.action.APP_PERM_EDITOR");
                intent.setPackage("com.miui.securitycenter");
                intent.putExtra("extra_pkgname", mContext.getPackageName());
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            }
        } else {
            LogUtil.i(TAG, "this is a special MIUI rom version, its version code %d", versionCode);
        }
        return intent;
    }
}
