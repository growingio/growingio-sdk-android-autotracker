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

import static com.google.common.truth.Truth.assertThat;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Application;
import android.os.Looper;

import androidx.test.core.app.ApplicationProvider;

import com.google.common.truth.Truth;
import com.google.common.util.concurrent.Uninterruptibles;
import com.growingio.android.debugger.shadow.ShadowThreadUtils;
import com.growingio.android.sdk.Tracker;
import com.growingio.android.sdk.TrackerContext;
import com.growingio.android.sdk.track.TrackMainThread;
import com.growingio.android.sdk.track.events.CustomEvent;
import com.growingio.android.sdk.track.log.Logger;
import com.growingio.android.sdk.track.modelloader.DataFetcher;
import com.growingio.android.sdk.track.modelloader.ModelLoader;
import com.growingio.android.sdk.track.middleware.webservice.Debugger;
import com.growingio.android.sdk.track.middleware.webservice.WebService;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.HashMap;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;


@Config(manifest = Config.NONE, shadows = {ShadowThreadUtils.class})
@RunWith(RobolectricTestRunner.class)
public class DebuggerTest implements WebSocketHandler.OnWebSocketListener {

    private WebSocketHandler webSocketHandler;
    private OkHttpClient client;
    private MockWebServer mockWebServer;
    private final Application application = ApplicationProvider.getApplicationContext();
    private int round = 0;

    private TrackerContext context;
    private ScreenshotProvider screenshotProvider;

    protected String getWsUrl() {
        String hostName = mockWebServer.getHostName();
        int port = mockWebServer.getPort();
        return "ws://" + hostName + ":" + port + "/";
    }

    private void sendMessage(WebSocket webSocket, String message) {
        try {
            webSocket.send(new JSONObject().put("msgType", message).toString());
        } catch (JSONException exception) {
            exception.printStackTrace();
        }
    }

    @Before
    public void setup() {
        Tracker tracker = new Tracker(application);
        DebuggerLibraryGioModule dModule = new DebuggerLibraryGioModule();
        tracker.registerComponent(dModule);
        context = tracker.getContext();
        screenshotProvider = context.getProvider(ScreenshotProvider.class);
        webSocketHandler = new WebSocketHandler(this, screenshotProvider);
        mockWebServer = new MockWebServer();
        mockWebServer.setDispatcher(new Dispatcher() {
            @Override
            public MockResponse dispatch(RecordedRequest request) {
                return new MockResponse().withWebSocketUpgrade(new WebSocketListener() {
                    @Override
                    public void onOpen(WebSocket webSocket, Response response) {
                        System.out.println("[server]:onOpen");
                    }

                    @Override
                    public void onMessage(WebSocket webSocket, String text) {
                        System.out.println("[server]:" + text);
                        try {
                            JSONObject message = new JSONObject(text);
                            String msgType = message.optString("msgType");
                            switch (msgType) {
                                case ScreenshotProvider.MSG_READY_TYPE:
                                    round = 1;
                                    sendMessage(webSocket, ScreenshotProvider.MSG_READY_TYPE);
                                    break;
                                case DebuggerEventProvider.SERVICE_LOGGER_OPEN:
                                    round = 2;
                                    sendMessage(webSocket, DebuggerEventProvider.SERVICE_LOGGER_OPEN);
                                    break;
                                case DebuggerEventProvider.SERVICE_LOGGER_CLOSE:
                                    round = 3;
                                    sendMessage(webSocket, DebuggerEventProvider.SERVICE_LOGGER_CLOSE);
                                    break;
                                case DebuggerEventProvider.SERVICE_DEBUGGER_TYPE:
                                    round = 4;
                                    sendMessage(webSocket, DebuggerEventProvider.SERVICE_DEBUGGER_TYPE);
                                    break;
                                case ScreenshotProvider.MSG_QUIT_TYPE:
                                    round = 5;
                                    sendMessage(webSocket, ScreenshotProvider.MSG_QUIT_TYPE);
                                    break;
                                default:
                                    webSocket.send("error data");
                                    webSocket.close(1000, "close");
                            }
                        } catch (JSONException ignored) {
                        }
                    }
                });
            }
        });
        client = new OkHttpClient.Builder()
                .connectTimeout(5, TimeUnit.SECONDS)
                .readTimeout(5, TimeUnit.SECONDS)
                .addInterceptor(chain -> {
                    // set http thread looper
                    try {
                        Looper.prepare();
                        System.out.println("[intercept]:" + Looper.myLooper());
                    } catch (Exception ignored) {
                    }
                    return chain.proceed(chain.request());
                })
                .build();
    }

