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

package com.growingio.android.encoder;

import com.growingio.android.sdk.track.http.EventEncoder;
import com.growingio.android.sdk.track.http.EventUrl;
import com.growingio.android.sdk.track.modelloader.DataFetcher;
import com.growingio.android.snappy.Snappy;
import com.growingio.android.snappy.XORUtils;

/**
 * <p>
 *
 * @author cpacm 2021/5/13
 */
public class EncoderDataFetcher implements DataFetcher<EventEncoder> {
    private static final String TAG = "EncoderDataFetcher";

    private final EventEncoder eventEncoder;

    public EncoderDataFetcher(EventEncoder eventEncoder) {
        this.eventEncoder = eventEncoder;
    }

    @Override
    public void loadData(DataCallback<? super EventEncoder> callback) {
        try {
            EventUrl eventUrl = eventEncoder.getEventUrl();
            long currentTimeMillis = eventUrl.getTime();
            byte[] compressData = Snappy.compress(eventUrl.getRequestBody());
            compressData = XORUtils.encrypt(compressData, (int) (currentTimeMillis & 0xFF));
            eventUrl.setMediaType("application/json");
            eventUrl.setBodyData(compressData);
            eventUrl.addHeader("X-Compress-Codec", "2");
            eventUrl.addHeader("X-Crypt-Codec", "1");
            callback.onDataReady(eventEncoder);
        } catch (Exception e) {
            callback.onLoadFailed(e);
        }
    }

    @Override
    public EventEncoder executeData() {
        EventUrl eventUrl = eventEncoder.getEventUrl();
        long currentTimeMillis = eventUrl.getTime();
        byte[] compressData = Snappy.compress(eventUrl.getRequestBody());
        compressData = XORUtils.encrypt(compressData, (int) (currentTimeMillis & 0xFF));
        eventUrl.setMediaType("application/json");
        eventUrl.setBodyData(compressData);
        eventUrl.addHeader("X-Compress-Codec", "2");
        eventUrl.addHeader("X-Crypt-Codec", "1");
        return eventEncoder;
    }

    @Override
    public void cleanup() {
    }

    @Override
    public void cancel() {
    }

    @Override
    public Class<EventEncoder> getDataClass() {
        return EventEncoder.class;
    }

}
