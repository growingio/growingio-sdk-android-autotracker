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

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import com.growingio.android.sdk.track.log.Logger;

import java.lang.reflect.Method;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RomChecker {
    private static final String TAG = "GIO.RomChecker";

    private RomChecker() {
    }

    public static boolean isHuaweiRom() {
        return getEmuiVersion() > 0;
    }

    public static boolean isMiuiRom() {
        return !TextUtils.isEmpty(getSystemProperty("ro.miui.ui.version.name"));
    }

    public static boolean isMeizuRom() {
        String meizuFlymeOSFlag = getSystemProperty("ro.build.display.id");
        if (TextUtils.isEmpty(meizuFlymeOSFlag)) {
            return false;
        } else {
            return meizuFlymeOSFlag.contains("flyme") || meizuFlymeOSFlag.toLowerCase(Locale.getDefault()).contains("flyme");
        }
    }

    public static boolean is360Rom() {
        return Build.MANUFACTURER.contains("QiKU")
                || Build.MANUFACTURER.contains("360");
    }

    /**
     * 获取小米 rom 版本号，获取失败返回 -1
     *
     * @return miui rom version code, if fail , return -1
     */
    public static int getMiuiVersion() {
        String version = getSystemProperty("ro.miui.ui.version.name");
        if (version != null) {
            try {
                return Integer.parseInt(version.substring(1));
            } catch (Exception e) {
                Logger.i(TAG, "get miui version code error, version : %s", version);
            }
        }
        return -1;
    }


    /**
     * 获取 emui 版本号
     *
     * @return -1.0 if not emui
     */
    public static double getEmuiVersion() {
        try {
            String emuiVersion = getSystemProperty("ro.build.version.emui");
            if (TextUtils.isEmpty(emuiVersion)) {
                return -1.0;
            }
            Pattern pattern = Pattern.compile("[0-9]+\\.[0-9]+");
            Matcher matcher = pattern.matcher(emuiVersion);
            if (matcher.find()) {
                return Double.parseDouble(matcher.group());
            }
        } catch (Exception e) {
            Logger.e(TAG, e, e.getMessage());
        }
        return 4.0;
    }

    /**
     * 获取系统属性
     *
     * @param propName
     * @return
     */
    @SuppressLint("PrivateApi")
    @SuppressWarnings("unchecked")
    public static String getSystemProperty(String propName) {
        try {
            Class clazz = Class.forName("android.os.SystemProperties");
            Method get = clazz.getMethod("get", String.class, String.class);
            return (String) get.invoke(null, propName, null);
        } catch (Exception e) {
            Logger.e(TAG, e, e.getMessage());
        }
        return null;
    }

    public static boolean isPhone(Context context) {
        TelephonyManager telephony = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        int type = telephony.getPhoneType();
        return type != TelephonyManager.PHONE_TYPE_NONE;
    }

}

