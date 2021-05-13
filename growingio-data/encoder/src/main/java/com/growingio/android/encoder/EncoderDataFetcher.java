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

import com.growingio.android.encoder.snappy.Snappy;
import com.growingio.android.sdk.track.http.EventStream;
import com.growingio.android.sdk.track.modelloader.DataFetcher;

/**
 * <p>
 *
 * @author cpacm 2021/5/13
 */
public class EncoderDataFetcher implements DataFetcher<EventStream> {
    private static final String TAG = "EncoderDataFetcher";

    private final EventStream eventStream;

    public EncoderDataFetcher(EventStream eventStream) {
        this.eventStream = eventStream;
    }

    @Override
    public void loadData(DataCallback<? super EventStream> callback) {
        try {
            long currentTimeMillis = System.currentTimeMillis();
            byte[] compressData = Snappy.compress(eventStream.getBodyData());
            compressData = XORUtils.encrypt(compressData, (int) (currentTimeMillis & 0xFF));
            callback.onDataReady(new EventStream(compressData));
        } catch (Exception e) {
            callback.onLoadFailed(e);
        }
    }

    @Override
    public EventStream executeData() {
        long currentTimeMillis = System.currentTimeMillis();
        byte[] compressData = Snappy.compress(eventStream.getBodyData());
        compressData = XORUtils.encrypt(compressData, (int) (currentTimeMillis & 0xFF));
        return new EventStream(compressData);
    }

    @Override
    public void cleanup() {
    }

    @Override
    public void cancel() {
    }

    @Override
    public Class<EventStream> getDataClass() {
        return EventStream.class;
    }

}
