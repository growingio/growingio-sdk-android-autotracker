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

package com.growingio.android.debugger;

import com.growingio.android.sdk.track.modelloader.ModelLoader;
import com.growingio.android.sdk.track.modelloader.ModelLoaderFactory;
import com.growingio.android.sdk.track.webservices.Debugger;
import com.growingio.android.sdk.track.webservices.WebService;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;

/**
 * <p>
 *
 * @author cpacm 2021/3/31
 */
public class DebuggerDataLoader implements ModelLoader<Debugger, WebService> {

    private final DebuggerService debuggerService;

    public DebuggerDataLoader(OkHttpClient client) {
        debuggerService = new DebuggerService(client);
    }

    @Override
    public LoadData<WebService> buildLoadData(Debugger debugger) {
        debuggerService.init(debugger.getParams());
        return new LoadData<>(debuggerService);
    }

    public static class Factory implements ModelLoaderFactory<Debugger, WebService> {
        private static volatile OkHttpClient sInternalClient;
        private final OkHttpClient client;

        private static final int DEFAULT_CONNECT_TIMEOUT = 5;
        private static final int DEFAULT_READ_TIMEOUT = 10;

        private static OkHttpClient getsInternalClient() {
            if (sInternalClient == null) {
                synchronized (Factory.class) {
                    if (sInternalClient == null) {
                        sInternalClient = new OkHttpClient.Builder()
                                .connectTimeout(DEFAULT_CONNECT_TIMEOUT, TimeUnit.SECONDS)
                                .readTimeout(DEFAULT_READ_TIMEOUT, TimeUnit.SECONDS)
                                .build();
                    }
                }
            }
            return sInternalClient;
        }

        public Factory() {
            this(getsInternalClient());
        }

        public Factory(OkHttpClient client) {
            this.client = client;
        }

        @Override
        public ModelLoader<Debugger, WebService> build() {
            return new DebuggerDataLoader(client);
        }
    }
}
