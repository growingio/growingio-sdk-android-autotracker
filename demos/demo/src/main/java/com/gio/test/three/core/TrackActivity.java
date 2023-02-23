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
import com.growingio.android.sdk.autotrack.GrowingAutotracker;
import com.growingio.android.sdk.track.events.CustomEvent;
import com.growingio.android.sdk.track.middleware.advert.DeepLinkCallback;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ModuleEntry("埋点SDK测试")
public class TrackActivity extends Activity {
    private static final String TAG = "TrackActivity";

    private static final String TRACK_CUSTOM_EVENT = "TrackCustomEvent";
    private static final String SET_USER_ID_ZHANGSAN = "设置账号为张三";
    private static final String SET_USER_ID_NULL = "清除账号设置";
    private static final String SET_USER_ID_LISI = "设置账号为李四,手机号";
    private static final String SET_LOCATION = "设置位置";
    private static final String CLOSE_DATA_COLLECTION = "关闭数据收集";
    private static final String OPEN_DATA_COLLECTION = "开启数据收集";
    private static final String TRACK_TIMER_START = "start timer";
    private static final String TRACK_TIMER_PAUSE = "pause timerId";
    private static final String TRACK_TIMER_RESUME = "resume timerId";
    private static final String TRACK_TIMER_END = "end timerId";
    private static final String REMOVE_TIMER = "remove timer";
    private static final String CLEAR_TRACK_TIMER = "clear all timer";
    private static final String DO_DEEPLINK = "doDeepLinkByUrl";
    private String timerId1 = "";
    private String timerId2 = "";

    private static final String[] ITEMS = {
            TRACK_CUSTOM_EVENT,
            SET_USER_ID_ZHANGSAN,
            SET_USER_ID_NULL,
            SET_USER_ID_LISI,
            SET_LOCATION,
            OPEN_DATA_COLLECTION,
            CLOSE_DATA_COLLECTION,
            TRACK_TIMER_START,
            TRACK_TIMER_PAUSE,
            TRACK_TIMER_RESUME,
            TRACK_TIMER_END,
            REMOVE_TIMER,
            CLEAR_TRACK_TIMER,
            DO_DEEPLINK
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
                GrowingAutotracker.get().trackCustomEvent("registerSuccess");

                Map<String, String> map = new HashMap<>();
                map.put("name", "June");
                map.put("age", "12");
                GrowingAutotracker.get().trackCustomEvent("registerSuccess", map);

                break;
            case SET_USER_ID_ZHANGSAN:
                GrowingAutotracker.get().setLoginUserId("zhangsan");
                break;
            case SET_USER_ID_NULL:
                GrowingAutotracker.get().cleanLoginUserId();
                break;
            case SET_USER_ID_LISI:
                GrowingAutotracker.get().setLoginUserId("lisi", "123456789asd");
                break;
            case SET_LOCATION:
                GrowingAutotracker.get().setLocation(100.0, 100.0);
                break;
            case OPEN_DATA_COLLECTION:
                GrowingAutotracker.get().setDataCollectionEnabled(true);
                break;
            case CLOSE_DATA_COLLECTION:
                GrowingAutotracker.get().setDataCollectionEnabled(false);
                break;
            case DO_DEEPLINK:
                GrowingAutotracker.get().doDeepLinkByUrl("https://ads-uat.growingio.cn/k4vsdOx", new DeepLinkCallback() {
                    @Override
                    public void onReceive(Map<String, String> params, int error, long appAwakePassedTime) {
                        Log.e(TAG, String.valueOf(params));
                    };
                });
                break;
            case TRACK_TIMER_START:
                timerId1 = GrowingAutotracker.get().trackTimerStart("event_1");
                timerId2 = GrowingAutotracker.get().trackTimerStart("event_2");
                GrowingAutotracker.get().trackTimerStart("event_3");
                GrowingAutotracker.get().trackTimerStart("event_4");
                GrowingAutotracker.get().trackTimerStart("event_5");
                GrowingAutotracker.get().trackTimerStart("event_6");
                break;
            case TRACK_TIMER_PAUSE:
                GrowingAutotracker.get().trackTimerPause(timerId1);
                break;
            case TRACK_TIMER_RESUME:
                GrowingAutotracker.get().trackTimerResume(timerId1);
                break;
            case TRACK_TIMER_END:
                Map<String, String> map2 = new HashMap<>();
                map2.put("name", "June");
                map2.put("age", "12");
                GrowingAutotracker.get().trackTimerEnd(timerId1, map2);

                List<String> list1 = new ArrayList<>();
                list1.add("aaaa");
                list1.add("bbbb");
                Map<String, String> map3 = CustomEvent.AttributesBuilder.getAttributesBuilder()
                        .addAttribute("key1", "value1")
                        .addAttribute("key2", Arrays.asList(1, 2, 3, 4))
                        .addAttribute(null, "value3")
                        .addAttribute(null, Arrays.asList(5, 4, 3, 2))
                        .addAttribute("key5", Arrays.asList())
                        .addAttribute(null, (List) null)
                        .addAttribute(null, (String) null)
                        .addAttribute("key8", "value8")
                        .addAttribute("key9", list1)
                        .addAttribute("", Arrays.asList("", ""))
                        .addAttribute("key10", Arrays.asList(null, "1"))
                        .getAttributes();

                GrowingAutotracker.get().trackTimerEnd(timerId2, map3);

                break;
            case REMOVE_TIMER:
                GrowingAutotracker.get().removeTimer(timerId1);
                break;
            case CLEAR_TRACK_TIMER:
                GrowingAutotracker.get().clearTrackTimer();
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + itemString);
        }
    }
}
