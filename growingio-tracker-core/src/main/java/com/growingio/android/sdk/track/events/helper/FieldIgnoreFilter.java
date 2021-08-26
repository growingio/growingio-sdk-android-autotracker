/*
 *   Copyright (C) 2020 Beijing Yishu Technology Co., Ltd.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package com.growingio.android.sdk.track.events.helper;

import androidx.annotation.IntDef;

import com.growingio.android.sdk.track.log.Logger;
import com.growingio.android.sdk.track.providers.ConfigurationProvider;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.Arrays;

public class FieldIgnoreFilter {

    @IntDef(
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
    public @interface FieldFilterType {
    }

    public static final int NETWORK_STATE = 1;
    public static final int SCREEN_HEIGHT = 1 << 1;
    public static final int SCREEN_WIDTH = 1 << 2;
    public static final int DEVICE_BRAND = 1 << 3;
    public static final int DEVICE_MODEL = 1 << 4;
    public static final int DEVICE_TYPE = 1 << 5;

    public static final int FIELD_IGNORE_ALL = (NETWORK_STATE | SCREEN_HEIGHT | SCREEN_WIDTH | DEVICE_BRAND | DEVICE_MODEL | DEVICE_TYPE);
    public static final int FIELD_IGNORE_NONE = 0;

    private static final ArrayList<String> FIELD_NAME_LIST = new ArrayList<>(
            Arrays.asList("networkState", "screenHeight", "screenWidth", "deviceBrand", "deviceModel", "deviceType"));

    private FieldIgnoreFilter() {
    }

    public static int of(@FieldFilterType int... types) {
        int value = 0;
        for (int type : types) {
            value |= type;
        }
        Logger.d("IgnoreFieldsParams", getFieldFilterLog(value));
        return value;
    }

    public static int valueOf(String typeName) {
        return FIELD_NAME_LIST.contains(typeName) ? (1 << FIELD_NAME_LIST.indexOf(typeName)) : 0;
    }

    public static boolean isFieldFilter(String typeName) {
        int ignoreFieldsFlag = ConfigurationProvider.core().getIgnoreField();
        if (ignoreFieldsFlag > 0) {
            int fieldMask = valueOf(typeName);
            return (ignoreFieldsFlag & fieldMask) > 0;
        }
        return false;
    }

    public static String getFieldFilterLog(int ignoreMask) {
        StringBuilder stringBuilder = new StringBuilder();
        int index = 0;
        while (ignoreMask > 0) {
            if (index >= FIELD_NAME_LIST.size()) break;
            if (ignoreMask % 2 > 0) {
                stringBuilder.append(FIELD_NAME_LIST.get(index));
                stringBuilder.append(",");
            }
            ignoreMask = ignoreMask / 2;
            index++;
        }
        if (stringBuilder.length() > 0) {
            stringBuilder.deleteCharAt(stringBuilder.length() - 1);
            stringBuilder.insert(0, "[").append("]");
            stringBuilder.append(" is ignoring ...");
            return stringBuilder.toString();
        }
        return "";
    }

}