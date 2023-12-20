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
package com.growingio.android.hybrid;

import android.annotation.SuppressLint;
import android.os.Build;
import android.text.TextUtils;
import android.webkit.ValueCallback;

import com.growingio.android.sdk.TrackerContext;
import com.growingio.android.sdk.track.SDKConfig;
import com.growingio.android.sdk.track.listener.Callback;
import com.growingio.android.sdk.track.listener.ListenerContainer;
import com.growingio.android.sdk.track.log.Logger;
import com.growingio.android.sdk.track.providers.AppInfoProvider;
import com.growingio.android.sdk.track.providers.ConfigurationProvider;
import com.growingio.android.sdk.track.providers.TrackerLifecycleProvider;
import com.growingio.android.sdk.track.providers.UserInfoProvider;

import org.json.JSONException;
import org.json.JSONObject;

import static com.growingio.android.hybrid.WebViewBridgeJavascriptInterface.JAVASCRIPT_GET_DOM_TREE_METHOD;

public class HybridBridgeProvider extends ListenerContainer<OnDomChangedListener, Void> implements TrackerLifecycleProvider {
    private static final String TAG = "HybridBridgePolicy";

    HybridBridgeProvider() {
    }


    private ConfigurationProvider configurationProvider;
    private AppInfoProvider appInfoProvider;
    private UserInfoProvider userInfoProvider;
    private boolean isDownGrade = false;

    @Override
    public void setup(TrackerContext context) {
        configurationProvider = context.getConfigurationProvider();
        appInfoProvider = context.getProvider(AppInfoProvider.class);
        userInfoProvider = context.getUserInfoProvider();
    }

    @Override
    public void shutdown() {

    }

    public void setDownGrade(boolean downGrade) {
        isDownGrade = downGrade;
    }

    private WebViewJavascriptBridgeConfiguration getJavascriptBridgeConfiguration() {
        String projectId = configurationProvider.core().getProjectId();
        String datasourceId = configurationProvider.core().getDataSourceId();
        String appId = configurationProvider.core().getUrlScheme();
        String appPackage = appInfoProvider.getPackageName();
        String nativeSdkVersion = isDownGrade ? SDKConfig.SDK_VERSION_DOWNGRADE : SDKConfig.SDK_VERSION;
        int nativeSdkVersionCode = isDownGrade ? SDKConfig.SDK_VERSION_CODE_DOWNGRADE : SDKConfig.SDK_VERSION_CODE;
        return new WebViewJavascriptBridgeConfiguration(projectId, datasourceId, appId, appPackage, nativeSdkVersion, nativeSdkVersionCode);
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
        if (webView.hasAddJavaScripted()) {
            Logger.d(TAG, "JavascriptInterface has already been added to the WebView");
            return;
        }
        webView.addJavascriptInterface(
                new WebViewBridgeJavascriptInterface(getJavascriptBridgeConfiguration(), this, userInfoProvider),
                WebViewBridgeJavascriptInterface.JAVASCRIPT_INTERFACE_NAME);
        webView.setAddJavaScript();
    }

    public void getWebViewDomTree(SuperWebView<?> webView, final Callback<JSONObject> callback) {
        Logger.d(TAG, "getWebViewDomTree");
        if (callback == null) {
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            int[] location = new int[2];
            webView.getLocationOnScreen(location);
            webView.evaluateJavascript("javascript:" + JAVASCRIPT_GET_DOM_TREE_METHOD + "(" +
                            location[0] + ", " + location[1] + ", " + webView.getWidth() + ", " + webView.getHeight() + ", 100)",
                    new ValueCallback<String>() {
                        @Override
                        public void onReceiveValue(String value) {
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
            callback.onFailed();
        }
    }

    @Override
    protected void singleAction(OnDomChangedListener listener, Void action) {
        listener.onDomChanged();
    }
}
