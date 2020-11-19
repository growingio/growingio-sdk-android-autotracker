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

import android.content.Context;
import android.os.Build;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;

import androidx.lifecycle.Lifecycle;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.gio.test.three.DemoApplication;
import com.gio.test.three.MainActivity;
import com.google.common.truth.Truth;
import com.growingio.android.sdk.track.BuildConfig;
import com.growingio.android.sdk.track.GrowingTracker;
import com.growingio.android.sdk.track.providers.ConfigurationProvider;
import com.growingio.autotest.EventsTest;
import com.growingio.autotest.TestTrackConfiguration;
import com.growingio.autotest.help.Awaiter;
import com.growingio.autotest.help.BeforeAppOnCreate;
import com.growingio.autotest.help.DataHelper;
import com.growingio.autotest.help.MockEventsApiServer;
import com.growingio.autotest.help.Uninterruptibles;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static java.util.concurrent.TimeUnit.SECONDS;


@RunWith(AndroidJUnit4.class)
@LargeTest
public class SessionEventsTest extends EventsTest {
    private static final String APP_CHANNEL = "AutoTest";

    private String mSessionId;
    private String mNewSessionId;
    private String mLoginUserId;
    private long mVisitTimestamp;

    @Rule
    public ActivityScenarioRule<MainActivity> scenarioRule = new ActivityScenarioRule<>(MainActivity.class);

    @BeforeAppOnCreate
    public static void beforeAppOnCreate() {
        DataHelper.deleteAllData();
        DemoApplication.setIsAutotracker(false);
        DemoApplication.setConfiguration(new TestTrackConfiguration()
                .setSessionInterval(10)
                .setChannel(APP_CHANNEL)
        );
    }

