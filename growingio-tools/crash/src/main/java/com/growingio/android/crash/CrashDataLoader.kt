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
package com.growingio.android.crash

import android.content.Context
import com.growingio.android.sdk.track.log.Crash
import com.growingio.android.sdk.track.modelloader.DataFetcher
import com.growingio.android.sdk.track.modelloader.ModelLoader
import com.growingio.android.sdk.track.modelloader.ModelLoader.LoadData
import com.growingio.android.sdk.track.modelloader.ModelLoaderFactory

/**
 *
 * @author cpacm 2021/5/13
 */
class CrashDataLoader(context: Context) : ModelLoader<Crash, Void?> {

    init {
        CrashManager.register(context)
    }

    override fun buildLoadData(eventData: Crash): LoadData<Void?> {
        return LoadData(object : DataFetcher<Void> {
            override fun loadData(callback: DataFetcher.DataCallback<in Void>) {
                callback.onDataReady(null)
            }

            override fun executeData(): Void? {
                return null; }

            override fun cleanup() {}

            override fun cancel() {}

            override fun getDataClass(): Class<Void> {
                return Void::class.java
            }
        })
    }

    class Factory(private val context: Context) : ModelLoaderFactory<Crash, Void?> {
        override fun build(): ModelLoader<Crash, Void?> {
            return CrashDataLoader(context)
        }
    }
}