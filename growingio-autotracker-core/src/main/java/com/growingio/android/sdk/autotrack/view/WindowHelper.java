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

import android.app.Activity;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import com.growingio.android.sdk.autotrack.page.Page;
import com.growingio.android.sdk.autotrack.shadow.WindowManagerShadow;
import com.growingio.android.sdk.track.log.Logger;
import com.growingio.android.sdk.track.providers.ActivityStateProvider;

import java.util.ArrayList;
import java.util.List;

public class WindowHelper {
    private static final String TAG = "WindowHelper";

    public static final String PAGE_PREFIX = "/Page";
    public static final String IGNORE_PAGE_PREFIX = "/IgnorePage";

    public static final String MAIN_WINDOW_PREFIX = "/MainWindow";
    public static final String DIALOG_WINDOW_PREFIX = "/DialogWindow";
    public static final String POPUP_WINDOW_PREFIX = "/PopupWindow";
    public static final String CUSTOM_WINDOW_PREFIX = "/CustomWindow";

    private final WindowManagerShadow mWindowManager;

    private WindowHelper() {
        WindowManagerShadow managerShadow = null;
        try {
            managerShadow = new WindowManagerShadow();
        } catch (Exception e) {
            Logger.e(TAG, e);
        }
        mWindowManager = managerShadow;
    }

    private static class SingleInstance {
        private static final WindowHelper INSTANCE = new WindowHelper();
    }

    public static WindowHelper get() {
        return SingleInstance.INSTANCE;
    }

    public boolean isDecorView(View rootView) {
        return !(rootView.getParent() instanceof View);
    }

    public String getMainWindowPrefix() {
        return MAIN_WINDOW_PREFIX;
    }

    public String getWindowPrefix(View root) {
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

    private String getSubWindowPrefix(View root) {
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

    @NonNull
    public List<DecorView> getTopActivityViews() {
        List<DecorView> topViews = new ArrayList<>();
        Activity activity = ActivityStateProvider.get().getForegroundActivity();
        List<DecorView> decorViews = getAllWindowDecorViews();
        boolean findTopActivity = false;
        for (DecorView decorView : decorViews) {
            View view = decorView.getView();
            if (view == activity.getWindow().getDecorView()
                    || view.getContext() == activity) {
                topViews.add(decorView);
                findTopActivity = true;
            } else if (findTopActivity) {
                topViews.add(decorView);
            }
        }
        return topViews;
    }

    public List<DecorView> getAllWindowDecorViews() {
        List<DecorView> decorViews = new ArrayList<>();
        View[] allViews = WindowHelper.get().getWindowViews();
        for (View view : allViews) {
            int[] location = new int[2];
            view.getLocationOnScreen(location);
            int x = location[0];
            int y = location[1];

            Rect area = new Rect(x, y, x + view.getWidth(), y + view.getHeight());
            if (view.getLayoutParams() instanceof WindowManager.LayoutParams) {
                WindowManager.LayoutParams windowParams = (WindowManager.LayoutParams) view.getLayoutParams();
                decorViews.add(new DecorView(view, area, windowParams));
            }
        }
        return decorViews;
    }

    @Nullable
    public View getTopActivityDecorView() {
        Activity current = ActivityStateProvider.get().getForegroundActivity();
        if (current != null) {
            try {
                return current.getWindow().getDecorView();
            } catch (Exception e) {
                // Unable to resume activity {xxxActivity}: java.lang.RuntimeException: Window couldn't find content container view
                Logger.e(TAG, e);
            }
        }
        return null;
    }

    private View[] getWindowViews() {
        if (mWindowManager != null) {
            try {
                return mWindowManager.getAllWindowViews();
            } catch (IllegalAccessException e) {
                Logger.e(TAG, e);
            }
        }

        // 如果无法获取WindowManager就只遍历当前Activity的内容
        View decorView = getTopActivityDecorView();
        if (decorView != null) {
            return new View[]{decorView};
        }

        return new View[0];
    }
}
