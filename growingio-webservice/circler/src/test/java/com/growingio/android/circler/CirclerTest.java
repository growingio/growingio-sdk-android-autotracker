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

import android.app.Application;
import android.os.Looper;

import androidx.test.core.app.ApplicationProvider;

import com.google.common.truth.Truth;
import com.google.common.util.concurrent.Uninterruptibles;
import com.growingio.android.circler.shadow.ShadowThreadUtils;
import com.growingio.android.sdk.Tracker;
import com.growingio.android.sdk.TrackerContext;
import com.growingio.android.sdk.track.modelloader.DataFetcher;
import com.growingio.android.sdk.track.modelloader.ModelLoader;
import com.growingio.android.sdk.track.middleware.webservice.Circler;
import com.growingio.android.sdk.track.middleware.webservice.WebService;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.HashMap;
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
public class CirclerTest implements WebSocketHandler.OnWebSocketListener {
    private WebSocketHandler webSocketHandler;
    private OkHttpClient client;
    private MockWebServer mockWebServer;
    private final Application application = ApplicationProvider.getApplicationContext();

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
        CirclerLibraryGioModule dModule = new CirclerLibraryGioModule();
        tracker.registerComponent(dModule);
        context = tracker.getContext();
        screenshotProvider = context.getProvider(ScreenshotProvider.class);
        webSocketHandler = new WebSocketHandler(this,screenshotProvider);
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
                                    sendMessage(webSocket,ScreenshotProvider.MSG_READY_TYPE);
                                    break;
                                case ScreenshotProvider.MSG_QUIT_TYPE:
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
        webSocketHandler.sendMessage("Circler Test");
        sendMessage(webSocketHandler.getWebSocket(), "quit");
        sendMessage(webSocketHandler.getWebSocket(), "Circler Test");
    }

    @Override
    public void onQuited() {
        System.out.println("[client]:onQuited");
    }

    @Override
    public void onFailed() {
        System.out.println("[client]:onFailed");
    }

    @Override
    public void onMessage(String msg) {
        Truth.assertThat(msg).isEqualTo("error data");
    }

    @Test
    public void circlerModelTest() {
        ModelLoader<Circler, WebService> dataLoader = context.getRegistry().getModelLoader(Circler.class, WebService.class);

        HashMap<String, String> param = new HashMap<>();
        param.put("wsUrl", getWsUrl());
        DataFetcher<WebService> dataFetcher = dataLoader.buildLoadData(new Circler(param)).fetcher;
        Truth.assertThat(dataFetcher.getDataClass()).isAssignableTo(WebService.class);
        dataFetcher.executeData();

        Uninterruptibles.sleepUninterruptibly(1, TimeUnit.SECONDS);
    }

}
