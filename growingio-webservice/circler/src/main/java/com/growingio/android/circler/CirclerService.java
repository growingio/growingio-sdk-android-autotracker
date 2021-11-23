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
package com.growingio.android.circler;


import com.growingio.android.circler.screenshot.CircleScreenshot;
import com.growingio.android.circler.screenshot.GrowingFlutterPlugin;
import com.growingio.android.circler.screenshot.ScreenshotProvider;
import com.growingio.android.sdk.TrackerContext;
import com.growingio.android.sdk.track.listener.IActivityLifecycle;
import com.growingio.android.sdk.track.listener.event.ActivityLifecycleEvent;
import com.growingio.android.sdk.track.log.Logger;
import com.growingio.android.sdk.track.modelloader.DataFetcher;
import com.growingio.android.sdk.track.providers.ActivityStateProvider;
import com.growingio.android.sdk.track.utils.ThreadUtils;
import com.growingio.android.sdk.track.webservices.WebService;
import com.growingio.android.sdk.track.webservices.message.ClientInfoMessage;
import com.growingio.android.sdk.track.webservices.message.QuitMessage;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import okhttp3.OkHttpClient;
import okhttp3.Request;

/**
 * <p>
 *
 * @author cpacm 5/19/21
 */
public class CirclerService implements DataFetcher<WebService>, IActivityLifecycle,
        ScreenshotProvider.OnScreenshotRefreshedListener,
        WebSocketHandler.OnWebSocketListener {

    private static final String TAG = "CirclerService";
    private static final String WS_URL = "wsUrl";
    private static final int SOCKET_STATE_INITIALIZE = 0;
    private static final int SOCKET_STATE_READIED = 1;
    private static final int SOCKET_STATE_CLOSED = 2;

    private final OkHttpClient client;
    private final ThreadSafeTipView safeTipView;
    private final WebSocketHandler webSocketHandler;
    private Map<String, String> params;
    protected final AtomicInteger socketState = new AtomicInteger(SOCKET_STATE_INITIALIZE);


    void init(Map<String, String> params) {
        this.params = params;
    }


    public CirclerService(OkHttpClient client) {
        this.client = client;
        ActivityStateProvider.get().registerActivityLifecycleListener(this);
        safeTipView = new ThreadSafeTipView(TrackerContext.get().getApplicationContext());
        webSocketHandler = new WebSocketHandler(this);
    }

    @Override
    public void loadData(DataCallback<? super WebService> callback) {
        // restart, and it doesn't mean if you set different wsurl after websocket started.
        if (socketState.get() == SOCKET_STATE_READIED) {
            if (callback != null) {
                callback.onDataReady(new WebService());
            }
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
        Request request = new Request.Builder().url(wsUrl).build();
        client.newWebSocket(request, webSocketHandler);

        ActivityStateProvider.get().registerActivityLifecycleListener(this);
        safeTipView.enableShow();

        ThreadUtils.postOnUiThreadDelayed(
                new Runnable() {
                    @Override
                    public void run() {
                        if (socketState.get() < SOCKET_STATE_READIED) {
                            Logger.e(TAG, "start WebSocketService timeout");
                            onFailed();
                        }
                    }
                }, 10000);
    }

    protected void sendMessage(String msg) {
        webSocketHandler.sendMessage(msg);
    }

    @Override
    public WebService executeData() {
        loadData(null);
        return new WebService();
    }

    @Override
    public void cleanup() {
        cancel();
    }

    @Override
    public void cancel() {
        socketState.set(SOCKET_STATE_CLOSED);
        if (webSocketHandler.getWebSocket() != null) {
            webSocketHandler.getWebSocket().cancel();
        }
        ScreenshotProvider.get().unregisterScreenshotRefreshedListener(this);
        safeTipView.dismiss();
        ActivityStateProvider.get().unregisterActivityLifecycleListener(this);
    }

    @Override
    public Class<WebService> getDataClass() {
        return WebService.class;
    }


    /************************** WebSocket Handler  ************************/
    @Override
    public void onReady() {
        sendMessage(ClientInfoMessage.createMessage().toJSONObject().toString());
        socketState.set(SOCKET_STATE_READIED);
        ScreenshotProvider.get().registerScreenshotRefreshedListener(this);
        safeTipView.onReady(new ThreadSafeTipView.OnExitListener() {
            @Override
            public void onExitDebugger() {
                exitCircler();
            }
        });
        GrowingFlutterPlugin.getInstance().onNativeCircleStart();
    }

    @Override
    public void onMessage(String msg) {
    }

    protected void exitCircler() {
        sendMessage(new QuitMessage().toJSONObject().toString());
        cleanup();
    }

    @Override
    public void onFailed() {
        if (socketState.get() >= SOCKET_STATE_CLOSED) {
            return;
        }
        socketState.set(SOCKET_STATE_CLOSED);
        safeTipView.setErrorMessage(R.string.growing_circler_connected_to_web_failed);
        Logger.e(TAG, "Start CirclerService Failed");
        safeTipView.showQuitedDialog(this::exitCircler);
        GrowingFlutterPlugin.getInstance().onNativeCircleStop();
    }

    @Override
    public void onQuited() {
        if (socketState.get() >= SOCKET_STATE_CLOSED) {
            return;
        }
        cancel();
        socketState.set(SOCKET_STATE_CLOSED);
        safeTipView.showQuitedDialog(this::exitCircler);
        GrowingFlutterPlugin.getInstance().onNativeCircleStop();
    }

    /************************** Screenshot ************************/

    @Override
    public void onScreenshotRefreshed(CircleScreenshot screenshot) {
        if (screenshot != null) {
            sendMessage(screenshot.toJSONObject().toString());
        }
    }


    /************************** Activity Lifecycle  ************************/

    @Override
    public void onActivityLifecycle(ActivityLifecycleEvent event) {
        if (event.eventType == ActivityLifecycleEvent.EVENT_TYPE.ON_RESUMED) {
            safeTipView.show(event.getActivity());
        } else if (event.eventType == ActivityLifecycleEvent.EVENT_TYPE.ON_PAUSED) {
            safeTipView.removeOnly();
        }
    }
}
