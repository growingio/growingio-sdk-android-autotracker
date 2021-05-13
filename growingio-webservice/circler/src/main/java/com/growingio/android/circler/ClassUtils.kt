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
package com.growingio.android.circler

import com.growingio.android.sdk.autotrack.Autotracker
import com.growingio.android.sdk.autotrack.hybrid.HybridBridgeProvider
import com.growingio.android.sdk.track.log.Logger

object ClassUtils {

    fun isAutoTracker(): Boolean {
        try {
            Autotracker.initializedSuccessfully()
            return true
        } catch (e: ClassNotFoundException) {
            Logger.e("ClassUtils", "Circler component should implement AutoTracker core.")
        }
        return false
    }
}
