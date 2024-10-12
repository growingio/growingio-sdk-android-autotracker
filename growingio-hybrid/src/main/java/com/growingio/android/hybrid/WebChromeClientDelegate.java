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
import android.webkit.ConsoleMessage;
import android.webkit.GeolocationPermissions;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.PermissionRequest;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebStorage;
import android.webkit.WebView;

import java.lang.ref.WeakReference;

class WebChromeClientDelegate extends WebChromeClient {

    private final WebViewJavascriptBridgeConfiguration bridgeConfiguration;
    private final WebChromeClient wrapper;
    private static final int MIN_PROGRESS_FOR_HOOK = 60;
    private static final int HOOK_DELAY = 1000;

    private JsSdkInitRunner jsSdkInitRunner;

    public WebChromeClientDelegate(WebChromeClient wrapper, WebViewJavascriptBridgeConfiguration bridgeConfiguration) {
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
    public void onProgressChanged(WebView view, int newProgress) {
        this.wrapper.onProgressChanged(view, newProgress);
        view.removeCallbacks(jsSdkInitRunner);
        if (newProgress >= MIN_PROGRESS_FOR_HOOK) {
            view.postDelayed(getJsSdkInitRunner(view), HOOK_DELAY);
        }
    }

    @Override
    public void onReceivedTitle(WebView view, String title) {
        this.wrapper.onReceivedTitle(view, title);
    }

    @Override
    public void onReceivedIcon(WebView view, Bitmap icon) {
        this.wrapper.onReceivedIcon(view, icon);
    }

    @Override
    public void onReceivedTouchIconUrl(WebView view, String url, boolean precomposed) {
        this.wrapper.onReceivedTouchIconUrl(view, url, precomposed);
    }

    @Override
    public void onShowCustomView(View view, CustomViewCallback callback) {
        this.wrapper.onShowCustomView(view, callback);
    }

    @Override
    public void onShowCustomView(View view, int requestedOrientation, CustomViewCallback callback) {
        this.wrapper.onShowCustomView(view, requestedOrientation, callback);
    }

    @Override
    public void onHideCustomView() {
        this.wrapper.onHideCustomView();
    }

    @Override
    public boolean onCreateWindow(WebView view, boolean isDialog, boolean isUserGesture, Message resultMsg) {
        return this.wrapper.onCreateWindow(view, isDialog, isUserGesture, resultMsg);
    }

    @Override
    public void onRequestFocus(WebView view) {
        this.wrapper.onRequestFocus(view);
    }

    @Override
    public void onCloseWindow(WebView window) {
        this.wrapper.onCloseWindow(window);
    }

    @Override
    public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
        return this.wrapper.onJsAlert(view, url, message, result);
    }

    @Override
    public boolean onJsConfirm(WebView view, String url, String message, JsResult result) {
        return this.wrapper.onJsConfirm(view, url, message, result);
    }

    @Override
    public boolean onJsPrompt(WebView view, String url, String message, String defaultValue, JsPromptResult result) {
        return this.wrapper.onJsPrompt(view, url, message, defaultValue, result);
    }

    @Override
    public boolean onJsBeforeUnload(WebView view, String url, String message, JsResult result) {
        return this.wrapper.onJsBeforeUnload(view, url, message, result);
    }

    @Override
    public void onExceededDatabaseQuota(String url, String databaseIdentifier, long quota, long estimatedDatabaseSize, long totalQuota, WebStorage.QuotaUpdater quotaUpdater) {
        this.wrapper.onExceededDatabaseQuota(url, databaseIdentifier, quota, estimatedDatabaseSize, totalQuota, quotaUpdater);
    }

    @Override
    public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback) {
        this.wrapper.onGeolocationPermissionsShowPrompt(origin, callback);
    }

    @Override
    public void onGeolocationPermissionsHidePrompt() {
        this.wrapper.onGeolocationPermissionsHidePrompt();
    }

    @Override
    public void onPermissionRequest(PermissionRequest request) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            this.wrapper.onPermissionRequest(request);
        }
    }

    @Override
    public void onPermissionRequestCanceled(PermissionRequest request) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            this.wrapper.onPermissionRequestCanceled(request);
        }
    }

    @Override
    public boolean onJsTimeout() {
        return this.wrapper.onJsTimeout();
    }

    @Override
    public void onConsoleMessage(String message, int lineNumber, String sourceID) {
        this.wrapper.onConsoleMessage(message, lineNumber, sourceID);
    }

    @Override
    public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
        return this.wrapper.onConsoleMessage(consoleMessage);
    }

    @Override
    public Bitmap getDefaultVideoPoster() {
        return this.wrapper.getDefaultVideoPoster();
    }

    @Override
    public View getVideoLoadingProgressView() {
        return this.wrapper.getVideoLoadingProgressView();
    }

    @Override
    public void getVisitedHistory(ValueCallback<String[]> callback) {
        this.wrapper.getVisitedHistory(callback);
    }

    @Override
    public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return this.wrapper.onShowFileChooser(webView, filePathCallback, fileChooserParams);
        }
        return super.onShowFileChooser(webView, filePathCallback, fileChooserParams);
    }
}
