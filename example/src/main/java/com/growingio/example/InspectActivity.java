package com.growingio.example;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.growingio.android.sdk.autotrack.GrowingAutotracker;
import com.growingio.android.sdk.autotrack.IgnorePolicy;

import java.util.HashMap;

public class InspectActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inspect);

        // 开启采集功能
        // https://growingio.github.io/growingio-sdk-docs/docs/android/base/api#1-%E6%95%B0%E6%8D%AE%E9%87%87%E9%9B%86%E5%BC%80%E5%85%B3
        findViewById(R.id.inspect_btn_enableDataCollect).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GrowingAutotracker.get().setDataCollectionEnabled(true);
            }
        });

        // 关闭采集功能
        // https://growingio.github.io/growingio-sdk-docs/docs/android/base/api#1-%E6%95%B0%E6%8D%AE%E9%87%87%E9%9B%86%E5%BC%80%E5%85%B3
        findViewById(R.id.inspect_btn_disableDataCollect).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GrowingAutotracker.get().setDataCollectionEnabled(false);
            }
        });

        // 设置登录用户id
        // https://growingio.github.io/growingio-sdk-docs/docs/android/base/api#1-%E6%95%B0%E6%8D%AE%E9%87%87%E9%9B%86%E5%BC%80%E5%85%B3
        findViewById(R.id.inspect_btn_setUserId).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GrowingAutotracker.get().setLoginUserId("sdk-integration", "name");
            }
        });

        // 清除登录用户id
        // https://growingio.github.io/growingio-sdk-docs/docs/android/base/api#3-%E6%B8%85%E9%99%A4%E7%99%BB%E5%BD%95%E7%94%A8%E6%88%B7id
        findViewById(R.id.inspect_btn_clearUserId).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GrowingAutotracker.get().cleanLoginUserId();
            }
        });

        // 设置经纬度
        // https://growingio.github.io/growingio-sdk-docs/docs/android/base/api#4-%E8%AE%BE%E7%BD%AE%E7%94%A8%E6%88%B7%E7%9A%84%E5%9C%B0%E7%90%86%E4%BD%8D%E7%BD%AE
        findViewById(R.id.inspect_btn_setLocation).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GrowingAutotracker.get().setLocation(100.0f, 100.0f);
            }
        });

        // 清除经纬度
        // https://growingio.github.io/growingio-sdk-docs/docs/android/base/api#5-%E6%B8%85%E9%99%A4%E7%94%A8%E6%88%B7%E7%9A%84%E5%9C%B0%E7%90%86%E4%BD%8D%E7%BD%AE
        findViewById(R.id.inspect_btn_clearLocation).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GrowingAutotracker.get().cleanLocation();
            }
        });

        // 发送自定义事件
        // https://growingio.github.io/growingio-sdk-docs/docs/android/base/api#6-%E8%AE%BE%E7%BD%AE%E5%9F%8B%E7%82%B9%E4%BA%8B%E4%BB%B6
        findViewById(R.id.inspect_btn_trackCustom).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HashMap<String, String> map = new HashMap<>();
                map.put("color", "red");
                map.put("id", "1001");
                GrowingAutotracker.get().trackCustomEvent("eventName", map, "1001", "people");
            }
        });

        // 发送登录用户属性
        // https://growingio.github.io/growingio-sdk-docs/docs/android/base/api#7-%E8%AE%BE%E7%BD%AE%E7%99%BB%E5%BD%95%E7%94%A8%E6%88%B7%E5%B1%9E%E6%80%A7
        findViewById(R.id.inspect_btn_setUserAttributes).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HashMap<String, String> map = new HashMap<>();
                map.put("color", "red");
                map.put("id", "1001");
                GrowingAutotracker.get().setLoginUserAttributes(map);
            }
        });

        final TextView tvDeviceId = findViewById(R.id.inspect_tv_deviceId);
        // 获取设备ID
        // https://growingio.github.io/growingio-sdk-docs/docs/android/base/api#8-%E8%8E%B7%E5%8F%96%E8%AE%BE%E5%A4%87id
        findViewById(R.id.inspect_btn_getDeviceId).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tvDeviceId.setText(GrowingAutotracker.get().getDeviceId());
            }
        });

        // 设置页面别名
        // https://growingio.github.io/growingio-sdk-docs/docs/android/base/api#9-%E8%AE%BE%E7%BD%AE%E9%A1%B5%E9%9D%A2%E5%88%AB%E5%90%8D
        findViewById(R.id.inspect_btn_setPageAlias).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InspectActivity.this.startActivity(new Intent(InspectActivity.this, PageAliasActivity.class));
            }
        });

        // 设置忽略page
        // https://growingio.github.io/growingio-sdk-docs/docs/android/base/api#10-%E8%AE%BE%E7%BD%AE%E5%BF%BD%E7%95%A5%E7%9A%84%E9%A1%B5%E9%9D%A2
        findViewById(R.id.inspect_btn_ignorePage).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InspectActivity.this.startActivity(new Intent(InspectActivity.this, PageIgnoreActivity.class));
            }
        });

        // 设置忽略view
        // https://growingio.github.io/growingio-sdk-docs/docs/android/base/api#11-%E8%AE%BE%E7%BD%AE%E5%BF%BD%E7%95%A5%E7%9A%84view
        findViewById(R.id.inspect_btn_ignoreView).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 忽略当前button相关事件
                GrowingAutotracker.get().ignoreView(v, IgnorePolicy.IGNORE_SELF);
            }
        });

        // 设置采集/忽略view的曝光事件
        // https://growingio.github.io/growingio-sdk-docs/docs/android/base/api#12-%E8%AE%BE%E7%BD%AE%E9%87%87%E9%9B%86view%E7%9A%84%E6%9B%9D%E5%85%89%E4%BA%8B%E4%BB%B6
        // https://growingio.github.io/growingio-sdk-docs/docs/android/base/api#13-%E5%81%9C%E6%AD%A2%E9%87%87%E9%9B%86view%E7%9A%84%E6%9B%9D%E5%85%89%E4%BA%8B%E4%BB%B6
        findViewById(R.id.inspect_btn_imp).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InspectActivity.this.startActivity(new Intent(InspectActivity.this, ImpActivity.class));
            }
        });

        // 设置view唯一标识
        // https://growingio.github.io/growingio-sdk-docs/docs/android/base/api#14-%E8%AE%BE%E7%BD%AEview%E5%94%AF%E4%B8%80tag
        findViewById(R.id.inspect_btn_setTag).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 给当前button设置唯一标识
                GrowingAutotracker.get().setUniqueTag(v, "tag");
            }
        });

        // 设置WebView接口调用
        // https://growingio.github.io/growingio-sdk-docs/docs/android/base/api#15-webview%E6%A1%A5%E6%8E%A5
        findViewById(R.id.inspect_btn_bridgeWebView).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InspectActivity.this.startActivity(new Intent(InspectActivity.this, WebViewActivity.class));
            }
        });
    }
}
