/*
 * Copyright (C) 2023 Beijing Yishu Technology Co., Ltd.
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
package com.growingio.android.sdk.track.utils;

import android.webkit.WebView;
import android.widget.AdapterView;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

public class ClassExistHelper {
    public static boolean sHasAndroidXViewPager;
    public static boolean sHasAndroidXListMenuItemView;
    private static final boolean HAS_X5_WEBVIEW;
    private static final boolean HAS_UC_WEBVIEW;
    private static final boolean HAS_ANDROIDX_RECYCLERVIEW;
    private static final boolean HAS_ANDROIDX_SWIPE_REFRESH;
    private static final boolean HAS_ANDROIDX_FRAGMENT;
    private static final boolean HAS_ANDROIDX_FRAGMENT_ACTIVITY;
    private static final boolean HAS_ANDROIDX_ALERT_DIALOG;

    private static final String SUPPORT_RECYCLER_VIEW_CLASS = "android.support.v7.widget.RecyclerView";
    private static final String SUPPORT_VIEW_PAGER_CLASS = "android.support.v4.view.ViewPager";
    private static final String SUPPORT_SWIPE_REFRESH_CLASS = "android.support.v4.widget.SwipeRefreshLayout";
    private static final String SUPPORT_FRAGMENT_CLASS = "android.support.v4.app.Fragment";
    private static final String SUPPORT_FRAGMENT_ACTIVITY_CLASS = "android.support.v4.app.FragmentActivity";
    private static final String SUPPORT_ALERT_DIALOG_CLASS = "android.support.v7.app.AlertDialog";
    private static final String SUPPORT_LIST_MENU_ITEM_CLASS = "android.support.v7.view.menu.ListMenuItemView";
    private static final String SUPPORT_TOOLBAR_CLASS = "android.support.v7.widget.Toolbar";

    static {
        HAS_X5_WEBVIEW = hasClass("com.tencent.smtt.sdk.WebView");
        HAS_UC_WEBVIEW = hasClass("com.uc.webview.export.WebView");
        HAS_ANDROIDX_RECYCLERVIEW = hasClass("androidx.recyclerview.widget.RecyclerView");
        sHasAndroidXViewPager = hasClass("androidx.viewpager.widget.ViewPager");
        HAS_ANDROIDX_SWIPE_REFRESH = hasClass("androidx.swiperefreshlayout.widget.SwipeRefreshLayout");
        HAS_ANDROIDX_FRAGMENT = hasClass("androidx.fragment.app.Fragment");
        HAS_ANDROIDX_FRAGMENT_ACTIVITY = hasClass("androidx.fragment.app.FragmentActivity");
        HAS_ANDROIDX_ALERT_DIALOG = hasClass("androidx.appcompat.app.AlertDialog");
        sHasAndroidXListMenuItemView = hasClass("androidx.appcompat.view.menu.ListMenuItemView");
    }

    private ClassExistHelper() {
    }

    public static boolean hasClass(String className) {
        try {
            Class.forName(className);
            return true;
        } catch (Throwable e) {
            return false;
        }
    }

    private static boolean isSupportClass(String className, Object support) {
        try {
            Class<?> supportClass = Class.forName(className);
            if (supportClass != null && support != null) return supportClass.isInstance(support);
            return false;
        } catch (Throwable e) {
            return false;
        }
    }

    /**
     * 判断View是否是RecyclerView, 包括Support包与AndroidX， 以及部分自定义的RecyclerView
     */
    public static boolean instanceOfRecyclerView(Object view) {
        return instanceOfAndroidXRecyclerView(view)
                || instanceOfSupportRecyclerView(view);
    }

    public static boolean isListView(Object view) {
        return (view instanceof AdapterView
                || (ClassExistHelper.instanceOfAndroidXRecyclerView(view))
                || (ClassExistHelper.instanceOfAndroidXViewPager(view))
                || (ClassExistHelper.instanceOfSupportRecyclerView(view))
                || (ClassExistHelper.instanceOfSupportViewPager(view)));
    }

    public static boolean isWebView(Object view) {
        return view instanceof WebView
                || ClassExistHelper.instanceOfX5WebView(view)
                || ClassExistHelper.instanceOfUcWebView(view);
    }

    public static boolean instanceOfAndroidXRecyclerView(Object view) {
        return HAS_ANDROIDX_RECYCLERVIEW && view instanceof androidx.recyclerview.widget.RecyclerView;
    }

    public static boolean instanceOfAndroidXViewPager(Object view) {
        return sHasAndroidXViewPager && view instanceof androidx.viewpager.widget.ViewPager;
    }

    public static boolean instanceofAndroidXSwipeRefreshLayout(Object view) {
        return HAS_ANDROIDX_SWIPE_REFRESH && view instanceof androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
    }

    public static boolean instanceOfX5WebView(Object view) {
        return HAS_X5_WEBVIEW && view instanceof com.tencent.smtt.sdk.WebView;
    }

    public static boolean instanceOfUcWebView(Object view) {
        return HAS_UC_WEBVIEW && view instanceof com.uc.webview.export.WebView;
    }

    public static boolean instanceOfAndroidXAlertDialog(Object dialog) {
        return HAS_ANDROIDX_ALERT_DIALOG && dialog instanceof AlertDialog;
    }

    public static boolean instanceOfAndroidXFragmentActivity(Object activity) {
        return HAS_ANDROIDX_FRAGMENT_ACTIVITY && activity instanceof androidx.fragment.app.FragmentActivity;
    }

    public static boolean instanceOfAndroidXFragment(Object fragment) {
        return HAS_ANDROIDX_FRAGMENT && fragment instanceof Fragment;
    }

    public static boolean instanceOfAndroidXListMenuItemView(Object itemView) {
        return sHasAndroidXListMenuItemView && itemView instanceof androidx.appcompat.view.menu.ListMenuItemView;
    }

    public static boolean instanceOfSupportRecyclerView(Object view) {
        return isSupportClass(SUPPORT_RECYCLER_VIEW_CLASS, view);
    }

    public static boolean instanceOfSupportViewPager(Object view) {
        return isSupportClass(SUPPORT_VIEW_PAGER_CLASS, view);
    }

    public static boolean instanceOfSupportSwipeRefreshLayout(Object view) {
        return isSupportClass(SUPPORT_SWIPE_REFRESH_CLASS, view);
    }

    public static boolean instanceOfSupportAlertDialog(Object dialog) {
        return isSupportClass(SUPPORT_ALERT_DIALOG_CLASS, dialog);
    }

    public static boolean instanceOfSupportFragmentActivity(Object activity) {
        return isSupportClass(SUPPORT_FRAGMENT_ACTIVITY_CLASS, activity);
    }

    public static boolean instanceOfSupportFragment(Object fragment) {
        return isSupportClass(SUPPORT_FRAGMENT_CLASS, fragment);
    }

    public static boolean instanceOfSupportListMenuItemView(Object itemView) {
        return isSupportClass(SUPPORT_LIST_MENU_ITEM_CLASS, itemView);
    }

    public static boolean instanceOfSupportToolBar(Object view) {
        return isSupportClass(SUPPORT_TOOLBAR_CLASS, view);
    }
}
