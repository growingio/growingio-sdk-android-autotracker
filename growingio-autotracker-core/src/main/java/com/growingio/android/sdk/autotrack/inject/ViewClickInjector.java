/*
 *   Copyright (C) 2020 Beijing Yishu Technology Co., Ltd.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package com.growingio.android.sdk.autotrack.inject;

import android.view.View;
import android.widget.AdapterView;
import android.widget.ExpandableListView;
import android.widget.RadioGroup;
import android.widget.Spinner;

import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.tabs.TabLayout;


public class ViewClickInjector {
    private static final String TAG = "ViewClickInjector";

    private ViewClickInjector() {
    }

    public static void viewOnClick(View.OnClickListener listener, View view) {
        ViewClickProvider.viewOnClickListener(view);
    }

    public static void adapterViewOnItemClick(AdapterView.OnItemClickListener listener, AdapterView adapterView, View view, int position, long id) {
        ViewClickProvider.adapterViewItemClick(view);
    }

    public static void adapterViewOnItemSelected(AdapterView.OnItemSelectedListener listener, AdapterView adapterView, View view, int position, long id) {
        if (adapterView instanceof Spinner) {
            // 目前只需要将Spinner的onItemSelected回调触发点击事件,因为Spinner的元素点击只会触发onItemSelected回调
            ViewClickProvider.spinnerViewOnClick(view);
        }
    }

    public static void expandableListViewOnGroupClick(ExpandableListView.OnGroupClickListener listener, ExpandableListView parent, View v, int groupPosition, long id) {
        ViewClickProvider.expandableListViewOnGroupClick(v);
    }

    public static void expandableListViewOnChildClick(ExpandableListView.OnChildClickListener listener, ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
        ViewClickProvider.expandableListViewOnChildClick(v);
    }

    public static void radioGroupOnChecked(RadioGroup.OnCheckedChangeListener listener, RadioGroup radioGroup, int i) {
        ViewClickProvider.radioGroupOnCheck(radioGroup.findViewById(radioGroup.getCheckedRadioButtonId()));
    }

    public static void materialButtonToggleGroupOnButtonChecked(MaterialButtonToggleGroup.OnButtonCheckedListener listener, MaterialButtonToggleGroup group, int checkedId, boolean isChecked) {
        if (isChecked) {
            ViewClickProvider.materialButtonToggleGroupOnButtonCheck(group.findViewById(checkedId));
        }
    }

    public static void tabLayoutOnTabSelected(TabLayout.OnTabSelectedListener listener, TabLayout.Tab tab) {
        ViewClickProvider.tabLayoutOnTabSelected(tab);
    }
}
