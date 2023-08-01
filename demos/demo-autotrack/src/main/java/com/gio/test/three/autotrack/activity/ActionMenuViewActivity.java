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
package com.gio.test.three.autotrack.activity;

import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ActionMenuView;
import android.widget.Toolbar;

import androidx.annotation.RequiresApi;

import com.gio.test.three.autotrack.R;

public class ActionMenuViewActivity extends Activity {
    private static final String TAG = "ActionMenuViewActivity";

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_action_menu_view);
        Toolbar toolbar = (Toolbar) findViewById(R.id.action_menu_tool);
        toolbar.setTitle("TestActionMenuView");
        setActionBar(toolbar);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        ActionMenuView amv = (ActionMenuView) findViewById(R.id.action_menu_main);
        getMenuInflater().inflate(R.menu.bottom_nav_menu, amv.getMenu());
        amv.setOnMenuItemClickListener(new ActionMenuView.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                Log.e(TAG, "onMenuItemClick: ");
                return true;
            }
        });
        return true;
    }
}