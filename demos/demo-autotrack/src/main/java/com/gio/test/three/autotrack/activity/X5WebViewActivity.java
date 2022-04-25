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

package com.gio.test.three.autotrack.activity;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;

import com.gio.test.three.autotrack.R;
import com.tencent.smtt.sdk.WebSettings;
import com.tencent.smtt.sdk.WebView;
import com.tencent.smtt.sdk.WebViewClient;

public class X5WebViewActivity extends Activity {
    private static final String LOAD_URL = "http://release-messages.growingio.cn/push/cdp/uat.html?key1=vaue1&key2=value2";

    private WebView mX5WebView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_x5_web_view);
        mX5WebView = findViewById(R.id.x5_web_view);
        initWebView();
        String loadUrl = getIntent().getStringExtra("LOAD_URL");
        if (TextUtils.isEmpty(loadUrl)) {
            loadUrl = LOAD_URL;
        }
        mX5WebView.loadUrl(loadUrl);
    }

    private void initWebView() {
        mX5WebView.setWebViewClient(new WebViewClient());
        WebSettings webSettings = mX5WebView.getSettings();
        webSettings.setDomStorageEnabled(true);
        webSettings.setJavaScriptEnabled(true);
        webSettings.setBlockNetworkImage(false);
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
        webSettings.setLoadsImagesAutomatically(true);
        webSettings.setMixedContentMode(android.webkit.WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
    }

    @Override
    public void onBackPressed() {
        if (mX5WebView.canGoBack()) {
            mX5WebView.goBack();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        if (mX5WebView != null) {
            mX5WebView.stopLoading();
            mX5WebView.getSettings().setJavaScriptEnabled(false);
            mX5WebView.clearHistory();
            mX5WebView.clearView();
            mX5WebView.removeAllViews();
            mX5WebView.destroy();

        }
        super.onDestroy();
    }
}