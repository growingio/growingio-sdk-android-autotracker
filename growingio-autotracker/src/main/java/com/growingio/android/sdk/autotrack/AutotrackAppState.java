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

import com.growingio.android.sdk.autotrack.providers.ActivityImpPolicy;
import com.growingio.android.sdk.track.interfaces.OnGIOMainInitSDK;
import com.growingio.android.sdk.track.providers.ActivityStateProvider;

public class AutotrackAppState implements OnGIOMainInitSDK {
    private volatile static ImpObserver sImpObserver;

    public static ImpObserver impObserver() {
        return sImpObserver == null ? new ImpObserver() : sImpObserver;
    }

    @Override
    public void onGIOMainInitSDK() {
        ActivityStateProvider.ActivityStatePolicy.get().registerActivityLifecycleListener(new ActivityImpPolicy());
        sImpObserver = new ImpObserver();
        if (sImpObserver == null) {
        }
    }
}
