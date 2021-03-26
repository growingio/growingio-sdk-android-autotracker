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

package com.growingio.autotest.autotracker;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.growingio.android.sdk.autotrack.GrowingAutotracker;
import com.growingio.android.sdk.track.GrowingTracker;
import com.growingio.autotest.help.BeforeAppOnCreate;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class UninitializedTest {

    @BeforeAppOnCreate
    public static void beforeAppOnCreate() {
        // TODO: android support 低版本无法和 androidx 同时依赖， 接口需要额外处理, 考虑通过dependency transform处理
        GrowingAutotracker.get().cleanLocation();
        GrowingAutotracker.get().setLocation(0, 0);
        GrowingAutotracker.get().cleanLoginUserId();
        GrowingAutotracker.get().setLoginUserId(null);
        GrowingAutotracker.get().getDeviceId();

        GrowingAutotracker.get().setConversionVariables(null);
        GrowingAutotracker.get().setLoginUserAttributes(null);
        GrowingAutotracker.get().setVisitorAttributes(null);

        GrowingAutotracker.get().setDataCollectionEnabled(true);
        GrowingAutotracker.get().setUniqueTag(null, null);

        GrowingAutotracker.get().trackViewImpression(null, null);
        GrowingAutotracker.get().trackViewImpression(null, null, null);
        GrowingAutotracker.get().stopTrackViewImpression(null);

        GrowingAutotracker.get().trackCustomEvent(null);

        GrowingTracker.get().cleanLocation();
        GrowingTracker.get().setLocation(0, 0);
        GrowingTracker.get().cleanLoginUserId();
        GrowingTracker.get().setLoginUserId(null);
        GrowingTracker.get().getDeviceId();

        GrowingTracker.get().setConversionVariables(null);
        GrowingTracker.get().setLoginUserAttributes(null);
        GrowingTracker.get().setVisitorAttributes(null);

        GrowingTracker.get().setDataCollectionEnabled(true);

        GrowingTracker.get().trackCustomEvent(null);
        GrowingTracker.get().trackCustomEvent(null, null);

        GrowingTracker.get().onActivityNewIntent(null, null);
    }

    @Test
    public void doNothing() {
    }
}
