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

package com.gio.test.three;

import android.app.Application;
import android.util.Log;

import com.growingio.android.sdk.autotrack.AutotrackConfiguration;
import com.growingio.android.sdk.autotrack.GrowingAutotracker;
import com.tencent.smtt.sdk.QbSdk;

public class ThreeVersionApplication extends Application {
    private static final String TAG = "ThreeVersionApplication";

    @Override
    public void onCreate() {
        super.onCreate();
        QbSdk.initX5Environment(this, new QbSdk.PreInitCallback() {
            @Override
            public void onCoreInitFinished() {
                Log.e(TAG, "onCoreInitFinished: ");
            }

            @Override
            public void onViewInitFinished(boolean b) {
                Log.e(TAG, "onViewInitFinished: " + b);
            }
        });

        GrowingAutotracker.startWithConfiguration(this,
                new AutotrackConfiguration()
                        .setUploadExceptionEnabled(false)
                        .setProjectId("testProjectId")
                        .setUrlScheme("testUrlScheme")
                        .setLogEnabled(true));
    }
}
