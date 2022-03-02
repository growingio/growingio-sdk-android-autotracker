package com.growingio.example;

import android.os.Bundle;
import android.webkit.WebView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class WebViewActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webview);

        WebView webView = findViewById(R.id.webview_wv);
        // 本地文件需要开启localstorage存储
        webView.getSettings().setDomStorageEnabled(true);
        webView.loadUrl("file:///android_asset/hybrid.html");
    }
}
