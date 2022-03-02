package com.growingio.example;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.growingio.android.sdk.autotrack.GrowingAutotracker;

public class ImpActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_imp);

        Button btnView = findViewById(R.id.imp_btn_view);

        findViewById(R.id.imp_btn_trackImp).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 采集view的曝光事件，从不可见到可见状态发送曝光事件
                // https://growingio.github.io/growingio-sdk-docs/docs/android/base/api#12-%E8%AE%BE%E7%BD%AE%E9%87%87%E9%9B%86view%E7%9A%84%E6%9B%9D%E5%85%89%E4%BA%8B%E4%BB%B6
                GrowingAutotracker.get().trackViewImpression(btnView, "btnView 曝光");
            }
        });

        findViewById(R.id.imp_btn_stopImp).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 不再采集view的曝光事件
                // https://growingio.github.io/growingio-sdk-docs/docs/android/base/api#13-%E5%81%9C%E6%AD%A2%E9%87%87%E9%9B%86view%E7%9A%84%E6%9B%9D%E5%85%89%E4%BA%8B%E4%BB%B6
                GrowingAutotracker.get().stopTrackViewImpression(btnView);
            }
        });

        findViewById(R.id.imp_btn_visible).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnView.setVisibility(View.VISIBLE);
            }
        });

        findViewById(R.id.imp_btn_gone).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnView.setVisibility(View.GONE);
            }
        });
    }
}
