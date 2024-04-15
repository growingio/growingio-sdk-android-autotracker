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
package com.growingio.android.okhttp3;

import com.growingio.android.sdk.track.middleware.http.EventResponse;
import com.growingio.android.sdk.track.middleware.http.EventUrl;
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
    private OkHttpClient customClient; // for special request.

    public OkHttpDataLoader(Call.Factory client) {
        this.client = client;
    }

    @Override
    public LoadData<EventResponse> buildLoadData(EventUrl eventUrl) {
        Call.Factory loadClient = client;
        if (eventUrl.getCallTimeout() > 0) {
            loadClient = newCustomClient(eventUrl.getCallTimeout());
        }
        return new LoadData<>(new OkHttpDataFetcher(loadClient, eventUrl));
    }

    private Call.Factory newCustomClient(long callTimeout) {
        if (customClient != null && customClient.callTimeoutMillis() == callTimeout) {
            return customClient;
        }
        if (client instanceof OkHttpClient) {
            OkHttpClient internalClient = (OkHttpClient) client;
            OkHttpClient.Builder builder = internalClient.newBuilder();
            builder.callTimeout(callTimeout, TimeUnit.MILLISECONDS);
            customClient = builder.build();
            return customClient;
        }
        return client;

    }

    public static class Factory implements ModelLoaderFactory<EventUrl, EventResponse> {
        private static volatile Call.Factory sInternalClient;
        private final Call.Factory client;

        private static Call.Factory getsInternalClient(OkHttpConfig config) {
            if (sInternalClient == null) {
                synchronized (Factory.class) {
                    if (sInternalClient == null) {
                        OkHttpClient.Builder builder = new OkHttpClient.Builder();
                        if (config.getHttpCallTimeout() > 0) {
                            builder.callTimeout(config.getHttpCallTimeout(), TimeUnit.MILLISECONDS);
                        } else {
                            builder.connectTimeout(config.getConnectTimeout(), TimeUnit.MILLISECONDS);
                            builder.readTimeout(config.getReadTimeout(), TimeUnit.MILLISECONDS);
                            builder.writeTimeout(config.getWriteTimeout(), TimeUnit.MILLISECONDS);
                        }
                        builder.addInterceptor(new SecurityExceptionInterceptor());
                        //builder.addInterceptor(new HttpLoggingInterceptor(message -> Logger.d("OKHTTP Logging", message)).setLevel(HttpLoggingInterceptor.Level.BODY));
                        sInternalClient = builder.build();
                    }
                }
            }
            return sInternalClient;
        }

        public Factory(OkHttpConfig config) {
            this(getsInternalClient(config));
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
