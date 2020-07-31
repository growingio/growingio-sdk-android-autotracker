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
import android.content.Intent;
import android.os.Build;

public class MeizuChecker extends RomPermissionChecker {

    public MeizuChecker(Activity activity) {
        super(activity);
    }

    @Override
    public boolean check() {
        final int version = Build.VERSION.SDK_INT;
        if (version >= 19) {
            return checkOp(24); //OP_SYSTEM_ALERT_WINDOW = 24;
        }
        return false;
    }

    @Override
    public Intent getApplyPermissionIntent() {
        Intent intent = new Intent("com.meizu.safe.security.SHOW_APPSEC");
        // intent.setClassName("com.meizu.safe", "com.meizu.safe.security.AppSecActivity");
        intent.putExtra("packageName", mContext.getPackageName());
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        return intent;
    }


}
