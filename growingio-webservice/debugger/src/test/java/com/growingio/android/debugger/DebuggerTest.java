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

import android.app.Application;
import android.os.Looper;

import androidx.test.core.app.ApplicationProvider;

import com.google.common.truth.Truth;
import com.google.common.util.concurrent.Uninterruptibles;
import com.growingio.android.sdk.TrackerContext;
import com.growingio.android.sdk.track.TrackMainThread;
import com.growingio.android.sdk.track.events.CustomEvent;
import com.growingio.android.sdk.track.modelloader.ModelLoader;
import com.growingio.android.sdk.track.modelloader.TrackerRegistry;
import com.growingio.android.sdk.track.utils.ThreadUtils;
import com.growingio.android.sdk.track.webservices.Debugger;
import com.growingio.android.sdk.track.webservices.WebService;
import com.growingio.android.sdk.track.webservices.message.QuitMessage;
import com.growingio.android.sdk.track.webservices.message.ReadyMessage;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
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


@Config(manifest = Config.NONE)
@RunWith(RobolectricTestRunner.class)
public class DebuggerTest implements WebSocketHandler.OnWebSocketListener {

    private WebSocketHandler webSocketHandler;
    private OkHttpClient client;
    private MockWebServer mockWebServer;
    private final Application context = ApplicationProvider.getApplicationContext();
    private int round = 0;

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
        webSocketHandler = new WebSocketHandler(this);
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
                                case ReadyMessage.MSG_TYPE:
                                    round = 1;
                                    sendMessage(webSocket, ReadyMessage.MSG_TYPE);
                                    break;
                                case DebuggerEventWrapper.SERVICE_LOGGER_OPEN:
                                    round = 2;
                                    sendMessage(webSocket, DebuggerEventWrapper.SERVICE_LOGGER_OPEN);
                                    break;
                                case DebuggerEventWrapper.SERVICE_LOGGER_CLOSE:
                                    round = 3;
                                    sendMessage(webSocket, DebuggerEventWrapper.SERVICE_LOGGER_CLOSE);
                                    break;
                                case DebuggerEventWrapper.SERVICE_DEBUGGER_TYPE:
                                    round = 4;
                                    sendMessage(webSocket, DebuggerEventWrapper.SERVICE_DEBUGGER_TYPE);
                                    break;
                                case QuitMessage.MSG_TYPE:
                                    round = 5;
                                    sendMessage(webSocket, QuitMessage.MSG_TYPE);
                                    break;
                                default:
                                    webSocket.send("error data");
                                    webSocket.cancel();
                                    //webSocket.close(1000, "close");
                            }
                        } catch (JSONException ignored) {
                        }
                    }
                });
            }
        });
        TrackerContext.init(context);
        client = new OkHttpClient.Builder()
                .connectTimeout(5, TimeUnit.SECONDS)
                .readTimeout(5, TimeUnit.SECONDS)
                .addInterceptor(chain -> {
                    // set http thread looper
                    try {
                        Looper.prepare();
                        System.out.println("[intercept]:" + Looper.myLooper());
                        ThreadUtils.setUiThread(Looper.myLooper());
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
    }


    @Override
    public void onReady() {
        System.out.println("[client]:onReady");
        Truth.assertThat(round).isEqualTo(1);
        sendMessage(webSocketHandler.getWebSocket(), DebuggerEventWrapper.SERVICE_LOGGER_OPEN);
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
            if (msgType.equals(DebuggerEventWrapper.SERVICE_LOGGER_OPEN)) {
                Truth.assertThat(round).isEqualTo(2);
                sendMessage(webSocketHandler.getWebSocket(), DebuggerEventWrapper.SERVICE_LOGGER_CLOSE);
            } else if (msgType.equals(DebuggerEventWrapper.SERVICE_LOGGER_CLOSE)) {
                Truth.assertThat(round).isEqualTo(3);
                sendMessage(webSocketHandler.getWebSocket(), DebuggerEventWrapper.SERVICE_DEBUGGER_TYPE);
            } else if (msgType.equals(DebuggerEventWrapper.SERVICE_DEBUGGER_TYPE)) {
                Truth.assertThat(round).isEqualTo(4);
                sendMessage(webSocketHandler.getWebSocket(), QuitMessage.MSG_TYPE);
            }
        } catch (JSONException e) {
            System.out.println("[client]:error json data");
        }
    }


    @Test
    public void debuggerModelTest() {
        DebuggerLibraryGioModule module = new DebuggerLibraryGioModule();
        TrackerRegistry registry = new TrackerRegistry();
        module.registerComponents(context, registry);
        ModelLoader<Debugger, WebService> dataLoader = registry.getModelLoader(Debugger.class, WebService.class);

        HashMap<String, String> param = new HashMap<>();
        param.put("wsUrl", getWsUrl());
        dataLoader.buildLoadData(new Debugger(param)).fetcher.executeData();
    }

    @Test
    public void debuggerScreenShotTest() {
        ScreenshotProvider.get().registerScreenshotRefreshedListener(new ScreenshotProvider.OnScreenshotRefreshedListener() {
            @Override
            public void onScreenshotRefreshed(DebuggerScreenshot screenshot) {
                Truth.assertThat(screenshot.toJSONObject().toString()).isEqualTo(
                        "{\"screenWidth\":320,\"screenHeight\":470,\"scale\":100,\"screenshot\":\"this test base64\",\"msgType\":\"refreshScreenshot\",\"snapshotKey\":0}");
            }
        });
        ScreenshotProvider.get().sendScreenshotRefreshed("this test base64", 100);
    }

    @Test
    public void debuggerEventTest() {
        DebuggerEventWrapper.get().ready();
        DebuggerEventWrapper.get().registerDebuggerEventListener(new DebuggerEventWrapper.OnDebuggerEventListener() {
            @Override
            public void onDebuggerMessage(String message) {
                try {
                    System.out.println(message);
                    JSONObject jsonObject = new JSONObject(message);
                    if (Objects.equals(jsonObject.opt("msgType"), DebuggerEventWrapper.SERVICE_DEBUGGER_TYPE)) {
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
}
