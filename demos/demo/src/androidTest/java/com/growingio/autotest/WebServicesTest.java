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

package com.growingio.autotest;

import com.google.common.truth.Truth;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;

import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;

public abstract class WebServicesTest {
    private MockWebServer mMockWebServer;
    private WebSocket mWebSocket;
    private OnReceivedMessageListener mOnReceivedMessageListener;

    @Before
    public void setUp() {
        mMockWebServer = new MockWebServer();
        mMockWebServer.enqueue(new MockResponse().withWebSocketUpgrade(new WebSocketListener() {
            @Override
            public void onOpen(WebSocket webSocket, Response response) {
                mWebSocket = webSocket;
            }

            @Override
            public void onMessage(WebSocket webSocket, String text) {
                try {
                    JSONObject message = new JSONObject(text);
                    String msgType = message.getString("msgType");
                    switch (msgType) {
                        case "ready":
                            handleReadyMessage(message);
                            break;
                        case "refreshScreenshot":
                            if (mOnReceivedMessageListener != null) {
                                mOnReceivedMessageListener.onReceivedRefreshScreenshotMessage(message);
                            }
                            break;
                        case "quit":
                            break;
                        default:
                            Truth.assertWithMessage("Unknown msgType = " + msgType).fail();
                            break;
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                    Truth.assertWithMessage(e.getMessage()).fail();
                }
            }

        }));
    }

    protected void setOnReceivedMessageListener(OnReceivedMessageListener onReceivedMessageListener) {
        mOnReceivedMessageListener = onReceivedMessageListener;
    }

    protected String getWsUrl() {
        String hostName = mMockWebServer.getHostName();
        int port = mMockWebServer.getPort();
        return "ws://" + hostName + ":" + port + "/";
    }

    private void sendMessage(String text) {
        Truth.assertThat(mWebSocket != null).isTrue();
        mWebSocket.send(text);
    }

    /**
     * {
     * "msgType": "ready",
     * "projectId": "0a1b4118dd954ec3bcc69da5138bdb96",
     * "timestamp": 1595318916686,
     * "domain": "com.cliff.release11demo",
     * "sdkVersion": "3.0.9",
     * "sdkVersionCode": "9",
     * "os": "Android",
     * "screenWidth": 720,
     * "screenHeight": 1520
     * }
     */
    private void handleReadyMessage(JSONObject message) throws JSONException {
        Truth.assertThat(message.getString("msgType")).isEqualTo("ready");
        Truth.assertThat(message.getString("projectId")).isEqualTo(TestTrackConfiguration.TEST_PROJECT_ID);
        Truth.assertThat(message.getLong("timestamp") > 0).isTrue();
        Truth.assertThat(message.getString("domain")).isEqualTo("com.gio.test.three");
        Truth.assertThat(message.getString("sdkVersion")).isNotEmpty();
        Truth.assertThat(message.getInt("sdkVersionCode") > 0).isTrue();
        Truth.assertThat(message.getString("os")).isEqualTo("Android");
        Truth.assertThat(message.getInt("screenWidth") > 0).isTrue();
        Truth.assertThat(message.getInt("screenHeight") > 0).isTrue();
        sendMessage(new JSONObject().put("msgType", "ready").toString());
    }

    public interface OnReceivedMessageListener {
        void onReceivedRefreshScreenshotMessage(JSONObject message) throws JSONException;
    }
}
