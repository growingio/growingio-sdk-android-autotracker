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
package com.growingio.android.urlconnection;

import com.growingio.android.sdk.track.middleware.http.EventResponse;
import com.growingio.android.sdk.track.middleware.http.EventUrl;
import com.growingio.android.sdk.track.modelloader.ModelLoader;
import com.growingio.android.sdk.track.modelloader.ModelLoaderFactory;

/**
 * <p>
 *
 * @author cpacm 2021/5/12
 */
public class UrlConnectionDataLoader implements ModelLoader<EventUrl, EventResponse> {

    private final UrlConnectionConfig config;

    public UrlConnectionDataLoader(UrlConnectionConfig config) {
        this.config = config;
    }

    @Override
    public LoadData<EventResponse> buildLoadData(EventUrl eventUrl) {
        return new LoadData<>(new UrlConnectionFetcher(config, eventUrl));
    }

    public static class Factory implements ModelLoaderFactory<EventUrl, EventResponse> {

        private final UrlConnectionConfig config;

        public Factory(UrlConnectionConfig config) {
            this.config = config;
        }

        @Override
        public ModelLoader<EventUrl, EventResponse> build() {
            return new UrlConnectionDataLoader(config);
        }
    }
}
