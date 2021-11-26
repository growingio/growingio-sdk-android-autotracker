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

package com.growingio.android.json;

import com.growingio.android.sdk.track.middleware.format.EventFormatData;
import com.growingio.android.sdk.track.middleware.format.EventByteArray;
import com.growingio.android.sdk.track.modelloader.ModelLoader;
import com.growingio.android.sdk.track.modelloader.ModelLoaderFactory;

/**
 * <p>
 *
 * @author cpacm 2021/5/13
 */
public class JsonDataLoader implements ModelLoader<EventFormatData, EventByteArray> {

    @Override
    public LoadData<EventByteArray> buildLoadData(EventFormatData eventData) {
        return new LoadData<>(new JsonDataFetcher(eventData));
    }

    public static class Factory implements ModelLoaderFactory<EventFormatData, EventByteArray> {
        @Override
        public ModelLoader<EventFormatData, EventByteArray> build() {
            return new JsonDataLoader();
        }
    }
}
