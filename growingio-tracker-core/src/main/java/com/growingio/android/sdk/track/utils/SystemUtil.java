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
import android.text.TextUtils;

import com.growingio.android.sdk.track.log.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

public class SystemUtil {
    private static final String TAG = "SystemUtil";

    private SystemUtil() {
    }

    public static String getProcessName() {
        try {
            try (BufferedReader mBufferedReader = new BufferedReader(
                    new FileReader(new File("/proc/" + android.os.Process.myPid() + "/" + "cmdline")))) {
                return mBufferedReader.readLine().trim();
            }
        } catch (Exception e) {
            Logger.e(TAG, e);
            return null;
        }
    }

    public static boolean isMainProcess(Context context) {
        String processName = getProcessName();
        return !TextUtils.isEmpty(processName) && processName.equals(context.getPackageName());
    }
}
