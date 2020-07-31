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
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;

import com.growingio.android.sdk.track.utils.LogUtil;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

public class CommonRomChecker extends RomPermissionChecker {
    private static final String TAG  = "CommonRomChecker";

    public CommonRomChecker(Activity activity) {
        super(activity);
    }

    @Override
    public boolean check() {
        if (Build.VERSION.SDK_INT >= 23) {
            try {
                return Settings.canDrawOverlays(mContext);
            } catch (Exception e) {
                LogUtil.i(TAG, Log.getStackTraceString(e));
            }
        }
        return true;
    }

    @Override
    @TargetApi(Build.VERSION_CODES.M)
    public Intent getApplyPermissionIntent() {
        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
        intent.setFlags(FLAG_ACTIVITY_NEW_TASK);
        intent.setData(Uri.parse("package:" + mContext.getPackageName()));
        return intent;
    }
}
