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
package com.growingio.autotest.autotracker.webservices;

import android.content.Intent;
import android.net.Uri;

import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.action.ViewActions;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.gio.test.R;
import com.gio.test.three.DemoApplication;
import com.gio.test.three.autotrack.activity.ClickTestActivity;
import com.google.common.truth.Truth;
import com.growingio.autotest.TestTrackConfiguration;
import com.growingio.autotest.help.BeforeAppOnCreate;
import com.growingio.autotest.help.DataHelper;
import com.growingio.autotest.help.Uninterruptibles;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.TimeUnit;

import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class DebuggerServiceTest {
    private static final String TAG = "DebuggerServiceTest";

    private MockWebServer mMockWebServer;
    private WebSocket mWebSocket;

    @Before
    public void setup() {
        mMockWebServer = new MockWebServer();
    }

    @BeforeAppOnCreate
    public static void beforeAppOnCreate() {
        DataHelper.deleteEventsDatabase();
        DemoApplication.setConfiguration(TestTrackConfiguration.getTestConfig("growing.bd71d91eb56f5f53"));
    }

    protected String getWsUrl() {
        String hostName = mMockWebServer.getHostName();
        int port = mMockWebServer.getPort();
        return "ws://" + hostName + ":" + port + "/";
    }

    @Test
    public void debuggerServiceTest() throws JSONException {
        MockResponse ready = new MockResponse().withWebSocketUpgrade(new WebSocketListener() {
            @Override
            public void onOpen(WebSocket webSocket, Response response) {
                mWebSocket = webSocket;
                sendMessage("ready");
            }

            @Override
            public void onMessage(WebSocket webSocket, String text) {
                try {
                    JSONObject message = new JSONObject(text);
                    String msgType = message.getString("msgType");
                    switch (msgType) {
                        case "ready":
                            ActivityScenario.launch(ClickTestActivity.class);
                            onView(withId(R.id.test_input)).perform(ViewActions.typeText(msgType));
                            sendMessage("logger_open");
                            onView(withId(R.id.test_input)).perform(ViewActions.typeText("logger_open"));
                            Uninterruptibles.sleepUninterruptibly(1, TimeUnit.SECONDS);
                            break;
                        case "refreshScreenshot":
                            break;
                        case "logger_data":
                            onView(withText("测试Button点击")).perform(click());
                            onView(withId(R.id.test_input)).perform(ViewActions.replaceText(text));
                            sendMessage("logger_close");
                            onView(withText("Android")).perform(click());
                            break;
                        case "debugger_data":
                            onView(withText("Android")).perform(click());
                            onView(withId(R.id.test_input)).perform(ViewActions.replaceText(text));
                            onView(withId(R.id.test_input)).perform(ViewActions.typeText("结束测试"));
                            sendMessage("quit");
                            break;
                        default:
                            //Truth.assertWithMessage("Unknown msgType = " + msgType).fail();
                            break;
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                    Truth.assertWithMessage(e.getMessage()).fail();
                }
            }
        });
        mMockWebServer.enqueue(ready);

        String uri = "growing.bd71d91eb56f5f53://growingio/webservice?serviceType=debugger&wsUrl=" + Uri.encode(getWsUrl());
        Intent intent = new Intent();
        intent.setData(Uri.parse(uri));
        ActivityScenario.launch(intent);

        Uninterruptibles.sleepUninterruptibly(8, TimeUnit.SECONDS);

    }

    private void sendMessage(String message) {
        try {
            mWebSocket.send(new JSONObject().put("msgType", message).toString());
        } catch (JSONException exception) {
            exception.printStackTrace();
        }
    }
}
