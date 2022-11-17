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

package com.growingio.android.sdk.track.utils;

import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.view.menu.ListMenuItemView;
import android.support.v7.widget.RecyclerView;
import android.webkit.WebView;
import android.widget.AdapterView;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

public class ClassExistHelper {
    public static boolean sHasSupportViewPager;
    public static boolean sHasSupportListMenuItemView;
    public static boolean sHasAndroidXViewPager;
    public static boolean sHasAndroidXListMenuItemView;
    private static boolean sHasX5WebView;
    private static boolean sHasUcWebView;
    private static boolean sHasSupportRecyclerView;
    private static boolean sHasSupportSwipeRefreshLayoutView;
    private static boolean sHasSupportFragment;
    private static boolean sHasSupportFragmentActivity;
    private static boolean sHasSupportAlertDialog;
    private static boolean sHasAndroidXRecyclerView;
    private static boolean sHasAndroidXSwipeRefreshLayoutView;
    private static boolean sHasAndroidXFragment;
    private static boolean sHasAndroidXFragmentActivity;
    private static boolean sHasAndroidXAlertDialog;

    static {
        sHasX5WebView = hasClass("com.tencent.smtt.sdk.WebView");
        sHasUcWebView = hasClass("com.uc.webview.export.WebView");
        sHasSupportRecyclerView = hasClass("android.support.v7.widget.RecyclerView");
        sHasSupportViewPager = hasClass("android.support.v4.view.ViewPager");
        sHasSupportSwipeRefreshLayoutView = hasClass("android.support.v4.widget.SwipeRefreshLayout");
        sHasSupportFragment = hasClass("android.support.v4.app.Fragment");
        sHasSupportFragmentActivity = hasClass("android.support.v4.app.FragmentActivity");
        sHasSupportAlertDialog = hasClass("android.support.v7.app.AlertDialog");
        sHasSupportListMenuItemView = hasClass("android.support.v7.view.menu.ListMenuItemView");
        sHasAndroidXRecyclerView = hasClass("androidx.recyclerview.widget.RecyclerView");
        sHasAndroidXViewPager = hasClass("androidx.viewpager.widget.ViewPager");
        sHasAndroidXSwipeRefreshLayoutView = hasClass("androidx.swiperefreshlayout.widget.SwipeRefreshLayout");
        sHasAndroidXFragment = hasClass("androidx.fragment.app.Fragment");
        sHasAndroidXFragmentActivity = hasClass("androidx.fragment.app.FragmentActivity");
        sHasAndroidXAlertDialog = hasClass("androidx.appcompat.app.AlertDialog");
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

    public static boolean instanceOfSupportRecyclerView(Object view) {
        return sHasSupportRecyclerView && view instanceof RecyclerView;
    }

    public static boolean instanceOfAndroidXRecyclerView(Object view) {
        return sHasAndroidXRecyclerView && view instanceof androidx.recyclerview.widget.RecyclerView;
    }

    public static boolean instanceOfSupportViewPager(Object view) {
        return sHasSupportViewPager && view instanceof ViewPager;
    }

    public static boolean instanceOfAndroidXViewPager(Object view) {
        return sHasAndroidXViewPager && view instanceof androidx.viewpager.widget.ViewPager;
    }

    public static boolean instanceOfSupportSwipeRefreshLayout(Object view) {
        return sHasSupportSwipeRefreshLayoutView && view instanceof SwipeRefreshLayout;
    }

    public static boolean instanceofAndroidXSwipeRefreshLayout(Object view) {
        return sHasAndroidXSwipeRefreshLayoutView && view instanceof androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
    }

    public static boolean instanceOfX5WebView(Object view) {
        return sHasX5WebView && view instanceof com.tencent.smtt.sdk.WebView;
    }

    public static boolean instanceOfUcWebView(Object view) {
        return sHasUcWebView && view instanceof com.uc.webview.export.WebView;
    }

    public static boolean instanceOfSupportAlertDialog(Object dialog) {
        return sHasSupportAlertDialog && dialog instanceof android.support.v7.app.AlertDialog;
    }

    public static boolean instanceOfAndroidXAlertDialog(Object dialog) {
        return sHasAndroidXAlertDialog && dialog instanceof AlertDialog;
    }

    public static boolean instanceOfSupportFragmentActivity(Object activity) {
        return sHasSupportFragmentActivity && activity instanceof FragmentActivity;
    }

    public static boolean instanceOfAndroidXFragmentActivity(Object activity) {
        return sHasAndroidXFragmentActivity && activity instanceof androidx.fragment.app.FragmentActivity;
    }

    public static boolean instanceOfSupportFragment(Object fragment) {
        return sHasSupportFragment && fragment instanceof android.support.v4.app.Fragment;
    }

    public static boolean instanceOfAndroidXFragment(Object fragment) {
        return sHasAndroidXFragment && fragment instanceof Fragment;
    }

    public static boolean instanceOfSupportListMenuItemView(Object itemView) {
        return sHasSupportListMenuItemView && itemView instanceof ListMenuItemView;
    }

    public static boolean instanceOfAndroidXListMenuItemView(Object itemView) {
        return sHasAndroidXListMenuItemView && itemView instanceof androidx.appcompat.view.menu.ListMenuItemView;
    }

}
