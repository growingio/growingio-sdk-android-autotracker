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

import android.os.Handler
import android.os.HandlerThread
import android.view.View
import com.growingio.android.debugger.ClassUtils.registerHybridScreenShot
import com.growingio.android.sdk.TrackerContext
import com.growingio.android.sdk.track.async.Callback
import com.growingio.android.sdk.track.async.Disposable
import com.growingio.android.sdk.track.listener.ListenerContainer
import com.growingio.android.sdk.track.log.Logger
import com.growingio.android.sdk.track.utils.DeviceUtil
import com.growingio.android.sdk.track.view.*
import com.growingio.android.sdk.track.webservices.widget.TipView
import java.io.IOException

class ScreenshotProvider private constructor() : ListenerContainer<ScreenshotProvider.OnScreenshotRefreshedListener, DebuggerScreenshot>() {
    private val mScale: Float
    private val mHandler: Handler
    private val mHandlerThread: HandlerThread
    private val mRefreshScreenshotRunnable = Runnable { dispatchScreenshot() }

    init {
        val metrics = DeviceUtil.getDisplayMetrics(TrackerContext.get().applicationContext)
        mScale = SCREENSHOT_STANDARD_WIDTH / Math.min(metrics.widthPixels, metrics.heightPixels)
        mHandlerThread = HandlerThread("ScreenshotProvider")
        mHandlerThread.start()
        mHandler = Handler(mHandlerThread.looper)
        ViewTreeStatusProvider.get().register(OnViewStateChangedListener { refreshScreenshot() })
        registerHybridScreenShot(this)
    }

    private object SingleInstance {
        val INSTANCE = ScreenshotProvider()
    }

    private fun dispatchScreenshot() {
        // if dno't has listener,just return
        if (listenerCount == 0) return
        val decorViews = WindowHelper.get().topActivityViews
        if (decorViews.isEmpty()) {
            return
        }
        for (i in decorViews.indices.reversed()) {
            if (decorViews[i].view is TipView) {
                decorViews.removeAt(i)
                break
            }
        }
        val topView = decorViews[decorViews.size - 1].view
        topView.addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
            override fun onViewAttachedToWindow(v: View) {}
            override fun onViewDetachedFromWindow(v: View) {
                refreshScreenshot()
            }
        })
        topView.post {
            try {
                val screenshotBase64 = ScreenshotUtil.getScreenshotBase64(mScale)
                sendScreenshotRefreshed(screenshotBase64, mScale)
            } catch (e: IOException) {
                Logger.e(TAG, e)
            }
        }
    }

    private var mDebuggerScreenshotDisposable: Disposable? = null
    private var mSnapshotKey: Long = 0

    private fun sendScreenshotRefreshed(screenshotBase64: String?, scale: Float) {
        if (mDebuggerScreenshotDisposable != null) {
            mDebuggerScreenshotDisposable!!.dispose()
        }
        mDebuggerScreenshotDisposable = DebuggerScreenshot.Builder()
                .setScale(scale)
                .setScreenshot(screenshotBase64)
                .setSnapshotKey(mSnapshotKey++)
                .build(object : Callback<DebuggerScreenshot> {
                    override fun onFailed() {
                        Logger.e(TAG, "Create debugger screenshot failed")
                    }

                    override fun onSuccess(result: DebuggerScreenshot?) {
                        if (result != null) {
                            dispatchActions(result)
                        }
                    }
                })
    }

    fun refreshScreenshot() {
        mHandler.removeCallbacks(mRefreshScreenshotRunnable)
        mHandler.postDelayed(mRefreshScreenshotRunnable, MIN_REFRESH_INTERVAL)
    }

    override fun singleAction(listener: OnScreenshotRefreshedListener, action: DebuggerScreenshot) {
        listener.onScreenshotRefreshed(action)
    }

    fun registerScreenshotRefreshedListener(listener: OnScreenshotRefreshedListener?) {
        register(listener)
        refreshScreenshot()
    }

    fun unregisterScreenshotRefreshedListener(listener: OnScreenshotRefreshedListener?) {
        unregister(listener)
    }

    interface OnScreenshotRefreshedListener {
        fun onScreenshotRefreshed(screenshot: DebuggerScreenshot)
    }

    companion object {
        private const val TAG = "ScreenshotProvider"
        private const val SCREENSHOT_STANDARD_WIDTH = 720f
        private const val MIN_REFRESH_INTERVAL = 300L

        @JvmStatic
        fun get(): ScreenshotProvider {
            return SingleInstance.INSTANCE
        }
    }
}