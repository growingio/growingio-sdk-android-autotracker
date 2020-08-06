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

package com.growingio.android.sdk.track.interfaces;


import android.support.annotation.AnyThread;
import android.support.annotation.Nullable;

import java.util.Map;

/**
 * GrowingIO 对外提供的埋点接口
 */
@AnyThread
public interface IGrowingTracker {

    /**
     * 埋点事件， 产生一条自定义事件
     *
     * @param eventName  对应事件名
     * @param attributes 对应事件的var, 支持Map与JSON两种格式
     * @return this
     */
    IGrowingTracker trackCustomEvent(String eventName, Map<String, String> attributes);

    IGrowingTracker setConversionVariables(Map<String, String> variables);

    IGrowingTracker setLoginUserAttributes(Map<String, String> attributes);

    IGrowingTracker setVisitorAttributes(Map<String, String> attributes);

    /**
     * 异步获取deviceId
     *
     * @param callback 对应回调,  回调线程为任意线程
     * @return this
     */
    IGrowingTracker getDeviceId(@Nullable ResultCallback<String> callback);

    /**
     * 禁止数据采集, 但不会禁止SDK相应功能
     * 从代码调用开始, 后续所有事件不再采集, 但是之前采集生成的历史数据仍会发送
     */
    IGrowingTracker setDataCollectionEnabled(boolean enabled);

    IGrowingTracker setLoginUserId(String userId);

    IGrowingTracker cleanLoginUserId();

    IGrowingTracker setLocation(double latitude, double longitude);

    IGrowingTracker cleanLocation();
}
