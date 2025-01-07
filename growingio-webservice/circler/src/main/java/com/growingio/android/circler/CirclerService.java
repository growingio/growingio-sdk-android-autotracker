/*
 * Copyright (C) 2023 Beijing Yishu Technology Co., Ltd.
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
package com.growingio.android.circler;


import com.growingio.android.sdk.TrackerContext;
import com.growingio.android.sdk.track.TrackMainThread;
import com.growingio.android.sdk.track.log.Logger;
import com.growingio.android.sdk.track.modelloader.LoadDataFetcher;
import com.growingio.android.sdk.track.middleware.webservice.Circler;
import com.growingio.android.sdk.track.middleware.webservice.WebService;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import okhttp3.OkHttpClient;
import okhttp3.Request;

/**
 * <p>
 *
 * @author cpacm 5/19/21
 */
public class CirclerService implements LoadDataFetcher<WebService>,
        ScreenshotProvider.OnScreenshotRefreshedListener,
        WebSocketHandler.OnWebSocketListener {

    private static final String TAG = "CirclerService";
    private static final String WS_URL = "wsUrl";
    private static final int SOCKET_STATE_INITIALIZE = 0;
    private static final int SOCKET_STATE_READIED = 1;
    private static final int SOCKET_STATE_CLOSED = 2;

    private final OkHttpClient client;
    private final WebSocketHandler webSocketHandler;
    private Map<String, String> params;
    private int circleDataType;
    protected final AtomicInteger socketState = new AtomicInteger(SOCKET_STATE_CLOSED);

    private final ScreenshotProvider screenshotProvider;

    void sendCircleData(Circler circler) {
        circleDataType = circler.circleDataType;
        if (circleDataType == Circler.CIRCLE_INIT) {
            this.params = circler.getParams();
        } else if (circleDataType == Circler.CIRCLE_DATA) {
            screenshotProvider.generateCircleData(circler.getCirclerData());
        } else if (circleDataType == Circler.CIRCLE_REFRESH) {
            if (socketState.get() == SOCKET_STATE_READIED) {
                screenshotProvider.refreshScreenshot();
            }
        }
    }

    public CirclerService(OkHttpClient client, TrackerContext context) {
        this.client = client;
        screenshotProvider = context.getProvider(ScreenshotProvider.class);
        webSocketHandler = new WebSocketHandler(this, screenshotProvider);
    }

    @Override
    public void loadData(DataCallback<? super WebService> callback) {
        // restart, and it doesn't mean if you set different wsurl after websocket started.
        if (socketState.get() == SOCKET_STATE_READIED) {
            if (callback != null) {
                callback.onDataReady(new WebService(true));
            }
            return;
        }
        if (circleDataType != Circler.CIRCLE_INIT) {
            // send data by screenshotProvider directly
            return;
        }
        if (webSocketHandler.getWebSocket() != null) {
            webSocketHandler.getWebSocket().cancel();
        }
        String wsUrl = params.get(WS_URL);
        if (wsUrl == null || wsUrl.isEmpty()) {
            if (callback != null) {
                callback.onLoadFailed(new NullPointerException("wsUrl is NULL, can't start WebSocketService"));
            }
            return;
        }
        socketState.set(SOCKET_STATE_INITIALIZE);
        Request request = new Request.Builder().url(wsUrl).build();
        client.newWebSocket(request, webSocketHandler);

        screenshotProvider.enableTipViewShow();

        TrackMainThread.trackMain().postOnUiThreadDelayed(
                () -> {
                    if (socketState.get() < SOCKET_STATE_READIED) {
                        Logger.e(TAG, "start WebSocketService timeout");
                        onFailed();
                    }
                }, 10000);
    }

    protected void sendMessage(String msg) {
        webSocketHandler.sendMessage(msg);
    }

    @Override
    public WebService executeData() {
        loadData(null);
        boolean isClosed = socketState.get() == SOCKET_STATE_CLOSED;
        return new WebService(!isClosed);
    }

    public void cleanup() {
        sendMessage(screenshotProvider.buildQuitMessage());
        cancel();
    }

    public void cancel() {
        socketState.set(SOCKET_STATE_CLOSED);
        if (webSocketHandler.getWebSocket() != null) {
            webSocketHandler.getWebSocket().close(1000, "exit");
        }
        screenshotProvider.disableTipView();
        screenshotProvider.unregisterScreenshotRefreshedListener();
    }

    @Override
    public Class<WebService> getDataClass() {
        return WebService.class;
    }


    /************************** WebSocket Handler  ************************/
    @Override
    public void onReady() {
        sendMessage(screenshotProvider.buildClientInfoMessage());
        socketState.set(SOCKET_STATE_READIED);
        screenshotProvider.registerScreenshotRefreshedListener(this);
        screenshotProvider.readyTipView(this::cleanup);
    }

    @Override
    public void onMessage(String msg) {
    }


    @Override
    public void onFailed() {
        if (socketState.get() >= SOCKET_STATE_CLOSED) {
            return;
        }
        socketState.set(SOCKET_STATE_CLOSED);
        Logger.e(TAG, "Start CirclerService Failed");

        screenshotProvider.setTipViewMessage(R.string.growing_circler_connected_to_web_failed);
        screenshotProvider.showQuitDialog(this::cleanup);
    }

    @Override
    public void onQuited() {
        if (socketState.get() >= SOCKET_STATE_CLOSED) {
            return;
        }
        cancel();
        socketState.set(SOCKET_STATE_CLOSED);
        screenshotProvider.showQuitDialog(this::cleanup);
    }

    /************************** Screenshot ************************/

    @Override
    public void onScreenshotRefreshed(CircleScreenshot screenshot) {
        if (screenshot != null) {
            sendMessage(screenshot.toJSONObject().toString());
        }
    }
}
