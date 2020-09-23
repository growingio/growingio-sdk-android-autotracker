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

package com.growingio.android.sdk.track.webservices;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.growingio.android.sdk.track.log.Logger;
import com.growingio.android.sdk.track.webservices.message.QuitMessage;
import com.growingio.android.sdk.track.webservices.message.ReadyMessage;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;

public abstract class BaseWebSocketService {
    private static final String TAG = "BaseWebSocketService";

    private final String mWsUrl;
    private WebSocket mWebSocket;

    public BaseWebSocketService(String wsUrl) {
        mWsUrl = wsUrl;
    }

    public void start() {
        OkHttpClient httpClient = new OkHttpClient.Builder()
                .readTimeout(5, TimeUnit.SECONDS)
                .writeTimeout(5, TimeUnit.SECONDS)
                .connectTimeout(5, TimeUnit.SECONDS)
                .build();

        Request request = new Request.Builder().url(mWsUrl).build();
        WebSocketListener socketListener = new WebSocketListener();

        httpClient.newWebSocket(request, socketListener);
        httpClient.dispatcher().executorService().shutdown();
    }

    public abstract String getServiceType();

    protected void onReady() {

    }

    protected void onFailed() {

    }

    protected void onQuited() {

    }

    protected void onMessage(String text) {

    }

    protected void sendMessage(String msg) {
        if (mWebSocket != null) {
            mWebSocket.send(msg);
        }
    }


    private final class WebSocketListener extends okhttp3.WebSocketListener {

        @Override
        public void onOpen(@NonNull WebSocket webSocket, @NonNull Response response) {
            Logger.d(TAG, "Created webSocket successfully");
            if (webSocket.send(ReadyMessage.createMessage().toJSONObject().toString())) {
                Logger.d(TAG, "send ready message successfully");
                mWebSocket = webSocket;
            } else {
                Logger.e(TAG, "send ready message failed");
                onFailed();
            }
        }

        @Override
        public void onMessage(@NonNull WebSocket webSocket, String text) {
            Logger.d(TAG, "Received message is " + text);
            if (TextUtils.isEmpty(text) || TextUtils.isEmpty(text.trim())) {
                Logger.e(TAG, "onMessage: message is NULL");
                return;
            }

            try {
                JSONObject message = new JSONObject(text);
                String msgType = message.optString("msgType");
                if (ReadyMessage.MSG_TYPE.equals(msgType)) {
                    Logger.d(TAG, "Web is ready");
                    onReady();
                    return;
                } else if (QuitMessage.MSG_TYPE.equals(msgType)) {
                    Logger.d(TAG, "Web is quited");
                    onQuited();
                    return;
                }
            } catch (JSONException e) {
                Logger.e(TAG, e);
            }
            BaseWebSocketService.this.onMessage(text);
        }

        @Override
        public void onClosed(WebSocket webSocket, int code, String reason) {
            Logger.e(TAG, "webSocket on onClosed, reason: " + reason);
            onQuited();
        }

        @Override
        public void onFailure(WebSocket webSocket, Throwable t, Response response) {
            Logger.e(TAG, t, "webSocket on onFailure, reason: ");
            onFailed();
        }
    }
}
