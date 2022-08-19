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

import com.growingio.android.sdk.track.events.AutotrackEventType;
import com.growingio.android.sdk.track.events.TrackEventType;
import com.growingio.android.sdk.track.providers.ConfigurationProvider;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.Arrays;

public final class EventExcludeFilter {

    @IntDef(
            flag = true,
            value = {
                    NONE,
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
                    EVENT_MASK_TRIGGER,
            }
    )
    @Retention(RetentionPolicy.CLASS)
    public @interface EventFilterLimit {
    }

    public static final int NONE = 0;
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
    public static final int ACTIVATE = 1 << 12;
    public static final int EVENT_MASK_TRIGGER = (VIEW_CLICK | VIEW_CHANGE | FORM_SUBMIT);


    public static final String EVENT_VISIT = TrackEventType.VISIT;
    public static final String EVENT_CUSTOM = TrackEventType.CUSTOM;
    public static final String EVENT_VISITOR_ATTRIBUTES = TrackEventType.VISITOR_ATTRIBUTES;
    public static final String EVENT_LOGIN_USER_ATTRIBUTES = TrackEventType.LOGIN_USER_ATTRIBUTES;
    public static final String EVENT_CONVERSION_VARIABLES = TrackEventType.CONVERSION_VARIABLES;
    public static final String EVENT_APP_CLOSED = TrackEventType.APP_CLOSED;
    public static final String EVENT_PAGE = AutotrackEventType.PAGE;
    public static final String EVENT_PAGE_ATTRIBUTES = AutotrackEventType.PAGE_ATTRIBUTES;
    public static final String EVENT_VIEW_CLICK = AutotrackEventType.VIEW_CLICK;
    public static final String EVENT_VIEW_CHANGE = AutotrackEventType.VIEW_CHANGE;
    public static final String EVENT_FORM_SUBMIT = "FORM_SUBMIT";
    public static final String EVENT_REENGAGE = "REENGAGE";
    public static final String EVENT_ACTIVATE = "ACTIVATE";


    //"FORM_SUBMIT" from Hybrid Module
    //"REENGAGE" is future's feature
    private static final ArrayList<String> EVENT_TYPE_LIST = new ArrayList<>(
            Arrays.asList(TrackEventType.VISIT, TrackEventType.CUSTOM, TrackEventType.VISITOR_ATTRIBUTES, TrackEventType.LOGIN_USER_ATTRIBUTES,
                    TrackEventType.CONVERSION_VARIABLES, TrackEventType.APP_CLOSED, AutotrackEventType.PAGE, AutotrackEventType.PAGE_ATTRIBUTES, AutotrackEventType.VIEW_CLICK,
                    AutotrackEventType.VIEW_CHANGE, "FORM_SUBMIT", "REENGAGE", "ACTIVATE"));


    private EventExcludeFilter() {
    }

    // of函数用于setExcludeEvent同时传入多个事件类型
    public static int of(@EventFilterLimit int... types) {
        int value = 0;
        if (types != null) {
            for (int type : types) {
                value |= type;
            }
        }
        return value;
    }

    public static int valueOf(String typeName) {
        return EVENT_TYPE_LIST.contains(typeName) ? (1 << EVENT_TYPE_LIST.indexOf(typeName)) : 0;
    }


    @Deprecated
    public static boolean isEventFilter(String typeName) {
        int filterFlag = ConfigurationProvider.core().getExcludeEvent();
        if (filterFlag > 0) {
            return (filterFlag & valueOf(typeName)) > 0;
        }
        return false;
    }

    public static boolean isEventFilter(String typeName, int filterFlag) {
        if (filterFlag > 0) {
            return (filterFlag & valueOf(typeName)) > 0;
        }
        return false;
    }

    public static String getEventFilterLog(int filterMask) {
        StringBuilder stringBuilder = new StringBuilder();
        int index = 0;

        while (filterMask > 0) {
            if (index >= EVENT_TYPE_LIST.size()) break;
            if (filterMask % 2 > 0) {
                stringBuilder.append(EVENT_TYPE_LIST.get(index));
                stringBuilder.append(",");
            }
            filterMask = filterMask / 2;
            index++;
        }
        if (stringBuilder.length() > 0) {
            stringBuilder.deleteCharAt(stringBuilder.length() - 1);
            stringBuilder.insert(0, "[").append("]");
            stringBuilder.append(" not tracking ...");
            return stringBuilder.toString();
        }
        return "";
    }

}