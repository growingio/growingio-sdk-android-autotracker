/*
 *   Copyright (C) 2020 Beijing Yishu Technology Co., Ltd.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
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

    public GEvent getEvent() {
        return event;
    }

    public int getEventOp() {
        return eventOp;
    }

    public List<byte[]> getEvents() {
        return events;
    }

    public static EventFormatData format(GEvent event) {
        EventFormatData fd = new EventFormatData();
        fd.event = event;
        fd.eventOp = DATA_FORMAT_EVENT;
        return fd;
    }

    public static EventFormatData merge(List<byte[]> events) {
        EventFormatData fd = new EventFormatData();
        fd.events = events;
        fd.eventOp = DATA_FORMAT_MERGE;
        return fd;
    }
}
