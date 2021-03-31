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

package com.growingio.android.websocket

import com.growingio.android.sdk.track.modelloader.ModelLoader
import com.growingio.android.sdk.track.modelloader.ModelLoaderFactory
import com.growingio.android.sdk.track.webservices.WebSocket
import okhttp3.Call
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

/**
 * <p>
 *
 * @author cpacm 2021/4/6
 */
class WebSocketLoader(val client: Call.Factory) : ModelLoader<String, WebSocket> {

    override fun buildLoadData(model: String): ModelLoader.LoadData<WebSocket> {
        return ModelLoader.LoadData(WebSocketFetcher(model, client))
    }

    class Factory @JvmOverloads constructor(private val client: Call.Factory = internalClient) : ModelLoaderFactory<String, WebSocket> {
        override fun build(): ModelLoader<String, WebSocket> {
            return WebSocketLoader(client)
        }

        companion object {
            private val internalClient: Call.Factory by lazy {
                OkHttpClient.Builder()
                        .readTimeout(5, TimeUnit.SECONDS)
                        .writeTimeout(5, TimeUnit.SECONDS)
                        .connectTimeout(5, TimeUnit.SECONDS)
                        .build()
            }
        }
    }
}