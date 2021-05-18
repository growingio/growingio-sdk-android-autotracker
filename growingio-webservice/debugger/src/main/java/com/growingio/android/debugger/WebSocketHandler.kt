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

import android.text.TextUtils
import android.util.Log
import com.growingio.android.sdk.track.log.Logger
import com.growingio.android.sdk.track.utils.ThreadUtils
import com.growingio.android.sdk.track.webservices.message.QuitMessage
import com.growingio.android.sdk.track.webservices.message.ReadyMessage
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import org.json.JSONException
import org.json.JSONObject

/**
 *
 * handle webSocket message
 *
 * @author cpacm 5/10/21
 */
class WebSocketHandler(private val webSocketListener: OnWebSocketListener) : WebSocketListener() {
    var webSocket: WebSocket? = null
        private set

    fun sendMessage(msg: String) {
        Log.d("WebSocket", msg)
        webSocket?.send(msg)
    }

    override fun onOpen(webSocket: WebSocket, response: Response) {
        Logger.d(TAG, "Created webSocket successfully")
        if (webSocket.send(ReadyMessage.createMessage().toJSONObject().toString())) {
            this.webSocket = webSocket
        } else {
            Logger.e(TAG, "send ready message failed")
            ThreadUtils.runOnUiThread {
                webSocketListener.onFailed()
            }
        }
    }

    override fun onMessage(webSocket: WebSocket, text: String) {
        if (TextUtils.isEmpty(text) || TextUtils.isEmpty(text.trim { it <= ' ' })) {
            return
        }
        Logger.d(TAG, "Received message is $text")
        try {
            val message = JSONObject(text)
            val msgType = message.optString("msgType")
            if (ReadyMessage.MSG_TYPE == msgType) {
                Logger.d(TAG, "Web is ready")
                ThreadUtils.runOnUiThread {
                    webSocketListener.onReady()
                }
                return
            } else if (QuitMessage.MSG_TYPE == msgType) {
                Logger.d(TAG, "Web is quited")
                ThreadUtils.runOnUiThread {
                    webSocketListener.onQuited()
                }
                return
            }
        } catch (e: JSONException) {
            Logger.e(TAG, e)
        }
        ThreadUtils.postOnUiThread { webSocketListener.onMessage(text) }
    }

    override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
        Logger.e(TAG, "webSocket on onClosed, reason: $reason")
        ThreadUtils.runOnUiThread {
            webSocketListener.onQuited()
        }
    }

    override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
        Logger.e(TAG, t, "webSocket on onFailure, reason: ")
        ThreadUtils.runOnUiThread {
            webSocketListener.onFailed()
        }
    }

    interface OnWebSocketListener {
        fun onReady()
        fun onQuited()
        fun onFailed()
        fun onMessage(msg: String)
    }

    companion object {
        private const val TAG = "WebSocketHandler"
    }
}