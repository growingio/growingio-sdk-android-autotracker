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

package com.growingio.android.okhttp3;

import com.growingio.android.sdk.track.http.EventResponse;
import com.growingio.android.sdk.track.http.EventUrl;
import com.growingio.android.sdk.track.modelloader.ModelLoader;
import com.growingio.android.sdk.track.modelloader.ModelLoaderFactory;

import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.OkHttpClient;

/**
 * <p>
 *
 * @author cpacm 2021/3/31
 */
public class OkHttpDataLoader implements ModelLoader<EventUrl, EventResponse> {

    private final Call.Factory client;

    public OkHttpDataLoader(Call.Factory client) {
        this.client = client;
    }

    @Override
    public LoadData<EventResponse> buildLoadData(EventUrl eventUrl) {
        return new LoadData<>(new OkHttpDataFetcher(client, eventUrl));
    }

    public static class Factory implements ModelLoaderFactory<EventUrl, EventResponse> {
        private static volatile Call.Factory sInternalClient;
        private final Call.Factory client;

        private static final int DEFAULT_CONNECT_TIMEOUT = 5;
        private static final int DEFAULT_READ_TIMEOUT = 10;

        private static Call.Factory getsInternalClient() {
            if (sInternalClient == null) {
                synchronized (Factory.class) {
                    if (sInternalClient == null) {
                        sInternalClient = new OkHttpClient.Builder()
                                .connectTimeout(DEFAULT_CONNECT_TIMEOUT, TimeUnit.SECONDS)
                                .readTimeout(DEFAULT_READ_TIMEOUT, TimeUnit.SECONDS)
                                .addInterceptor(new SecurityExceptionInterceptor())
                                .build();
                    }
                }
            }
            return sInternalClient;
        }

        public Factory() {
            this(getsInternalClient());
        }

        public Factory(Call.Factory client) {
            this.client = client;
        }

        @Override
        public ModelLoader<EventUrl, EventResponse> build() {
            return new OkHttpDataLoader(client);
        }
    }
}
