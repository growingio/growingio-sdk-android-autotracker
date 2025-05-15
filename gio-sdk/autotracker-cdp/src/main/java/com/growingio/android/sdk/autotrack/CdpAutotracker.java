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
package com.growingio.android.sdk.autotrack;

import android.app.Activity;
import android.content.Context;

import com.growingio.android.sdk.autotrack.page.PageProvider;
import com.growingio.android.sdk.autotrack.page.SuperFragment;
import com.growingio.android.sdk.track.TrackMainThread;
import com.growingio.android.sdk.track.log.Logger;
import com.growingio.android.sdk.track.providers.TrackerLifecycleProvider;

import java.util.Map;

public class CdpAutotracker extends Autotracker {
    private static final String TAG = "GrowingAutotracker";

    public CdpAutotracker(Context context) {
        super(context);
    }

    public void ignorePage(Activity activity) {
        if (!isInited || activity == null) {
            Logger.e(TAG, "sdk not init or activity is NULL");
            return;
        }
        TrackMainThread.trackMain().runOnUiThread(() -> {
            Logger.d(TAG, "ignorePage: " + activity.getClass().getSimpleName());
            PageProvider.get().ignoreActivity(activity, true);
        });
    }


    /**
     * android.app.Fragment is deprecated
     */
    @Deprecated
    public void ignoreSystemPage(final android.app.Fragment fragment) {
        if (!isInited || fragment == null) {
            Logger.e(TAG, "sdk not init or fragment is NULL");
            return;
        }
        Logger.d(TAG, "ignorePage: " + fragment.getClass().getSimpleName());
        TrackMainThread.trackMain().runOnUiThread(() -> PageProvider.get().ignoreFragment(SuperFragment.make(fragment), true));
    }

    /**
     * android.support.v4.app.Fragment is deprecated
     */
    @Deprecated
    public void ignoreSupportPage(final android.support.v4.app.Fragment fragment) {
        if (!isInited || fragment == null) {
            Logger.e(TAG, "sdk not init or fragment is NULL");
            return;
        }
        Logger.d(TAG, "ignorePage: " + fragment.getClass().getSimpleName());
        TrackMainThread.trackMain().runOnUiThread(() -> PageProvider.get().ignoreFragment(SuperFragment.makeSupport(fragment), true));
    }

    public void ignorePage(final androidx.fragment.app.Fragment fragment) {
        if (!isInited || fragment == null) {
            Logger.e(TAG, "sdk not init or fragment is NULL");
            return;
        }
        Logger.d(TAG, "ignorePage: " + fragment.getClass().getSimpleName());
        TrackMainThread.trackMain().runOnUiThread(() -> PageProvider.get().ignoreFragment(SuperFragment.makeX(fragment), true));
    }

    @Override
    protected Map<Class<? extends TrackerLifecycleProvider>, TrackerLifecycleProvider> extraProviders() {
        Map<Class<? extends TrackerLifecycleProvider>, TrackerLifecycleProvider> providerMap = super.extraProviders();
        providerMap.put(CdpDowngradeProvider.class, new CdpDowngradeProvider());
        return providerMap;
    }
}
