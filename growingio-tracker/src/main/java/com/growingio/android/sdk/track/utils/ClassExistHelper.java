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

import android.annotation.TargetApi;
import android.os.Build;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.view.menu.ListMenuItemView;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.tencent.smtt.sdk.WebChromeClient;
import com.tencent.smtt.sdk.WebView;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ClassExistHelper {
    private static final String TAG = "GIO.ClassExist";

    public static boolean sHasCustomRecyclerView = false;
    public static boolean sHasSupportViewPager;
    public static boolean sHasSupportListMenuItemView;
    public static boolean sHasAndroidXViewPager;
    public static boolean sHasAndroidXListMenuItemView;
    private static Class sCRVClass; // CustomRecyclerView
    private static Method sCRVGetChildAdapterPositionMethod;
    // Edited by Gradle plugin
    private static boolean sHasTransform;
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
    private static boolean sHasAdvertisingIdClient;
    private static boolean sHasMdidSdkHelper;

    static {
        if (!sHasTransform) {
            // Don't Edit This Directly, use Unit Test to generate this.
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
            sHasAdvertisingIdClient = hasClass("com.google.android.gms.ads.identifier.AdvertisingIdClient");
            sHasMdidSdkHelper = hasClass("com.bun.miitmdid.core.MdidSdkHelper");
        }
    }

    private ClassExistHelper() {
    }

    private static boolean hasClass(String className) {
        try {
            Class.forName(className);
            return true;
        } catch (Throwable e) {
            return false;
        }
    }

    // 仅仅用于保护
    public static int invokeCRVGetChildAdapterPositionMethod(View customRecyclerView, View childView) {
        try {
            if (customRecyclerView.getClass() == sCRVClass) {
                return (Integer) ClassExistHelper.sCRVGetChildAdapterPositionMethod.invoke(customRecyclerView, childView);
            }
        } catch (IllegalAccessException e) {
            LogUtil.d(e);
        } catch (InvocationTargetException ignored) {
            LogUtil.d(ignored);
        }
        return -1;
    }

    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    public static void checkCustomRecyclerView(Class<?> viewClass, String viewName) {
        if (sHasAndroidXRecyclerView || sHasSupportRecyclerView || sHasCustomRecyclerView) {
            return;
        }
        if (viewName != null && viewName.contains("RecyclerView")) {
            try {
                Class<?> rootCRVClass = findRecyclerInSuper(viewClass);
                if (rootCRVClass != null && sCRVGetChildAdapterPositionMethod != null) {
                    sCRVClass = viewClass;
                    sHasCustomRecyclerView = true;
                }
            } catch (Exception ignore) {
                LogUtil.d(ignore);
            }
        }
    }

    public static boolean issHasAdvertisingIdClient() {
        return sHasAdvertisingIdClient;
    }

    private static Class<?> findRecyclerInSuper(Class<?> viewClass) {
        while (viewClass != null && !viewClass.equals(ViewGroup.class)) {
            try {
                sCRVGetChildAdapterPositionMethod = viewClass.getDeclaredMethod("getChildAdapterPosition", View.class);
            } catch (NoSuchMethodException ignore) {
            }
            if (sCRVGetChildAdapterPositionMethod == null) {
                try {
                    sCRVGetChildAdapterPositionMethod = viewClass.getDeclaredMethod("getChildPosition", View.class);
                } catch (NoSuchMethodException ignore) {
                }
            }
            if (sCRVGetChildAdapterPositionMethod != null) {
                return viewClass;
            } else {
                viewClass = viewClass.getSuperclass();
            }
        }
        return null;
    }

    public static boolean isHasMdidSdkHelper() {
        return sHasMdidSdkHelper;
    }

    /**
     * 判断View是否是RecyclerView, 包括Support包与AndroidX， 以及部分自定义的RecyclerView
     */
    public static boolean instanceOfRecyclerView(Object view) {
        return instanceOfAndroidXRecyclerView(view)
                || instanceOfSupportRecyclerView(view)
                || (sHasCustomRecyclerView && view != null && sCRVClass.isAssignableFrom(view.getClass()));
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
        return sHasX5WebView && view instanceof WebView;
    }

    public static boolean instanceOfUcWebView(Object view) {
        return sHasUcWebView && view instanceof com.uc.webview.export.WebView;
    }

    public static boolean instanceOfX5ChromeClient(Object client) {
        return sHasX5WebView && client instanceof WebChromeClient;
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

    public static void dumpClassInfo() {
        String info = "For Support Class: \n"
                + "sHasSupportRecyclerView=" + sHasSupportRecyclerView + ", sHasSupportFragmentActivity=" + sHasSupportFragmentActivity
                + "\nsHasSupportFragment=" + sHasSupportFragment + ", sHasSupportAlertDialog=" + sHasSupportAlertDialog
                + "\nsHasSupportSwipeRefreshLayoutView=" + sHasSupportSwipeRefreshLayoutView + ", sHasSupportViewPager=" + sHasSupportViewPager
                + "\nsHasSupportListMenuItemView=" + sHasSupportListMenuItemView
                + "\nFor AndroidX Class: \n"
                + "sHasAndroidXRecyclerView=" + sHasAndroidXRecyclerView + ", sHasAndroidXFragmentActivity=" + sHasAndroidXFragmentActivity
                + "\nsHasAndroidXFragment=" + sHasAndroidXFragment + ", sHasAndroidXAlertDialog=" + sHasAndroidXAlertDialog
                + "\nsHasAndroidXSwipeRefreshLayoutView=" + sHasAndroidXSwipeRefreshLayoutView + ", sHasAndroidXViewPager=" + sHasAndroidXViewPager
                + "\nsHasAndroidXListMenuItemView=" + sHasAndroidXListMenuItemView
                + "\nsHasUcWebView=" + sHasUcWebView
                + "\nAnd sHasTransform=" + sHasTransform;
        LogUtil.d("GIO.ClassExist", info);
    }


}
