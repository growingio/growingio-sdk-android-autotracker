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
import android.os.Build;

public class FloatPermissionUtil {

    private FloatPermissionUtil() {
    }

    public static RomPermissionChecker getPermissionChecker(Activity activity) {
        if (Build.VERSION.SDK_INT < 23) {
            if (RomChecker.isMiuiRom()) {
                return new MiUiChecker(activity);
            } else if (RomChecker.isMeizuRom()) {
                return new MeizuChecker(activity);
            } else if (RomChecker.isHuaweiRom()) {
                return new HuaweiChecker(activity);
            } else if (RomChecker.is360Rom()) {
                return new QikuChecker(activity);
            }
        } else if (RomChecker.isMeizuRom()) {
            return new MeizuChecker(activity);
        }
        return new CommonRomChecker(activity);
    }
}
