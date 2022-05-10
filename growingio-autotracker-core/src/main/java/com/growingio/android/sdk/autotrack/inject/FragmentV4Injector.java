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

package com.growingio.android.sdk.autotrack.inject;

import com.growingio.android.sdk.autotrack.page.PageProvider;
import com.growingio.android.sdk.autotrack.page.SuperFragment;
import com.growingio.android.sdk.track.log.Logger;

public class FragmentV4Injector {
    private static final String TAG = "FragmentV4Injector";

    private FragmentV4Injector() {
    }

    public static void v4FragmentOnResume(android.support.v4.app.Fragment fragment) {
        Logger.d(TAG, "v4FragmentOnResume: fragment = " + fragment.getClass().getName());
        PageProvider.get().createOrResumePage(SuperFragment.makeSupport(fragment));
    }

    public static void v4FragmentSetUserVisibleHint(android.support.v4.app.Fragment fragment, boolean isVisibleToUser) {
        Logger.d(TAG, "v4FragmentSetUserVisibleHint: fragment = " + fragment.getClass().getName() + ", isVisibleToUser = " + isVisibleToUser);
        if (isVisibleToUser) {
            PageProvider.get().createOrResumePage(SuperFragment.makeSupport(fragment));
        }
    }

    public static void v4FragmentOnHiddenChanged(android.support.v4.app.Fragment fragment, boolean hidden) {
        Logger.d(TAG, "v4FragmentOnHiddenChanged: fragment = " + fragment.getClass().getName() + ", hidden = " + hidden);
        PageProvider.get().fragmentOnHiddenChanged(SuperFragment.makeSupport(fragment), hidden);
    }

    public static void v4FragmentOnDestroyView(android.support.v4.app.Fragment fragment) {
        Logger.d(TAG, "v4FragmentOnDestroyView: fragment = " + fragment.getClass().getName());
        PageProvider.get().removePage(SuperFragment.makeSupport(fragment));
    }
}
