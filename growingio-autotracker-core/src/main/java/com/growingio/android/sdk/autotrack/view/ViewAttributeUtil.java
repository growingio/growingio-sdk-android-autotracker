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

package com.growingio.android.sdk.autotrack.view;

import android.view.View;

import com.growingio.android.sdk.autotrack.IgnorePolicy;
import com.growingio.android.sdk.autotrack.R;
import com.growingio.android.sdk.autotrack.page.Page;

public class ViewAttributeUtil {

    private ViewAttributeUtil() {
    }

    public static void setCustomId(View view, String cid) {
        view.setTag(R.id.growing_tracker_view_custom_id, cid);
    }

    public static String getCustomId(View view) {
        Object customId = view.getTag(R.id.growing_tracker_view_custom_id);
        if (customId instanceof String) {
            return (String) customId;
        }

        return null;
    }

    /**
     * 是否采集EditText中text
     */
    public static void setTrackText(View view, Boolean trackText) {
        view.setTag(R.id.growing_tracker_track_text, trackText);
    }

    public static Boolean getTrackText(View view) {
        Object trackText = view.getTag(R.id.growing_tracker_track_text);
        if (trackText instanceof Boolean) {
            return (Boolean) trackText;
        }

        return null;
    }

    public static void setContent(View view, String content) {
        view.setTag(R.id.growing_tracker_view_content, content);
    }

    public static String getContent(View view) {
        Object content = view.getTag(R.id.growing_tracker_view_content);
        if (content instanceof String) {
            return (String) content;
        }

        return null;
    }

    public static void setIgnorePolicy(View view, IgnorePolicy policy) {
        view.setTag(R.id.growing_tracker_ignore_policy, policy);
    }

    public static IgnorePolicy getIgnorePolicy(View view) {
        Object ignorePolicy = view.getTag(R.id.growing_tracker_ignore_policy);
        if (ignorePolicy instanceof IgnorePolicy) {
            return (IgnorePolicy) ignorePolicy;
        }

        return null;
    }

    public static void setMonitoringViewTreeEnabled(View view, boolean monitoring) {
        if (monitoring) {
            view.setTag(R.id.growing_tracker_monitoring_view_tree_enabled, new Object());
        } else {
            view.setTag(R.id.growing_tracker_monitoring_view_tree_enabled, null);
        }
    }

    public static boolean isMonitoringViewTree(View view) {
        Object monitoring = view.getTag(R.id.growing_tracker_monitoring_view_tree_enabled);
        return monitoring != null;
    }

    public static void setMonitoringFocusContent(View view, String text) {
        view.setTag(R.id.growing_tracker_monitoring_focus_content, text);
    }

    public static String getMonitoringFocusContent(View view) {
        Object text = view.getTag(R.id.growing_tracker_monitoring_focus_content);
        if (text instanceof String) {
            return (String) text;
        }

        return null;
    }

    public static void setViewPage(View view, Page<?> page) {
        view.setTag(R.id.growing_tracker_view_page, page);
    }

    public static Page<?> getViewPage(View view) {
        Object page = view.getTag(R.id.growing_tracker_view_page);
        if (page instanceof Page) {
            return (Page<?>) page;
        }
        return null;
    }

}
