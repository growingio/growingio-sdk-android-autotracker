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
package com.growingio.android.sdk.autotrack.hybrid;


import android.app.Application;
import android.webkit.WebView;

import androidx.test.core.app.ApplicationProvider;

import com.growingio.android.sdk.autotrack.Autotracker;
import com.growingio.android.sdk.autotrack.RobolectricActivity;
import com.growingio.android.sdk.autotrack.inject.UcWebViewInjector;
import com.growingio.android.sdk.autotrack.inject.WebViewInjector;
import com.growingio.android.sdk.autotrack.inject.X5WebViewInjector;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.HashMap;

@Config(manifest = Config.NONE, sdk = 23)
@RunWith(RobolectricTestRunner.class)
public class HybridTest {

    Application application = ApplicationProvider.getApplicationContext();

    @Before
    public void setup() {
        Autotracker autotracker = new Autotracker(application);
    }

    @Test
    public void webInjectTest() {
        RobolectricActivity activity = Robolectric.buildActivity(RobolectricActivity.class).create().get();
        WebView webView = new WebView(activity);
        WebViewInjector.webkitWebViewLoadUrl(webView, "https://www.baidu.com/");
        WebViewInjector.webkitWebViewLoadUrl(webView, "https://www.baidu.com/", new HashMap<>());
        WebViewInjector.webkitWebViewLoadData(webView, "<html></html>", "text", "UTF8");
        WebViewInjector.webkitWebViewLoadDataWithBaseURL(webView, "https://www.baidu.com/", "<p>", "text", "UTF8", "https://www/growingio.com/");
        WebViewInjector.webkitWebViewPostUrl(webView, "https://www.baidu.com/", null);

        com.uc.webview.export.WebView ucWebView = new com.uc.webview.export.WebView(activity);
        UcWebViewInjector.ucWebViewLoadUrl(ucWebView, "https://www.baidu.com/");
        UcWebViewInjector.ucWebViewLoadUrl(ucWebView, "https://www.baidu.com/", new HashMap<>());
        UcWebViewInjector.ucWebViewLoadData(ucWebView, "<html></html>", "text", "UTF8");
        UcWebViewInjector.ucWebViewLoadDataWithBaseURL(ucWebView, "https://www.baidu.com/", "<p>", "text", "UTF8", "https://www/growingio.com/");
        UcWebViewInjector.ucWebViewPostUrl(ucWebView, "https://www.baidu.com/", null);

        com.tencent.smtt.sdk.WebView x5WebView = new com.tencent.smtt.sdk.WebView(activity);
        X5WebViewInjector.x5WebViewLoadUrl(x5WebView, "https://www.baidu.com/");
        X5WebViewInjector.x5WebViewLoadUrl(x5WebView, "https://www.baidu.com/", new HashMap<>());
        X5WebViewInjector.x5WebViewLoadData(x5WebView, "<html></html>", "text", "UTF8");
        X5WebViewInjector.x5WebViewLoadDataWithBaseURL(x5WebView, "https://www.baidu.com/", "<p>", "text", "UTF8", "https://www/growingio.com/");
        X5WebViewInjector.x5WebViewPostUrl(x5WebView, "https://www.baidu.com/", null);

    }
}
