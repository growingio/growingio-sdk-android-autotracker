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

import android.app.AlertDialog
import android.content.DialogInterface
import android.text.TextUtils
import com.growingio.android.debugger.DebuggerEventWrapper.Companion.get
import com.growingio.android.debugger.WebSocketHandler.OnWebSocketListener
import com.growingio.android.sdk.TrackerContext
import com.growingio.android.sdk.track.SDKConfig
import com.growingio.android.sdk.track.listener.IActivityLifecycle
import com.growingio.android.sdk.track.listener.event.ActivityLifecycleEvent
import com.growingio.android.sdk.track.log.Logger
import com.growingio.android.sdk.track.modelloader.DataFetcher
import com.growingio.android.sdk.track.providers.ActivityStateProvider
import com.growingio.android.sdk.track.providers.AppInfoProvider
import com.growingio.android.sdk.track.utils.ThreadUtils
import com.growingio.android.sdk.track.webservices.WebService
import com.growingio.android.sdk.track.webservices.message.QuitMessage
import com.growingio.android.sdk.track.webservices.widget.TipView
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONException
import org.json.JSONObject
import java.util.concurrent.atomic.AtomicInteger

class DebuggerService(private val client: OkHttpClient) : DataFetcher<WebService>, IActivityLifecycle, OnWebSocketListener {
    private val tipView: TipView?
    private val webSocketHandler: WebSocketHandler
    private lateinit var params: Map<String, String>
    private val socketState = AtomicInteger(SOCKET_STATE_INITIALIZE)


    init {
        get().registerDebuggerEventListener(object : DebuggerEventWrapper.OnDebuggerEventListener {
            override fun onDebuggerMessage(message: String) {
                sendMessage(message)
            }
        })
        ActivityStateProvider.get().registerActivityLifecycleListener(this)
        tipView = TipView(TrackerContext.get().applicationContext)
        webSocketHandler = WebSocketHandler(this)
    }

    fun init(params: Map<String, String>) {
        this.params = params
    }

    /************************** ModelData Fetcher  ************************/
    override fun loadData(callback: DataFetcher.DataCallback<in WebService>) {
        // restart, and it doesn't mean if you set different wsurl after websocket started.
        if (socketState.get() == SOCKET_STATE_READIED) {
            callback.onDataReady(WebService())
            return
        }
        webSocketHandler.webSocket?.cancel()
        val wsUrl = params.get(WS_URL)
        if (TextUtils.isEmpty(wsUrl)) {
            callback.onLoadFailed(NullPointerException("wsUrl is NULL, can't start WebSocketService"))
            return
        }
        val request = Request.Builder().url(wsUrl!!).build()
        client.newWebSocket(request, webSocketHandler)
        //client.dispatcher().executorService().shutdown()

        ActivityStateProvider.get().registerActivityLifecycleListener(this)
        tipView?.show()
        tipView?.setContent(R.string.growing_debugger_connecting_to_web)

        ThreadUtils.postOnUiThreadDelayed({
            if (socketState.get() < SOCKET_STATE_READIED) {
                Logger.e(TAG, "start WebSocketService timeout")
                onFailed()
            }
        }, 10000)
    }

    protected fun sendMessage(msg: String) {
        webSocketHandler.sendMessage(msg)
    }

    override fun executeData(): WebService? {
        return null
    }

    override fun cleanup() {
        cancel()
    }

    override fun cancel() {
        socketState.set(SOCKET_STATE_CLOSED)
        if (webSocketHandler.webSocket != null) {
            webSocketHandler.webSocket?.cancel()
        }
        get().end()
        tipView?.dismiss()
        ActivityStateProvider.get().unregisterActivityLifecycleListener(this)
    }

    override fun getDataClass(): Class<WebService> {
        return WebService::class.java
    }

    /************************** WebSocket Handler  ************************/
    override fun onReady() {
        get().registerDebuggerEventListener(object : DebuggerEventWrapper.OnDebuggerEventListener {
            override fun onDebuggerMessage(message: String) {
                sendMessage(message)
            }
        })
        socketState.set(SOCKET_STATE_READIED)

        get().ready()
        tipView!!.setContent(R.string.growing_debugger_progress)
        tipView.setOnClickListener { showExitDialog() }
    }

    private fun showExitDialog() {
        val activity = ActivityStateProvider.get().foregroundActivity
        if (activity == null) {
            Logger.e(TAG, "showExitDialog: ForegroundActivity is NULL")
            return
        }
        val message = """
            ${activity.getString(R.string.growing_debugger_app_version)}${AppInfoProvider.get().appVersion}
            ${activity.getString(R.string.growing_debugger_sdk_version)}${SDKConfig.SDK_VERSION}
            """.trimIndent()
        AlertDialog.Builder(activity)
                .setTitle(R.string.growing_debugger_progress)
                .setMessage(message)
                .setPositiveButton(R.string.growing_debugger_exit) { _: DialogInterface?, _: Int -> exitDebugger() }
                .setNegativeButton(R.string.growing_debugger_continue, null)
                .create()
                .show()
    }

    override fun onMessage(msg: String) {
        try {
            val message = JSONObject(msg)
            val msgType = message.optString("msgType")
            if (DebuggerEventWrapper.SERVICE_LOGGER_OPEN == msgType) {
                get().openLogger()
            } else if (DebuggerEventWrapper.SERVICE_LOGGER_CLOSE == msgType) {
                get().closeLogger()
            }
        } catch (e: JSONException) {
            Logger.e(TAG, e)
        }
    }

    private fun exitDebugger() {
        sendMessage(QuitMessage().toJSONObject().toString())
        cleanup()
    }

    override fun onFailed() {
        if (socketState.get() >= SOCKET_STATE_CLOSED) {
            return
        }
        socketState.set(SOCKET_STATE_CLOSED)
        tipView?.setErrorMessage(R.string.growing_debugger_connected_to_web_failed)
        Logger.e(TAG, "Start DebuggerService Failed")
        showQuitedDialog()
    }

    override fun onQuited() {
        if (socketState.get() >= SOCKET_STATE_CLOSED) {
            return
        }
        cancel()
        socketState.set(SOCKET_STATE_CLOSED)
        showQuitedDialog()
    }

    private fun showQuitedDialog() {
        val activity = ActivityStateProvider.get().foregroundActivity
        if (activity == null) {
            Logger.e(TAG, "showQuitedDialog: ForegroundActivity is NULL")
            return
        }
        AlertDialog.Builder(activity)
                .setTitle(R.string.growing_debugger_device_unconnected)
                .setMessage(R.string.growing_debugger_unconnected)
                .setPositiveButton(R.string.growing_debugger_exit) { _: DialogInterface?, _: Int -> exitDebugger() }
                .setOnDismissListener { exitDebugger() }
                .setCancelable(false)
                .create()
                .show()
    }

    /************************** Activity Lifecycle  ************************/
    override fun onActivityLifecycle(event: ActivityLifecycleEvent) {
        if (event.eventType == ActivityLifecycleEvent.EVENT_TYPE.ON_RESUMED) {
            tipView?.show(event.activity)
        } else if (event.eventType == ActivityLifecycleEvent.EVENT_TYPE.ON_PAUSED) {
            tipView?.remove()
        }
    }

    companion object {
        private const val TAG = "DebuggerService"
        private const val WS_URL = "wsUrl"
        const val SOCKET_STATE_INITIALIZE = 0
        const val SOCKET_STATE_READIED = 1
        const val SOCKET_STATE_CLOSED = 2
    }
}