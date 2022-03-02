package com.growingio.example;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.growingio.android.sdk.autotrack.GrowingAutotracker;

public class PageAliasActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_page_alias);

        // 设置页面的名称为PageAliasActivityAliasName
        // https://growingio.github.io/growingio-sdk-docs/docs/android/base/api#9-%E8%AE%BE%E7%BD%AE%E9%A1%B5%E9%9D%A2%E5%88%AB%E5%90%8D
        GrowingAutotracker.get().setPageAlias(this, "PageAliasActivityAliasName");
    }
}
