/*
 *  Copyright (C) 2020 Beijing Yishu Technology Co., Ltd.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.growingio.android.debugger

import com.growingio.android.sdk.track.SDKConfig
import com.growingio.android.sdk.track.TrackMainThread
import com.growingio.android.sdk.track.async.Callback
import com.growingio.android.sdk.track.async.Disposable
import com.growingio.android.sdk.track.events.EventBuildInterceptor
import com.growingio.android.sdk.track.events.base.BaseEvent
import com.growingio.android.sdk.track.log.CircularFifoQueue
import com.growingio.android.sdk.track.log.Logger
import com.growingio.android.sdk.track.middleware.GEvent
import com.growingio.android.sdk.track.providers.ConfigurationProvider
import com.growingio.android.sdk.track.webservices.log.WsLogger
import org.json.JSONException
import org.json.JSONObject
import java.util.*

/**
 *
 * debug event wrapper for debugger service
 *
 * @author cpacm 2021/2/24
 */
class DebuggerEventWrapper private constructor() : EventBuildInterceptor, ScreenshotProvider.OnScreenshotRefreshedListener {
    private var mOnDebuggerEventListener: OnDebuggerEventListener? = null

    private object SingleInstance {
        val INSTANCE = DebuggerEventWrapper()
    }

    fun registerDebuggerEventListener(listener: OnDebuggerEventListener?) {
        mOnDebuggerEventListener = listener
    }

    fun observeEventBuild() {
        TrackMainThread.trackMain().addEventBuildInterceptor(this)
    }

    fun ready() {
        mIsConnected = true
        observeEventBuild()
        ScreenshotProvider.get().registerScreenshotRefreshedListener(this)
        sendCacheMessage()
    }

    fun end() {
        mIsConnected = false
        ScreenshotProvider.get().unregisterScreenshotRefreshedListener(this)
        TrackMainThread.trackMain().removeEventBuildInterceptor(this)
        closeLogger()
        mOnDebuggerEventListener = null
    }

    /***************** Base Event  *****************/
    @Volatile
    private var mIsConnected = false
    private val mCollectionMessage: Queue<String> = CircularFifoQueue(50)
    private fun sendCacheMessage() {
        for (message in mCollectionMessage) {
            if (mOnDebuggerEventListener != null) {
                mOnDebuggerEventListener!!.onDebuggerMessage(message)
            }
        }
        mCollectionMessage.clear()
    }

    override fun eventWillBuild(eventBuilder: BaseEvent.BaseBuilder<*>?) {}
    override fun eventDidBuild(event: GEvent) {
        if (event is BaseEvent) {
            try {
                val eventJson = event.toJSONObject()
                //添加额外的url,以便debugger显示请求地址
                eventJson.put("url", url)
                val json = JSONObject()
                json.put("msgType", SERVICE_DEBUGGER_TYPE)
                json.put("sdkVersion", SDKConfig.SDK_VERSION)
                json.put("data", eventJson)
                if (mIsConnected && mOnDebuggerEventListener != null) {
                    mOnDebuggerEventListener!!.onDebuggerMessage(json.toString())
                } else {
                    mCollectionMessage.add(json.toString())
                }
            } catch (ignored: JSONException) {
                Logger.e("DebuggerEventWrapper", "can't get event json " + event.getEventType())
            }
        }
    }

    private val url: String
        get() {
            val url = StringBuilder(ConfigurationProvider.get().trackConfiguration.dataCollectionServerHost)
            if (url.length > 0 && url[url.length - 1] != '/') {
                url.append("/")
            }
            url.append("v3/projects/")
            val projectId = ConfigurationProvider.get().trackConfiguration.projectId
            url.append(projectId)
            url.append("/collect?stm=")
            url.append(System.currentTimeMillis())
            return url.toString()
        }

    /***************** Logger  *****************/
    private var mWsLogger: WsLogger? = null
    fun printLog() {
        if (mWsLogger != null) {
            mWsLogger!!.printOut()
        }
    }

    fun openLogger() {
        if (mWsLogger == null) {
            mWsLogger = WsLogger()
            mWsLogger!!.openLog()
        }
        mWsLogger!!.setCallback { logMessage: String ->
            if (mOnDebuggerEventListener != null) {
                mOnDebuggerEventListener!!.onDebuggerMessage(logMessage)
            }
        }
    }

    fun closeLogger() {
        if (mWsLogger == null) {
            return
        }
        mWsLogger!!.closeLog()
        mWsLogger!!.setCallback(null)
        mWsLogger = null
    }

    /***************** ScreenShot  *****************/
    override fun onScreenshotRefreshed(screenshot: DebuggerScreenshot) {
        if (mOnDebuggerEventListener != null) {
            mOnDebuggerEventListener!!.onDebuggerMessage(screenshot.toJSONObject().toString())
        }
        printLog()
    }


    interface OnDebuggerEventListener {
        fun onDebuggerMessage(message: String)
    }

    companion object {
        private const val TAG = "DebuggerEventWrapper"
        const val SERVICE_LOGGER_OPEN = "logger_open"
        const val SERVICE_LOGGER_CLOSE = "logger_close"
        const val SERVICE_DEBUGGER_TYPE = "debugger_data"

        @JvmStatic
        fun get(): DebuggerEventWrapper {
            return SingleInstance.INSTANCE
        }
    }

}