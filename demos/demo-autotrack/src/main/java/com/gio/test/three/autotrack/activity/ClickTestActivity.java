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
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.RadioGroup;
import android.widget.RatingBar;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Switch;

import com.gio.test.three.autotrack.R;

public class ClickTestActivity extends Activity {
    private static final String TAG = "ClickTestActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_click_test);
        findViewById(R.id.content_parent).setOnClickListener(v -> Log.e(TAG, "content_parent click"));
        findViewById(R.id.btn_test_click).setOnClickListener(v -> Log.e(TAG, "btn_test_click click"));

        Spinner spinner = findViewById(R.id.spinner_test);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.e(TAG, "onItemSelected: ");
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                Log.e(TAG, "onNothingSelected: ");
            }
        });

        CheckBox checkBox1 = findViewById(R.id.check_box_android);
        checkBox1.setOnCheckedChangeListener((buttonView, isChecked) -> Log.e(TAG, "checkBox1"));
        CheckBox checkBox2 = findViewById(R.id.check_box_ios);
        checkBox2.setOnCheckedChangeListener((buttonView, isChecked) -> Log.e(TAG, "checkBox2"));

        RadioGroup radioGroup = findViewById(R.id.radio_group_gender);
        radioGroup.setOnCheckedChangeListener((group, checkedId) -> {

        });

        Switch switchBtn = findViewById(R.id.switch1);
        switchBtn.setOnCheckedChangeListener((buttonView, isChecked) -> {

        });

        SeekBar seekBar = findViewById(R.id.seek_bar);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        RatingBar ratingBar = findViewById(R.id.rating_bar);
        ratingBar.setOnRatingBarChangeListener((ratingBar1, rating, fromUser) -> {

        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.bottom_nav_menu, menu);
        return true;
    }

//    @Override
//    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
//        return super.onOptionsItemSelected(item);
//    }
}