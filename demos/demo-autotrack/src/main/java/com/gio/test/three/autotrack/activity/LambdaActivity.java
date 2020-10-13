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
import android.util.Log;
import android.view.View;

import com.gio.test.three.autotrack.R;
import com.gio.test.three.autotrack.utils.DialogUtil;
import com.growingio.android.sdk.track.log.Logger;

import org.json.JSONException;
import org.json.JSONObject;

public class LambdaActivity extends Activity implements View.OnClickListener {
    private static final String TAG = "LambdaActivity";

    private void beforeClick(View view) {
        Logger.d(TAG, "This is beforeClick");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lambda);

        findViewById(R.id.btn_simple_lambda).setOnClickListener(v -> {
            Logger.d(TAG, "This is Simple Lambda");
        });
        String otherArgs = "This is other args";
        String otherArgs2 = "This is otherArgs2";
        String otherArgs3 = "This is otherArgs3";
        findViewById(R.id.btn_lambda_with_other_arg).setOnClickListener(v -> {
            Log.d(TAG, "withOtherArgs2:" + otherArgs2);
            Log.d(TAG, "with otherArgs: " + otherArgs);
            Log.d(TAG, "with otherArgs3: " + otherArgs3);
            JSONObject json = new JSONObject();
            if (otherArgs3 != null) {
                try {
                    String result = json.getString("test");
                    System.out.println(result);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        findViewById(R.id.btn_before_method_lambda).setOnClickListener(this::beforeClick);
        findViewById(R.id.btn_after_method_lambda).setOnClickListener(this::afterClick);
        findViewById(R.id.btn_implement_interface_lambda).setOnClickListener(this::onClick);
        findViewById(R.id.btn_no_lambda).setOnClickListener(this);
        findViewById(R.id.btn_another_class_lambda).setOnClickListener(new AnotherClass()::anotherClick);
        findViewById(R.id.btn_static_method_lambda).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogUtil.showDialog(LambdaActivity.this, "title", "确定", v1 -> {
                    Log.e(TAG, "onClick: ");
                }, "取消", DialogUtil::cancelDialog);
            }
        });
    }

    private void afterClick(View view) {
        Logger.d(TAG, "This is afterClick");
    }

    @Override
    public void onClick(View v) {
        Logger.d(TAG, "onClick: The OnClickListener's implement ");
    }

    private static class AnotherClass {

        public void anotherClick(View view) {
            Logger.d(TAG, "This is anotherClick");
        }
    }
}