    private void checkVisitEvent(JSONArray jsonArray) throws JSONException {
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            Truth.assertThat(jsonObject.getString("networkState")).isIn(Arrays.asList("2G", "3G", "4G", "5G", "WIFI", "UNKNOWN"));
            Truth.assertThat(jsonObject.getString("appChannel")).isEqualTo(APP_CHANNEL);

            DisplayMetrics metrics = new DisplayMetrics();
            Display display = ((WindowManager) ApplicationProvider.getApplicationContext().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                display.getRealMetrics(metrics);
            } else {
                display.getMetrics(metrics);
            }
            Truth.assertThat(jsonObject.getInt("screenHeight")).isEqualTo(metrics.heightPixels);
            Truth.assertThat(jsonObject.getInt("screenWidth")).isEqualTo(metrics.widthPixels);

            Truth.assertThat(jsonObject.getString("deviceBrand")).isEqualTo(Build.BRAND == null ? "UNKNOWN" : Build.BRAND);
            Truth.assertThat(jsonObject.getString("deviceModel")).isEqualTo(Build.MODEL == null ? "UNKNOWN" : Build.MODEL);
            Truth.assertThat(jsonObject.getString("deviceType")).isEqualTo("PHONE");
            Truth.assertThat(jsonObject.getString("appName")).isEqualTo("demos");
            Truth.assertThat(jsonObject.getString("appVersion")).isEqualTo("3.2");
            Truth.assertThat(jsonObject.getString("language")).isNotEmpty();

            if (jsonObject.has("latitude")) {
                Truth.assertThat(jsonObject.getDouble("latitude")).isGreaterThan(0);
                Truth.assertThat(jsonObject.getDouble("longitude")).isGreaterThan(0);
            }
            if (jsonObject.has("imei")) {
                Truth.assertThat(jsonObject.getString("imei")).isNotEmpty();
            }
            if (jsonObject.has("androidId")) {
                Truth.assertThat(jsonObject.getString("androidId")).isNotEmpty();
            }
            if (jsonObject.has("oaid")) {
                Truth.assertThat(jsonObject.getString("oaid")).isNotEmpty();
            }
            if (jsonObject.has("googleAdvertisingId")) {
                Truth.assertThat(jsonObject.getString("googleAdvertisingId")).isNotEmpty();
            }
            Truth.assertThat(jsonObject.getString("sdkVersion")).isEqualTo(BuildConfig.VERSION_NAME);
        }
    }

    /**
     * 1. 正常发送 VISIT 和 APP_CLOSED
     * 2. APP退到后台时间 < SessionInterval，sessionId不改变
     * 3. APP退到后台时间 ≥ SessionInterval，sessionId改变
     */
    @Test
    public void sessionEventsTest() {
        final AtomicBoolean receivedVisit = new AtomicBoolean(false);
        final AtomicBoolean receivedAppClosed = new AtomicBoolean(false);
        getEventsApiServer().setOnReceivedEventListener(new MockEventsApiServer.OnReceivedEventListener() {
            @Override
            protected void onReceivedVisitEvents(JSONArray jsonArray) throws JSONException {
                checkVisitEvent(jsonArray);
                JSONObject visit = jsonArray.getJSONObject(0);
                mSessionId = visit.getString("sessionId");
                receivedVisit.set(true);
            }

            @Override
            protected void onReceivedAppClosedEvents(JSONArray jsonArray) throws JSONException {
                JSONObject appClosed = jsonArray.getJSONObject(0);
                if (mSessionId.equals(appClosed.getString("sessionId"))) {
                    receivedAppClosed.set(true);
                }
            }
        });
        Awaiter.untilTrue(receivedVisit);

        //To State STOP
        scenarioRule.getScenario().moveToState(Lifecycle.State.CREATED);
        Awaiter.untilTrue(receivedAppClosed);

        receivedAppClosed.set(false);
        getEventsApiServer().setOnReceivedEventListener(new MockEventsApiServer.OnReceivedEventListener() {
            @Override
            protected void onReceivedVisitEvents(JSONArray jsonArray) throws JSONException {
                Truth.assertWithMessage("Received Visit Event").fail();
            }

            @Override
            protected void onReceivedAppClosedEvents(JSONArray jsonArray) throws JSONException {
                JSONObject appClosed = jsonArray.getJSONObject(0);
                if (mSessionId.equals(appClosed.getString("sessionId"))) {
                    receivedAppClosed.set(true);
                }
            }
        });
        //To State RESUMED
        scenarioRule.getScenario().moveToState(Lifecycle.State.RESUMED);
        //To State STOP
        scenarioRule.getScenario().moveToState(Lifecycle.State.CREATED);
        Awaiter.untilTrue(receivedAppClosed);

        receivedVisit.set(false);
        receivedAppClosed.set(false);
        getEventsApiServer().setCheckSessionId(false);

        getEventsApiServer().setOnReceivedEventListener(new MockEventsApiServer.OnReceivedEventListener() {
            @Override
            protected void onReceivedVisitEvents(JSONArray jsonArray) throws JSONException {
                checkVisitEvent(jsonArray);
                JSONObject visit = jsonArray.getJSONObject(0);
                mNewSessionId = visit.getString("sessionId");
                if (!mNewSessionId.equals(mSessionId)) {
                    receivedVisit.set(true);
                }
            }

            @Override
            protected void onReceivedAppClosedEvents(JSONArray jsonArray) throws JSONException {
                JSONObject appClosed = jsonArray.getJSONObject(0);
                String sessionId = appClosed.getString("sessionId");
                if (!mSessionId.equals(sessionId) && mNewSessionId.equals(sessionId)) {
                    receivedAppClosed.set(true);
                }
            }
        });

        long delayTime = ConfigurationProvider.get().getTrackConfiguration().getSessionInterval();
        Uninterruptibles.sleepUninterruptibly(delayTime * 1000 + 1, TimeUnit.MILLISECONDS);

        //To State RESUMED
        scenarioRule.getScenario().moveToState(Lifecycle.State.RESUMED);
        Awaiter.untilTrue(receivedVisit);
        //To State DESTROYED
        scenarioRule.getScenario().moveToState(Lifecycle.State.DESTROYED);
        Awaiter.untilTrue(receivedAppClosed);
    }

    /**
     * 1. LoginUserId设置从空到非空，如果是第一次登陆发visit事件，sessionId不变，否则遵循4
     * 2. LoginUserId设置从非空到空，visit不发送
     * 3. LoginUserId设置从"A"到"B", visit发送，sessionId改变
     * 4. LoginUserId设置从"A"到空再到"B", visit发送，sessionId改变
     * 5. LoginUserId设置从"A"到"A"，visit不发送，sessionId不变
     * <p>
     * 总结出一句话，一个sessionId不能有两个用户ID
     */
    @Test
    public void resendVisitEventByLoginUserIdChangedTest() {
        getEventsApiServer().setCheckSessionId(false);
        getEventsApiServer().setCheckUserId(false);
        final AtomicBoolean receivedVisit = new AtomicBoolean(false);
        getEventsApiServer().setOnReceivedEventListener(new MockEventsApiServer.OnReceivedEventListener() {
            @Override
            protected void onReceivedVisitEvents(JSONArray jsonArray) throws JSONException {
                checkVisitEvent(jsonArray);
                JSONObject visit = jsonArray.getJSONObject(0);
                mSessionId = visit.getString("sessionId");
                mLoginUserId = visit.optString("userId");
                mVisitTimestamp = visit.getLong("timestamp");
                if (TextUtils.isEmpty(mLoginUserId)) {
                    receivedVisit.set(true);
                }
            }
        });
        Awaiter.untilTrue(receivedVisit);

        resendVisitEventLoginUserIdFirstFromNull2NotNull();
        resendVisitEventLoginUserIdFromNotNull2Null();
        resendVisitEventLoginUserIdSecondFromNull2OldNotNull();
        resendVisitEventLoginUserIdFromNotNull2Null();
        resendVisitEventLoginUserIdFromNull2New();
        resendVisitEventLoginUserIdFromNotNull2Same();
        resendVisitEventLoginUserIdFromNotNull2New();
    }

    private void resendVisitEventLoginUserIdFromNull2New() {
        final AtomicBoolean receivedVisit = new AtomicBoolean(false);
        getEventsApiServer().setOnReceivedEventListener(new MockEventsApiServer.OnReceivedEventListener() {
            @Override
            protected void onReceivedVisitEvents(JSONArray jsonArray) throws JSONException {
                checkVisitEvent(jsonArray);
                JSONObject visit = jsonArray.getJSONObject(0);
                if (!visit.getString("sessionId").equals(mSessionId)
                        && visit.getString("userId").equals("New" + mLoginUserId)
                        && visit.getLong("timestamp") > mVisitTimestamp) {
                    mLoginUserId = visit.getString("userId");
                    receivedVisit.set(true);
                }
            }
        });
        GrowingTracker.get().setLoginUserId("New" + mLoginUserId);
        Awaiter.untilTrue(receivedVisit);
    }

    private void resendVisitEventLoginUserIdFromNotNull2Same() {
        if (TextUtils.isEmpty(mLoginUserId)) {
            Truth.assertWithMessage("mLoginUserId is NULL").fail();
        }
        getEventsApiServer().setOnReceivedEventListener(new MockEventsApiServer.OnReceivedEventListener() {
            @Override
            protected void onReceivedVisitEvents(JSONArray jsonArray) throws JSONException {
                Truth.assertWithMessage("Received Visit Event").fail();
            }
        });
        GrowingTracker.get().setLoginUserId(mLoginUserId);
        Uninterruptibles.sleepUninterruptibly(1, SECONDS);
    }

    private void resendVisitEventLoginUserIdFromNotNull2New() {
        if (TextUtils.isEmpty(mLoginUserId)) {
            Truth.assertWithMessage("mLoginUserId is NULL").fail();
        }
        final AtomicBoolean receivedVisit = new AtomicBoolean(false);
        getEventsApiServer().setOnReceivedEventListener(new MockEventsApiServer.OnReceivedEventListener() {
            @Override
            protected void onReceivedVisitEvents(JSONArray jsonArray) throws JSONException {
                checkVisitEvent(jsonArray);
                JSONObject visit = jsonArray.getJSONObject(0);
                if (!visit.getString("sessionId").equals(mSessionId)
                        && visit.getString("userId").equals("New" + mLoginUserId)
                        && visit.getLong("timestamp") > mVisitTimestamp) {
                    receivedVisit.set(true);
                }
            }
        });
        GrowingTracker.get().setLoginUserId("New" + mLoginUserId);
        Awaiter.untilTrue(receivedVisit);
    }

    private void resendVisitEventLoginUserIdSecondFromNull2OldNotNull() {
        getEventsApiServer().setOnReceivedEventListener(new MockEventsApiServer.OnReceivedEventListener() {
            @Override
            protected void onReceivedVisitEvents(JSONArray jsonArray) throws JSONException {
                Truth.assertWithMessage("Received Visit Event").fail();
            }
        });
        GrowingTracker.get().setLoginUserId("TestMockedName");
        Uninterruptibles.sleepUninterruptibly(1, SECONDS);
    }


    private void resendVisitEventLoginUserIdFirstFromNull2NotNull() {
        final AtomicBoolean receivedVisit = new AtomicBoolean(false);
        getEventsApiServer().setOnReceivedEventListener(new MockEventsApiServer.OnReceivedEventListener() {
            @Override
            protected void onReceivedVisitEvents(JSONArray jsonArray) throws JSONException {
                checkVisitEvent(jsonArray);
                JSONObject visit = jsonArray.getJSONObject(0);
                if (visit.getString("sessionId").equals(mSessionId)
                        && visit.getString("userId").equals("TestMockedName")
                        && visit.getLong("timestamp") == mVisitTimestamp) {
                    mLoginUserId = visit.getString("userId");
                    receivedVisit.set(true);
                }
            }
        });
        GrowingTracker.get().setLoginUserId("TestMockedName");
        Awaiter.untilTrue(receivedVisit);

    }

    private void resendVisitEventLoginUserIdFromNotNull2Null() {
        getEventsApiServer().setOnReceivedEventListener(new MockEventsApiServer.OnReceivedEventListener() {
            @Override
            protected void onReceivedVisitEvents(JSONArray jsonArray) throws JSONException {
                Truth.assertWithMessage("Received Visit Event").fail();
            }
        });
        GrowingTracker.get().cleanLoginUserId();
        Uninterruptibles.sleepUninterruptibly(1, SECONDS);
    }

    @Test
    public void resendVisitEventByLocationChangedTest() {
        final AtomicBoolean receivedVisit = new AtomicBoolean(false);
        getEventsApiServer().setOnReceivedEventListener(new MockEventsApiServer.OnReceivedEventListener() {
            @Override
            protected void onReceivedVisitEvents(JSONArray jsonArray) throws JSONException {
                checkVisitEvent(jsonArray);
                JSONObject visit = jsonArray.getJSONObject(0);
                mSessionId = visit.getString("sessionId");
                mVisitTimestamp = visit.getLong("timestamp");
                receivedVisit.set(true);
            }
        });
        Awaiter.untilTrue(receivedVisit);

        resendVisitEventLocationFromNull2NotNull();
        resendVisitEventLocationFromNotNull2NotNull();

        GrowingTracker.get().cleanLocation();
        resendVisitEventLocationFromNull2NotNull();
    }

    private void resendVisitEventLocationFromNull2NotNull() {
        final AtomicBoolean receivedVisit = new AtomicBoolean(false);
        getEventsApiServer().setOnReceivedEventListener(new MockEventsApiServer.OnReceivedEventListener() {
            @Override
            protected void onReceivedVisitEvents(JSONArray jsonArray) throws JSONException {
                checkVisitEvent(jsonArray);
                JSONObject visit = jsonArray.getJSONObject(0);
                if (visit.getString("sessionId").equals(mSessionId)
                        && visit.getLong("timestamp") == mVisitTimestamp) {
                    receivedVisit.set(true);
                }
            }
        });
        GrowingTracker.get().setLocation(99, 99);
        Awaiter.untilTrue(receivedVisit);
    }

    private void resendVisitEventLocationFromNotNull2NotNull() {
        getEventsApiServer().setOnReceivedEventListener(new MockEventsApiServer.OnReceivedEventListener() {
            @Override
            protected void onReceivedVisitEvents(JSONArray jsonArray) throws JSONException {
                Truth.assertWithMessage("Received Visit Event").fail();
            }
        });
        GrowingTracker.get().setLocation(100, 100);
        Uninterruptibles.sleepUninterruptibly(1, SECONDS);
    }

}
