/*
 *  Copyright (C) 2024 Beijing Yishu Technology Co., Ltd.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.growingio.android.hybrid;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Message;
import android.view.View;

import com.tencent.smtt.export.external.interfaces.ConsoleMessage;
import com.tencent.smtt.export.external.interfaces.GeolocationPermissionsCallback;
import com.tencent.smtt.export.external.interfaces.IX5WebChromeClient;
import com.tencent.smtt.export.external.interfaces.JsPromptResult;
import com.tencent.smtt.export.external.interfaces.JsResult;
import com.tencent.smtt.export.external.interfaces.PermissionRequest;
import com.tencent.smtt.sdk.ValueCallback;
import com.tencent.smtt.sdk.WebStorage;
import com.tencent.smtt.sdk.WebView;

import java.lang.ref.WeakReference;

class WebChromeClientX5Delegate extends com.tencent.smtt.sdk.WebChromeClient {
    private final WebViewJavascriptBridgeConfiguration bridgeConfiguration;
    private final com.tencent.smtt.sdk.WebChromeClient wrapper;
    private static final int MIN_PROGRESS_FOR_HOOK = 60;
    private static final int HOOK_DELAY = 1000;
    private JsSdkInitRunner jsSdkInitRunner;


    public WebChromeClientX5Delegate(com.tencent.smtt.sdk.WebChromeClient wrapper, WebViewJavascriptBridgeConfiguration bridgeConfiguration) {
        this.wrapper = wrapper;
        this.bridgeConfiguration = bridgeConfiguration;
    }

    private class JsSdkInitRunner implements Runnable {

        private WeakReference<WebView> weakReference;

        private void setWebView(WebView webView) {
            weakReference = new WeakReference<>(webView);
        }

        @Override
        public void run() {
            WebView webView = weakReference.get();
            if (webView == null) {
                return;
            }
            String jsId = "_growing_js_sdk";
            String jsSrc = "https://assets.giocdn.com/sdk/webjs/gdp-full.js";
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                webView.evaluateJavascript(bridgeConfiguration.injectScriptFile(jsId, jsSrc), null);
            } else {
                webView.loadUrl(bridgeConfiguration.injectScriptFile(jsId, jsSrc));
            }
        }
    }

    private Runnable getJsSdkInitRunner(WebView webView) {
        if (jsSdkInitRunner != null) {
            jsSdkInitRunner.setWebView(webView);
        } else {
            if (webView != null) {
                jsSdkInitRunner = new JsSdkInitRunner();
                jsSdkInitRunner.setWebView(webView);
            }
        }
        return jsSdkInitRunner;
    }

    @Override
    public void onProgressChanged(WebView webView, int i) {
        this.wrapper.onProgressChanged(webView, i);
        webView.removeCallbacks(jsSdkInitRunner);
        if (i >= MIN_PROGRESS_FOR_HOOK) {
            webView.postDelayed(getJsSdkInitRunner(webView), HOOK_DELAY);
        }
    }

    @Override
    public void onExceededDatabaseQuota(String s, String s1, long l, long l1, long l2, WebStorage.QuotaUpdater quotaUpdater) {
        this.wrapper.onExceededDatabaseQuota(s, s1, l, l1, l2, quotaUpdater);
    }

    @Override
    public Bitmap getDefaultVideoPoster() {
        return this.wrapper.getDefaultVideoPoster();
    }

    @Override
    public void getVisitedHistory(ValueCallback<String[]> valueCallback) {
        this.wrapper.getVisitedHistory(valueCallback);
    }

    @Override
    public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
        return this.wrapper.onConsoleMessage(consoleMessage);
    }

    @Override
    public boolean onCreateWindow(WebView webView, boolean b, boolean b1, Message message) {
        return this.wrapper.onCreateWindow(webView, b, b1, message);
    }

    @Override
    public void onGeolocationPermissionsHidePrompt() {
        this.wrapper.onGeolocationPermissionsHidePrompt();
    }

    @Override
    public void onGeolocationPermissionsShowPrompt(String s, GeolocationPermissionsCallback geolocationPermissionsCallback) {
        this.wrapper.onGeolocationPermissionsShowPrompt(s, geolocationPermissionsCallback);
    }

    @Override
    public void onHideCustomView() {
        this.wrapper.onHideCustomView();
    }

    @Override
    public boolean onJsAlert(WebView webView, String s, String s1, JsResult jsResult) {
        return this.wrapper.onJsAlert(webView, s, s1, jsResult);
    }

    @Override
    public boolean onJsConfirm(WebView webView, String s, String s1, JsResult jsResult) {
        return this.wrapper.onJsConfirm(webView, s, s1, jsResult);
    }

    @Override
    public boolean onJsPrompt(WebView webView, String s, String s1, String s2, JsPromptResult jsPromptResult) {
        return this.wrapper.onJsPrompt(webView, s, s1, s2, jsPromptResult);
    }

    @Override
    public boolean onJsBeforeUnload(WebView webView, String s, String s1, JsResult jsResult) {
        return this.wrapper.onJsBeforeUnload(webView, s, s1, jsResult);
    }

    @Override
    public boolean onJsTimeout() {
        return this.wrapper.onJsTimeout();
    }

    @Override
    public void onReachedMaxAppCacheSize(long l, long l1, WebStorage.QuotaUpdater quotaUpdater) {
        this.wrapper.onReachedMaxAppCacheSize(l, l1, quotaUpdater);
    }

    @Override
    public void onReceivedIcon(WebView webView, Bitmap bitmap) {
        this.wrapper.onReceivedIcon(webView, bitmap);
    }

    @Override
    public void onReceivedTouchIconUrl(WebView webView, String s, boolean b) {
        this.wrapper.onReceivedTouchIconUrl(webView, s, b);
    }

    @Override
    public void onReceivedTitle(WebView webView, String s) {
        this.wrapper.onReceivedTitle(webView, s);
    }

    @Override
    public void onRequestFocus(WebView webView) {
        this.wrapper.onRequestFocus(webView);
    }

    @Override
    public void onShowCustomView(View view, IX5WebChromeClient.CustomViewCallback customViewCallback) {
        this.wrapper.onShowCustomView(view, customViewCallback);
    }

    @Override
    public void onShowCustomView(View view, int i, IX5WebChromeClient.CustomViewCallback customViewCallback) {
        this.wrapper.onShowCustomView(view, i, customViewCallback);
    }

    @Override
    public void onCloseWindow(WebView webView) {
        this.wrapper.onCloseWindow(webView);
    }

    @Override
    public View getVideoLoadingProgressView() {
        return this.wrapper.getVideoLoadingProgressView();
    }

    @Override
    public void openFileChooser(ValueCallback<Uri> valueCallback, String s, String s1) {
        this.wrapper.openFileChooser(valueCallback, s, s1);
    }

    @Override
    public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> valueCallback, FileChooserParams fileChooserParams) {
        return this.wrapper.onShowFileChooser(webView, valueCallback, fileChooserParams);
    }

    @Override
    public void onPermissionRequest(PermissionRequest permissionRequest) {
        this.wrapper.onPermissionRequest(permissionRequest);
    }

    @Override
    public void onPermissionRequestCanceled(PermissionRequest permissionRequest) {
        this.wrapper.onPermissionRequestCanceled(permissionRequest);
    }
}
