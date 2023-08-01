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
package com.growingio.android.volley;

import android.content.Context;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.growingio.android.sdk.track.middleware.http.EventResponse;
import com.growingio.android.sdk.track.middleware.http.EventUrl;
import com.growingio.android.sdk.track.modelloader.ModelLoader;
import com.growingio.android.sdk.track.modelloader.ModelLoaderFactory;

/**
 * <p>
 *
 * @author cpacm 2021/3/31
 */
public class VolleyDataLoader implements ModelLoader<EventUrl, EventResponse> {

    private final RequestQueue requestQueue;
    private final VolleyRequestFactory requestFactory;

    public VolleyDataLoader(RequestQueue requestQueue) {
        this(requestQueue, VolleyDataFetcher.DEFAULT_REQUEST_FACTORY);
    }

    // Public API.
    @SuppressWarnings("WeakerAccess")
    public VolleyDataLoader(RequestQueue requestQueue, VolleyRequestFactory requestFactory) {
        this.requestQueue = requestQueue;
        this.requestFactory = requestFactory;
    }


    @Override
    public LoadData<EventResponse> buildLoadData(EventUrl eventUrl) {
        return new LoadData<>(new VolleyDataFetcher(requestQueue, eventUrl, requestFactory));
    }

    public static class Factory implements ModelLoaderFactory<EventUrl, EventResponse> {
        private static volatile RequestQueue sInternalQueue;

        private final VolleyRequestFactory requestFactory;
        private final RequestQueue requestQueue;

        /**
         * Constructor for a new Factory that runs requests using a static singleton request queue.
         */
        public Factory(Context context) {
            this(getInternalQueue(context));
        }

        /**
         * Constructor for a new Factory that runs requests using the given {@link RequestQueue}.
         */
        public Factory(RequestQueue requestQueue) {
            this(requestQueue, VolleyDataFetcher.DEFAULT_REQUEST_FACTORY);
        }

        /**
         * Constructor for a new Factory with a custom Volley request factory that runs requests using
         * the given {@link RequestQueue}.
         */
        public Factory(RequestQueue requestQueue, VolleyRequestFactory requestFactory) {
            this.requestFactory = requestFactory;
            this.requestQueue = requestQueue;
        }

        private static RequestQueue getInternalQueue(Context context) {
            if (sInternalQueue == null) {
                synchronized (Factory.class) {
                    if (sInternalQueue == null) {
                        sInternalQueue = Volley.newRequestQueue(context);
                    }
                }
            }
            return sInternalQueue;
        }

        @Override
        public ModelLoader<EventUrl, EventResponse> build() {
            return new VolleyDataLoader(requestQueue, requestFactory);
        }
    }
}
