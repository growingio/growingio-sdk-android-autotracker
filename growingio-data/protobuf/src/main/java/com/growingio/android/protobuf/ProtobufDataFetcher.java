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

package com.growingio.android.protobuf;


import com.growingio.android.sdk.track.log.Logger;
import com.growingio.android.sdk.track.middleware.GEvent;
import com.growingio.android.sdk.track.middleware.format.EventByteArray;
import com.growingio.android.sdk.track.middleware.format.EventFormatData;
import com.growingio.android.sdk.track.middleware.format.FormatDataFetcher;

import java.util.List;

/**
 * <p>
 *
 * @author cpacm 2021/5/13
 */
public class ProtobufDataFetcher implements FormatDataFetcher<EventByteArray> {
    private static final String TAG = "ProtobufDataFetcher";

    private final EventFormatData eventData;

    public ProtobufDataFetcher(EventFormatData eventData) {
        this.eventData = eventData;
    }

    @Override
    public EventByteArray executeData() {
        try {
            if (eventData.getEventOp() == EventFormatData.DATA_FORMAT_EVENT) {
                assertCondition(eventData.getEvent() != null, "leak necessary event");
                return format(eventData.getEvent());
            } else if (eventData.getEventOp() == EventFormatData.DATA_FORMAT_MERGE) {
                assertCondition(eventData.getEvents() != null, "leak necessary events");
                return merge(eventData.getEvents());
            }
            return new EventByteArray(null);
        } catch (IllegalArgumentException e) {
            Logger.e(TAG, e);
            return new EventByteArray(null);
        }
    }

    @Override
    public EventByteArray format(GEvent gEvent) {
        byte[] data = EventProtocolTransfer.protocolByte(gEvent);
        return new EventByteArray(data, "application/protobuf");
    }

    @Override
    public EventByteArray merge(List<byte[]> events) {
        byte[] data = marshall(events);
        return new EventByteArray(data, "application/protobuf");
    }

    private byte[] marshall(List<byte[]> events) {
        if (events == null || events.isEmpty()) {
            return new byte[]{};
        }
        EventV3Protocol.EventV3List.Builder listBuilder = EventV3Protocol.EventV3List.newBuilder();
        for (byte[] data : events) {
            EventV3Protocol.EventV3Dto event = EventProtocolTransfer.covertToProtobuf(data);
            if (event != null) listBuilder.addValues(event);
        }
        return listBuilder.build().toByteArray();
    }

    private void assertCondition(boolean condition, String msg) throws IllegalArgumentException {
        if (!condition) throw new IllegalArgumentException(msg);
    }

    @Override
    public Class<EventByteArray> getDataClass() {
        return EventByteArray.class;
    }

}
