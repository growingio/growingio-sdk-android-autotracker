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

import android.text.TextUtils;
import android.webkit.JavascriptInterface;

import com.growingio.android.sdk.TrackerContext;
import com.growingio.android.sdk.track.log.Logger;
import com.growingio.android.sdk.track.providers.DeviceInfoProvider;
import com.growingio.android.sdk.track.providers.PersistentDataProvider;
import com.growingio.android.sdk.track.providers.UserInfoProvider;

import org.json.JSONException;
import org.json.JSONObject;

class WebViewBridgeJavascriptInterface {
    static final String JAVASCRIPT_INTERFACE_NAME = "GrowingWebViewJavascriptBridge";
    static final String JAVASCRIPT_GET_DOM_TREE_METHOD = "window.GrowingWebViewJavascriptBridge.getDomTree";
    private static final String TAG = "WebViewHybridBridge";
    private final WebViewJavascriptBridgeConfiguration mConfiguration;
    private final NativeBridge mNativeBridge;

    private final HybridBridgeProvider mHybridBridgeProvider;
    private final UserInfoProvider mUserInfoProvider;
    private final DeviceInfoProvider mDeviceInfoProvider;
    private final PersistentDataProvider mPersistentDataProvider;

    WebViewBridgeJavascriptInterface(WebViewJavascriptBridgeConfiguration configuration,
                                     HybridBridgeProvider hybridBridgeProvider,
                                     TrackerContext context) {
        mConfiguration = configuration;
        this.mHybridBridgeProvider = hybridBridgeProvider;
        mUserInfoProvider = context.getUserInfoProvider();
        mDeviceInfoProvider = context.getDeviceInfoProvider();
        mPersistentDataProvider = context.getProvider(PersistentDataProvider.class);
        mNativeBridge = new NativeBridge(mUserInfoProvider);
    }

    @JavascriptInterface
    @com.uc.webview.export.JavascriptInterface
    public String getConfiguration() {
        return mConfiguration.toJSONObject().toString();
    }

    @JavascriptInterface
    @com.uc.webview.export.JavascriptInterface
    public String getNativeIdentity() {
        // 身份值不落日志：与 setNativeUserId 打入参不同，这里输出的是 SDK 全量身份
        Logger.d(TAG, "getNativeIdentity");
        JSONObject identity = new JSONObject();
        try {
            identity.put("deviceId", mDeviceInfoProvider.getDeviceId());
            String userId = mUserInfoProvider.getLoginUserId();
            if (!TextUtils.isEmpty(userId)) {
                identity.put("userId", userId);
            }
            // idMappingEnabled 关闭时 loginUserKey 恒为 null，自然不会带上
            String userKey = mUserInfoProvider.getLoginUserKey();
            if (!TextUtils.isEmpty(userKey)) {
                identity.put("userKey", userKey);
            }
            identity.put("isNewDevice", mPersistentDataProvider.isNewDevice());
        } catch (JSONException e) {
            Logger.e(TAG, e.getMessage(), e);
        }
        return identity.toString();
    }

    @JavascriptInterface
    @com.uc.webview.export.JavascriptInterface
    public void onDomChanged() {
        mHybridBridgeProvider.onDomChanged();
    }

    @JavascriptInterface
    @com.uc.webview.export.JavascriptInterface
    public void dispatchEvent(String event) {
        Logger.printJson(TAG, "dispatchEvent: ", event);
        if (TextUtils.isEmpty(event)) {
            return;
        }
        mNativeBridge.dispatchEvent(event);
    }

    @JavascriptInterface
    @com.uc.webview.export.JavascriptInterface
    public void setNativeUserId(String userId) {
        Logger.d(TAG, "setNativeUserId: " + userId);
        mNativeBridge.setNativeUserId(userId);
    }

    @JavascriptInterface
    @com.uc.webview.export.JavascriptInterface
    public void clearNativeUserId() {
        Logger.d(TAG, "clearNativeUserId: ");
        mNativeBridge.clearNativeUserId();
    }

    @JavascriptInterface
    @com.uc.webview.export.JavascriptInterface
    public void setNativeUserIdAndUserKey(String userId, String userKey) {
        Logger.d(TAG, "setNativeUserIdAndUserKey: " + userId + ", " + userKey);
        mNativeBridge.setNativeUserIdAndUserKey(userId, userKey);
    }

    @JavascriptInterface
    @com.uc.webview.export.JavascriptInterface
    public void clearNativeUserIdAndUserKey() {
        Logger.d(TAG, "clearNativeUserIdAndUserKey: ");
        mNativeBridge.clearNativeUserIdAndUserKey();
    }

}
