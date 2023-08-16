/*
 *   Copyright (C) 2020 Beijing Yishu Technology Co., Ltd.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package com.growingio.android.sdk.autotrack.inject;

import com.growingio.android.sdk.TrackerContext;
import com.growingio.android.sdk.autotrack.AutotrackConfig;
import com.growingio.android.sdk.track.log.Logger;
import com.growingio.android.sdk.track.modelloader.ModelLoader;
import com.growingio.android.sdk.track.middleware.hybrid.HybridBridge;
import com.growingio.android.sdk.track.providers.ConfigurationProvider;
import com.uc.webview.export.WebView;

import java.util.Map;

public class UcWebViewInjector {
    private static final String TAG = "UCWebViewInjector";

    private static void bridgeForWebView(WebView view) {
        if (!TrackerContext.initializedSuccessfully()) {
            Logger.e(TAG, "Autotracker do not initialized successfully");
            return;
        }

        AutotrackConfig config = ConfigurationProvider.get().getConfiguration(AutotrackConfig.class);
        if (!config.isWebViewBridgeEnabled()) {
            Logger.d(TAG, "Autotracker webViewBridgeEnabled is false");
            return;
        }

        boolean result = false;
        ModelLoader<HybridBridge, Boolean> modelLoader = TrackerContext.get().getRegistry().getModelLoader(HybridBridge.class, Boolean.class);
        if (modelLoader != null) {
            result = modelLoader.buildLoadData(new HybridBridge(view)).fetcher.executeData();
        }
        Logger.d(TAG, "bridgeForWebView: webView = " + view.getClass().getName() + ", result = " + result);
    }

    private UcWebViewInjector() {
    }

    public static void ucWebViewLoadUrl(WebView webView, String url) {
        Logger.d(TAG, "ucWebViewLoadUrl: webView = " + webView.getClass().getName() + ", url = " + url);
        bridgeForWebView(webView);
    }

    public static void ucWebViewLoadUrl(WebView webView, String url, Map<String, String> additionalHttpHeaders) {
        Logger.d(TAG, "ucWebViewLoadUrl: webView = " + webView.getClass().getName() + ", url = " + url + ", additionalHttpHeaders = " + additionalHttpHeaders);
        bridgeForWebView(webView);
    }

    public static void ucWebViewLoadData(WebView webView, String data, String mimeType, String encoding) {
        Logger.d(TAG, "ucWebViewLoadData: webView = " + webView.getClass().getName());
        bridgeForWebView(webView);
    }

    public static void ucWebViewLoadDataWithBaseURL(WebView webView, String baseUrl, String data, String mimeType, String encoding, String historyUrl) {
        Logger.d(TAG, "ucWebViewLoadDataWithBaseURL: webView = " + webView.getClass().getName());
        bridgeForWebView(webView);
    }

    public static void ucWebViewPostUrl(WebView webView, String url, byte[] postData) {
        Logger.d(TAG, "ucWebViewPostUrl: webView = " + webView.getClass().getName());
        bridgeForWebView(webView);
    }

}
