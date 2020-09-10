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

package com.growingio.autotest.help;

import java.io.IOException;

import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockWebServer;

public class MockNetwork {
    public static final String MOCK_SERVER_HOST = "http://localhost:8910";
    private final MockWebServer mMockWebServer = new MockWebServer();

    private final Dispatcher mDispatcher;

    public MockNetwork(Dispatcher realDispatcher) {
        mDispatcher = realDispatcher;
    }

    public void start() throws IOException {
        mMockWebServer.setDispatcher(mDispatcher);
        mMockWebServer.start(8910);
    }

    public void shutdown() throws IOException {
        mMockWebServer.shutdown();
    }
}
