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

package com.growingio.android.sdk.autotrack.hybrid;

import android.webkit.WebView;

import com.growingio.android.sdk.track.log.Logger;
import com.growingio.sdk.inject.annotation.Before;

import java.util.Map;

public class WebViewInjector {
    private static final String TAG = "WebViewInjector";

    @Before(clazz = WebView.class, method = "loadUrl", parameterTypes = {String.class})
    public static void webkitWebViewLoadUrl(WebView webView, String url) {
        Logger.d(TAG, "webkitWebViewLoadUrl: webView = " + webView.getClass().getName() + ", url = " + url);
        HybridBridgeProvider.get().bridgeForWebView(SuperWebView.make(webView));
    }

    @Before(clazz = WebView.class, method = "loadUrl", parameterTypes = {String.class, Map.class})
    public static void webkitWebViewLoadUrl(WebView webView, String url, Map<String, String> additionalHttpHeaders) {
        Logger.d(TAG, "webkitWebViewLoadUrl: webView = " + webView.getClass().getName() + ", url = " + url + ", additionalHttpHeaders = " + additionalHttpHeaders);
        HybridBridgeProvider.get().bridgeForWebView(SuperWebView.make(webView));
    }

    @Before(clazz = WebView.class, method = "loadData", parameterTypes = {String.class, String.class, String.class})
    public static void webkitWebViewLoadData(WebView webView, String data, String mimeType, String encoding) {
        Logger.d(TAG, "webkitWebViewLoadData: webView = " + webView.getClass().getName());
        HybridBridgeProvider.get().bridgeForWebView(SuperWebView.make(webView));
    }

    @Before(clazz = com.tencent.smtt.sdk.WebView.class, method = "loadUrl", parameterTypes = {String.class})
    public static void x5WebViewLoadUrl(com.tencent.smtt.sdk.WebView webView, String url) {
        Logger.d(TAG, "x5WebViewLoadUrl: webView = " + webView.getClass().getName() + ", url = " + url);
        HybridBridgeProvider.get().bridgeForWebView(SuperWebView.make(webView));
    }

    @Before(clazz = com.tencent.smtt.sdk.WebView.class, method = "loadUrl", parameterTypes = {String.class, Map.class})
    public static void x5WebViewLoadUrl(com.tencent.smtt.sdk.WebView webView, String url, Map<String, String> additionalHttpHeaders) {
        Logger.d(TAG, "x5WebViewLoadUrl: webView = " + webView.getClass().getName() + ", url = " + url + ", additionalHttpHeaders = " + additionalHttpHeaders);
        HybridBridgeProvider.get().bridgeForWebView(SuperWebView.make(webView));
    }

    @Before(clazz = com.tencent.smtt.sdk.WebView.class, method = "loadData", parameterTypes = {String.class, String.class, String.class})
    public static void x5WebViewLoadData(com.tencent.smtt.sdk.WebView webView, String data, String mimeType, String encoding) {
        Logger.d(TAG, "x5WebViewLoadData: webView = " + webView.getClass().getName());
        HybridBridgeProvider.get().bridgeForWebView(SuperWebView.make(webView));
    }

    @Before(clazz = com.uc.webview.export.WebView.class, method = "loadUrl", parameterTypes = {String.class})
    public static void ucWebViewLoadUrl(com.uc.webview.export.WebView webView, String url) {
        Logger.d(TAG, "ucWebViewLoadUrl: webView = " + webView.getClass().getName() + ", url = " + url);
        HybridBridgeProvider.get().bridgeForWebView(SuperWebView.make(webView));
    }

    @Before(clazz = com.uc.webview.export.WebView.class, method = "loadUrl", parameterTypes = {String.class, Map.class})
    public static void ucWebViewLoadUrl(com.uc.webview.export.WebView webView, String url, Map<String, String> additionalHttpHeaders) {
        Logger.d(TAG, "ucWebViewLoadUrl: webView = " + webView.getClass().getName() + ", url = " + url + ", additionalHttpHeaders = " + additionalHttpHeaders);
        HybridBridgeProvider.get().bridgeForWebView(SuperWebView.make(webView));
    }

    @Before(clazz = com.uc.webview.export.WebView.class, method = "loadData", parameterTypes = {String.class, String.class, String.class})
    public static void ucWebViewLoadData(com.uc.webview.export.WebView webView, String data, String mimeType, String encoding) {
        Logger.d(TAG, "ucWebViewLoadData: webView = " + webView.getClass().getName());
        HybridBridgeProvider.get().bridgeForWebView(SuperWebView.make(webView));
    }

    @Before(clazz = WebView.class, method = "loadDataWithBaseURL", parameterTypes = {String.class, String.class, String.class, String.class, String.class})
    public static void webkitWebViewLoadDataWithBaseURL(WebView webView, String baseUrl, String data, String mimeType, String encoding, String historyUrl) {
        Logger.d(TAG, "webkitWebViewLoadDataWithBaseURL: webView = " + webView.getClass().getName());
        HybridBridgeProvider.get().bridgeForWebView(SuperWebView.make(webView));
    }

    @Before(clazz = com.tencent.smtt.sdk.WebView.class, method = "loadDataWithBaseURL", parameterTypes = {String.class, String.class, String.class, String.class, String.class})
    public static void x5WebViewLoadDataWithBaseURL(com.tencent.smtt.sdk.WebView webView, String baseUrl, String data, String mimeType, String encoding, String historyUrl) {
        Logger.d(TAG, "x5WebViewLoadDataWithBaseURL: webView = " + webView.getClass().getName());
        HybridBridgeProvider.get().bridgeForWebView(SuperWebView.make(webView));
    }

    @Before(clazz = com.uc.webview.export.WebView.class, method = "loadDataWithBaseURL", parameterTypes = {String.class, String.class, String.class, String.class, String.class})
    public static void ucWebViewLoadDataWithBaseURL(com.uc.webview.export.WebView webView, String baseUrl, String data, String mimeType, String encoding, String historyUrl) {
        Logger.d(TAG, "ucWebViewLoadDataWithBaseURL: webView = " + webView.getClass().getName());
        HybridBridgeProvider.get().bridgeForWebView(SuperWebView.make(webView));
    }
}
