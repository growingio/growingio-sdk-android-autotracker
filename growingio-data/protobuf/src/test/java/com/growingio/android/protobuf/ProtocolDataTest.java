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
package com.growingio.android.protobuf;



import com.google.common.truth.Truth;
import com.google.protobuf.InvalidProtocolBufferException;
import com.growingio.android.sdk.track.events.CustomEvent;
import com.growingio.android.sdk.track.middleware.format.EventByteArray;
import com.growingio.android.sdk.track.middleware.format.EventFormatData;
import com.growingio.android.sdk.track.modelloader.DataFetcher;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.ArrayList;

@Config(manifest = Config.NONE)
@RunWith(RobolectricTestRunner.class)
public class ProtocolDataTest {
    @Test
    public void dataFormat() {
        CustomEvent customEvent = new CustomEvent.Builder()
                .setEventName("jsonTest")
                .build();

        EventFormatData eventData = EventFormatData.format(customEvent);
        DataFetcher<EventByteArray> dataFetcher = new ProtobufDataFetcher(eventData);
        EventByteArray data = dataFetcher.executeData();
        try {
            EventV3Protocol.EventV3Dto event = EventV3Protocol.EventV3Dto.parseFrom(data.getBodyData());
            String eventName = event.getEventName();
            Truth.assertThat(event.getEventType()).isEqualTo(EventV3Protocol.EventType.CUSTOM);
            Truth.assertThat(eventName).isEqualTo("jsonTest");
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
            Truth.assertThat(true).isFalse();
        }
    }

    @Test
    public void dataMerge() {
        ArrayList<byte[]> arrayList = new ArrayList<>();
        CustomEvent customEvent = new CustomEvent.Builder()
                .setEventName("merge")
                .build();
        CustomEvent customEvent2 = new CustomEvent.Builder()
                .setEventName("cpacm")
                .build();
        arrayList.add(EventProtocolTransfer.protocolByte(customEvent));
        arrayList.add(EventProtocolTransfer.protocolByte(customEvent2));

        EventFormatData eventData = EventFormatData.merge(arrayList);
        DataFetcher<EventByteArray> dataFetcher = new ProtobufDataFetcher(eventData);
        Truth.assertThat(dataFetcher.getDataClass()).isAssignableTo(EventByteArray.class);
        EventByteArray data = dataFetcher.executeData();

        try {
            EventV3Protocol.EventV3List list = EventV3Protocol.EventV3List.parseFrom(data.getBodyData());
            Truth.assertThat(list.getValuesCount()).isEqualTo(2);
            EventV3Protocol.EventV3Dto e1 = list.getValues(0);
            EventV3Protocol.EventV3Dto e2 = list.getValues(1);
            Truth.assertThat(e1.getEventName()).isEqualTo("merge");
            Truth.assertThat(e2.getEventName()).isEqualTo("cpacm");
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
            Truth.assertThat(true).isFalse();
        }

    }

}
