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

package com.growingio.android.sdk.track.events;

import androidx.annotation.IntDef;

import com.growingio.android.sdk.track.providers.ConfigurationProvider;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class IgnoreFieldsParams {

    @IntDef(
            flag = true,
            value = {
                    NETWORK_STATE,
                    SCREEN_HEIGHT,
                    SCREEN_WIDTH,
                    DEVICE_BRAND,
                    DEVICE_MODEL,
                    DEVICE_TYPE,
            }
    )
    @Retention(RetentionPolicy.SOURCE)
    public @interface IgnoreFieldsType {
    }

    public static final int NETWORK_STATE = 1;
    public static final int SCREEN_HEIGHT = 1 << 1;
    public static final int SCREEN_WIDTH = 1 << 2;
    public static final int DEVICE_BRAND = 1 << 3;
    public static final int DEVICE_MODEL = 1 << 4;
    public static final int DEVICE_TYPE = 1 << 5;

    public static final int IGNORE_ALL_FIELDS = of(NETWORK_STATE, SCREEN_HEIGHT, SCREEN_WIDTH, DEVICE_BRAND, DEVICE_MODEL, DEVICE_TYPE);

    private IgnoreFieldsParams() {
    }

    public static int of(@IgnoreFieldsParams.IgnoreFieldsType int... types) {
        int value = 0;
        for (int type : types) {
            value |= type;
        }
        return value;
    }

    @IgnoreFieldsParams.IgnoreFieldsType
    public static int valueOf(String typeName) {
        switch (typeName) {
            case "networkState":
                return NETWORK_STATE;
            case "screenHeight":
                return SCREEN_HEIGHT;
            case "screenWidth":
                return SCREEN_WIDTH;
            case "deviceBrand":
                return DEVICE_BRAND;
            case "deviceModel":
                return DEVICE_MODEL;
            case "deviceType":
                return DEVICE_TYPE;
            default:
                return 0;
        }
    }

    public static boolean isIgnoreField(String typeName) {

        int ignoreFieldsMask = ConfigurationProvider.core().getIgnoreFieldsMask();
        if (ignoreFieldsMask > 0) {
            int fieldMask = valueOf(typeName);
            return (ignoreFieldsMask & fieldMask) > 0;
        }
        return false;
    }

}