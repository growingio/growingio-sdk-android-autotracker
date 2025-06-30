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

import android.content.Context
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.core.view.GestureDetectorCompat
import androidx.core.view.children
import com.growingio.android.sdk.track.middleware.webservice.Circler
import com.growingio.android.sdk.track.middleware.webservice.Debugger
import com.growingio.android.sdk.track.middleware.webservice.WebService
import com.growingio.android.sdk.track.modelloader.TrackerRegistry

internal class GrowingWindowCallback(val context: Context, private val window: Window, private val registry: TrackerRegistry?) : WindowCallbackDelegate(window.callback) {

    init {
        window.callback = this
    }

    val composeAndroidView: HashSet<ComposeAndroidView> = hashSetOf()

    fun clear() {
        composeAndroidView.clear()
    }

    fun findComposeAndroidView() {
        if (composeAndroidView.isNotEmpty()) return
        val decorView = window.decorView
        val composeViews = arrayListOf<View>()

        fun recursiveFindComposeView(composeViews: ArrayList<View>, view: View) {
            if (ComposeAndroidView.isComposeView(view)) {
                composeViews.add(view)
            } else if (view is ViewGroup) {
                view.children.forEach {
                    recursiveFindComposeView(composeViews, it)
                }
            }
        }
        recursiveFindComposeView(composeViews, decorView)
        composeViews.forEach {
            this.composeAndroidView.add(ComposeAndroidView(it))
        }
    }

    private val gestureDetect: GestureDetectorCompat = GestureDetectorCompat(
        context.applicationContext,
        object : GestureDetector.OnGestureListener {

            override fun onDown(e: MotionEvent): Boolean = false

            override fun onShowPress(e: MotionEvent) {}

            override fun onSingleTapUp(e: MotionEvent): Boolean {
                findComposeAndroidView()
                composeAndroidView.forEach {
                    it.click(e)
                }
                return false
            }

            override fun onScroll(e1: MotionEvent?, e2: MotionEvent, distanceX: Float, distanceY: Float): Boolean = false

            override fun onLongPress(e: MotionEvent) {}

            override fun onFling(e1: MotionEvent?, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean = false
        },
    )

    override fun onContentChanged() {
        composeAndroidView.clear()
        super.onContentChanged()
    }

    override fun dispatchTouchEvent(event: MotionEvent?): Boolean {
        if (event != null) {
            val obtain = MotionEvent.obtain(event)
            try {
                gestureDetect.onTouchEvent(obtain)
            } finally {
                obtain.recycle()
            }

            if (event.action == MotionEvent.ACTION_UP) {
                registry?.executeData(Circler(Circler.CIRCLE_REFRESH), Circler::class.java, WebService::class.java)
                registry?.executeData(Debugger(Debugger.DEBUGGER_REFRESH), Debugger::class.java, WebService::class.java)
            }
        }
        return super.dispatchTouchEvent(event)
    }
}
