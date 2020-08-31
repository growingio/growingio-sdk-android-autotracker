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

package com.growingio.android.sdk.autotrack.page;

import android.app.Fragment;
import android.app.ListFragment;
import android.preference.PreferenceFragment;
import android.webkit.WebViewFragment;

import com.growingio.android.sdk.track.log.Logger;
import com.growingio.sdk.inject.annotation.AfterSuper;

public class FragmentInjector {
    private static final String TAG = "FragmentInjector";

    private FragmentInjector() {
    }

    @AfterSuper(clazz = Fragment.class, method = "onResume")
    public static void systemFragmentOnResume(Fragment fragment) {
        Logger.d(TAG, "systemFragmentOnResume: fragment = " + fragment.getClass().getName());
        PageProvider.get().createOrResumePage(SuperFragment.make(fragment));
    }

    @AfterSuper(clazz = Fragment.class, method = "setUserVisibleHint", parameterTypes = {boolean.class})
    public static void systemFragmentSetUserVisibleHint(Fragment fragment, boolean isVisibleToUser) {
        Logger.d(TAG, "systemFragmentSetUserVisibleHint: fragment = " + fragment.getClass().getName() + ", isVisibleToUser = " + isVisibleToUser);
        if (isVisibleToUser) {
            PageProvider.get().createOrResumePage(SuperFragment.make(fragment));
        }
    }

    @AfterSuper(clazz = Fragment.class, method = "onHiddenChanged", parameterTypes = {boolean.class})
    public static void systemFragmentOnHiddenChanged(Fragment fragment, boolean hidden) {
        Logger.d(TAG, "systemFragmentOnHiddenChanged: fragment = " + fragment.getClass().getName() + ", hidden" + hidden);
        if (!hidden) {
            PageProvider.get().createOrResumePage(SuperFragment.make(fragment));
        }
    }

    @AfterSuper(clazz = Fragment.class, method = "onDestroyView")
    public static void systemFragmentOnDestroyView(Fragment fragment) {
        Logger.d(TAG, "systemFragmentOnDestroyView: fragment = " + fragment.getClass().getName());
        PageProvider.get().removePage(SuperFragment.make(fragment));
    }

    @AfterSuper(clazz = WebViewFragment.class, method = "onResume")
    public static void webViewFragmentOnResume(WebViewFragment fragment) {
        Logger.d(TAG, "webViewFragmentOnResume: fragment = " + fragment.getClass().getName());
        PageProvider.get().createOrResumePage(SuperFragment.make(fragment));
    }

    @AfterSuper(clazz = WebViewFragment.class, method = "setUserVisibleHint", parameterTypes = {boolean.class})
    public static void webViewFragmentSetUserVisibleHint(WebViewFragment fragment, boolean isVisibleToUser) {
        Logger.d(TAG, "webViewFragmentSetUserVisibleHint: fragment = " + fragment.getClass().getName() + ", isVisibleToUser = " + isVisibleToUser);
        if (isVisibleToUser) {
            PageProvider.get().createOrResumePage(SuperFragment.make(fragment));
        }
    }

    @AfterSuper(clazz = WebViewFragment.class, method = "onHiddenChanged", parameterTypes = {boolean.class})
    public static void webViewFragmentOnHiddenChanged(WebViewFragment fragment, boolean hidden) {
        Logger.d(TAG, "webViewFragmentOnHiddenChanged: fragment = " + fragment.getClass().getName() + ", hidden" + hidden);
        if (!hidden) {
            PageProvider.get().createOrResumePage(SuperFragment.make(fragment));
        }
    }

    @AfterSuper(clazz = WebViewFragment.class, method = "onDestroyView")
    public static void webViewFragmentOnDestroyView(WebViewFragment fragment) {
        Logger.d(TAG, "webViewFragmentOnDestroyView: fragment = " + fragment.getClass().getName());
        PageProvider.get().removePage(SuperFragment.make(fragment));
    }

    @AfterSuper(clazz = PreferenceFragment.class, method = "onResume")
    public static void preferenceFragmentOnResume(PreferenceFragment fragment) {
        Logger.d(TAG, "preferenceFragmentOnResume: fragment = " + fragment.getClass().getName());
        PageProvider.get().createOrResumePage(SuperFragment.make(fragment));
    }

    @AfterSuper(clazz = PreferenceFragment.class, method = "setUserVisibleHint", parameterTypes = {boolean.class})
    public static void preferenceFragmentSetUserVisibleHint(PreferenceFragment fragment, boolean isVisibleToUser) {
        Logger.d(TAG, "preferenceFragmentSetUserVisibleHint: fragment = " + fragment.getClass().getName() + ", isVisibleToUser = " + isVisibleToUser);
        if (isVisibleToUser) {
            PageProvider.get().createOrResumePage(SuperFragment.make(fragment));
        }
    }

    @AfterSuper(clazz = PreferenceFragment.class, method = "onHiddenChanged", parameterTypes = {boolean.class})
    public static void preferenceFragmentOnHiddenChanged(PreferenceFragment fragment, boolean hidden) {
        Logger.d(TAG, "preferenceFragmentOnHiddenChanged: fragment = " + fragment.getClass().getName() + ", hidden" + hidden);
        if (!hidden) {
            PageProvider.get().createOrResumePage(SuperFragment.make(fragment));
        }
    }

    @AfterSuper(clazz = PreferenceFragment.class, method = "onDestroyView")
    public static void preferenceFragmentOnDestroyView(PreferenceFragment fragment) {
        Logger.d(TAG, "preferenceFragmentOnDestroyView: fragment = " + fragment.getClass().getName());
        PageProvider.get().removePage(SuperFragment.make(fragment));
    }

