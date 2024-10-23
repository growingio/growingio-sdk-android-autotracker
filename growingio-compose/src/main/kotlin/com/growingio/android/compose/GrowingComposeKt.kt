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

import androidx.compose.ui.node.LayoutModifierNode
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection
import com.growingio.android.sdk.track.TrackMainThread
import com.growingio.android.sdk.track.log.Logger
import com.growingio.android.sdk.track.utils.DeviceUtil

internal object GrowingComposeKt {

    private const val TAG = "GrowingComposeKt"

    fun String.path(): String {
        return if (this.startsWith("/")) {
            this
        } else {
            "/$this"
        }
    }

    private const val OFFSET_NODE_CLASSNAME: String =
        "androidx.compose.foundation.layout.OffsetNode"
    private const val OFFSET_PX_NODE_CLASSNAME: String =
        "androidx.compose.foundation.layout.OffsetPxNode"

    fun reflectModifierNode(
        nodeInfo: ComposeNode,
        modifierNode: LayoutModifierNode,
        layoutDirection: LayoutDirection,
    ) {
        val className = modifierNode::class.java.canonicalName
        val context = TrackMainThread.trackMain().context ?: return
        try {
            if (OFFSET_NODE_CLASSNAME == className) {
                val fieldX = modifierNode::class.java.getDeclaredField("x")
                fieldX.isAccessible = true
                val x = fieldX.get(modifierNode) as Float

                val fieldY = modifierNode::class.java.getDeclaredField("y")
                fieldY.isAccessible = true
                val y = fieldY.get(modifierNode) as Float

                val rtlAware = modifierNode::class.java.getDeclaredField("rtlAware")
                rtlAware.isAccessible = true
                val rtl = rtlAware.get(modifierNode) as Boolean

                val xPx = DeviceUtil.dp2Px(context, x)
                val yPx = DeviceUtil.dp2Px(context, y)

                nodeInfo.offset =
                    IntOffset(if (rtl && layoutDirection == LayoutDirection.Rtl) -xPx else xPx, yPx)
                Logger.d(TAG, "nodeInfo offset: ${nodeInfo.offset}")
            } else if (OFFSET_PX_NODE_CLASSNAME == className) {
                val density = context.resources.displayMetrics.density
                val offsetField = modifierNode::class.java.getDeclaredField("offset")
                offsetField.isAccessible = true
                val offset = offsetField.get(modifierNode) as Function1<Density, IntOffset>
                val value = offset.invoke(Density(density))

                val rtlAware = modifierNode::class.java.getDeclaredField("rtlAware")
                rtlAware.isAccessible = true
                val rtl = rtlAware.get(modifierNode) as Boolean

                nodeInfo.offset = IntOffset(
                    if (rtl && layoutDirection == LayoutDirection.Rtl) -value.x else value.x,
                    value.y,
                )
                Logger.d(TAG, "nodeInfo offset: ${nodeInfo.offset}")
            }
        } catch (e: Exception) {
            Logger.e(TAG, "reflectModifierNode error: ${e.message}")
        }
    }
}
