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

package com.growingio.android.sdk.track.variation;

import com.growingio.android.sdk.track.events.VisitEvent;
import com.growingio.android.sdk.track.events.base.BaseAttributesEvent;
import com.growingio.android.sdk.track.events.base.BaseCustomEvent;
import com.growingio.android.sdk.track.events.base.BaseEvent;
import com.growingio.android.sdk.track.events.base.BaseEventWithSequenceId;
import com.growingio.android.sdk.track.events.marshaller.EventMarshaller;
import com.growingio.android.sdk.track.middleware.GEvent;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class TrackEventJsonMarshaller implements EventMarshaller<JSONObject, JSONArray> {

    @Override
    public JSONObject marshall(GEvent event) {
        try {
            JSONObject json = new JSONObject();
            if (event instanceof BaseEvent) {
                writeBaseContent(json, (BaseEvent) event);
            }

            if (event instanceof BaseEventWithSequenceId) {
                writeSequenceId(json, (BaseEventWithSequenceId) event);
            }

            if (event instanceof BaseAttributesEvent) {
                writeAttributesEvent(json, (BaseAttributesEvent) event);
            }

            if (event instanceof VisitEvent) {
                writeVisitEvent(json, (VisitEvent) event);
            } else if (event instanceof BaseCustomEvent) {
                writeCustomEvent(json, (BaseCustomEvent) event);
            }

            return json;
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public JSONArray marshall(List<GEvent> events) {
        JSONArray jsonArray = new JSONArray();
        if (events == null || events.isEmpty()) {
            return jsonArray;
        }

        for (GEvent event : events) {
            JSONObject eventJson = marshall(event);
            if (eventJson != null) {
                jsonArray.put(marshall(event));
            }
        }
        return jsonArray;
    }

    private void writeBaseContent(JSONObject generator, BaseEvent event) throws JSONException {
        generator.put(MarshallerConstants.Key.TIMESTAMP, event.getTimestamp());
        generator.put(MarshallerConstants.Key.EVENT_TYPE, event.getEventType().toString());
        generator.put(MarshallerConstants.Key.DEVICE_ID, event.getDeviceId());
        generator.put(MarshallerConstants.Key.SESSION_ID, event.getSessionId());
        generator.put(MarshallerConstants.Key.DOMAIN, event.getDomain());
        generator.put(MarshallerConstants.Key.USER_ID, event.getUserId());
        generator.put(MarshallerConstants.Key.IS_INTERACTIVE, event.isInteractive() ? 1 : 0);
    }

    private void writeSequenceId(JSONObject generator, BaseEventWithSequenceId event) throws JSONException {
        generator.put(MarshallerConstants.Key.GLOBAL_SEQUENCE_ID, event.getGlobalSequenceId());
        generator.put(MarshallerConstants.Key.EVENT_SEQUENCE_ID, event.getEventSequenceId());
    }

    private void writeAttributesEvent(JSONObject generator, BaseAttributesEvent event) throws JSONException {
        generator.put(MarshallerConstants.Key.ATTRIBUTES, event.getAttributes());
    }

    private void writeCustomEvent(JSONObject generator, BaseCustomEvent event) throws JSONException {
        generator.put(MarshallerConstants.Key.EVENT_NAME, event.getEventName());
        generator.put(MarshallerConstants.Key.PAGE_NAME, event.getPageName());
        generator.put(MarshallerConstants.Key.PAGE_SHOW_TIMESTAMP, event.getPageShowTimestamp());
    }

    private void writeVisitEvent(JSONObject generator, VisitEvent event) throws JSONException {
        generator.put(MarshallerConstants.Key.APP_CHANNEL, event.getAppChannel());
        generator.put(MarshallerConstants.Key.SCREEN_HEIGHT, event.getScreenHeight());
        generator.put(MarshallerConstants.Key.SCREEN_WIDTH, event.getScreenWidth());
        generator.put(MarshallerConstants.Key.DEVICE_BRAND, event.getDeviceBrand());
        generator.put(MarshallerConstants.Key.DEVICE_MODEL, event.getDeviceModel());
        generator.put(MarshallerConstants.Key.IS_PHONE, event.isPhone() ? 1 : 0);
        generator.put(MarshallerConstants.Key.OPERATING_SYSTEM, event.getOperatingSystem());
        generator.put(MarshallerConstants.Key.OPERATING_SYSTEM_VERSION, event.getOperatingSystemVersion());
        generator.put(MarshallerConstants.Key.SDK_VERSION, event.getSdkVersion());
        generator.put(MarshallerConstants.Key.APP_NAME, event.getAppName());
        generator.put(MarshallerConstants.Key.APP_VERSION, event.getAppVersion());
        generator.put(MarshallerConstants.Key.URL_SCHEME, event.getUrlScheme());
        generator.put(MarshallerConstants.Key.LANGUAGE, event.getLanguage());
        generator.put(MarshallerConstants.Key.LATITUDE, event.getLatitude());
        generator.put(MarshallerConstants.Key.LONGITUDE, event.getLongitude());
        generator.put(MarshallerConstants.Key.ANDROID_ID, event.getAndroidId());
        generator.put(MarshallerConstants.Key.GOOGLE_ADVERTISING_ID, event.getGoogleAdvertisingId());
        generator.put(MarshallerConstants.Key.IMEI, event.getImei());
        generator.put(MarshallerConstants.Key.FEATURES_VERSION, event.getFeaturesVersion());
    }
}
