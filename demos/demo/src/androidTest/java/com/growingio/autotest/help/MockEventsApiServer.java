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

package com.growingio.autotest.help;

import android.net.Uri;
import android.os.Build;
import android.text.TextUtils;

import com.google.common.truth.Truth;
import com.growingio.android.sdk.track.events.AutotrackEventType;
import com.growingio.android.sdk.track.events.TrackEventType;
import com.growingio.android.sdk.track.providers.ConfigurationProvider;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;

import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.RecordedRequest;

public class MockEventsApiServer extends MockServer {
    private OnReceivedEventListener mOnReceivedEventListener;
    private volatile boolean mIsCheckUserId = true;
    private volatile boolean mIsCheckSessionId = true;
    private volatile boolean mIsCheckDomain = true;
    private volatile boolean mIsCheckTimestamp = true;
    private volatile String mSessionId;

    public void setCheckUserId(boolean checkUserId) {
        mIsCheckUserId = checkUserId;
    }

    public void setCheckSessionId(boolean checkSessionId) {
        mIsCheckSessionId = checkSessionId;
    }

    public void setCheckDomain(boolean checkDomain) {
        mIsCheckDomain = checkDomain;
    }

    public void setCheckTimestamp(boolean checkTimestamp) {
        mIsCheckTimestamp = checkTimestamp;
    }

    public void setOnReceivedEventListener(OnReceivedEventListener onReceivedEventListener) {
        mOnReceivedEventListener = onReceivedEventListener;
    }

    public MockEventsApiServer() {
        Dispatcher dispatcher = new Dispatcher() {
            @Override
            public MockResponse dispatch(RecordedRequest request) throws InterruptedException {
                Uri uri = Uri.parse(request.getRequestUrl().toString());
                checkPath(uri);

                String json = request.getBody().readUtf8();
                dispatchReceivedEvents(json);
                return new MockResponse().setResponseCode(200);
            }
        };
        setDispatcher(dispatcher);
    }

    private void checkPath(Uri uri) {
        String expectedPath = "/v3/projects/testProjectId/collect";
        Truth.assertThat(uri.getPath()).isEqualTo(expectedPath);

        long stm = Long.parseLong(uri.getQueryParameter("stm"));
        Truth.assertThat(System.currentTimeMillis() - stm).isAtMost(5000);
    }

