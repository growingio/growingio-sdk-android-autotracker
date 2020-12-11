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

package com.growingio.android.sdk.track;


import android.app.Activity;
import android.content.Intent;
import android.support.annotation.AnyThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.Map;

/**
 * GrowingIO 对外提供的埋点接口
 */
@AnyThread
public interface IGrowingTracker {

    void trackCustomEvent(String eventName);

    void trackCustomEvent(String eventName, Map<String, String> attributes);

    void setLoginUserAttributes(Map<String, String> attributes);

    void setVisitorAttributes(Map<String, String> attributes);

    void setConversionVariables(Map<String, String> variables);

    void setLoginUserId(String userId);

    void cleanLoginUserId();

    void setLocation(double latitude, double longitude);

    void cleanLocation();

    void setDataCollectionEnabled(boolean enabled);

    @Nullable
    String getDeviceId();

    void onActivityNewIntent(@NonNull Activity activity, Intent intent);
}
