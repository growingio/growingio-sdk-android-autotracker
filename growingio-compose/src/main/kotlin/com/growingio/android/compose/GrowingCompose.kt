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

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.semantics.SemanticsPropertyKey
import androidx.compose.ui.semantics.semantics
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner

object GrowingCompose {

    const val COMPOSABLE = "GrowingComposable"
    const val CALL = "GrowingCall"
    const val PAGE_TAG = "GrowingPage"
    const val TAG = "GrowingTag"

    const val INTERRUPT_CLICK = "InterruptClick"

    const val SEMANTICS_PROP_DIALOG = "IsDialog"
    const val SEMANTICS_PROP_POPUP = "IsPopup"
    const val SEMANTICS_PROP_CLICK = "OnClick"

    private val GROWING_TAG = SemanticsPropertyKey<String>(
        name = TAG,
        mergePolicy = { _, new -> new },
    )

    private val GROWING_COMPOSABLE = SemanticsPropertyKey<String>(
        name = COMPOSABLE,
        mergePolicy = { _, new -> new },
    )

    private val GROWING_CALL = SemanticsPropertyKey<String>(
        name = CALL,
        mergePolicy = { _, new -> new },
    )

    private val GROWING_PAGE_TAG = SemanticsPropertyKey<String>(
        name = PAGE_TAG,
        mergePolicy = { _, new -> new },
    )

    private val GROWING_INTERRUPT_CLICK = SemanticsPropertyKey<Boolean>(
        name = INTERRUPT_CLICK,
        mergePolicy = { _, new -> new },
    )

    fun Modifier.interruptClick(): Modifier {
        return semantics(
            properties = {
                this[GROWING_INTERRUPT_CLICK] = true
            },
        )
    }

    @JvmStatic
    fun Modifier.autotrackElement(composableName: String, callName: String): Modifier {
        return semantics(
            properties = {
                this[GROWING_COMPOSABLE] = composableName
                this[GROWING_CALL] = callName
            },
        )
    }

    fun Modifier.growingTag(tag: String): Modifier {
        return semantics(
            properties = {
                this[GROWING_TAG] = tag
            },
        )
    }

    private fun Modifier.growingPage(alias: String): Modifier {
        return semantics(
            properties = {
                this[GROWING_PAGE_TAG] = alias
            },
        )
    }

    @Composable
    fun GrowingComposePage(
        alias: String,
        attributes: Map<String, String> = hashMapOf(),
        content: @Composable () -> Unit,
    ) {
        val lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current
        DisposableEffect(alias) {
            val observer = LifecycleEventObserver { _: LifecycleOwner, event: Lifecycle.Event ->
                if (event == Lifecycle.Event.ON_RESUME) {
                    ComposeAutotrackProvider.addOrResumeComposePage(alias, attributes)
                }
            }
            lifecycleOwner.lifecycle.addObserver(observer)
            onDispose {
                ComposeAutotrackProvider.removeComposePage(alias)
                lifecycleOwner.lifecycle.removeObserver(observer)
            }
        }
        Box(modifier = Modifier.growingPage(alias)) { content() }
    }
}