    private void checkBaseEventBody(JSONArray jsonArray) throws JSONException {
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            Truth.assertThat(jsonObject.getString("platform")).isEqualTo("Android");
            Truth.assertThat(jsonObject.getString("platformVersion")).isEqualTo(Build.VERSION.RELEASE == null ? "UNKNOWN" : Build.VERSION.RELEASE);

            String deviceId = jsonObject.getString("deviceId");
            Truth.assertThat(deviceId).isNotEmpty();
            if (TextUtils.isEmpty(EventsTestDataHelper.INSTANCE.getDeviceId())) {
                EventsTestDataHelper.INSTANCE.saveDeviceId(deviceId);
            } else {
                Truth.assertThat(deviceId).isEqualTo(EventsTestDataHelper.INSTANCE.getDeviceId());
            }

            if (mIsCheckUserId) {
                String userId = jsonObject.optString("userId");
                if (!TextUtils.isEmpty(userId)) {
                    if (TextUtils.isEmpty(EventsTestDataHelper.INSTANCE.getUserId())) {
                        EventsTestDataHelper.INSTANCE.saveUserId(userId);
                    } else {
                        Truth.assertThat(userId).isEqualTo(EventsTestDataHelper.INSTANCE.getUserId());
                    }
                }
            }

            String sessionId = jsonObject.getString("sessionId");
            Truth.assertThat(sessionId).isNotEmpty();
            if (mIsCheckSessionId) {
                if (TextUtils.isEmpty(mSessionId)) {
                    mSessionId = sessionId;
                } else {
                    Truth.assertThat(sessionId).isEqualTo(mSessionId);
                }
            }

            Truth.assertThat(jsonObject.getString("eventType")).isNotEmpty();
            if (mIsCheckTimestamp) {
                Truth.assertThat(System.currentTimeMillis() - jsonObject.getLong("timestamp")).isAtMost(60 * 1000);
            }
            if (mIsCheckDomain) {
                Truth.assertThat(jsonObject.getString("domain")).isEqualTo("com.gio.test.three");
            }
            Truth.assertThat(jsonObject.getString("urlScheme")).isEqualTo(ConfigurationProvider.core().getUrlScheme());
            Truth.assertThat(jsonObject.getString("appState")).isIn(Arrays.asList("FOREGROUND", "BACKGROUND"));
            Truth.assertThat(jsonObject.getLong("eventSequenceId")).isGreaterThan(0);
        }
    }

    private void dispatchReceivedEvents(String json) {
        try {
            JSONArray jsonArray = new JSONArray(json);
            checkBaseEventBody(jsonArray);
            if (mOnReceivedEventListener != null) {
                mOnReceivedEventListener.onReceivedEvents(jsonArray);
            }

            JSONObject jsonObject = jsonArray.getJSONObject(0);
            String eventType = jsonObject.getString("eventType");
            switch (eventType) {
                case TrackEventType.CUSTOM:
                    if (mOnReceivedEventListener != null) {
                        mOnReceivedEventListener.onReceivedCustomEvents(jsonArray);
                    }
                    break;
                case TrackEventType.VISIT:
                    if (mOnReceivedEventListener != null) {
                        mOnReceivedEventListener.onReceivedVisitEvents(jsonArray);
                    }
                    break;
                case TrackEventType.APP_CLOSED:
                    if (mOnReceivedEventListener != null) {
                        mOnReceivedEventListener.onReceivedAppClosedEvents(jsonArray);
                    }
                    break;
                case TrackEventType.VISITOR_ATTRIBUTES:
                    if (mOnReceivedEventListener != null) {
                        mOnReceivedEventListener.onReceivedVisitorAttributesEvents(jsonArray);
                    }
                    break;
                case TrackEventType.LOGIN_USER_ATTRIBUTES:
                    if (mOnReceivedEventListener != null) {
                        mOnReceivedEventListener.onReceivedLoginUserAttributesEvents(jsonArray);
                    }
                    break;
                case TrackEventType.CONVERSION_VARIABLES:
                    if (mOnReceivedEventListener != null) {
                        mOnReceivedEventListener.onReceivedConversionVariablesEvents(jsonArray);
                    }
                    break;
                case AutotrackEventType.VIEW_CLICK:
                    if (mOnReceivedEventListener != null) {
                        mOnReceivedEventListener.onReceivedViewClickEvents(jsonArray);
                    }
                    break;
                case AutotrackEventType.VIEW_CHANGE:
                    if (mOnReceivedEventListener != null) {
                        mOnReceivedEventListener.onReceivedViewChangeEvents(jsonArray);
                    }
                    break;
                case AutotrackEventType.PAGE:
                    if (mOnReceivedEventListener != null) {
                        mOnReceivedEventListener.onReceivedPageEvents(jsonArray);
                    }
                    break;
                case TrackEventType.FORM_SUBMIT:
                    if (mOnReceivedEventListener != null) {
                        mOnReceivedEventListener.onReceivedHybridFormSubmitEvents(jsonArray);
                    }
                    break;
                default:
                    Truth.assertWithMessage("Undefined eventType " + eventType).fail();
                    break;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public abstract static class OnReceivedEventListener {
        protected void onReceivedEvents(JSONArray jsonArray) throws JSONException {

        }

        protected void onReceivedVisitEvents(JSONArray jsonArray) throws JSONException {

        }

        protected void onReceivedAppClosedEvents(JSONArray jsonArray) throws JSONException {

        }

        protected void onReceivedCustomEvents(JSONArray jsonArray) throws JSONException {

        }

        protected void onReceivedVisitorAttributesEvents(JSONArray jsonArray) throws JSONException {

        }

        protected void onReceivedLoginUserAttributesEvents(JSONArray jsonArray) throws JSONException {

        }

        protected void onReceivedConversionVariablesEvents(JSONArray jsonArray) throws JSONException {

        }

        protected void onReceivedViewClickEvents(JSONArray jsonArray) throws JSONException {

        }

        protected void onReceivedViewChangeEvents(JSONArray jsonArray) throws JSONException {

        }

        protected void onReceivedPageEvents(JSONArray jsonArray) throws JSONException {

        }

        protected void onReceivedHybridFormSubmitEvents(JSONArray jsonArray) throws JSONException {

        }
    }
}
