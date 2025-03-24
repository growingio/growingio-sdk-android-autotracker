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
package com.growingio.android.sdk.autotrack.inject;

import com.growingio.android.sdk.autotrack.page.PageProvider;
import com.growingio.android.sdk.autotrack.page.SuperFragment;
import com.growingio.android.sdk.track.log.Logger;

public class FragmentInjector {
    private static final String TAG = "FragmentInjector";

    private FragmentInjector() {
    }

    public static void systemFragmentOnResume(android.app.Fragment fragment) {
        Logger.d(TAG, "systemFragmentOnResume: fragment = " + fragment.getClass().getName());
        PageProvider.get().createOrResumePage(SuperFragment.make(fragment));
    }

    public static void systemFragmentOnStop(android.app.Fragment fragment) {
        Logger.d(TAG, "systemFragmentOnStop: fragment = " + fragment.getClass().getName());
    }

    public static void systemFragmentSetUserVisibleHint(android.app.Fragment fragment, boolean isVisibleToUser) {
        Logger.d(TAG, "systemFragmentSetUserVisibleHint: fragment = " + fragment.getClass().getName() + ", isVisibleToUser = " + isVisibleToUser);
        if (isVisibleToUser) {
            PageProvider.get().createOrResumePage(SuperFragment.make(fragment));
        } else {
            PageProvider.get().fragmentOnHiddenChanged(SuperFragment.make(fragment), true);
        }
    }

    public static void systemFragmentOnHiddenChanged(android.app.Fragment fragment, boolean hidden) {
        Logger.d(TAG, "systemFragmentOnHiddenChanged: fragment = " + fragment.getClass().getName() + ", hidden" + hidden);
        PageProvider.get().fragmentOnHiddenChanged(SuperFragment.make(fragment), hidden);
    }

    public static void systemFragmentOnDestroyView(android.app.Fragment fragment) {
        Logger.d(TAG, "systemFragmentOnDestroyView: fragment = " + fragment.getClass().getName());
        PageProvider.get().removePage(SuperFragment.make(fragment));
    }

    public static void androidxFragmentOnResume(androidx.fragment.app.Fragment fragment) {
        Logger.d(TAG, "androidxFragmentOnResume: fragment = " + fragment.getClass().getName());
        PageProvider.get().createOrResumePage(SuperFragment.makeX(fragment));
    }

    public static void androidxFragmentOnStop(androidx.fragment.app.Fragment fragment) {
        Logger.d(TAG, "androidxFragmentOnStop: fragment = " + fragment.getClass().getName());
    }

    /**
     * 新版本的AndroidX Fragment setUserVisibleHint 将通过 FragmentTransaction setMaxLifecycle 来控制生命周期实现
     */
    @Deprecated
    public static void androidxFragmentSetUserVisibleHint(androidx.fragment.app.Fragment fragment, boolean isVisibleToUser) {
        Logger.d(TAG, "androidxFragmentSetUserVisibleHint: fragment = " + fragment.getClass().getName() + ", isVisibleToUser = " + isVisibleToUser);
        if (isVisibleToUser) {
            PageProvider.get().createOrResumePage(SuperFragment.makeX(fragment));
        } else {
            PageProvider.get().fragmentOnHiddenChanged(SuperFragment.makeX(fragment), true);
        }
    }

    public static void androidxFragmentOnHiddenChanged(androidx.fragment.app.Fragment fragment, boolean hidden) {
        Logger.d(TAG, "androidxFragmentOnHiddenChanged: fragment = " + fragment.getClass().getName() + ", hidden" + hidden);
        PageProvider.get().fragmentOnHiddenChanged(SuperFragment.makeX(fragment), hidden);
    }

    public static void androidxFragmentOnDestroyView(androidx.fragment.app.Fragment fragment) {
        Logger.d(TAG, "androidxFragmentOnDestroyView: fragment = " + fragment.getClass().getName());
        PageProvider.get().removePage(SuperFragment.makeX(fragment));
    }

}
