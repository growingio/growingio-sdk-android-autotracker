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


import android.support.annotation.AnyThread;
import android.support.annotation.Nullable;

import com.growingio.android.sdk.track.interfaces.ResultCallback;

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

    /**
     * 打开或关闭数据采集
     * 如果关闭，从代码调用开始, 后续所有事件不再采集, 但是之前采集生成的历史数据仍会发送
     *
     * @param enabled true打开数据采集，false关闭数据采集
     */
    void setDataCollectionEnabled(boolean enabled);

    /**
     * 异步获取deviceId
     *
     * @param callback 对应回调,  回调线程不确定
     */
    void getDeviceId(@Nullable ResultCallback<String> callback);
}
