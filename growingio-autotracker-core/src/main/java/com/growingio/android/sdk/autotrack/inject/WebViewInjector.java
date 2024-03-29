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
package com.growingio.android.sdk.autotrack.inject;

import android.view.View;
import android.webkit.WebView;

import com.growingio.android.sdk.TrackerContext;
import com.growingio.android.sdk.track.log.Logger;

import java.util.Map;

public class WebViewInjector {
    private static final String TAG = "WebViewInjector";

    private static void bridgeForWebView(View view) {
        if (!TrackerContext.initializedSuccessfully()) {
            Logger.e(TAG, "Autotracker do not initialized successfully");
            return;
        }

        InjectorProvider.get().bridgeForWebView(view);
    }

    private WebViewInjector() {
    }

    public static void webkitWebViewLoadUrl(WebView webView, String url) {
        Logger.d(TAG, "webkitWebViewLoadUrl: webView = " + webView.getClass().getName() + ", url = " + url);
        bridgeForWebView(webView);
    }

    public static void webkitWebViewLoadUrl(WebView webView, String url, Map<String, String> additionalHttpHeaders) {
        Logger.d(TAG, "webkitWebViewLoadUrl: webView = " + webView.getClass().getName() + ", url = " + url + ", additionalHttpHeaders = " + additionalHttpHeaders);
        bridgeForWebView(webView);
    }

    public static void webkitWebViewLoadData(WebView webView, String data, String mimeType, String encoding) {
        Logger.d(TAG, "webkitWebViewLoadData: webView = " + webView.getClass().getName());
        bridgeForWebView(webView);
    }

    public static void webkitWebViewLoadDataWithBaseURL(WebView webView, String baseUrl, String data, String mimeType, String encoding, String historyUrl) {
        Logger.d(TAG, "webkitWebViewLoadDataWithBaseURL: webView = " + webView.getClass().getName());
        bridgeForWebView(webView);
    }

    public static void webkitWebViewPostUrl(WebView webView, String url, byte[] postData) {
        Logger.d(TAG, "webkitWebViewPostUrl: webView = " + webView.getClass().getName());
        bridgeForWebView(webView);
    }
}
