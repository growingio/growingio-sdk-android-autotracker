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

package com.growingio.android.analytics.firebase;

import android.os.Bundle;

/**
 * <p>
 * Google Analytics message sender
 *
 * @author cpacm 2022/5/19
 */
public class FirebaseAnalyticsInjector {

    public static void logEvent(String name, Bundle bundle) {
        FirebaseAnalyticsAdapter.get().logEvent(name, bundle);
    }

    public static void setDefaultEventParameters(Bundle bundle) {
        FirebaseAnalyticsAdapter.get().setDefaultEventParameters(bundle);
    }

    public static void setUserId(String userId) {
        FirebaseAnalyticsAdapter.get().setUserId(userId);
    }

    public static void setUserProperty(String name, String value) {
        FirebaseAnalyticsAdapter.get().setUserProperty(name, value);
    }

    public static void setAnalyticsCollectionEnabled(boolean enabled) {
        FirebaseAnalyticsAdapter.get().setAnalyticsCollectionEnabled(enabled);
    }
}
