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
package com.growingio.android.debugger;

import android.text.TextUtils;
import android.util.Log;

import com.growingio.android.sdk.track.log.Logger;
import com.growingio.android.sdk.track.utils.ThreadUtils;
import com.growingio.android.sdk.track.webservices.message.QuitMessage;
import com.growingio.android.sdk.track.webservices.message.ReadyMessage;

import org.json.JSONException;
import org.json.JSONObject;

import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

/**
 * <p>
 * handle webSocket message
 *
 * @author cpacm 5/19/21
 */
class WebSocketHandler extends WebSocketListener {

    private final OnWebSocketListener webSocketListener;
    private WebSocket webSocket;
    private static final String TAG = "WebSocketHandler";

    WebSocketHandler(OnWebSocketListener webSocketListener) {
        this.webSocketListener = webSocketListener;
    }

    public void sendMessage(String msg) {
        Log.d("WebSocket", msg);
        if (webSocket != null) {
            webSocket.send(msg);
        }
    }

    @Override
    public void onOpen(WebSocket webSocket, Response response) {
        Logger.d(TAG, "Created webSocket successfully");
        if (webSocket.send(ReadyMessage.createMessage().toJSONObject().toString())) {
            this.webSocket = webSocket;
        } else {
            Logger.e(TAG, "send ready message failed");
            ThreadUtils.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    webSocketListener.onFailed();
                }
            });
        }
    }

    @Override
    public void onMessage(WebSocket webSocket, String text) {
        if (TextUtils.isEmpty(text) || TextUtils.isEmpty(text.trim())) {
            return;
        }
        Logger.d(TAG, "Received message is " + text);

        if (text.contains("had disconnected")) {
            ThreadUtils.runOnUiThread(() -> webSocketListener.onQuited());
            return;
        }
        try {
            JSONObject message = new JSONObject(text);
            String msgType = message.optString("msgType");
            if (ReadyMessage.MSG_TYPE.equals(msgType)) {
                Logger.d(TAG, "Web is ready");
                ThreadUtils.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        webSocketListener.onReady();
                    }
                });
                return;
            } else if (QuitMessage.MSG_TYPE.equals(msgType)) {
                Logger.d(TAG, "Web is quited");
                ThreadUtils.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        webSocketListener.onQuited();
                    }
                });
                return;
            }
        } catch (JSONException e) {
            Logger.e(TAG, e);
        }
        ThreadUtils.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                webSocketListener.onMessage(text);
            }
        });
    }

    @Override
    public void onClosed(WebSocket webSocket, int code, String reason) {
        Logger.e(TAG, "webSocket on onClosed, reason: $reason");
        ThreadUtils.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                webSocketListener.onQuited();
            }
        });
    }

    @Override
    public void onFailure(WebSocket webSocket, Throwable t, Response response) {
        Logger.e(TAG, t, "webSocket on onFailure, reason: ");
        ThreadUtils.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                webSocketListener.onFailed();
            }
        });
    }

    public WebSocket getWebSocket() {
        return webSocket;
    }

    interface OnWebSocketListener {
        void onReady();

        void onQuited();

        void onFailed();

        void onMessage(String msg);
    }
}
