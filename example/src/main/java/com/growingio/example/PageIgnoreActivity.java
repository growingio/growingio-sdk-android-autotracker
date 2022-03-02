package com.growingio.example;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.growingio.android.sdk.autotrack.GrowingAutotracker;
import com.growingio.android.sdk.autotrack.IgnorePolicy;

public class PageIgnoreActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_page_ignore);

        // 忽略当前页面所有页面事件
        // https://growingio.github.io/growingio-sdk-docs/docs/android/base/api#10-%E8%AE%BE%E7%BD%AE%E5%BF%BD%E7%95%A5%E7%9A%84%E9%A1%B5%E9%9D%A2
        GrowingAutotracker.get().ignorePage(this, IgnorePolicy.IGNORE_ALL);
    }
}
