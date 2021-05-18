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

import com.growingio.android.sdk.track.http.EventData;
import com.growingio.android.sdk.track.http.EventStream;
import com.growingio.android.sdk.track.modelloader.ModelLoader;
import com.growingio.android.sdk.track.modelloader.ModelLoaderFactory;

/**
 * <p>
 *
 * @author cpacm 2021/5/13
 */
public class JsonDataLoader implements ModelLoader<EventData, EventStream> {

    @Override
    public LoadData<EventStream> buildLoadData(EventData eventData) {
        return new LoadData<>(new JsonDataFetcher(eventData));
    }

    public static class Factory implements ModelLoaderFactory<EventData, EventStream> {
        @Override
        public ModelLoader<EventData, EventStream> build() {
            return new JsonDataLoader();
        }
    }
}
