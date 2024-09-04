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

import android.view.View
import android.view.ViewGroup
import androidx.core.view.children
import com.growingio.android.sdk.TrackerContext
import com.growingio.android.sdk.track.modelloader.TrackerRegistry
import com.growingio.android.sdk.track.providers.TrackerLifecycleProvider
import com.growingio.android.sdk.track.view.OnDecorViewsObserver
import com.growingio.android.sdk.track.view.WindowHelper

class ComposeAutotrackProvider : TrackerLifecycleProvider, OnDecorViewsObserver {

    // Map<DecorView.HashCode,Window.Callback>
    private val composeMaps: HashMap<Int, GrowingWindowCallback?> = hashMapOf()
    private var registry: TrackerRegistry? = null

    override fun setup(context: TrackerContext) {
        if (context.configurationProvider.isDowngrade) return
        this.registry = context.registry
        WindowHelper.get().addWindowManagerViewsObserver(this)
    }

    override fun onDecorViewAdded(view: View?) {
        if (view == null) return
        val composeViews = arrayListOf<View>()
        findComposeView(composeViews, view)
        if (composeViews.isEmpty()) {
            return
        }
        // now we find some compose views in a new window.
        val window = WindowHelper.get().pullWindow(view)
        val context = view.context.applicationContext
        if (window != null) {
            val composeWindowCallback = GrowingWindowCallback(context, window, registry)
            this.composeMaps[view.hashCode()] = composeWindowCallback
        }
    }

    override fun onDecorViewRemoved(view: View?) {
        if (view == null) return
        val callback = this.composeMaps.remove(view.hashCode())
        callback?.clear()
    }

    override fun shutdown() {
        WindowHelper.get().removeWindowManagerViewsObserver(this)
    }

    private fun findComposeView(composeViews: ArrayList<View>, view: View) {
        if (view.mightBeComposeView) {
            composeViews.add(view)
        } else if (view is ViewGroup) {
            view.children.forEach {
                findComposeView(composeViews, it)
            }
        }
    }

    private val View.mightBeComposeView: Boolean
        get() = "androidx.compose.ui.platform.ComposeView" in this::class.java.name || "AndroidComposeView" in this::class.java.name
}
