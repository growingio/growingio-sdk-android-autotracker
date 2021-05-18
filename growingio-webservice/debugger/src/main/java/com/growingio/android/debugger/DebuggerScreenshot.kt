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

import com.growingio.android.sdk.TrackerContext
import com.growingio.android.sdk.track.async.Callback
import com.growingio.android.sdk.track.async.Disposable
import com.growingio.android.sdk.track.async.UnsubscribedDisposable
import com.growingio.android.sdk.track.utils.DeviceUtil
import org.json.JSONException
import org.json.JSONObject
import java.util.concurrent.atomic.AtomicInteger

/**
 * create screenshot server date from app's screenshot for debugger
 */
class DebuggerScreenshot(private val builder: Builder) {
    private val mScreenWidth: Int
    private val mScreenHeight: Int
    private val mScale: Float
    private val mScreenshot: String?
    private val mMsgType: String
    private val mSnapshotKey: Long

    init {
        mMsgType = MSG_TYPE
        mScreenWidth = builder.mScreenWidth
        mScreenHeight = builder.mScreenHeight
        mScale = builder.mScale
        mScreenshot = builder.mScreenshot
        mSnapshotKey = builder.mSnapshotKey
    }

    fun toJSONObject(): JSONObject {
        val json = JSONObject()
        try {
            json.put("screenWidth", mScreenWidth)
            json.put("screenHeight", mScreenHeight)
            json.put("scale", mScale.toDouble())
            json.put("screenshot", mScreenshot)
            json.put("msgType", mMsgType)
            json.put("snapshotKey", mSnapshotKey)
        } catch (ignored: JSONException) {
        }
        return json
    }

    class Builder {
        var mScreenWidth = 0
        var mScreenHeight = 0
        var mScale = 0f
        var mScreenshot: String? = null
        var mSnapshotKey: Long = 0
        val mWebViewCount = AtomicInteger(0)
        var mScreenshotResultCallback: Callback<DebuggerScreenshot>? = null
        var mBuildDisposable: Disposable? = null
        fun setScale(scale: Float): Builder {
            mScale = scale
            return this
        }

        fun setScreenshot(screenshot: String?): Builder {
            mScreenshot = screenshot
            return this
        }

        fun setSnapshotKey(snapshotKey: Long): Builder {
            mSnapshotKey = snapshotKey
            return this
        }

        fun build(callback: Callback<DebuggerScreenshot>?): Disposable {
            if (callback == null) {
                return Disposable.EMPTY_DISPOSABLE
            }
            mBuildDisposable = UnsubscribedDisposable()
            mScreenshotResultCallback = callback
            val displayMetrics = DeviceUtil.getDisplayMetrics(TrackerContext.get().applicationContext)
            mScreenWidth = displayMetrics.widthPixels
            mScreenHeight = displayMetrics.heightPixels
            if (mWebViewCount.get() == 0) {
                callResultOnSuccess()
            }
            return mBuildDisposable as UnsubscribedDisposable
        }

        private fun callResultOnSuccess() {
            if (!mBuildDisposable!!.isDisposed) {
                mBuildDisposable!!.dispose()
                if (mScreenshotResultCallback != null) {
                    mScreenshotResultCallback!!.onSuccess(DebuggerScreenshot(this))
                }
            }
        }

        private fun callResultOnFailed() {
            if (!mBuildDisposable!!.isDisposed) {
                mBuildDisposable!!.dispose()
                if (mScreenshotResultCallback != null) {
                    mScreenshotResultCallback!!.onFailed()
                }
            }
        }
    }

    companion object {
        private const val MSG_TYPE = "refreshScreenshot"
    }

}