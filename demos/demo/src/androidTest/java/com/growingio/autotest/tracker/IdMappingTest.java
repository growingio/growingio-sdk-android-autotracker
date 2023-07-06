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

package com.growingio.autotest.tracker;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.gio.test.three.DemoApplication;
import com.gio.test.three.core.TrackActivity;
import com.growingio.android.sdk.CoreConfiguration;
import com.growingio.android.sdk.autotrack.AutotrackConfiguration;
import com.growingio.android.sdk.autotrack.GrowingAutotracker;
import com.growingio.autotest.EventsTest;
import com.growingio.autotest.TestTrackConfiguration;
import com.growingio.autotest.help.Awaiter;
import com.growingio.autotest.help.BeforeAppOnCreate;
import com.growingio.autotest.help.DataHelper;
import com.growingio.autotest.help.MockEventsApiServer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.concurrent.atomic.AtomicBoolean;


@RunWith(AndroidJUnit4.class)
@LargeTest
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class IdMappingTest extends EventsTest {
    private static AutotrackConfiguration sTestTrackConfiguration;
    private final String userKey = "outlook 邮箱";

    @BeforeAppOnCreate
    public static void beforeAppOnCreate() {
        DataHelper.deleteEventsDatabase();
        sTestTrackConfiguration = TestTrackConfiguration.getTestConfig()
                .setIdMappingEnabled(true);
        DemoApplication.setConfiguration(sTestTrackConfiguration);
    }

    @Override
    @Before
    public void setUp() throws IOException {
        super.setUp();
        getEventsApiServer().setCheckUserId(false);
        getEventsApiServer().setCheckSessionId(false);
    }

    /**
     * 开关开启，可设置userKey
     */
    @Test
    public void test1CheckUserKeySetSuccess() {
        final AtomicBoolean receivedEvent = new AtomicBoolean(false);
        getEventsApiServer().setOnReceivedEventListener(new MockEventsApiServer.OnReceivedEventListener() {
            @Override
            protected void onReceivedCustomEvents(JSONArray jsonArray) throws JSONException {
                JSONObject jsonObject = jsonArray.getJSONObject(0);
                if (jsonObject.getString("eventName").equals("testUserKey")) {
                    if (jsonObject.getString("userKey").equals(userKey) &&
                            jsonObject.getString("userId").equals("zhangsan")) {
                        receivedEvent.set(true);
                    }
                }
            }
        });
        ActivityScenario<TrackActivity> scenario = ActivityScenario.launch(TrackActivity.class);
        GrowingAutotracker.get().setLoginUserId("zhangsan", userKey);
        GrowingAutotracker.get().trackCustomEvent("testUserKey");
        Awaiter.untilTrue(receivedEvent);
    }

    /**
     * userKey持久化验证：app冷启动带有上次设置的userKey
     */
    @Test
    public void test2UserKeyLocalStorage() {
        final AtomicBoolean receivedVisit = new AtomicBoolean(false);
        getEventsApiServer().setOnReceivedEventListener(new MockEventsApiServer.OnReceivedEventListener() {
            @Override
            protected void onReceivedVisitEvents(JSONArray jsonArray) throws JSONException {
                JSONObject jsonObject = jsonArray.getJSONObject(0);
                if (jsonObject.getString("userKey").equals(userKey) &&
                        jsonObject.getString("userId").equals("zhangsan")) {
                    receivedVisit.set(true);
                }
            }
        });
        ActivityScenario<TrackActivity> scenario = ActivityScenario.launch(TrackActivity.class);
        Awaiter.untilTrue(receivedVisit);
        scenario.close();
    }

    /**
     * 特殊场景：
     * 1. 不设置userKey
     * 2. 设置userKey为null
     * 3. 关闭IdMapping开关，不能设置userKey
     */
//    @Test
    public void test3SpecialValidation() {
        final AtomicBoolean receivedEvent = new AtomicBoolean(false);
        final AtomicBoolean receivedVisit = new AtomicBoolean(false);
        getEventsApiServer().setOnReceivedEventListener(new MockEventsApiServer.OnReceivedEventListener() {

            @Override
            protected void onReceivedVisitEvents(JSONArray jsonArray) throws JSONException {
                JSONObject jsonObject = jsonArray.getJSONObject(0);
                if (jsonObject.optString("userKey").isEmpty()) {
                    receivedVisit.set(true);
                }
            }

            @Override
            protected void onReceivedCustomEvents(JSONArray jsonArray) throws JSONException {
                JSONObject jsonObject = jsonArray.getJSONObject(0);
                if (jsonObject.getString("eventName").equals("testDisableIdMapping")) {
                    if (jsonObject.optString("userKey").isEmpty() &&
                            jsonObject.getString("userId").equals("lisi")) {
                        receivedEvent.set(true);
                    }
                }
            }
        });
        ActivityScenario<TrackActivity> scenario = ActivityScenario.launch(TrackActivity.class);
        GrowingAutotracker.get().setLoginUserId("wangwu");
        Awaiter.untilTrue(receivedVisit);

        GrowingAutotracker.get().setLoginUserId("zhangsan", null);
        Awaiter.untilTrue(receivedVisit);

        try {
            Field field = findFieldObj(CoreConfiguration.class, "mIdMappingEnabled");
            field.set(sTestTrackConfiguration.core(), false);
            System.out.println(sTestTrackConfiguration.isIdMappingEnabled());
        } catch (Exception ignored) {
        }
        GrowingAutotracker.get().setLoginUserId("lisi", "13456789");
        GrowingAutotracker.get().trackCustomEvent("testDisableIdMapping");
        Awaiter.untilTrue(receivedEvent);
        scenario.close();
    }

    public static Field findFieldObj(Class<?> current, String fieldName) {
        while (current != Object.class) {
            try {
                Field field = current.getDeclaredField(fieldName);
                field.setAccessible(true);
                return field;
            } catch (NoSuchFieldException e) {
                current = current.getSuperclass();
            }
        }
        return null;
    }
}

