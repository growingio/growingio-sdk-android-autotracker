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

import com.growingio.android.sdk.track.middleware.http.EventEncoder;
import com.growingio.android.sdk.track.modelloader.ModelLoader;
import com.growingio.android.sdk.track.modelloader.ModelLoaderFactory;

/**
 * <p>
 *
 * @author cpacm 2021/5/13
 */
public class EncoderDataLoader implements ModelLoader<EventEncoder, EventEncoder> {

    @Override
    public LoadData<EventEncoder> buildLoadData(EventEncoder eventEncoder) {
        return new LoadData<>(new EncoderDataFetcher(eventEncoder));
    }

    public static class Factory implements ModelLoaderFactory<EventEncoder, EventEncoder> {
        @Override
        public ModelLoader<EventEncoder, EventEncoder> build() {
            return new EncoderDataLoader();
        }
    }
}
