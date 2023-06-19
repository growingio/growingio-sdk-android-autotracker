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

package com.growingio.android.sdk.track.events;

import java.util.Map;

public interface EventFilterInterceptor {

    /**
     * filter events based on eventType
     *
     * @param eventType event's type
     * @return true:pass false:block
     */
    boolean filterEventType(String eventType);

    /**
     * filter custom events based on eventName
     *
     * @param eventName custom event's name
     * @return true:pass false:block
     */
    boolean filterEventName(String eventName);

    /**
     * filter events based on path
     *
     * @param path event's path
     * @return true:pass false:block
     */
    boolean filterEventPath(String path);

    /**
     * filter event's field
     *
     * @param type      event's type
     * @param fieldArea event's field map
     * @return map. if the value is true,the field will pass,and if the value is false, sdk will make the field blank.
     */
    Map<String, Boolean> filterEventField(String type, Map<String, Boolean> fieldArea);
}
