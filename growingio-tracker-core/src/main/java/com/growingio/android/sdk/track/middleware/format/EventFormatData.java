/*
 * Copyright (C) 2023 Beijing Yishu Technology Co., Ltd.
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
package com.growingio.android.sdk.track.middleware.format;


import com.growingio.android.sdk.track.middleware.GEvent;

import java.util.List;

/**
 * <p>
 *
 * @author cpacm 5/13/21
 */
public class EventFormatData {

    public static final int DATA_FORMAT_EVENT = 0;
    public static final int DATA_FORMAT_MERGE = 1;

    private int eventOp;
    private GEvent event;
    private List<byte[]> events;
    // 与 events 一一对应的强制登录待替换标志；为 null 时视为全部不替换
    private List<Boolean> forceLoginPending;
    private String pendingDeviceIdReplacement;

    public GEvent getEvent() {
        return event;
    }

    public int getEventOp() {
        return eventOp;
    }

    public List<byte[]> getEvents() {
        return events;
    }

    /**
     * 第 index 个事件是否需要在组包时替换 deviceId
     */
    public boolean isForceLoginPending(int index) {
        if (pendingDeviceIdReplacement == null || pendingDeviceIdReplacement.isEmpty()) {
            return false;
        }
        return forceLoginPending != null && index < forceLoginPending.size()
                && Boolean.TRUE.equals(forceLoginPending.get(index));
    }

    public String getPendingDeviceIdReplacement() {
        return pendingDeviceIdReplacement;
    }

    public static EventFormatData format(GEvent event) {
        EventFormatData fd = new EventFormatData();
        fd.event = event;
        fd.eventOp = DATA_FORMAT_EVENT;
        return fd;
    }

    public static EventFormatData merge(List<byte[]> events) {
        return merge(events, null, "");
    }

    /**
     * @param forceLoginPending          与 events 一一对应的强制登录待替换标志
     * @param pendingDeviceIdReplacement 待替换事件组包时使用的 deviceId，空字符串表示不替换
     */
    public static EventFormatData merge(List<byte[]> events, List<Boolean> forceLoginPending, String pendingDeviceIdReplacement) {
        EventFormatData fd = new EventFormatData();
        fd.events = events;
        fd.forceLoginPending = forceLoginPending;
        fd.pendingDeviceIdReplacement = pendingDeviceIdReplacement;
        fd.eventOp = DATA_FORMAT_MERGE;
        return fd;
    }
}
