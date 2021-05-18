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
import android.view.ViewGroup;
import android.view.WindowManager;

import com.growingio.android.sdk.autotrack.page.Page;
import com.growingio.android.sdk.track.providers.ActivityStateProvider;

/**
 * <p>
 *
 * @author cpacm 5/10/21
 */
class PageHelper {

    public static final String PAGE_PREFIX = "/Page";
    public static final String IGNORE_PAGE_PREFIX = "/IgnorePage";

    public static final String MAIN_WINDOW_PREFIX = "/MainWindow";
    public static final String DIALOG_WINDOW_PREFIX = "/DialogWindow";
    public static final String POPUP_WINDOW_PREFIX = "/PopupWindow";
    public static final String CUSTOM_WINDOW_PREFIX = "/CustomWindow";

    private PageHelper() {
    }

    static String getMainWindowPrefix() {
        return MAIN_WINDOW_PREFIX;
    }

    static String getWindowPrefix(View root) {
        String windowPrefix;
        Page<?> page = ViewAttributeUtil.getViewPage(root);
        if (page != null) {
            if (page.isIgnored()) {
                windowPrefix = IGNORE_PAGE_PREFIX;
            } else {
                windowPrefix = PAGE_PREFIX;
            }
        } else if (root.hashCode() == ActivityStateProvider.get().getCurrentRootWindowsHashCode()) {
            windowPrefix = getMainWindowPrefix();
        } else {
            windowPrefix = getSubWindowPrefix(root);
        }

        return windowPrefix;
    }

    private static String getSubWindowPrefix(View root) {
        ViewGroup.LayoutParams params = root.getLayoutParams();
        if (params instanceof WindowManager.LayoutParams) {
            WindowManager.LayoutParams windowParams = (WindowManager.LayoutParams) params;
            int type = windowParams.type;
            if (type == WindowManager.LayoutParams.TYPE_BASE_APPLICATION) {
                return MAIN_WINDOW_PREFIX;
            } else if (type < WindowManager.LayoutParams.LAST_APPLICATION_WINDOW) {
                return DIALOG_WINDOW_PREFIX;
            } else if (type < WindowManager.LayoutParams.LAST_SUB_WINDOW) {
                return POPUP_WINDOW_PREFIX;
            }
        }
        return CUSTOM_WINDOW_PREFIX;
    }
}
