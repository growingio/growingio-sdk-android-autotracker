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

import android.text.TextUtils;
import android.webkit.JavascriptInterface;

import com.growingio.android.sdk.track.utils.LogUtil;

class WebViewBridgeJavascriptInterface {
    static final String JAVASCRIPT_INTERFACE_NAME = "GrowingWebViewJavascriptBridge";
    static final String JAVASCRIPT_GET_DOM_TREE_METHOD = "window.GrowingWebViewJavascriptBridge.getDomTree";
    private static final String TAG = "GIO.WebViewBridge";
    private final WebViewJavascriptBridgeConfiguration mConfiguration;
    private final NativeBridge mNativeBridge = new NativeBridge();

    WebViewBridgeJavascriptInterface(WebViewJavascriptBridgeConfiguration configuration) {
        mConfiguration = configuration;
    }

    @JavascriptInterface
    @com.uc.webview.export.JavascriptInterface
    public String getConfiguration() {
        return mConfiguration.toJSONObject().toString();
    }

    @JavascriptInterface
    @com.uc.webview.export.JavascriptInterface
    public void onDomChanged() {
        HybridBridgeProvider.HybridBridgePolicy.get().onDomChanged();
    }

    @JavascriptInterface
    @com.uc.webview.export.JavascriptInterface
    public void dispatchEvent(String event) {
        LogUtil.printJson(TAG, "dispatchEvent: ", event);
        if (TextUtils.isEmpty(event)) {
            return;
        }
        mNativeBridge.dispatchEvent(event);
    }

    @JavascriptInterface
    @com.uc.webview.export.JavascriptInterface
    public void setNativeUserId(String userId) {
        LogUtil.d(TAG, "setNativeUserId: " + userId);
        mNativeBridge.setNativeUserId(userId);
    }

    @JavascriptInterface
    @com.uc.webview.export.JavascriptInterface
    public void clearNativeUserId() {
        LogUtil.d(TAG, "clearNativeUserId: ");
        mNativeBridge.clearNativeUserId();
    }

}
