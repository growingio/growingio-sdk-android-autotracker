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
package com.growingio.android.sdk.track.utils;

import android.app.Application;
import android.content.Context;
import android.os.Build;
import android.text.TextUtils;

import com.growingio.android.sdk.track.log.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class SystemUtil {
    private static final String TAG = "SystemUtil";

    private SystemUtil() {
    }

    public static String getProcessName() {
        if (Build.VERSION.SDK_INT >= 28) {
            return Application.getProcessName();
        }

        String processName = null;
        try {
            Class<?> activityThread = Class.forName("android.app.ActivityThread");
            // Before API 18, the method was incorrectly named "currentPackageName", but it still returned the process name
            // See https://github.com/aosp-mirror/platform_frameworks_base/commit/b57a50bd16ce25db441da5c1b63d48721bb90687
            String methodName = Build.VERSION.SDK_INT >= 18 ? "currentProcessName" : "currentPackageName";

            Method getProcessName = activityThread.getDeclaredMethod(methodName);
            processName = (String) getProcessName.invoke(null);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        }
        if (processName == null) {
            try {
                try (BufferedReader mBufferedReader = new BufferedReader(
                        new FileReader(new File("/proc/" + android.os.Process.myPid() + "/" + "cmdline")))) {
                    processName = mBufferedReader.readLine().trim();
                }
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return processName;
    }

    public static boolean isMainProcess(Context context) {
        try {
            String processName = getProcessName();
            return !TextUtils.isEmpty(processName) && processName.equals(context.getPackageName());
        } catch (Exception e) {
            Logger.e(TAG, e);
            return false;
        }
    }

}
