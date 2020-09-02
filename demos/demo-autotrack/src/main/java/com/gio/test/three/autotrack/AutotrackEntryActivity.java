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

package com.gio.test.three.autotrack;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

import com.gio.test.three.ModuleEntry;
import com.gio.test.three.autotrack.activity.ClickTestActivity;
import com.gio.test.three.autotrack.activity.DialogTestActivity;
import com.gio.test.three.autotrack.activity.ExpandableListSubActivity;
import com.gio.test.three.autotrack.activity.ExpandableListViewActivity;
import com.gio.test.three.autotrack.activity.HideFragmentActivity;
import com.gio.test.three.autotrack.activity.LambdaActivity;
import com.gio.test.three.autotrack.activity.NavFragmentActivity;
import com.gio.test.three.autotrack.activity.NestedFragmentActivity;
import com.gio.test.three.autotrack.activity.RecyclerViewImpActivity;
import com.gio.test.three.autotrack.activity.TabFragmentActivity;
import com.gio.test.three.autotrack.activity.WebViewActivity;
import com.gio.test.three.autotrack.activity.X5WebViewActivity;
import com.gio.test.three.autotrack.activity.ui.login.LoginActivity;
import com.growingio.android.sdk.autotrack.GrowingAutotracker;
import com.growingio.android.sdk.autotrack.IgnorePolicy;
import com.growingio.android.sdk.autotrack.view.ViewAttributeUtil;
import com.growingio.android.sdk.track.GrowingTracker;
import com.growingio.android.sdk.track.interfaces.ResultCallback;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

@ModuleEntry("无埋点SDK测试")
public class AutotrackEntryActivity extends Activity {
    private static final String TAG = "AutotrackEntryActivity";

    private static final String GO_TO_NESTED_FRAGMENT_ACTIVITY = "Go To NestedFragmentActivity";
    private static final String GO_TO_HIDE_FRAGMENT_ACTIVITY = "Go To HideFragmentActivity";
    private static final String GO_TO_NAV_FRAGMENT_ACTIVITY = "Go To NavFragmentActivity";
    private static final String GO_TO_TAB_FRAGMENT_ACTIVITY = "Go To TabFragmentActivity";

    private static final String GO_TO_WEB_VIEW_ACTIVITY = "Go To WebViewActivity";
    private static final String GO_TO_X5_WEB_VIEW_ACTIVITY = "Go To X5WebView Activity";
    private static final String GO_TO_X5_WEB_VIEW_DEBUG_ACTIVITY = "Go To X5WebView Debug Activity";
    private static final String GO_TO_DIALOG_TEST_ACTIVITY = "Go To DialogTestActivity";
    private static final String GO_TO_DIALOG_LAMBDA_ACTIVITY = "Go To LambdaActivity";
    private static final String GO_TO_DIALOG_RECYCLER_VIEW_IMP_ACTIVITY = "Go To RecyclerViewImpActivity";
    private static final String GO_TO_CLICK_TEST_ACTIVITY = "Go To ClickTestActivity";
    private static final String GO_TO_EXPANDABLE_LIST_VIEW_ACTIVITY = "Go To ExpandableListViewActivity";
    private static final String GO_TO_EXPANDABLE_LIST_SUB_ACTIVITY = "Go To ExpandableListSubActivity";
    private static final String GO_TO_LOGIN_ACTIVITY = "Go To LoginActivity";


    private static final String[] ITEMS = {
            GO_TO_NESTED_FRAGMENT_ACTIVITY,
            GO_TO_HIDE_FRAGMENT_ACTIVITY,
            GO_TO_NAV_FRAGMENT_ACTIVITY,
            GO_TO_TAB_FRAGMENT_ACTIVITY,
            GO_TO_WEB_VIEW_ACTIVITY,
            GO_TO_X5_WEB_VIEW_DEBUG_ACTIVITY,
            GO_TO_X5_WEB_VIEW_ACTIVITY,
            GO_TO_DIALOG_TEST_ACTIVITY,
            GO_TO_DIALOG_LAMBDA_ACTIVITY,
            GO_TO_DIALOG_RECYCLER_VIEW_IMP_ACTIVITY,
            GO_TO_CLICK_TEST_ACTIVITY,
            GO_TO_EXPANDABLE_LIST_VIEW_ACTIVITY,
            GO_TO_EXPANDABLE_LIST_SUB_ACTIVITY,
            GO_TO_LOGIN_ACTIVITY,
    };

