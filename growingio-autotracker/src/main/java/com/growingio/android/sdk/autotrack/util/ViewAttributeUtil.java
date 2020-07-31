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

package com.growingio.android.sdk.autotrack.util;

import android.view.View;

import com.growingio.android.sdk.autotrack.page.Page;

import java.util.List;

public class ViewAttributeUtil {

    private static final int GROWING_TAG_KEY = 0x5042b06; //16进制的北京易数科技有限公司电话号码
    private static final int GROWING_WEB_CLIENT_KEY = GROWING_TAG_KEY + 1;
    private static final int GROWING_WEB_BRIDGE_KEY = GROWING_WEB_CLIENT_KEY + 1;
    private static final int GROWING_VIEW_NAME_KEY = GROWING_WEB_BRIDGE_KEY + 1;
    private static final int GROWING_VIEW_ID_KEY = GROWING_VIEW_NAME_KEY + 1;
    private static final int GROWING_INHERIT_INFO_KEY = GROWING_VIEW_ID_KEY + 1; // 原e中obj的tag, 目前废弃
    private static final int GROWING_CONTENT_KEY = GROWING_INHERIT_INFO_KEY + 1;
    private static final int GROWING_MONITORING_VIEWTREE_KEY = GROWING_CONTENT_KEY + 1;
    private static final int GROWING_MONITORING_FOCUS_KEY = GROWING_MONITORING_VIEWTREE_KEY + 1;
    private static final int GROWING_BANNER_KEY = GROWING_MONITORING_FOCUS_KEY + 1;
    private static final int GROWING_IGNORE_VIEW_KEY = GROWING_BANNER_KEY + 1;
    private static final int GROWING_HEAT_MAP_KEY = GROWING_IGNORE_VIEW_KEY + 1;
    private static final int GROWING_HOOK_LISTENTER = GROWING_HEAT_MAP_KEY + 1;
    private static final int GROWING_TRACK_TEXT = GROWING_HOOK_LISTENTER + 1;      // 记录改EditText的文本值信息
    private static final int GROWING_RN_PAGE_KEY = GROWING_TRACK_TEXT + 1;
    private static final int GROWING_IGNORE_VIEW_IMP_KEY = GROWING_RN_PAGE_KEY + 1;
    private static final int GROWING_WEB_VIEW_URL = GROWING_IGNORE_VIEW_IMP_KEY + 1;
    private static final int GROWING_IMP_TAG_MARKED = GROWING_WEB_VIEW_URL + 1;
    private static final int GROWING_VIEW_CUSTOM_ID = GROWING_IMP_TAG_MARKED + 1;
    private static final int GROWING_VIEW_PAGE_KEY = GROWING_VIEW_CUSTOM_ID + 1;

    private ViewAttributeUtil() {
    }

    public static void setViewId(View view, String id) {
        view.setTag(GROWING_VIEW_ID_KEY, id);
    }

    public static String getViewId(View view) {
        Object viewId = view.getTag(GROWING_VIEW_ID_KEY);
        if (viewId instanceof String) {
            return (String) viewId;
        }

        return null;
    }

    public static void setCustomId(View view, String cid) {
        view.setTag(GROWING_VIEW_CUSTOM_ID, cid);
    }

    public static String getCustomId(View view) {
        Object customId = view.getTag(GROWING_VIEW_CUSTOM_ID);
        if (customId instanceof String) {
            return (String) customId;
        }

        return null;
    }

    public static void setImpMarked(View view, Boolean marked) {
        view.setTag(GROWING_IMP_TAG_MARKED, marked);
    }

    public static Boolean getImpMarked(View view) {
        Object marked = view.getTag(GROWING_IMP_TAG_MARKED);
        if (marked instanceof Boolean) {
            return (Boolean) marked;
        }

        return null;
    }

    /**
     * 是否采集EditText中text
     */
    public static void setTrackText(View view, Boolean trackText) {
        view.setTag(GROWING_TRACK_TEXT, trackText);
    }

