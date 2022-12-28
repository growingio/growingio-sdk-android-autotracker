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
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.View;

import com.gio.test.three.autotrack.R;
import com.gio.test.three.autotrack.fragments.GreenFragment;
import com.gio.test.three.autotrack.fragments.RedFragment;
import com.growingio.android.sdk.autotrack.GrowingAutotracker;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

public class HideFragmentActivity extends FragmentActivity {
    private final Stack<Fragment> mAllFragments = new Stack<>();
    private static final String TAG = "HideFragmentActivity";
    private int i = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hide_fragment);
        findViewById(R.id.add).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getSupportFragmentManager().beginTransaction().add(R.id.frame_content, createdFragment()).commit();
            }
        });

        findViewById(R.id.remove).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mAllFragments.isEmpty()) {
                    return;
                }
                getSupportFragmentManager().beginTransaction().remove(mAllFragments.pop()).commit();
            }
        });

        findViewById(R.id.hide).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mAllFragments.empty()) {
                    return;
                }
                getSupportFragmentManager().beginTransaction().hide(mAllFragments.peek()).commit();
            }
        });

        findViewById(R.id.show).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mAllFragments.empty()) {
                    return;
                }
                getSupportFragmentManager().beginTransaction().show(mAllFragments.peek()).commit();
            }
        });

    }

    private Fragment createdFragment() {
        if (mAllFragments.isEmpty()) {
            Fragment fragment = new RedFragment();
            if (i % 2 == 0) {
                Map<String, String> map = new HashMap<>();
                map.put("name", "HideFragmentActivity-RedFragment");
                GrowingAutotracker.get().setPageAttributesSupport(fragment, map);
                i++;
            } else {
                i++;
            }
//            GrowingAutotracker.get().setPageAttributesSupport(fragment, null);
            mAllFragments.add(fragment);
            return fragment;
        }

        if (mAllFragments.peek() instanceof GreenFragment) {
            Fragment fragment = new RedFragment();
            mAllFragments.add(fragment);
            return fragment;
        } else {
            Fragment fragment = new GreenFragment();
            mAllFragments.add(fragment);
            return fragment;
        }
    }
}