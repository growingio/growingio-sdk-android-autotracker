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
package com.growingio.android.debugger

import com.growingio.android.sdk.track.modelloader.ModelLoader
import com.growingio.android.sdk.track.modelloader.ModelLoader.LoadData
import com.growingio.android.sdk.track.modelloader.ModelLoaderFactory
import com.growingio.android.sdk.track.providers.ActivityStateProvider
import com.growingio.android.sdk.track.webservices.Debugger
import com.growingio.android.sdk.track.webservices.WebService
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

class DebuggerDataLoader(client: OkHttpClient?) : ModelLoader<Debugger, WebService> {
    private val debuggerService: DebuggerService = DebuggerService(client!!)

    override fun buildLoadData(debugger: Debugger): LoadData<WebService> {
        debuggerService.init(debugger.params)
        return LoadData(debuggerService)
    }

    class Factory @JvmOverloads constructor(private val client: OkHttpClient? = internalClient) : ModelLoaderFactory<Debugger, WebService> {
        override fun build(): ModelLoader<Debugger, WebService> {
            return DebuggerDataLoader(client)
        }

        companion object {
            @Volatile
            private var internalClient: OkHttpClient? = null
                get() {
                    if (field == null) {
                        synchronized(Factory::class.java) {
                            if (field == null) {
                                field = OkHttpClient.Builder()
                                        .readTimeout(OKHTTP_CLIENT_TIMEOUT.toLong(), TimeUnit.SECONDS)
                                        .writeTimeout(OKHTTP_CLIENT_TIMEOUT.toLong(), TimeUnit.SECONDS)
                                        .connectTimeout(OKHTTP_CLIENT_TIMEOUT.toLong(), TimeUnit.SECONDS)
                                        .build()
                            }
                        }
                    }
                    return field
                }
            private const val OKHTTP_CLIENT_TIMEOUT = 10
        }
    }

}