    public static Boolean getTrackText(View view) {
        Object trackText = view.getTag(GROWING_TRACK_TEXT);
        if (trackText instanceof Boolean) {
            return (Boolean) trackText;
        }

        return null;
    }

    public static void setWebViewUrl(View view, String url) {
        view.setTag(GROWING_WEB_VIEW_URL, url);
    }

    public static String getWebViewUrl(View view) {
        Object url = view.getTag(GROWING_WEB_VIEW_URL);
        if (url instanceof String) {
            return (String) url;
        }

        return null;
    }

    public static void setIgnoreImpKey(View view, Boolean ignore) {
        view.setTag(GROWING_IGNORE_VIEW_IMP_KEY, ignore);
    }

    public static Boolean getIgnoreImpKey(View view) {
        Object ignore = view.getTag(GROWING_IGNORE_VIEW_IMP_KEY);
        if (ignore instanceof Boolean) {
            return (Boolean) ignore;
        }

        return null;
    }

    public static void setBannerKey(View view, List bannerContents) {
        view.setTag(GROWING_BANNER_KEY, bannerContents);
    }

    public static List getBannerKey(View view) {
        Object bannerContents = view.getTag(GROWING_BANNER_KEY);
        if (bannerContents instanceof List) {
            return (List) bannerContents;
        }

        return null;
    }

    public static void setViewNameKey(View view, String name) {
        view.setTag(GROWING_VIEW_NAME_KEY, name);
    }

    public static String getViewNameKey(View view) {
        Object name = view.getTag(GROWING_VIEW_NAME_KEY);
        if (name instanceof String) {
            return (String) name;
        }

        return null;
    }

    public static void setContentKey(View view, String content) {
        view.setTag(GROWING_CONTENT_KEY, content);
    }

    public static String getContentKey(View view) {
        Object content = view.getTag(GROWING_CONTENT_KEY);
        if (content instanceof String) {
            return (String) content;
        }

        return null;
    }

    public static void setRnPageKey(View view, String rnPage) {
        view.setTag(GROWING_RN_PAGE_KEY, rnPage);
    }

    public static String getRnPageKey(View view) {
        Object rnPage = view.getTag(GROWING_RN_PAGE_KEY);
        if (rnPage instanceof String) {
            return (String) rnPage;
        }

        return null;
    }

    public static void setIgnoreViewKey(View view, Boolean ignore) {
        view.setTag(GROWING_IGNORE_VIEW_KEY, ignore);
    }

    public static Boolean getIgnoreViewKey(View view) {
        Object ignore = view.getTag(GROWING_IGNORE_VIEW_KEY);
        if (ignore instanceof Boolean) {
            return (Boolean) ignore;
        }

        return null;
    }

    public static void setMonitoringViewTreeEnabled(View view, boolean monitoring) {
        if (monitoring) {
            view.setTag(GROWING_MONITORING_VIEWTREE_KEY, new Object());
        } else {
            view.setTag(GROWING_MONITORING_VIEWTREE_KEY, null);
        }
    }

    public static boolean isMonitoringViewTree(View view) {
        Object monitoring = view.getTag(GROWING_MONITORING_VIEWTREE_KEY);
        return monitoring != null;
    }

    public static void setMonitoringFocusKey(View view, String text) {
        view.setTag(GROWING_MONITORING_FOCUS_KEY, text);
    }

    public static String getMonitoringFocusKey(View view) {
        Object text = view.getTag(GROWING_MONITORING_FOCUS_KEY);
        if (text instanceof String) {
            return (String) text;
        }

        return null;
    }

    public static void setViewPage(View view, Page<?> page) {
        view.setTag(GROWING_VIEW_PAGE_KEY, page);
    }

    public static Page<?> getViewPage(View view) {
        Object page = view.getTag(GROWING_VIEW_PAGE_KEY);
        if (page instanceof Page) {
            return (Page<?>) page;
        }
        return null;
    }

}
