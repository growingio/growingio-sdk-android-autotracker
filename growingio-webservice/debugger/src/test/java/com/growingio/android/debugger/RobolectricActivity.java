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
package com.growingio.android.debugger;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.growingio.android.sdk.autotrack.page.ActivityPage;

import static android.widget.LinearLayout.VERTICAL;

/**
 * <p>
 * test activity
 *
 * @author cpacm 2021/6/10
 */
public class RobolectricActivity extends Activity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TextView textView = new TextView(this);
        textView.setText("this is cpacm");
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //can circle
            }
        });

        ImageView imageView = new ImageView(this);
        imageView.setTag(com.growingio.android.sdk.autotrack.R.id.growing_tracker_view_page, new ActivityPage(this));

        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(VERTICAL);
        linearLayout.addView(textView);
        linearLayout.addView(imageView);
        setContentView(linearLayout);
    }
}
