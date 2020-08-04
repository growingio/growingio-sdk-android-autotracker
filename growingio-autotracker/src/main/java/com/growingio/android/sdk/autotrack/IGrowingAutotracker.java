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
import android.support.v4.app.Fragment;
import android.view.View;

import com.growingio.android.sdk.autotrack.IgnorePolicy;
import com.growingio.android.sdk.track.interfaces.IGrowingTracker;

import java.util.Map;

/**
 * GrowingIO对外无埋点部分接口
 */
public interface IGrowingAutotracker extends IGrowingTracker {

    IGrowingTracker setUniqueTag(View view, String tag);

    IGrowingAutotracker setPageAttributes(Activity activity, Map<String, String> attributes);

    IGrowingTracker trackViewImpression(ImpressionConfig config);

    IGrowingTracker stopTrackViewImpression(View trackedView);

    IGrowingTracker setPageAlias(String alias);

    IGrowingTracker ignorePage(Activity activity, IgnorePolicy policy);

    IGrowingTracker ignorePage(Fragment fragment, IgnorePolicy policy);

    IGrowingTracker ignoreView(View view, IgnorePolicy policy);

}