    @AfterSuper(clazz = ListFragment.class, method = "onResume")
    public static void listFragmentOnResume(ListFragment fragment) {
        Logger.d(TAG, "listFragmentOnResume: fragment = " + fragment.getClass().getName());
        PageProvider.get().createOrResumePage(SuperFragment.make(fragment));
    }

    @AfterSuper(clazz = ListFragment.class, method = "setUserVisibleHint", parameterTypes = {boolean.class})
    public static void listFragmentSetUserVisibleHint(ListFragment fragment, boolean isVisibleToUser) {
        Logger.d(TAG, "listFragmentSetUserVisibleHint: fragment = " + fragment.getClass().getName() + ", isVisibleToUser = " + isVisibleToUser);
        if (isVisibleToUser) {
            PageProvider.get().createOrResumePage(SuperFragment.make(fragment));
        }
    }

    @AfterSuper(clazz = ListFragment.class, method = "onHiddenChanged", parameterTypes = {boolean.class})
    public static void listFragmentOnHiddenChanged(ListFragment fragment, boolean hidden) {
        Logger.d(TAG, "listFragmentOnHiddenChanged: fragment = " + fragment.getClass().getName() + ", hidden" + hidden);
        if (!hidden) {
            PageProvider.get().createOrResumePage(SuperFragment.make(fragment));
        }
    }

    @AfterSuper(clazz = ListFragment.class, method = "onDestroyView")
    public static void listFragmentOnDestroyView(ListFragment fragment) {
        Logger.d(TAG, "listFragmentOnDestroyView: fragment = " + fragment.getClass().getName());
        PageProvider.get().removePage(SuperFragment.make(fragment));
    }

    @AfterSuper(clazz = android.support.v4.app.Fragment.class, method = "onResume")
    public static void v4FragmentOnResume(android.support.v4.app.Fragment fragment) {
        Logger.d(TAG, "v4FragmentOnResume: fragment = " + fragment.getClass().getName());
        PageProvider.get().createOrResumePage(SuperFragment.make(fragment));
    }

    @AfterSuper(clazz = android.support.v4.app.Fragment.class, method = "setUserVisibleHint", parameterTypes = {boolean.class})
    public static void v4FragmentSetUserVisibleHint(android.support.v4.app.Fragment fragment, boolean isVisibleToUser) {
        Logger.d(TAG, "v4FragmentSetUserVisibleHint: fragment = " + fragment.getClass().getName() + ", isVisibleToUser = " + isVisibleToUser);
        if (isVisibleToUser) {
            PageProvider.get().createOrResumePage(SuperFragment.make(fragment));
        }
    }

    @AfterSuper(clazz = android.support.v4.app.Fragment.class, method = "onHiddenChanged", parameterTypes = {boolean.class})
    public static void v4FragmentOnHiddenChanged(android.support.v4.app.Fragment fragment, boolean hidden) {
        Logger.d(TAG, "v4FragmentOnHiddenChanged: fragment = " + fragment.getClass().getName() + ", hidden" + hidden);
        if (!hidden) {
            PageProvider.get().createOrResumePage(SuperFragment.make(fragment));
        }
    }

    @AfterSuper(clazz = android.support.v4.app.Fragment.class, method = "onDestroyView")
    public static void v4FragmentOnDestroyView(android.support.v4.app.Fragment fragment) {
        Logger.d(TAG, "v4FragmentOnDestroyView: fragment = " + fragment.getClass().getName());
        PageProvider.get().removePage(SuperFragment.make(fragment));
    }

    @AfterSuper(clazz = androidx.fragment.app.Fragment.class, method = "onResume")
    public static void androidxFragmentOnResume(androidx.fragment.app.Fragment fragment) {
        Logger.d(TAG, "androidxFragmentOnResume: fragment = " + fragment.getClass().getName());
        PageProvider.get().createOrResumePage(SuperFragment.make(fragment));
    }

    @AfterSuper(clazz = androidx.fragment.app.Fragment.class, method = "setUserVisibleHint", parameterTypes = {boolean.class})
    public static void androidxFragmentSetUserVisibleHint(androidx.fragment.app.Fragment fragment, boolean isVisibleToUser) {
        Logger.d(TAG, "androidxFragmentSetUserVisibleHint: fragment = " + fragment.getClass().getName() + ", isVisibleToUser = " + isVisibleToUser);
        if (isVisibleToUser) {
            PageProvider.get().createOrResumePage(SuperFragment.make(fragment));
        }
    }

    @AfterSuper(clazz = androidx.fragment.app.Fragment.class, method = "onHiddenChanged", parameterTypes = {boolean.class})
    public static void androidxFragmentOnHiddenChanged(androidx.fragment.app.Fragment fragment, boolean hidden) {
        Logger.d(TAG, "androidxFragmentOnHiddenChanged: fragment = " + fragment.getClass().getName() + ", hidden" + hidden);
        if (!hidden) {
            PageProvider.get().createOrResumePage(SuperFragment.make(fragment));
        }
    }

    @AfterSuper(clazz = androidx.fragment.app.Fragment.class, method = "onDestroyView")
    public static void androidxFragmentOnDestroyView(androidx.fragment.app.Fragment fragment) {
        Logger.d(TAG, "androidxFragmentOnDestroyView: fragment = " + fragment.getClass().getName());
        PageProvider.get().removePage(SuperFragment.make(fragment));
    }

}
