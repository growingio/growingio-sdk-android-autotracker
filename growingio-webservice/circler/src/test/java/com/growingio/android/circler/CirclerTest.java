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

import android.app.Activity;
import android.app.Application;
import android.graphics.Rect;
import android.os.Looper;
import android.view.View;
import android.view.WindowManager;

import androidx.test.core.app.ApplicationProvider;

import com.google.common.truth.Truth;
import com.growingio.android.circler.screenshot.CircleScreenshot;
import com.growingio.android.circler.screenshot.ScreenshotProvider;
import com.growingio.android.sdk.TrackerContext;
import com.growingio.android.sdk.track.async.Callback;
import com.growingio.android.sdk.track.modelloader.ModelLoader;
import com.growingio.android.sdk.track.modelloader.TrackerRegistry;
import com.growingio.android.sdk.track.providers.ActivityStateProvider;
import com.growingio.android.sdk.track.utils.ThreadUtils;
import com.growingio.android.sdk.track.view.DecorView;
import com.growingio.android.sdk.track.webservices.Circler;
import com.growingio.android.sdk.track.webservices.WebService;
import com.growingio.android.sdk.track.webservices.message.QuitMessage;
import com.growingio.android.sdk.track.webservices.message.ReadyMessage;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
public class CirclerTest implements WebSocketHandler.OnWebSocketListener {
    private WebSocketHandler webSocketHandler;
    private OkHttpClient client;
    private MockWebServer mockWebServer;
    private final Application context = ApplicationProvider.getApplicationContext();

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
                                    sendMessage(webSocket, ReadyMessage.MSG_TYPE);
                                    break;
                                case QuitMessage.MSG_TYPE:
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
        CirclerLibraryGioModule module = new CirclerLibraryGioModule();
        TrackerRegistry registry = new TrackerRegistry();
        module.registerComponents(context, registry);
        ModelLoader<Circler, WebService> dataLoader = registry.getModelLoader(Circler.class, WebService.class);

        HashMap<String, String> param = new HashMap<>();
        param.put("wsUrl", getWsUrl());
        dataLoader.buildLoadData(new Circler(param)).fetcher.executeData();
    }

    @Test
    public void circlerScreenShotTest() {
        ScreenshotProvider.get().sendScreenshotRefreshed("this test base64", 100);
        ScreenshotProvider.get().registerScreenshotRefreshedListener(new ScreenshotProvider.OnScreenshotRefreshedListener() {
            @Override
            public void onScreenshotRefreshed(CircleScreenshot screenshot) {
                System.out.println(screenshot.toJSONObject());
                Truth.assertThat(screenshot.toJSONObject().toString()).isEqualTo(
                        "{\"screenWidth\":320,\"screenHeight\":470,\"scale\":100,\"screenshot\":\"this is test base64\",\"msgType\":\"refreshScreenshot\",\"snapshotKey\":0,\"elements\":[{\"xpath\":\"\\/MainWindow\\/DecorView\\/ActionBarOverlayLayout[0]\\/FrameLayout[0]\\/LinearLayout[0]\\/TextView[0]\",\"left\":0,\"top\":0,\"width\":0,\"height\":0,\"nodeType\":\"TEXT\",\"content\":\"this is cpacm\",\"page\":\"\\/RobolectricActivity\",\"zLevel\":0}],\"pages\":[{\"path\":\"\\/RobolectricActivity\",\"left\":0,\"top\":0,\"width\":0,\"height\":0,\"isIgnored\":false}]}"
                );
            }
        });

        new CircleScreenshot.Builder()
                .setScale(100)
                .setScreenshot("this is test base64")
                .setSnapshotKey(0)
                .build(getAllWindowDecorViews(), new Callback<CircleScreenshot>() {
                    @Override
                    public void onSuccess(CircleScreenshot result) {
                        ScreenshotProvider.get().sendScreenshot(result);
                    }

                    @Override
                    public void onFailed() {

                    }
                });
    }

    public List<DecorView> getAllWindowDecorViews() {
        Activity activity = Robolectric.buildActivity(RobolectricActivity.class).create().resume().get();
        ActivityStateProvider.get().onActivityCreated(activity, null);
        ActivityStateProvider.get().onActivityResumed(activity);
        View view = activity.getWindow().getDecorView();

        List<DecorView> decorViews = new ArrayList<>();
        int[] location = new int[2];
        view.getLocationOnScreen(location);
        int x = location[0];
        int y = location[1];

        Rect area = new Rect(x, y, x + view.getWidth(), y + view.getHeight());
        WindowManager.LayoutParams wlp = new WindowManager.LayoutParams();
        decorViews.add(new DecorView(view, area, wlp));

        return decorViews;
    }

}
