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
package com.growingio.android.platform.harmony;

import android.annotation.SuppressLint;
import android.text.TextUtils;

import com.growingio.android.sdk.track.middleware.platform.PlatformInfo;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;

public class HarmonyHandler {

    public static PlatformInfo proceed() {
        String harmonyVersion = getHarmonyVersion();
        if (TextUtils.isEmpty(harmonyVersion)) {
            return null;
        }
        return new PlatformInfo(ConstantPool.HARMONY, harmonyVersion);
    }

    private static String getHarmonyVersion() {
        try {
            return tryGetHarmonyVersionFromProp();
        } catch (Throwable ignored) {
            return tryGetHarmonyVersionFromRuntime();
        }
    }

    @SuppressLint("PrivateApi")
    private static String tryGetHarmonyVersionFromProp() throws Exception {
        Class<?> systemProperties = Class.forName(ConstantPool.CLASS_SYSTEM_PROPERTIES);
        Method getMethod = systemProperties.getDeclaredMethod(ConstantPool.GET_METHOD, String.class);
        String harmonyVersion = (String) getMethod.invoke(null, ConstantPool.HARMONY_VERSION_KEY);
        if (!TextUtils.isEmpty(harmonyVersion)) {
            return harmonyVersion;
        }

        return null;
    }

    private static String tryGetHarmonyVersionFromRuntime() {
        Process process = null;
        try {
            process = Runtime.getRuntime().exec(new String[]{ConstantPool.EXEC_CMD_GETPROP, ConstantPool.HARMONY_VERSION_KEY});
            try (InputStream in = process.getInputStream();
                InputStreamReader ir = new InputStreamReader(in);
                BufferedReader br = new BufferedReader(ir)) {
                String harmonyVersion = br.readLine();
                if (!TextUtils.isEmpty(harmonyVersion)) {
                    return harmonyVersion.trim();
                }
            }
        } catch (Throwable ignored) {
        } finally {
            if (process != null) {
                process.destroy();
            }
        }

        return null;
    }
}
