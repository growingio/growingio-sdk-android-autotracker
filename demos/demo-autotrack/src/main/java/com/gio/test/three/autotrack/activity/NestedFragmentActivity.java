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

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

import com.gio.test.three.autotrack.R;
import com.gio.test.three.autotrack.fragments.GreenFragment;
import com.gio.test.three.autotrack.fragments.RedFragment;

public class NestedFragmentActivity extends FragmentActivity {
    private static final String TAG  = "FragmentsActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.e(TAG, "onCreate: ");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fragments);
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment1, new GreenFragment()).commit();
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment2, new RedFragment()).commit();
    }

    @Override
    protected void onResume() {
        Log.e(TAG, "onResume: ");
        super.onResume();
    }
}