    private ListView mListView;
    private ArrayAdapter<String> mArrayAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ignoreExample();

        setContentView(R.layout.activity_autotrck_entry);
        EditText search = findViewById(R.id.et_search);

        mListView = findViewById(R.id.content);
        List<String> items = new ArrayList<>(Arrays.asList(ITEMS));
        mArrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, items);
        mListView.setAdapter(mArrayAdapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String item = mArrayAdapter.getItem(position);
                Log.e(TAG, "onItemClick: " + item);
                handleItemClick(item);
            }
        });

        search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchChanged(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    private void searchChanged(String text) {
        List<String> newItems = new ArrayList<>();
        for (String item : ITEMS) {
            if (item.toLowerCase().contains(text.toLowerCase())) {
                newItems.add(item);
            }
        }
        mArrayAdapter.clear();
        mArrayAdapter.addAll(newItems);
        mArrayAdapter.notifyDataSetChanged();
    }

    private void ignoreExample() {
        View view = getWindow().getDecorView();

        GrowingAutotracker.get().ignorePage(this, IgnorePolicy.IGNORE_ALL);
        GrowingAutotracker.get().ignoreView(view, IgnorePolicy.IGNORE_SELF);
    }

    private void autotrackExample() {
        View view = getWindow().getDecorView();
        GrowingAutotracker.get().setUniqueTag(view, "current_unique_tag");
        String uid = ViewAttributeUtil.getCustomId(view);
        Log.d(TAG, "unique id = " + uid);

        GrowingAutotracker.get().setPageAlias(this, "autotrack_entry_alias");
        HashMap<String, String> map = new HashMap<>();
        map.put("name", "hello");
        map.put("age", "18");

        GrowingAutotracker.get().setPageAttributes(this, map);

        GrowingTracker.get().getDeviceId(new ResultCallback<String>() {
            @Override
            public void onResult(@Nullable String result) {
                Log.d(TAG, "device id = " + result);
            }
        });

        HashMap<String, String> impAttrs = new HashMap<>();
        impAttrs.put("name", "hello");
        impAttrs.put("age", "18");
    }

    private void handleItemClick(String itemString) {
        autotrackExample();

        switch (itemString) {
            case GO_TO_NESTED_FRAGMENT_ACTIVITY:
                startActivity(new Intent(this, NestedFragmentActivity.class));
                break;
            case GO_TO_HIDE_FRAGMENT_ACTIVITY:
                startActivity(new Intent(this, HideFragmentActivity.class));
                break;
            case GO_TO_NAV_FRAGMENT_ACTIVITY:
                startActivity(new Intent(this, NavFragmentActivity.class));
                break;
            case GO_TO_TAB_FRAGMENT_ACTIVITY:
                startActivity(new Intent(this, TabFragmentActivity.class));
                break;
            case GO_TO_WEB_VIEW_ACTIVITY:
                startActivity(new Intent(this, WebViewActivity.class));
                break;
            case GO_TO_X5_WEB_VIEW_DEBUG_ACTIVITY:
                Intent intent = new Intent(this, X5WebViewActivity.class);
                intent.putExtra("LOAD_URL", "http://debugtbs.qq.com");
                startActivity(intent);
                break;
            case GO_TO_X5_WEB_VIEW_ACTIVITY:
                startActivity(new Intent(this, X5WebViewActivity.class));
                break;
            case GO_TO_DIALOG_TEST_ACTIVITY:
                startActivity(new Intent(this, DialogTestActivity.class));
                break;
            case GO_TO_DIALOG_LAMBDA_ACTIVITY:
                startActivity(new Intent(this, LambdaActivity.class));
                break;
            case GO_TO_DIALOG_RECYCLER_VIEW_IMP_ACTIVITY:
                startActivity(new Intent(this, RecyclerViewImpActivity.class));
                break;
            case GO_TO_CLICK_TEST_ACTIVITY:
                startActivity(new Intent(this, ClickTestActivity.class));
                break;
            case GO_TO_EXPANDABLE_LIST_VIEW_ACTIVITY:
                startActivity(new Intent(this, ExpandableListViewActivity.class));
                break;
            case GO_TO_EXPANDABLE_LIST_SUB_ACTIVITY:
                startActivity(new Intent(this, ExpandableListSubActivity.class));
                break;
            case GO_TO_LOGIN_ACTIVITY:
                startActivity(new Intent(this, LoginActivity.class));
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + itemString);
        }
    }
}
