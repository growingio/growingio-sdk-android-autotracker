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
package com.growingio.android.sdk;


import android.app.Application;
import android.webkit.WebView;

import androidx.test.core.app.ApplicationProvider;


import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.HashMap;
import java.util.Map;

import static com.google.common.truth.Truth.assertThat;

@Config(manifest = Config.NONE)
@RunWith(RobolectricTestRunner.class)
public class TrackerTest {

    Application application = ApplicationProvider.getApplicationContext();

    @Test
    public void initTest() {
        try {
            Tracker nullTracker = new Tracker(null);
        }catch (Exception e){
            assertThat(e).isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Test
    public void apiTest() {
        Tracker tracker = new Tracker(application);
        Map<String, String> valueMap = new HashMap<>();
        valueMap.put("user", "cpacm");
        tracker.trackCustomEvent("test");
        tracker.setConversionVariables(null);
        tracker.setConversionVariables(valueMap);
        tracker.setLoginUserId("cpacm");
        tracker.setLoginUserAttributes(null);
        tracker.setLoginUserAttributes(valueMap);
        tracker.setVisitorAttributes(null);
        tracker.setVisitorAttributes(valueMap);
        assertThat(tracker.getDeviceId()).isNotEmpty();

        tracker.setDataCollectionEnabled(true);
        tracker.setDataCollectionEnabled(false);
        tracker.cleanLocation();
        tracker.setLocation(0d, 1d);
        tracker.cleanLocation();
        tracker.onActivityNewIntent(null, null);

        WebView webView = new WebView(application);
        tracker.bridgeWebView(webView);

        tracker.setLoginUserId("cpacm");
        tracker.setLoginUserId("cpacm", "username");
        tracker.cleanLoginUserId();

        TestLibraryGioModule testLibraryGioModule = new TestLibraryGioModule();
        testLibraryGioModule.registerComponents(tracker.getContext());

        tracker.registerComponent(testLibraryGioModule);
    }

    @Test
    public void coreConfiguration() {
        CoreConfiguration coreConfiguration = new CoreConfiguration("TrackerTest", "growingio://tracker");
        coreConfiguration.setDataCollectionServerHost("https://www.growingio.com/");
        coreConfiguration.setDataCollectionEnabled(true);
        coreConfiguration.setChannel("origin");
        coreConfiguration.setDebugEnabled(true);
        coreConfiguration.setCellularDataLimit(100);
        coreConfiguration.setDataUploadInterval(100);
        coreConfiguration.setSessionInterval(100);
        coreConfiguration.setRequireAppProcessesEnabled(false);
        coreConfiguration.setIdMappingEnabled(false);
        coreConfiguration.addPreloadComponent(null);
    }


    public static class TestLibraryGioModule extends LibraryGioModule implements Configurable {
        @Override
        public void registerComponents(TrackerContext context) {
            super.registerComponents(context);
        }
    }

}
