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

import android.net.Uri;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.gio.test.three.core.TrackActivity;
import com.growingio.android.sdk.track.GrowingTracker;
import com.growingio.android.sdk.track.events.TrackEventType;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.concurrent.Callable;

import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class TrackEventsTest {
    private static final String TAG = "TrackEventsTest";
    private static final String CUSTOM_EVENT_NAME = "testCustomEvent";

    private volatile boolean mSendCustomEvent = false;

    private final MockWebServer mMockWebServer = new MockWebServer();
    private final Dispatcher mDispatcher = new Dispatcher() {
        @Override
        public MockResponse dispatch(RecordedRequest request) {
            Uri uri = Uri.parse(request.getRequestUrl().toString());
            String json = request.getBody().readUtf8();
            try {
                JSONArray jsonArray = new JSONArray(json);
                JSONObject jsonObject = jsonArray.getJSONObject(0);
                mSendCustomEvent = TrackEventType.CUSTOM.equals(jsonObject.getString("eventType"))
                        && CUSTOM_EVENT_NAME.equals(jsonObject.optString("eventName"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return new MockResponse().setResponseCode(200);
        }
    };

    @Before
    public void setUp() throws IOException {
        mMockWebServer.setDispatcher(mDispatcher);
        mMockWebServer.start(8910);
    }

    @After
    public void teardown() throws IOException {
        mMockWebServer.shutdown();
    }

    @Rule
    public ActivityScenarioRule<TrackActivity> mRule = new ActivityScenarioRule<>(TrackActivity.class);

    @Test
    public void trackCustomEventTest() {
        GrowingTracker.get().trackCustomEvent(CUSTOM_EVENT_NAME);
        await().atMost(5, SECONDS).until(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return mSendCustomEvent;
            }
        });
    }
}
