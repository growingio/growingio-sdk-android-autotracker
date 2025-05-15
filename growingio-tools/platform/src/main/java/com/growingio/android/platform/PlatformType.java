/*
 *  Copyright (C) 2025 Beijing Yishu Technology Co., Ltd.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.growingio.android.platform;

import android.annotation.SuppressLint;
import android.app.UiModeManager;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Build;
import android.telephony.TelephonyManager;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Locale;

class PlatformType {

    private final static String SAMSUNG_BRAND = "samsung";
    private final static String HUAWEI_BRAND = "huawei";
    private final static String HONOR_BRAND = "honor";
    private final static String OPPO_BRAND = "oppo";
    private final static String VIVO_BRAND = "vivo";
    private final static String XIAOMI_BRAND = "xiaomi";
    private final static String ZTE_BRAND = "zte";
    private final static String MOTOROLA_BRAND = "motorola";
    private final static String GOOGLE_BRAND = "google";
    private final static String ROYOLE_BRAND = "royole";


    private static final String DEVICE_TYPE_PHONE = "PHONE";
    private static final String DEVICE_TYPE_PAD = "PAD";
    private static final String DEVICE_TYPE_FOLD = "FOLD";
    // private static final String DEVICE_TYPE_FLIP = "FLIP";
    private static final String DEVICE_TYPE_TV = "TV";

    static String getDeviceType(Context context) {
        if (isTvDevice(context)) {
            return DEVICE_TYPE_TV;
        }

        boolean isPhone = isPhone(context);
        if (!isPhone) {
            return DEVICE_TYPE_PAD;
        }

        if (isFoldableDevice(context)) {
            return DEVICE_TYPE_FOLD;
        }

        if (isTablet(context)) {
            return DEVICE_TYPE_PAD;
        }

        return DEVICE_TYPE_PHONE;
    }

    @SuppressLint("WrongConstant")
    static boolean isPhone(Context context) {
        TelephonyManager telephony = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        if (telephony == null) return false;
        int type = telephony.getPhoneType();
        return type != TelephonyManager.PHONE_TYPE_NONE;
    }

    static boolean isTablet(Context context) {
        return context.getResources().getConfiguration().smallestScreenWidthDp >= 600;
    }

    @SuppressLint("WrongConstant")
    static boolean isTvDevice(Context context) {
        // Detect TVs via ui mode (Android TVs) or system features (Fire TV).
        if (context.getPackageManager().hasSystemFeature("amazon.hardware.fire_tv")) {
            return true;
        }
        UiModeManager uiManager = (UiModeManager) context.getSystemService(Context.UI_MODE_SERVICE);
        if (uiManager != null && uiManager.getCurrentModeType() == Configuration.UI_MODE_TYPE_TELEVISION) {
            return true;
        }
        return false;
    }

    static boolean isFoldableDevice(Context context) {
        String brand = Build.BRAND;
        String model = Build.MODEL;
        if (brand == null || brand.isEmpty() || model == null || model.isEmpty()) return false;

        // check foldable via exist models
        if (isSamsungFold()) {
            return true;
        }
        if (HUAWEI_BRAND.equalsIgnoreCase(brand) && containsIgnoreCase(HUAWEI_MODEL_LIST, model)) {
            return true;
        }
        if (HONOR_BRAND.equalsIgnoreCase(brand) && containsIgnoreCase(HONOR_MODEL_LIST, model)) {
            return true;
        }
        if (VIVO_BRAND.equalsIgnoreCase(brand) && containsIgnoreCase(VIVO_MODEL_LIST, model)) {
            return true;
        }
        if (OPPO_BRAND.equalsIgnoreCase(brand) && containsIgnoreCase(OPPO_MODEL_LIST, model)) {
            return true;
        }
        if (XIAOMI_BRAND.equalsIgnoreCase(brand) && containsIgnoreCase(XIAOMI_MODEL_LIST, model)) {
            return true;
        }
        if (ZTE_BRAND.equalsIgnoreCase(brand) && containsIgnoreCase(NUBIA_MODEL_LIST, model)) {
            return true;
        }
        if (MOTOROLA_BRAND.equalsIgnoreCase(brand) && containsIgnoreCase(MOTO_MODEL_LIST, model)) {
            return true;
        }
        if (GOOGLE_BRAND.equalsIgnoreCase(brand) && containsIgnoreCase(PIXEL_MODEL_LIST, model)) {
            return true;
        }
        if (ROYOLE_BRAND.equalsIgnoreCase(brand) && containsIgnoreCase(VERTU_MODEL_LIST, model)) {
            return true;
        }

        // check foldable via system features
        if (HUAWEI_BRAND.equalsIgnoreCase(brand) && context.getPackageManager().hasSystemFeature("com.huawei.hardware.sensor.posture")) {
            return true;
        }
        if (isOppoFold()) {
            return true;
        }
        if (isVivoFold()) {
            return true;
        }
        if (isXiaomiFold()) {
            return true;
        }

        return false;
    }

    private static boolean isOppoFold() {
        boolean isFold = false;
        try {
            Class<?> cls = Class.forName("com.oplus.content.OplusFeatureConfigManager");
            Method instance = cls.getMethod("getInstance");
            Object configManager = instance.invoke(null);
            Method hasFeature = cls.getDeclaredMethod("hasFeature", String.class);
            Object object = hasFeature.invoke(configManager, "oplus.hardware.type.fold");
            if (object instanceof Boolean) {
                isFold = (boolean) object;
            }
        } catch (Exception ignored) {
        }
        return isFold;
    }

    private static boolean isVivoFold() {
        try {
            @SuppressLint("PrivateApi") Class<?> c = Class.forName("android.util.FtDeviceInfo");
            Method m = c.getMethod("getDeviceType");
            Object dType = m.invoke(c);
            return "foldable".equals(dType);
        } catch (Exception ignored) {

        }
        return false;
    }

    private static boolean isXiaomiFold() {
        try {
            @SuppressLint("PrivateApi") Class c = Class.forName("android.os.SystemProperties");
            Method m = c.getMethod("getInt", String.class, int.class);
            int type = (int) m.invoke(c, "persist.sys.muiltdisplay_type", 0);
            return type == 2;
        } catch (Throwable ignored) {
        }
        return false;
    }

    private static boolean isSamsungFold() {
        String brand = Build.BRAND;
        String model = Build.MODEL;
        // check samsung foldable
        if (SAMSUNG_BRAND.equalsIgnoreCase(brand)) {
            // fold model startWith SM-F9
            if (model.toUpperCase().startsWith("SM-F9")) {
                return true;
            }
            // flip model startWith SM-F7
            if (model.toUpperCase().startsWith("SM-F7")) {
                return true;
            }
            // special model for fold or flip
            if (containsIgnoreCase(GALAXY_MODEL_LIST, model)) {
                return true;
            }
        }
        return false;
    }

    public boolean isEmulator() {
        return Build.FINGERPRINT.startsWith("generic")
                || Build.FINGERPRINT.startsWith("unknown")
                || Build.MODEL.contains("google_sdk")
                || Build.MODEL.toLowerCase(Locale.ROOT).contains("droid4x")
                || Build.MODEL.contains("Emulator")
                || Build.MODEL.contains("Android SDK built for x86")
                || Build.MANUFACTURER.contains("Genymotion")
                || Build.HARDWARE.contains("goldfish")
                || Build.HARDWARE.contains("ranchu")
                || Build.HARDWARE.contains("vbox86")
                || Build.PRODUCT.contains("sdk")
                || Build.PRODUCT.contains("google_sdk")
                || Build.PRODUCT.contains("sdk_google")
                || Build.PRODUCT.contains("sdk_x86")
                || Build.PRODUCT.contains("vbox86p")
                || Build.PRODUCT.contains("emulator")
                || Build.PRODUCT.contains("simulator")
                || Build.BOARD.toLowerCase(Locale.ROOT).contains("nox")
                || Build.BOOTLOADER.toLowerCase(Locale.ROOT).contains("nox")
                || Build.HARDWARE.toLowerCase(Locale.ROOT).contains("nox")
                || Build.PRODUCT.toLowerCase(Locale.ROOT).contains("nox")
                || Build.SERIAL.toLowerCase(Locale.ROOT).contains("nox")
                || (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic"));
    }

    private static boolean containsIgnoreCase(List<String> list, String name) {
        for (String item : list) {
            if (item.equalsIgnoreCase(name)) {
                return true;
            }
        }
        return false;
    }


    // fold and flip
    private final static List<String> HUAWEI_MODEL_LIST =
            List.of("TAH-AN00", "GRL-AL10", "TET-AN50", "PAL-AL00", "TAH-N29m", "TAH-AN00m", "ICL-AL20", "ICL-AL10",
                    "BAL-AL00", "BAL-L49", "BAL-AL60", "PSD-AL00", "LEM-AL00", "ALT-AL10");

    // fold and flip
    private final static List<String> VIVO_MODEL_LIST =
            List.of("V2337A", "V2330", "V2178A", "V2229A", "V2266A", "V2303A",
                    "V2256A");

    // fold and flip
    private final static List<String> OPPO_MODEL_LIST =
            List.of("PKH110", "CPH2671", "PKH120", "CPH2499", "PHN110", "PEUM00",
                    "CPH2519", "PHT110", "PGT110", "CPH2437");

    // fold and flip
    private final static List<String> GALAXY_MODEL_LIST =
            List.of("SCV47", "SCG04", "SC-54B", "SCG12", "SC-54C", "SCG17", "SC-54D", "SCG23", "SC-54E", "SCG29",
                    "SC-55B", "SCG11", "SC-55C", "SCG16", "SC-55D", "SCG22", "SC-55E", "SCG28");

    // fold and flip
    private final static List<String> HONOR_MODEL_LIST =
            List.of("FCP-AN00", "FCP-AN10", "FCP-N49",
                    "LRA-AN00");

    // fold and flip
    private final static List<String> XIAOMI_MODEL_LIST =
            List.of("M2011J18C", "22061218C", "2308CPXD0C", "24072PX77C", "Xiaomi for arm64",
                    "2405CPX3DC", "2405CPX3DG");

    // flip
    private final static List<String> NUBIA_MODEL_LIST =
            List.of("NX732J", "NX724J");

    // flip
    private final static List<String> MOTO_MODEL_LIST =
            List.of("XT2323-3", "XT2321-2", "XT2451-4", "XT2251-1", "XT2451-3");

    // fold
    private final static List<String> PIXEL_MODEL_LIST =
            List.of("Pixel Fold", "Pixel 9 Pro Fold");

    // 柔宇ROYOLE fold
    private final static List<String> VERTU_MODEL_LIST =
            List.of("VERTU Ayxta Fold 3", "SEL135F18GM");

}
