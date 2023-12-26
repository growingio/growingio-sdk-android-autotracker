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
package com.growingio.android.encoder;



import com.google.common.truth.Truth;
import com.growingio.android.sdk.track.middleware.http.EventEncoder;
import com.growingio.android.sdk.track.middleware.http.EventUrl;
import com.growingio.android.sdk.track.modelloader.DataFetcher;
import com.growingio.android.snappy.Snappy;
import com.growingio.android.snappy.XORUtils;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

@Config(manifest = Config.NONE)
@RunWith(RobolectricTestRunner.class)
public class EncoderTest {
    @Test
    public void encoder() {

        EventUrl eventUrl = new EventUrl("https://localhost", 10000L)
                .addPath("v3")
                .addPath("projects")
                .addPath("testId")
                .addPath("collect")
                .setBodyData("cpacm".getBytes())
                .addParam("stm", String.valueOf(10000L));

        EventEncoder encoder = new EventEncoder(eventUrl);
        DataFetcher<EventEncoder> dataFetcher = new EncoderDataFetcher(encoder);
        Truth.assertThat(dataFetcher.getDataClass()).isAssignableTo(EventEncoder.class);
        EventEncoder data = dataFetcher.executeData();
        EventUrl eurl = data.getEventUrl();
        byte[] compressData = Snappy.compress("cpacm".getBytes());
        compressData = XORUtils.encrypt(compressData, (int) (eventUrl.getTime() & 0xFF));
        Truth.assertThat(eurl.getRequestBody()).isEqualTo(compressData);
    }

}
