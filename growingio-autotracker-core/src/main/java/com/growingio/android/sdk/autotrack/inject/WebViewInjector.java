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

import android.view.View;
import android.webkit.WebView;

import com.growingio.android.sdk.TrackerContext;
import com.growingio.android.sdk.track.log.Logger;
import com.growingio.android.sdk.track.modelloader.ModelLoader;
import com.growingio.android.sdk.track.modelloader.data.HybridBridge;
import com.growingio.sdk.inject.annotation.Before;

import java.util.Map;

public class WebViewInjector {
    private static final String TAG = "WebViewInjector";

    private static void bridgeForWebView(View view) {
        if (!TrackerContext.initializedSuccessfully()) {
            Logger.e(TAG, "Autotracker do not initialized successfully");
            return;
        }

        boolean result = false;
        ModelLoader<HybridBridge, Boolean> modelLoader = TrackerContext.get().getRegistry().getModelLoader(HybridBridge.class, Boolean.class);
        if (modelLoader != null) {
            result = modelLoader.buildLoadData(new HybridBridge(view)).fetcher.executeData();
        }
        Logger.d(TAG, "bridgeForWebView: webView = " + view.getClass().getName() + ", result = " + result);
    }

    private WebViewInjector() {
    }

    @Before(clazz = WebView.class, method = "loadUrl", parameterTypes = {String.class})
    public static void webkitWebViewLoadUrl(WebView webView, String url) {
        Logger.d(TAG, "webkitWebViewLoadUrl: webView = " + webView.getClass().getName() + ", url = " + url);
        bridgeForWebView(webView);
    }

    @Before(clazz = WebView.class, method = "loadUrl", parameterTypes = {String.class, Map.class})
    public static void webkitWebViewLoadUrl(WebView webView, String url, Map<String, String> additionalHttpHeaders) {
        Logger.d(TAG, "webkitWebViewLoadUrl: webView = " + webView.getClass().getName() + ", url = " + url + ", additionalHttpHeaders = " + additionalHttpHeaders);
        bridgeForWebView(webView);
    }

    @Before(clazz = WebView.class, method = "loadData", parameterTypes = {String.class, String.class, String.class})
    public static void webkitWebViewLoadData(WebView webView, String data, String mimeType, String encoding) {
        Logger.d(TAG, "webkitWebViewLoadData: webView = " + webView.getClass().getName());
        bridgeForWebView(webView);
    }

    @Before(clazz = WebView.class, method = "loadDataWithBaseURL", parameterTypes = {String.class, String.class, String.class, String.class, String.class})
    public static void webkitWebViewLoadDataWithBaseURL(WebView webView, String baseUrl, String data, String mimeType, String encoding, String historyUrl) {
        Logger.d(TAG, "webkitWebViewLoadDataWithBaseURL: webView = " + webView.getClass().getName());
        bridgeForWebView(webView);
    }

    @Before(clazz = WebView.class, method = "postUrl", parameterTypes = {String.class, byte[].class})
    public static void webkitWebViewPostUrl(WebView webView, String url, byte[] postData) {
        Logger.d(TAG, "webkitWebViewPostUrl: webView = " + webView.getClass().getName());
        bridgeForWebView(webView);
    }

    @Before(clazz = com.tencent.smtt.sdk.WebView.class, method = "loadUrl", parameterTypes = {String.class})
    public static void x5WebViewLoadUrl(com.tencent.smtt.sdk.WebView webView, String url) {
        Logger.d(TAG, "x5WebViewLoadUrl: webView = " + webView.getClass().getName() + ", url = " + url);
        bridgeForWebView(webView);
    }

    @Before(clazz = com.tencent.smtt.sdk.WebView.class, method = "loadUrl", parameterTypes = {String.class, Map.class})
    public static void x5WebViewLoadUrl(com.tencent.smtt.sdk.WebView webView, String url, Map<String, String> additionalHttpHeaders) {
        Logger.d(TAG, "x5WebViewLoadUrl: webView = " + webView.getClass().getName() + ", url = " + url + ", additionalHttpHeaders = " + additionalHttpHeaders);
        bridgeForWebView(webView);
    }

    @Before(clazz = com.tencent.smtt.sdk.WebView.class, method = "loadData", parameterTypes = {String.class, String.class, String.class})
    public static void x5WebViewLoadData(com.tencent.smtt.sdk.WebView webView, String data, String mimeType, String encoding) {
        Logger.d(TAG, "x5WebViewLoadData: webView = " + webView.getClass().getName());
        bridgeForWebView(webView);
    }

    @Before(clazz = com.tencent.smtt.sdk.WebView.class, method = "loadDataWithBaseURL", parameterTypes = {String.class, String.class, String.class, String.class, String.class})
    public static void x5WebViewLoadDataWithBaseURL(com.tencent.smtt.sdk.WebView webView, String baseUrl, String data, String mimeType, String encoding, String historyUrl) {
        Logger.d(TAG, "x5WebViewLoadDataWithBaseURL: webView = " + webView.getClass().getName());
        bridgeForWebView(webView);
    }

    @Before(clazz = com.tencent.smtt.sdk.WebView.class, method = "postUrl", parameterTypes = {String.class, byte[].class})
    public static void x5WebViewPostUrl(com.tencent.smtt.sdk.WebView webView, String url, byte[] postData) {
        Logger.d(TAG, "x5WebViewPostUrl: webView = " + webView.getClass().getName());
        bridgeForWebView(webView);
    }

    @Before(clazz = com.uc.webview.export.WebView.class, method = "loadUrl", parameterTypes = {String.class})
    public static void ucWebViewLoadUrl(com.uc.webview.export.WebView webView, String url) {
        Logger.d(TAG, "ucWebViewLoadUrl: webView = " + webView.getClass().getName() + ", url = " + url);
        bridgeForWebView(webView);
    }

    @Before(clazz = com.uc.webview.export.WebView.class, method = "loadUrl", parameterTypes = {String.class, Map.class})
    public static void ucWebViewLoadUrl(com.uc.webview.export.WebView webView, String url, Map<String, String> additionalHttpHeaders) {
        Logger.d(TAG, "ucWebViewLoadUrl: webView = " + webView.getClass().getName() + ", url = " + url + ", additionalHttpHeaders = " + additionalHttpHeaders);
        bridgeForWebView(webView);
    }

    @Before(clazz = com.uc.webview.export.WebView.class, method = "loadData", parameterTypes = {String.class, String.class, String.class})
    public static void ucWebViewLoadData(com.uc.webview.export.WebView webView, String data, String mimeType, String encoding) {
        Logger.d(TAG, "ucWebViewLoadData: webView = " + webView.getClass().getName());
        bridgeForWebView(webView);
    }

    @Before(clazz = com.uc.webview.export.WebView.class, method = "loadDataWithBaseURL", parameterTypes = {String.class, String.class, String.class, String.class, String.class})
    public static void ucWebViewLoadDataWithBaseURL(com.uc.webview.export.WebView webView, String baseUrl, String data, String mimeType, String encoding, String historyUrl) {
        Logger.d(TAG, "ucWebViewLoadDataWithBaseURL: webView = " + webView.getClass().getName());
        bridgeForWebView(webView);
    }

    @Before(clazz = com.uc.webview.export.WebView.class, method = "postUrl", parameterTypes = {String.class, byte[].class})
    public static void ucWebViewPostUrl(com.uc.webview.export.WebView webView, String url, byte[] postData) {
        Logger.d(TAG, "ucWebViewPostUrl: webView = " + webView.getClass().getName());
        bridgeForWebView(webView);
    }
}
