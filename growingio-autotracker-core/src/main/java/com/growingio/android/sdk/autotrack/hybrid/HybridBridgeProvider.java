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
import android.support.annotation.UiThread;
import android.text.TextUtils;
import android.webkit.ValueCallback;

import com.growingio.android.sdk.track.SDKConfig;
import com.growingio.android.sdk.track.async.Callback;
import com.growingio.android.sdk.track.async.Disposable;
import com.growingio.android.sdk.track.async.HandlerDisposable;
import com.growingio.android.sdk.track.listener.ListenerContainer;
import com.growingio.android.sdk.track.log.Logger;
import com.growingio.android.sdk.track.providers.AppInfoProvider;
import com.growingio.android.sdk.track.providers.ConfigurationProvider;

import org.json.JSONException;
import org.json.JSONObject;

import static com.growingio.android.sdk.autotrack.hybrid.WebViewBridgeJavascriptInterface.JAVASCRIPT_GET_DOM_TREE_METHOD;

public class HybridBridgeProvider extends ListenerContainer<OnDomChangedListener, Void> {
    private static final String TAG = "HybridBridgePolicy";

    private static final int EVALUATE_JAVASCRIPT_TIMEOUT = 5000;

    private static class SingleInstance {
        private static final HybridBridgeProvider INSTANCE = new HybridBridgeProvider();
    }

    private HybridBridgeProvider() {
    }

    public static HybridBridgeProvider get() {
        return SingleInstance.INSTANCE;
    }

    private WebViewJavascriptBridgeConfiguration getJavascriptBridgeConfiguration() {
        String projectId = ConfigurationProvider.core().getProjectId();
        String appId = ConfigurationProvider.core().getUrlScheme();
        String appPackage = AppInfoProvider.get().getPackageName();
        String nativeSdkVersion = SDKConfig.SDK_VERSION;
        int nativeSdkVersionCode = SDKConfig.SDK_VERSION_CODE;
        return new WebViewJavascriptBridgeConfiguration(projectId, appId, appPackage, nativeSdkVersion, nativeSdkVersionCode);
    }

    public void onDomChanged() {
        dispatchActions(null);
    }

    public void registerDomChangedListener(OnDomChangedListener listener) {
        register(listener);
    }

    public void unregisterDomChangedListener(OnDomChangedListener listener) {
        unregister(listener);
    }

    @SuppressLint("SetJavaScriptEnabled")
    public void bridgeForWebView(SuperWebView<?> webView) {
        webView.setJavaScriptEnabled(true);
        webView.addJavascriptInterface(new WebViewBridgeJavascriptInterface(getJavascriptBridgeConfiguration()), WebViewBridgeJavascriptInterface.JAVASCRIPT_INTERFACE_NAME);
    }

    @UiThread
    public Disposable getWebViewDomTree(SuperWebView<?> webView, final Callback<JSONObject> callback) {
        Logger.d(TAG, "getWebViewDomTree");
        if (callback == null) {
            return Disposable.EMPTY_DISPOSABLE;
        }
        final Disposable disposable = new HandlerDisposable().schedule(new Runnable() {
            @Override
            public void run() {
                Logger.e(TAG, "getWebViewDomTree timeout");
                callback.onFailed();
            }
        }, EVALUATE_JAVASCRIPT_TIMEOUT);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            int[] location = new int[2];
            webView.getLocationOnScreen(location);
            webView.evaluateJavascript("javascript:" + JAVASCRIPT_GET_DOM_TREE_METHOD + "(" +
                            location[0] + ", " + location[1] + ", " + webView.getWidth() + ", " + webView.getHeight() + ", 100)",
                    new ValueCallback<String>() {
                        @Override
                        public void onReceiveValue(String value) {
                            if (disposable.isDisposed()) {
                                return;
                            }
                            disposable.dispose();
                            if (TextUtils.isEmpty(value) || "null".equals(value)) {
                                Logger.e(TAG, "getWebViewDomTree ValueCallback is NULL");
                                callback.onFailed();
                                return;
                            }
                            try {
                                JSONObject domTree = new JSONObject(value);
                                callback.onSuccess(domTree);
                            } catch (JSONException e) {
                                Logger.e(TAG, e);
                                callback.onFailed();
                            }
                        }
                    });
        } else {
            Logger.e(TAG, "You need use after Android 4.4 to getWebViewDomTree");
            if (!disposable.isDisposed()) {
                disposable.dispose();
                callback.onFailed();
            }
        }
        return disposable;
    }

    @Override
    protected void singleAction(OnDomChangedListener listener, Void action) {
        listener.onDomChanged();
    }
}