    @Test
    public void socketHandlerTest() {
        Request request = new Request.Builder().url(getWsUrl()).build();
        client.newWebSocket(request, webSocketHandler);
        Truth.assertThat(getWsUrl()).contains(String.valueOf(request.url().port()));
    }


    @Override
    public void onReady() {
        System.out.println("[client]:onReady");
        Truth.assertThat(round).isEqualTo(1);
        sendMessage(webSocketHandler.getWebSocket(), DebuggerEventProvider.SERVICE_LOGGER_OPEN);
    }

    @Override
    public void onQuited() {
        System.out.println("[client]:onQuited");
        Truth.assertThat(round).isEqualTo(5);
        sendMessage(webSocketHandler.getWebSocket(), "close");
    }

    @Override
    public void onFailed() {
        System.out.println("[client]:onFailed");
        webSocketHandler.sendMessage("failed");
    }

    @Override
    public void onMessage(String msg) {
        System.out.println("[client]:" + msg);
        try {
            JSONObject message = new JSONObject(msg);
            String msgType = message.optString("msgType");
            if (msgType.equals(DebuggerEventProvider.SERVICE_LOGGER_OPEN)) {
                Truth.assertThat(round).isEqualTo(2);
                sendMessage(webSocketHandler.getWebSocket(), DebuggerEventProvider.SERVICE_LOGGER_CLOSE);
            } else if (msgType.equals(DebuggerEventProvider.SERVICE_LOGGER_CLOSE)) {
                Truth.assertThat(round).isEqualTo(3);
                sendMessage(webSocketHandler.getWebSocket(), DebuggerEventProvider.SERVICE_DEBUGGER_TYPE);
            } else if (msgType.equals(DebuggerEventProvider.SERVICE_DEBUGGER_TYPE)) {
                Truth.assertThat(round).isEqualTo(4);
                sendMessage(webSocketHandler.getWebSocket(), ScreenshotProvider.MSG_QUIT_TYPE);
            }
        } catch (JSONException e) {
            System.out.println("[client]:error json data");
        }
    }


    @Test
    public void debuggerModelTest() throws JSONException {
        DebuggerLibraryGioModule module = new DebuggerLibraryGioModule();
        module.registerComponents(context);
        ModelLoader<Debugger, WebService> dataLoader = context.getRegistry().getModelLoader(Debugger.class, WebService.class);

        HashMap<String, String> param = new HashMap<>();
        param.put("wsUrl", getWsUrl());
        DataFetcher<WebService> dataFetcher =
                dataLoader.buildLoadData(new Debugger(param)).fetcher;
        Truth.assertThat(dataFetcher.getDataClass()).isAssignableTo(WebService.class);
        dataFetcher.executeData();

        if (dataFetcher instanceof DebuggerService) {
            serviceTest((DebuggerService) dataFetcher);
        }
    }

    public void serviceTest(DebuggerService service) throws JSONException {
        Activity activity = Robolectric.buildActivity(RobolectricActivity.class).create().resume().get();
        service.sendMessage("test");
        service.socketState.getAndSet(1);
        service.onMessage(new JSONObject().put("msgType", DebuggerEventProvider.SERVICE_LOGGER_OPEN).toString());
        service.onFailed();
        service.exitDebugger();
        service.onQuited();
    }

    @Test
    public void debuggerEventTest() {
        DebuggerEventProvider debuggerEventProvider = context.getProvider(DebuggerEventProvider.class);
        debuggerEventProvider.ready();
        debuggerEventProvider.registerDebuggerEventListener(new DebuggerEventProvider.OnDebuggerEventListener() {
            @Override
            public void onDebuggerMessage(String message) {
                try {
                    System.out.println(message);
                    JSONObject jsonObject = new JSONObject(message);
                    if (Objects.equals(jsonObject.opt("msgType"), DebuggerEventProvider.SERVICE_DEBUGGER_TYPE)) {
                        Truth.assertThat(jsonObject.getJSONObject("data").opt("eventName")).isEqualTo("custom");
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        TrackMainThread.trackMain().postEventToTrackMain(new CustomEvent.Builder()
                .setEventName("custom"));

        Uninterruptibles.sleepUninterruptibly(500, TimeUnit.MILLISECONDS);
    }

    @Test
    public void logTest() {
        WsLogger wsLogger = new WsLogger();
        wsLogger.setCallback(new WsLogger.Callback() {
            @SuppressLint("CheckResult")
            @Override
            public void disposeLog(String logMessage) {
                assertThat(logMessage.contains("this is test log"));
            }
        });
        wsLogger.openLog();
        Logger.v("test", "this is test log");
        wsLogger.closeLog();
        wsLogger.printOut();
        wsLogger.setCallback(null);

    }

}
