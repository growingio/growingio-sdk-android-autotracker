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

import com.growingio.android.sdk.autotrack.CdpAutotrackConfiguration;

import static com.growingio.autotest.help.MockServer.MOCK_SERVER_HOST;

public class TestTrackConfiguration {
    public static final String TEST_PROJECT_ID = "testProjectId";
    public static final String TEST_URL_SCHEME = "testUrlScheme";
    public static final String TEST_DATA_SOURCE_ID = "testDataSourceId";

    private TestTrackConfiguration() {
    }

    public static CdpAutotrackConfiguration getTestConfig() {
        CdpAutotrackConfiguration configuration = new CdpAutotrackConfiguration(TEST_PROJECT_ID, TEST_URL_SCHEME);
        configuration.setDataCollectionServerHost(MOCK_SERVER_HOST)
                .setDataSourceId(TEST_DATA_SOURCE_ID)
                .setDebugEnabled(true);
        return configuration;
    }

    public static CdpAutotrackConfiguration getTestConfig(String urlScheme) {
        CdpAutotrackConfiguration configuration = new CdpAutotrackConfiguration(TEST_PROJECT_ID, urlScheme);
        configuration.setDataCollectionServerHost(MOCK_SERVER_HOST)
                .setDataSourceId(TEST_DATA_SOURCE_ID)
                .setDebugEnabled(true);
        return configuration;
    }
}
