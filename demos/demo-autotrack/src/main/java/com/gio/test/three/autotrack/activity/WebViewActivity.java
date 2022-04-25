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
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;

import com.gio.test.three.autotrack.R;
import com.growingio.android.sdk.autotrack.GrowingAutotracker;

public class WebViewActivity extends Activity {
    private static String sLoadUrl = "http://release-messages.growingio.cn/push/cdp/uat.html";

    private WebView mWebView;
    private Button buttonF, buttonF1;
    private View tttt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_view);
        mWebView = findViewById(R.id.myWebView);
        initWebView();
        mWebView.loadUrl(sLoadUrl);

        buttonF = findViewById(R.id.f);
        buttonF1 = findViewById(R.id.f1);
        tttt = findViewById(R.id.tttt);
        buttonF.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tttt.setVisibility(View.VISIBLE);
            }
        });

        buttonF1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tttt.setVisibility(View.GONE);
            }
        });
        tttt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GrowingAutotracker.get().trackCustomEvent("testview");
            }
        });
    }

    public static void setLoadUrl(String loadUrl) {
        sLoadUrl = loadUrl;
    }

    public WebView getWebView() {
        return mWebView;
    }

    private void initWebView() {
        mWebView.setWebViewClient(new WebViewClient());
        WebSettings webSettings = mWebView.getSettings();
        webSettings.setDomStorageEnabled(true);
        webSettings.setJavaScriptEnabled(true);
        webSettings.setBlockNetworkImage(false);
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
        webSettings.setLoadsImagesAutomatically(true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            webSettings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }
    }

    @Override
    public void onBackPressed() {
        if (mWebView.canGoBack()) {
            mWebView.goBack();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        if (mWebView != null) {
            mWebView.stopLoading();
            mWebView.getSettings().setJavaScriptEnabled(false);
            mWebView.clearHistory();
            mWebView.clearView();
            mWebView.removeAllViews();
            mWebView.destroy();

        }
        super.onDestroy();
    }
}