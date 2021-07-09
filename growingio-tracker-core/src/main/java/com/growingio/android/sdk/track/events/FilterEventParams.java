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
import java.util.ArrayList;
import java.util.Arrays;

public final class FilterEventParams {

    @IntDef(
            flag = true,
            value = {
                    VISIT,
                    CUSTOM,
                    VISITOR_ATTRIBUTES,
                    LOGIN_USER_ATTRIBUTES,
                    CONVERSION_VARIABLES,
                    APP_CLOSED,
                    PAGE,
                    PAGE_ATTRIBUTES,
                    VIEW_CLICK,
                    VIEW_CHANGE,
                    FORM_SUBMIT,
                    REENGAGE,
            }
    )
    @Retention(RetentionPolicy.SOURCE)
    public @interface FilterEventType {
    }

    public static final int VISIT = 1;
    public static final int CUSTOM = 1 << 1;
    public static final int VISITOR_ATTRIBUTES = 1 << 2;
    public static final int LOGIN_USER_ATTRIBUTES = 1 << 3;
    public static final int CONVERSION_VARIABLES = 1 << 4;
    public static final int APP_CLOSED = 1 << 5;
    public static final int PAGE = 1 << 6;
    public static final int PAGE_ATTRIBUTES = 1 << 7;
    public static final int VIEW_CLICK = 1 << 8;
    public static final int VIEW_CHANGE = 1 << 9;
    public static final int FORM_SUBMIT = 1 << 10;
    public static final int REENGAGE = 1 << 11;

    public static final int MASK_CLICK_CHANGE_SUBMIT = of(VIEW_CLICK, VIEW_CHANGE, FORM_SUBMIT);

    private static final ArrayList<String> EVENT_LIST = new ArrayList<>(
            Arrays.asList("VISIT", "CUSTOM", "VISITOR_ATTRIBUTES", "LOGIN_USER_ATTRIBUTES",
                    "CONVERSION_VARIABLES", "APP_CLOSED", "PAGE", "PAGE_ATTRIBUTES", "VIEW_CLICK",
                    "VIEW_CHANGE", "FORM_SUBMIT", "REENGAGE"));

    private FilterEventParams() {
    }

    public static int of(@FilterEventType int... types) {
        int value = 0;
        for (int type : types) {
            value |= type;
        }
        return value;
    }

    public static int valueOf(String typeName) {
        return EVENT_LIST.contains(typeName) ? (1 << EVENT_LIST.indexOf(typeName)) : 0;
    }

    public static boolean isFilterEvent(String typeName) {
        int filterMask = ConfigurationProvider.core().getEventFilterMask();
        if (filterMask > 0) {
            return (filterMask & valueOf(typeName)) > 0;
        }
        return false;
    }

    public static String getFilterEventLog() {
        int filterMask = ConfigurationProvider.core().getEventFilterMask();
        StringBuilder stringBuilder = new StringBuilder();
        int index = 0;

        while (filterMask > 0) {
            if (filterMask % 2 > 0) {
                stringBuilder.append(EVENT_LIST.get(index));
                stringBuilder.append(",");
            }
            filterMask = filterMask / 2;
            index++;
        }

        if (stringBuilder.length() > 0) {
            stringBuilder.deleteCharAt(stringBuilder.length() - 1);
            stringBuilder.append(" not tracking ...");
            return stringBuilder.toString();
        }
        return "";
    }

}