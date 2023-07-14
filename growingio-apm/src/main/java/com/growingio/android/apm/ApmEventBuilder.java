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

package com.growingio.android.apm;

import com.growingio.android.gmonitor.event.Breadcrumb;
import com.growingio.android.sdk.track.events.CustomEvent;

import java.util.HashMap;

/**
 * <p>
 *
 * @author cpacm 2022/9/27
 */
public class ApmEventBuilder {

    private ApmEventBuilder() {
    }

    static final String EVENT_ERROR_NAME = "apm_system_error";
    private static final String EVENT_ERROR_TITLE = "error_type";
    private static final String EVENT_ERROR_CONTENT = "error_content";

    static final String EVENT_APP_LAUNCHTIME_NAME = "apm_app_launch";
    private static final String EVENT_REBOOT_MODE = "reboot_mode";
    private static final String EVENT_REBOOT_TIME = "reboot_duration";
    private static final String EVENT_PAGE_NAME = "title";
    private static final String EVENT_PAGE_DURATION = "page_launch_duration";

    static CustomEvent.Builder filterWithApmBreadcrumb(Breadcrumb breadcrumb) {
        if (equals(breadcrumb.getType(), Breadcrumb.TYPE_ERROR)) {
            String title = String.valueOf(breadcrumb.getData().get(Breadcrumb.ATTR_ERROR_TYPE));
            String message = String.valueOf(breadcrumb.getData().get(Breadcrumb.ATTR_ERROR_MESSAGE));
            return errorBuilder(title, message);
        } else if (breadcrumb.getType().equals(Breadcrumb.TYPE_PERFORMANCE)) {
            if (equals(breadcrumb.getCategory(), Breadcrumb.CATEGORY_PERFORMANCE_APP)) {
                // ignore app_start_interval
                return null;
//                boolean isCold = true;
//                Object isColdObj = breadcrumb.getData().get(Breadcrumb.ATTR_PERFORMANCE_APP_COLD);
//                if (isColdObj != null) {
//                    isCold = (boolean) isColdObj;
//                }
//                long duration = 0L;
//                Object durationObj = breadcrumb.getData().get(Breadcrumb.ATTR_PERFORMANCE_DURATION);
//                if (durationObj != null) {
//                    duration = (long) durationObj;
//                }
//                return appStartBuilder(isCold, duration);
            } else if (equals(breadcrumb.getCategory(), Breadcrumb.CATEGORY_PERFORMANCE_ACTIVITY)) {
                String name = String.valueOf(breadcrumb.getData().get(Breadcrumb.ATTR_PERFORMANCE_PAGE_NAME));
                long duration = 0L;
                Object durationObj = breadcrumb.getData().get(Breadcrumb.ATTR_PERFORMANCE_DURATION);
                if (durationObj != null) {
                    duration = (long) durationObj;
                }
                if (breadcrumb.getData().containsKey(Breadcrumb.ATTR_PERFORMANCE_APP_COLD)) {
                    boolean isCold = true;
                    Object isColdObj = breadcrumb.getData().get(Breadcrumb.ATTR_PERFORMANCE_APP_COLD);
                    if (isColdObj != null) {
                        isCold = (boolean) isColdObj;
                    }
                    long appDuration = 0L;
                    Object appDurationObj = breadcrumb.getData().get(Breadcrumb.ATTR_PERFORMANCE_APP_DURATION);
                    if (appDurationObj != null) {
                        appDuration = (long) appDurationObj;
                    }
                    return pageStartBuilderWithHot(name, duration, isCold, appDuration);
                }
                return pageStartBuilder(name, duration);
            } else if (equals(breadcrumb.getCategory(), Breadcrumb.CATEGORY_PERFORMANCE_FRAGMENT)) {
                String name = String.valueOf(breadcrumb.getData().get(Breadcrumb.ATTR_PERFORMANCE_PAGE_NAME));
                long duration = 0L;
                Object durationObj = breadcrumb.getData().get(Breadcrumb.ATTR_PERFORMANCE_DURATION);
                if (durationObj != null) {
                    duration = (long) durationObj;
                }
                return pageStartBuilder(name, duration);
            }
        }
        return null;
    }

    private static CustomEvent.Builder errorBuilder(String title, String errorContent) {
        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put(EVENT_ERROR_TITLE, title);
        hashMap.put(EVENT_ERROR_CONTENT, errorContent);

        CustomEvent.Builder builder = new CustomEvent.Builder();
        builder.setEventName(EVENT_ERROR_NAME)
                .setAttributes(hashMap);
        return builder;
    }

    private static CustomEvent.Builder appStartBuilder(boolean isCold, long duration) {
        HashMap<String, String> hashMap = new HashMap<>();
        if (isCold) {
            hashMap.put(EVENT_REBOOT_MODE, "cold");
            hashMap.put(EVENT_REBOOT_TIME, String.valueOf(duration));
        } else {
            hashMap.put(EVENT_REBOOT_MODE, "warm");
            hashMap.put(EVENT_REBOOT_TIME, String.valueOf(duration));
        }

        CustomEvent.Builder builder = new CustomEvent.Builder();
        builder.setEventName(EVENT_APP_LAUNCHTIME_NAME)
                .setAttributes(hashMap);
        return builder;
    }

    private static CustomEvent.Builder pageStartBuilder(String pageName, long pageDuration) {
        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put(EVENT_PAGE_NAME, pageName);
        hashMap.put(EVENT_PAGE_DURATION, String.valueOf(pageDuration));
        CustomEvent.Builder builder = new CustomEvent.Builder();
        builder.setEventName(EVENT_APP_LAUNCHTIME_NAME)
                .setAttributes(hashMap);
        return builder;
    }

    private static CustomEvent.Builder pageStartBuilderWithHot(String pageName, long pageDuration, boolean isCold, long appDuration) {
        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put(EVENT_PAGE_NAME, pageName);
        hashMap.put(EVENT_PAGE_DURATION, String.valueOf(pageDuration));
        if (isCold) {
            hashMap.put(EVENT_REBOOT_MODE, "cold");
            hashMap.put(EVENT_REBOOT_TIME, String.valueOf(appDuration));
        } else {
            hashMap.put(EVENT_REBOOT_MODE, "warm");
            hashMap.put(EVENT_REBOOT_TIME, String.valueOf(appDuration == 0L ? pageDuration : appDuration));
        }
        CustomEvent.Builder builder = new CustomEvent.Builder();
        builder.setEventName(EVENT_APP_LAUNCHTIME_NAME)
                .setAttributes(hashMap);
        return builder;
    }

    private static boolean equals(Object a, Object b) {
        return (a == b) || (a != null && a.equals(b));
    }
}
