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

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AppOpsManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Binder;
import android.os.Build;
import android.util.Log;

import com.growingio.android.sdk.track.log.Logger;

import java.lang.reflect.Method;

public abstract class RomPermissionChecker {

    private static final String TAG  = "RomPermissionChecker";
    protected Activity mContext;

    public RomPermissionChecker(Activity activity) {
        mContext = activity;
    }

    public abstract boolean check();

    public abstract Intent getApplyPermissionIntent();

    @TargetApi(Build.VERSION_CODES.KITKAT)
    protected boolean checkOp(int op) {
        final int version = Build.VERSION.SDK_INT;
        if (version >= 19) {
            AppOpsManager manager = (AppOpsManager) mContext.getSystemService(Context.APP_OPS_SERVICE);
            try {
                Class clazz = AppOpsManager.class;
                Method method = clazz.getDeclaredMethod("checkOp", int.class, int.class, String.class);
                return AppOpsManager.MODE_ALLOWED == (int) method.invoke(manager, op, Binder.getCallingUid(), mContext.getPackageName());
            } catch (Exception ignore) {
                Logger.i(TAG, Log.getStackTraceString(ignore));
            }
        } else {
            Logger.i(TAG, "Below API 19 cannot invoke!");
        }
        return false;
    }

    protected Intent getValidIntent() {
        Intent intent = getApplyPermissionIntent();
        return intent.resolveActivityInfo(mContext.getPackageManager(), PackageManager.MATCH_DEFAULT_ONLY) != null ? intent : null;
    }
}
