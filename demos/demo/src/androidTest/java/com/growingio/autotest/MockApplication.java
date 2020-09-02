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

import android.app.Application;

import com.growingio.android.sdk.autotrack.AutotrackConfiguration;
import com.growingio.android.sdk.autotrack.GrowingAutotracker;

public class MockApplication extends Application {
    public static final String MOCK_SERVER_HOST = "http://localhost:8910";

    @Override
    public void onCreate() {
        super.onCreate();
        GrowingAutotracker.startWithConfiguration(this,
                new AutotrackConfiguration()
                        .setUploadExceptionEnabled(false)
                        .setProjectId("mockProjectId")
                        .setUrlScheme("mockUrlScheme")
                        .setDataCollectionServerHost(MOCK_SERVER_HOST)
                        .setDebugEnabled(true)
        );
    }
}
