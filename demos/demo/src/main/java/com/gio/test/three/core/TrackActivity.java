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

package com.gio.test.three.core;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.gio.test.R;
import com.gio.test.three.ModuleEntry;
import com.growingio.android.sdk.track.GrowingTracker;

import java.util.HashMap;
import java.util.Map;

@ModuleEntry("埋点SDK测试")
public class TrackActivity extends Activity {
    private static final String TAG = "TrackActivity";

    private static final String TRACK_CUSTOM_EVENT = "TrackCustomEvent";
    private static final String SET_USER_ID_ZHANGSAN = "设置账号为张三";
    private static final String SET_USER_ID_NULL = "清除账号设置";
    private static final String SET_USER_ID_LISI = "设置账号为李四";

    private static final String[] ITEMS = {
            TRACK_CUSTOM_EVENT,
            SET_USER_ID_ZHANGSAN,
            SET_USER_ID_NULL,
            SET_USER_ID_LISI,
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: ");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_track);
        ListView listView = findViewById(R.id.content);
        final ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, ITEMS);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String item = adapter.getItem(position);
                Log.e(TAG, "onItemClick: " + item);
                handleItemClick(item);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void handleItemClick(String itemString) {
        switch (itemString) {
            case TRACK_CUSTOM_EVENT:
                GrowingTracker.get().trackCustomEvent("registerSuccess");

                Map<String, String> map = new HashMap<>();
                map.put("name", "June");
                map.put("age", "12");
                GrowingTracker.get().trackCustomEvent("registerSuccess", map);

                break;
            case SET_USER_ID_ZHANGSAN:
                GrowingTracker.get().setLoginUserId("zhangsan");
                break;
            case SET_USER_ID_NULL:
                GrowingTracker.get().cleanLoginUserId();
                break;
            case SET_USER_ID_LISI:
                GrowingTracker.get().setLoginUserId("lisi");
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + itemString);
        }
    }
}
