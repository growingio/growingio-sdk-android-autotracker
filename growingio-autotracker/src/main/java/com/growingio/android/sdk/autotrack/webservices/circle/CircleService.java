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

package com.growingio.android.sdk.autotrack.webservices.circle;

import android.os.Process;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.growingio.android.sdk.autotrack.webservices.ScreenshotProvider;
import com.growingio.android.sdk.autotrack.webservices.circle.entity.CircleScreenshot;
import com.growingio.android.sdk.autotrack.webservices.message.QuitMessage;
import com.growingio.android.sdk.autotrack.webservices.message.ReadyMessage;
import com.growingio.android.sdk.track.async.Callback;
import com.growingio.android.sdk.track.async.Disposable;
import com.growingio.android.sdk.track.utils.LogUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

public class CircleService implements ScreenshotProvider.OnScreenshotRefreshedListener {
    private static final String TAG = "CircleService";

    private WebSocket mWebSocket;
    private long mSnapshotKey = 0;
    private Disposable mCircleScreenshotDisposable;

    public CircleService(String url) {
        OkHttpClient httpClient = new OkHttpClient.Builder()
                .readTimeout(5, TimeUnit.SECONDS)
                .writeTimeout(5, TimeUnit.SECONDS)
                .connectTimeout(5, TimeUnit.SECONDS)
                .build();

        Request request = new Request.Builder().url(url).build();
        CircleWebSocketListener socketListener = new CircleWebSocketListener();

        httpClient.newWebSocket(request, socketListener);
        httpClient.dispatcher().executorService().shutdown();
    }

    private void registerScreenshotRefreshedListener() {
        ScreenshotProvider.ScreenshotPolicy.get().registerScreenshotRefreshedListener(this);
    }

    public void destroy() {
        ScreenshotProvider.ScreenshotPolicy.get().unregisterScreenshotRefreshedListener(this);
    }

    @Override
    public void onScreenshotRefreshed(String screenshotBase64, float scale) {
        if (mWebSocket != null) {
            if (mCircleScreenshotDisposable != null) {
                mCircleScreenshotDisposable.dispose();
            }

            mCircleScreenshotDisposable = new CircleScreenshot.Builder()
                    .setScale(scale)
                    .setScreenshot(screenshotBase64)
                    .setSnapshotKey(Process.myPid() + "-" + mSnapshotKey++)
                    .build(new Callback<CircleScreenshot>() {
                        @Override
                        public void onSuccess(CircleScreenshot result) {
                            LogUtil.d(TAG, "Create circle screenshot successfully");
                            mWebSocket.send(result.toJSONObject().toString());
                        }

                        @Override
                        public void onFailed() {
                            LogUtil.e(TAG, "Create circle screenshot failed");
                        }
                    });
        }
    }

    private final class CircleWebSocketListener extends WebSocketListener {

        @Override
        public void onOpen(@NonNull WebSocket webSocket, @NonNull Response response) {
            LogUtil.d(TAG, "Created webSocket successfully");
            mWebSocket = webSocket;
            if (mWebSocket.send(ReadyMessage.createMessage().toJSONObject().toString())) {
                LogUtil.d(TAG, "send ready message successfully");
            } else {
                LogUtil.e(TAG, "send ready message failed");
            }
            registerScreenshotRefreshedListener();
        }

        @Override
        public void onMessage(@NonNull WebSocket webSocket, String text) {
            LogUtil.d(TAG, "Received message is " + text);
            if (TextUtils.isEmpty(text) || TextUtils.isEmpty(text.trim())) {
                LogUtil.e(TAG, "onMessage: message is NULL");
                return;
            }

            try {
                JSONObject message = new JSONObject(text);
                String msgType = message.optString("msgType");
                if (ReadyMessage.MSG_TYPE.equals(msgType)) {
                    LogUtil.d(TAG, "Web is ready");
                    registerScreenshotRefreshedListener();
                } else if (QuitMessage.MSG_TYPE.equals(msgType)) {
                    // TODO: 2020/7/30 quitMessage
                }
            } catch (JSONException e) {
                LogUtil.e(TAG, e);
            }
        }

        @Override
        public void onClosed(WebSocket webSocket, int code, String reason) {
            destroy();
        }

        @Override
        public void onFailure(WebSocket webSocket, Throwable t, Response response) {
            destroy();
        }
    }
}
