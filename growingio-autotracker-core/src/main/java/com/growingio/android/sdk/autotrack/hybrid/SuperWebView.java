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

import android.annotation.SuppressLint;
import android.os.Build;
import android.view.View;
import android.webkit.ValueCallback;
import android.webkit.WebView;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.annotation.Size;

public abstract class SuperWebView<T extends View> {
    private final T mRealWebView;

    protected SuperWebView(T realWebView) {
        mRealWebView = realWebView;
    }

    public T getRealWebView() {
        return mRealWebView;
    }

    public void getLocationOnScreen(@Size(2) int[] outLocation) {
        getRealWebView().getLocationOnScreen(outLocation);
    }

    public int getHeight() {
        return getRealWebView().getHeight();
    }

    public int getWidth() {
        return getRealWebView().getWidth();
    }

    public abstract void setJavaScriptEnabled(boolean flag);

    public abstract void addJavascriptInterface(Object obj, String interfaceName);

    public abstract void evaluateJavascript(String script, @Nullable ValueCallback<String> resultCallback);

    public static SuperWebView<WebView> make(WebView webView) {
        return new SystemWebView(webView);
    }

    public static SuperWebView<com.tencent.smtt.sdk.WebView> make(com.tencent.smtt.sdk.WebView webView) {
        return new X5WebView(webView);
    }

    public static SuperWebView<com.uc.webview.export.WebView> make(com.uc.webview.export.WebView webView) {
        return new UCWebView(webView);
    }

    private static final class SystemWebView extends SuperWebView<WebView> {

        protected SystemWebView(WebView realWebView) {
            super(realWebView);
        }

        @Override
        public void setJavaScriptEnabled(boolean flag) {
            getRealWebView().getSettings().setJavaScriptEnabled(flag);
        }

        @SuppressLint("JavascriptInterface")
        @Override
        public void addJavascriptInterface(Object obj, String interfaceName) {
            getRealWebView().addJavascriptInterface(obj, interfaceName);
        }

        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
        @Override
        public void evaluateJavascript(String script, @Nullable ValueCallback<String> resultCallback) {
            getRealWebView().evaluateJavascript(script, resultCallback);
        }
    }

    private static final class X5WebView extends SuperWebView<com.tencent.smtt.sdk.WebView> {

        protected X5WebView(com.tencent.smtt.sdk.WebView realWebView) {
            super(realWebView);
        }

        @Override
        public void setJavaScriptEnabled(boolean flag) {
            getRealWebView().getSettings().setJavaScriptEnabled(flag);
        }

        @Override
        public void addJavascriptInterface(Object obj, String interfaceName) {
            getRealWebView().addJavascriptInterface(obj, interfaceName);
        }

        @Override
        public void evaluateJavascript(String script, @Nullable final ValueCallback<String> resultCallback) {
            getRealWebView().evaluateJavascript(script, new com.tencent.smtt.sdk.ValueCallback<String>() {
                @Override
                public void onReceiveValue(String s) {
                    if (resultCallback != null) {
                        resultCallback.onReceiveValue(s);
                    }
                }
            });
        }
    }

    private static final class UCWebView extends SuperWebView<com.uc.webview.export.WebView> {

        protected UCWebView(com.uc.webview.export.WebView realWebView) {
            super(realWebView);
        }

        @Override
        public void setJavaScriptEnabled(boolean flag) {
            getRealWebView().getSettings().setJavaScriptEnabled(flag);
        }

        @Override
        public void addJavascriptInterface(Object obj, String interfaceName) {
            getRealWebView().addJavascriptInterface(obj, interfaceName);
        }

        @Override
        public void evaluateJavascript(String script, @Nullable final ValueCallback<String> resultCallback) {
            getRealWebView().evaluateJavascript(script, resultCallback);
        }
    }
}
