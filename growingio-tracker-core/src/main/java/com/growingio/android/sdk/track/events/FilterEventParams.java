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

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

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
    public @interface FilterEventType { }

    public static final int VISIT                  = 1;
    public static final int CUSTOM                 = 1 << 1;
    public static final int VISITOR_ATTRIBUTES     = 1 << 2;
    public static final int LOGIN_USER_ATTRIBUTES  = 1 << 3;
    public static final int CONVERSION_VARIABLES   = 1 << 4;
    public static final int APP_CLOSED             = 1 << 5;
    public static final int PAGE                   = 1 << 6;
    public static final int PAGE_ATTRIBUTES        = 1 << 7;
    public static final int VIEW_CLICK             = 1 << 8;
    public static final int VIEW_CHANGE            = 1 << 9;
    public static final int FORM_SUBMIT            = 1 << 10;
    public static final int REENGAGE               = 1 << 11;

    public static final int MASK_CLICK_CHANGE_SUBMIT = of(VIEW_CLICK, VIEW_CHANGE, FORM_SUBMIT);

    private FilterEventParams() {
    }

    public static int of(@FilterEventType int... types) {
        int value = 0;
        for (int type : types) {
            value |= type;
        }
        return value;
    }

    @FilterEventType
    public static int valueOf(String typeName) {
        switch (typeName) {
            case "VISIT":
                return VISIT;
            case "CUSTOM":
                return CUSTOM;
            case "VISITOR_ATTRIBUTES":
                return VISITOR_ATTRIBUTES;
            case "LOGIN_USER_ATTRIBUTES":
                return LOGIN_USER_ATTRIBUTES;
            case "CONVERSION_VARIABLES":
                return CONVERSION_VARIABLES;
            case "APP_CLOSED":
                return APP_CLOSED;
            case "PAGE":
                return PAGE;
            case "PAGE_ATTRIBUTES":
                return PAGE_ATTRIBUTES;
            case "VIEW_CLICK":
                return VIEW_CLICK;
            case "VIEW_CHANGE":
                return VIEW_CHANGE;
            case "FORM_SUBMIT":
                return FORM_SUBMIT;
            case "REENGAGE":
                return REENGAGE;
            default:
                return 0;
        }
    }

}