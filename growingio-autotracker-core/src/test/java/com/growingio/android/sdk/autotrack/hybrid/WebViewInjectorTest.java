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

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest(HybridBridgeProvider.class)
public class WebViewInjectorTest {
    @Before
    public void setUp() {
        PowerMockito.mockStatic(HybridBridgeProvider.class);
    }

    @Test
    public void testWebViewInjector() {
        HybridBridgeProvider hybridBridgeProvider = PowerMockito.mock(HybridBridgeProvider.class);
        PowerMockito.when(HybridBridgeProvider.get()).thenReturn(hybridBridgeProvider);
        WebView webView = PowerMockito.mock(WebView.class);
        WebViewInjector.webkitWebViewLoadUrl(webView, "url");
        Mockito.verify(hybridBridgeProvider).bridgeForWebView(Mockito.any());
        WebViewInjector.webkitWebViewLoadUrl(webView, "url", null);
        Mockito.verify(hybridBridgeProvider, Mockito.times(2)).bridgeForWebView(Mockito.any());
        WebViewInjector.webkitWebViewLoadData(webView, "data", "mimeType", "encoding");
        Mockito.verify(hybridBridgeProvider, Mockito.times(3)).bridgeForWebView(Mockito.any());
        WebViewInjector.webkitWebViewLoadDataWithBaseURL(webView, "url", "data", "mimeType", "encoding", "historyUrl");
        Mockito.verify(hybridBridgeProvider, Mockito.times(4)).bridgeForWebView(Mockito.any());

        com.uc.webview.export.WebView ucWebView = PowerMockito.mock(com.uc.webview.export.WebView.class);
        WebViewInjector.ucWebViewLoadUrl(ucWebView, "url");
        Mockito.verify(hybridBridgeProvider, Mockito.times(5)).bridgeForWebView(Mockito.any());
        WebViewInjector.ucWebViewLoadUrl(ucWebView, "url", null);
        Mockito.verify(hybridBridgeProvider, Mockito.times(6)).bridgeForWebView(Mockito.any());
        WebViewInjector.ucWebViewLoadData(ucWebView, "data", "mimeType", "encoding");
        Mockito.verify(hybridBridgeProvider, Mockito.times(7)).bridgeForWebView(Mockito.any());
        WebViewInjector.ucWebViewLoadDataWithBaseURL(ucWebView, "url", "data", "mimeType", "encoding", "historyUrl");
        Mockito.verify(hybridBridgeProvider, Mockito.times(8)).bridgeForWebView(Mockito.any());

        com.tencent.smtt.sdk.WebView x5WebView = PowerMockito.mock(com.tencent.smtt.sdk.WebView.class);
        WebViewInjector.x5WebViewLoadUrl(x5WebView, "url");
        Mockito.verify(hybridBridgeProvider, Mockito.times(9)).bridgeForWebView(Mockito.any());
        WebViewInjector.x5WebViewLoadUrl(x5WebView, "url", null);
        Mockito.verify(hybridBridgeProvider, Mockito.times(10)).bridgeForWebView(Mockito.any());
        WebViewInjector.x5WebViewLoadData(x5WebView, "data", "mimeType", "encoding");
        Mockito.verify(hybridBridgeProvider, Mockito.times(11)).bridgeForWebView(Mockito.any());
        WebViewInjector.x5WebViewLoadDataWithBaseURL(x5WebView, "url", "data", "mimeType", "encoding", "historyUrl");
        Mockito.verify(hybridBridgeProvider, Mockito.times(12)).bridgeForWebView(Mockito.any());
    }
}
