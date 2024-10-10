/*
 * Copyright (C) 2024 Beijing Yishu Technology Co., Ltd.
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
package com.growingio.android.compose

import com.growingio.android.sdk.LibraryGioModule
import com.growingio.android.sdk.TrackerContext
import com.growingio.android.sdk.track.middleware.compose.ComposeData
import com.growingio.android.sdk.track.middleware.compose.ComposeJson
import com.growingio.android.sdk.track.providers.TrackerLifecycleProvider
import com.growingio.sdk.annotation.GIOLibraryModule

@GIOLibraryModule
class ComposeLibraryGioModule : LibraryGioModule() {

    override fun registerComponents(context: TrackerContext) {
        context.registry.register(ComposeData::class.java, ComposeJson::class.java, Factory(context))
    }

    override fun setupProviders(providerStore: MutableMap<Class<out TrackerLifecycleProvider>, TrackerLifecycleProvider>) {
        providerStore[ComposeAutotrackProvider::class.java] = ComposeAutotrackProvider()
    }
}
