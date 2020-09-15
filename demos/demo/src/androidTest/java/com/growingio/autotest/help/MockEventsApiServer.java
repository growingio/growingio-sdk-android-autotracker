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

import com.growingio.android.sdk.autotrack.events.AutotrackEventType;
import com.growingio.android.sdk.track.events.TrackEventType;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.RecordedRequest;

public class MockEventsApiServer extends MockServer {

    private final Dispatcher mDispatcher = new Dispatcher() {
        @Override
        public MockResponse dispatch(RecordedRequest request) throws InterruptedException {
            Uri uri = Uri.parse(request.getRequestUrl().toString());
            String json = request.getBody().readUtf8();
            dispatchReceivedEvents(json);
            return new MockResponse().setResponseCode(200);
        }
    };

    private OnReceivedEventListener mOnReceivedEventListener;

    public void setOnReceivedEventListener(OnReceivedEventListener onReceivedEventListener) {
        mOnReceivedEventListener = onReceivedEventListener;
    }

    public MockEventsApiServer() {
        setDispatcher(mDispatcher);
    }

    private void dispatchReceivedEvents(String json) {
        try {
            JSONArray jsonArray = new JSONArray(json);
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
                case AutotrackEventType.PAGE:
                    if (mOnReceivedEventListener != null) {
                        mOnReceivedEventListener.onReceivedPageEvents(jsonArray);
                    }
                    break;
                default:
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

        protected void onReceivedPageEvents(JSONArray jsonArray) throws JSONException {

        }
    }
}
