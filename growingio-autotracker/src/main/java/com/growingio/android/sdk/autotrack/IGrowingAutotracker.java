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

package com.growingio.android.sdk.autotrack;

import android.app.Activity;
import android.view.View;

import com.growingio.android.sdk.track.IGrowingTracker;

import java.util.Map;

/**
 * GrowingIO对外无埋点部分接口
 */
public interface IGrowingAutotracker extends IGrowingTracker {

    IGrowingAutotracker setUniqueTag(View view, String tag);

    IGrowingAutotracker setPageAttributes(Activity activity, Map<String, String> attributes);

    IGrowingAutotracker setPageAttributes(android.app.Fragment fragment, Map<String, String> attributes);

    IGrowingAutotracker setPageAttributes(android.support.v4.app.Fragment fragment, Map<String, String> attributes);

    IGrowingAutotracker setPageAttributes(androidx.fragment.app.Fragment fragment, Map<String, String> attributes);

    IGrowingAutotracker trackViewImpression(View view, String impressionEventName);

    IGrowingAutotracker trackViewImpression(View view, String impressionEventName, Map<String, String> attributes);

    IGrowingAutotracker stopTrackViewImpression(View trackedView);

    IGrowingAutotracker setPageAlias(Activity activity, String alias);

    IGrowingAutotracker setPageAlias(android.app.Fragment fragment, String alias);

    IGrowingAutotracker setPageAlias(android.support.v4.app.Fragment fragment, String alias);

    IGrowingAutotracker setPageAlias(androidx.fragment.app.Fragment fragment, String alias);

    IGrowingAutotracker ignorePage(Activity activity, IgnorePolicy policy);

    IGrowingAutotracker ignorePage(android.app.Fragment fragment, IgnorePolicy policy);

    IGrowingAutotracker ignorePage(android.support.v4.app.Fragment fragment, IgnorePolicy policy);

    IGrowingAutotracker ignorePage(androidx.fragment.app.Fragment fragment, IgnorePolicy policy);

    IGrowingAutotracker ignoreView(View view, IgnorePolicy policy);

}
