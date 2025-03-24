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
package com.growingio.android.debugger;

import android.util.Base64;

import com.growingio.android.sdk.TrackerContext;
import com.growingio.android.sdk.track.TrackMainThread;
import com.growingio.android.sdk.track.log.Logger;
import com.growingio.android.sdk.track.modelloader.LoadDataFetcher;
import com.growingio.android.sdk.track.middleware.webservice.Debugger;
import com.growingio.android.sdk.track.middleware.webservice.WebService;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import okhttp3.OkHttpClient;
import okhttp3.Request;

/**
 * <p>
 *
 * @author cpacm 5/19/21
 */
public class DebuggerService implements LoadDataFetcher<WebService>,
        WebSocketHandler.OnWebSocketListener, ScreenshotProvider.OnScreenshotRefreshedListener {

    private static final String TAG = "DebuggerService";
    private static final String WS_URL = "wsUrl";
    private static final int SOCKET_STATE_INITIALIZE = 0;
    private static final int SOCKET_STATE_READIED = 1;
    private static final int SOCKET_STATE_CLOSED = 2;

    private final OkHttpClient client;
    private final WebSocketHandler webSocketHandler;
    private Map<String, String> params;

    private int debuggerDataType;
    protected final AtomicInteger socketState = new AtomicInteger(SOCKET_STATE_INITIALIZE);

    private final ScreenshotProvider screenshotProvider;
    private final DebuggerEventProvider debuggerEventProvider;

    void sendDebuggerData(Debugger debugger) {
        debuggerDataType = debugger.debuggerDataType;
        if (debuggerDataType == Debugger.DEBUGGER_INIT) {
            this.params = debugger.getParams();
        } else if (debuggerDataType == Debugger.DEBUGGER_SCREENSHOT) {
            byte[] screenshot = debugger.getScreenshot();
            if (screenshot != null) {
                String screenshotBase64 = "data:image/jpeg;base64," + Base64.encodeToString(screenshot, Base64.DEFAULT);
                screenshotProvider.generateDebuggerData(screenshotBase64);
            }
        } else if (debuggerDataType == Debugger.DEBUGGER_REFRESH) {
            if (socketState.get() == SOCKET_STATE_READIED) {
                screenshotProvider.refreshScreenshot();
            }
        }
    }

    public DebuggerService(OkHttpClient client, TrackerContext context) {
        this.client = client;
        screenshotProvider = context.getProvider(ScreenshotProvider.class);
        debuggerEventProvider = context.getProvider(DebuggerEventProvider.class);
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
        if (debuggerDataType != Debugger.DEBUGGER_INIT) {
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
        //client.dispatcher().executorService().shutdown()

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
        boolean isRunning = socketState.get() == SOCKET_STATE_READIED;
        return new WebService(isRunning);
    }

    public void cleanup() {
        sendMessage(screenshotProvider.buildQuitMessage());
        cancel();
    }

    public void cancel() {
        Logger.e(TAG, "end DebuggerService");
        socketState.set(SOCKET_STATE_CLOSED);
        if (webSocketHandler.getWebSocket() != null) {
            webSocketHandler.getWebSocket().close(1000, "exit");
        }
        debuggerEventProvider.end();
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
        debuggerEventProvider.registerDebuggerEventListener(this::sendMessage);
        socketState.set(SOCKET_STATE_READIED);
        debuggerEventProvider.ready();
        screenshotProvider.registerScreenshotRefreshedListener(this);
        screenshotProvider.readyTipView(this::cleanup);
    }

    @Override
    public void onMessage(String msg) {
        try {
            JSONObject message = new JSONObject(msg);
            String msgType = message.optString("msgType");
            if (DebuggerEventProvider.SERVICE_LOGGER_OPEN.equals(msgType)) {
                debuggerEventProvider.openLogger();
            } else if (DebuggerEventProvider.SERVICE_LOGGER_CLOSE.equals(msgType)) {
                debuggerEventProvider.closeLogger();
            }
        } catch (JSONException e) {
            Logger.e(TAG, e);
        }
    }

    protected void exitDebugger() {
        cleanup();
    }

    @Override
    public void onFailed() {
        if (socketState.get() >= SOCKET_STATE_CLOSED) {
            return;
        }
        socketState.set(SOCKET_STATE_CLOSED);

        Logger.e(TAG, "Start DebuggerService Failed");
        screenshotProvider.setTipViewMessage(R.string.growing_debugger_connected_to_web_failed);
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

    /************************** Activity Lifecycle  ************************/
    @Override
    public void onScreenshotRefreshed(DebuggerScreenshot screenshot) {
        if (screenshot != null) {
            sendMessage(screenshot.toJSONObject().toString());
        }
    }
}
