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

import com.growingio.android.sdk.track.modelloader.DataFetcher
import com.growingio.android.sdk.track.webservices.WebSocket
import okhttp3.Call

/**
 * <p>
 *
 * @author cpacm 2021/4/2
 */
class WebSocketFetcher(private val wsUrl: String, private val client: Call.Factory) : DataFetcher<WebSocket> {

    init {

    }

    override fun loadData(callback: DataFetcher.DataCallback<in WebSocket>?) {
        //not used
    }

    override fun executeData(): WebSocket {
        TODO("Not yet implemented")
    }

    override fun cleanup() {
        TODO("Not yet implemented")
    }

    override fun cancel() {
        TODO("Not yet implemented")
    }

    override fun getDataClass(): Class<WebSocket> {
        TODO("Not yet implemented")
    }
}