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

package com.growingio.android.sdk.track.models;

import androidx.annotation.AnyThread;

import com.growingio.android.sdk.track.events.EventType;
import com.growingio.android.sdk.track.utils.GIOProviders;

/**
 * 此类放在models中的原因是希望支持多个协议,
 * 不同的协议间尚未统一url规则, 为每个协议提供自己单独的UrlProvider
 */
public interface EventUrlProvider {

    /**
     * 获取对应类型事件的上传地址
     *
     * @param type 对应的服务端类型
     */
    @AnyThread
    String getUrl(String projectId, EventType type);

    class EventUrlPolicy implements EventUrlProvider {

        public static EventUrlProvider get() {
            return GIOProviders.provider(EventUrlProvider.class, new GIOProviders.DefaultCallback<EventUrlProvider>() {
                @Override
                public EventUrlProvider value() {
                    return new EventUrlPolicy();
                }
            });
        }

        @Override
        public String getUrl(String projectId, EventType type) {
            return "https://api.growingio.com/v3/" + projectId + "/android/" + type + "?stm=" + System.currentTimeMillis();
        }
    }
}
