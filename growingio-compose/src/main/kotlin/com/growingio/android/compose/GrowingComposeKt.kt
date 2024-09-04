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

import com.growingio.android.sdk.track.TrackMainThread
import com.growingio.android.sdk.track.events.PageEvent

internal object GrowingComposeKt {

    fun String.path(): String {
        return if (this.startsWith("/")) {
            this
        } else {
            "/$this"
        }
    }

    fun trackComposePage(alias: String, attributes: Map<String, String>? = null) {
        TrackMainThread.trackMain().postEventToTrackMain(
            PageEvent.Builder()
                .setPath(alias.path())
                .setTitle(alias)
                .setTimestamp(System.currentTimeMillis())
                .setAttributes(attributes),
        )
    }
}
