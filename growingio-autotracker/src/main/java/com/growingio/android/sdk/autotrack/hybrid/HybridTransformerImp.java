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

package com.growingio.android.sdk.autotrack.hybrid;

import androidx.annotation.Nullable;

import com.growingio.android.sdk.autotrack.events.AutotrackEventType;
import com.growingio.android.sdk.autotrack.hybrid.event.HybridCustomEvent;
import com.growingio.android.sdk.autotrack.hybrid.event.HybridEventType;
import com.growingio.android.sdk.autotrack.hybrid.event.HybridPageAttributesEvent;
import com.growingio.android.sdk.autotrack.hybrid.event.HybridPageEvent;
import com.growingio.android.sdk.autotrack.hybrid.event.HybridViewElementEvent;
import com.growingio.android.sdk.track.events.ConversionVariablesEvent;
import com.growingio.android.sdk.track.events.LoginUserAttributesEvent;
import com.growingio.android.sdk.track.events.TrackEventType;
import com.growingio.android.sdk.track.events.VisitorAttributesEvent;
import com.growingio.android.sdk.track.events.base.BaseEvent;
import com.growingio.android.sdk.track.utils.JsonUtil;
import com.growingio.android.sdk.track.utils.LogUtil;

import org.json.JSONException;
import org.json.JSONObject;

public class HybridTransformerImp implements HybridTransformer {
    private static final String TAG = "HybridTransformerImp";

    @Nullable
    @Override
    public BaseEvent.BaseBuilder<?> transform(String hybridEvent) {
        try {
            JSONObject evenJson = new JSONObject(hybridEvent);
            String type = evenJson.getString("eventType");
            if (AutotrackEventType.PAGE.equals(type)) {
                return new HybridPageEvent.Builder()
                        .setDomain(evenJson.getString("domain"))
                        .setProtocolType(evenJson.getString("protocolType"))
                        .setQueryParameters(evenJson.optString("queryParameters"))
                        .setPageName(evenJson.getString("pageName"))
                        .setReferralPage(evenJson.optString("referralPage"))
                        .setTitle(evenJson.optString("title"))
                        .setTimestamp(evenJson.getLong("timestamp"));

            } else if (AutotrackEventType.PAGE_ATTRIBUTES.equals(type)) {
                return new HybridPageAttributesEvent.Builder()
                        .setDomain(evenJson.getString("domain"))
                        .setQueryParameters(evenJson.optString("queryParameters"))
                        .setPageName(evenJson.getString("pageName"))
                        .setPageShowTimestamp(evenJson.getLong("pageShowTimestamp"))
                        .setAttributes(JsonUtil.copyToMap(evenJson.getJSONObject("attributes")));

            } else if (AutotrackEventType.VIEW_CLICK.equals(type)) {
                return transformViewElementEventBuilder(evenJson)
                        .setEventType(type);

            } else if (AutotrackEventType.VIEW_CHANGE.equals(type)) {
                return transformViewElementEventBuilder(evenJson)
                        .setEventType(type);

            } else if (HybridEventType.FORM_SUBMIT.equals(type)) {
                return transformViewElementEventBuilder(evenJson)
                        .setEventType(type);

            } else if (TrackEventType.CUSTOM.equals(type)) {
                return new HybridCustomEvent.Builder()
                        .setDomain(evenJson.getString("domain"))
                        .setQueryParameters(evenJson.optString("queryParameters"))
                        .setPageName(evenJson.getString("pageName"))
                        .setPageShowTimestamp(evenJson.getLong("pageShowTimestamp"))
                        .setEventName(evenJson.getString("eventName"))
                        .setAttributes(JsonUtil.copyToMap(evenJson.optJSONObject("attributes")));

            } else if (TrackEventType.LOGIN_USER_ATTRIBUTES.equals(type)) {
                return new LoginUserAttributesEvent.Builder()
                        .setAttributes(JsonUtil.copyToMap(evenJson.getJSONObject("attributes")));

            } else if (TrackEventType.VISITOR_ATTRIBUTES.equals(type)) {
                return new VisitorAttributesEvent.Builder()
                        .setAttributes(JsonUtil.copyToMap(evenJson.getJSONObject("attributes")));

            } else if (TrackEventType.CONVERSION_VARIABLES.equals(type)) {
                return new ConversionVariablesEvent.Builder()
                        .setVariables(JsonUtil.copyToMap(evenJson.getJSONObject("variables")));
            }
        } catch (JSONException e) {
            LogUtil.e(TAG, e.getMessage(), e);
        }


        return null;
    }

    private HybridViewElementEvent.Builder transformViewElementEventBuilder(JSONObject json) throws JSONException {
        return new HybridViewElementEvent.Builder()
                .setHyperlink(json.optString("hyperlink"))
                .setDomain(json.getString("domain"))
                .setQueryParameters(json.optString("queryParameters"))
                .setIndex(json.optInt("index", -1))
                .setTextValue(json.optString("textValue"))
                .setXpath(json.getString("xpath"))
                .setPageName(json.getString("pageName"))
                .setPageShowTimestamp(json.getLong("pageShowTimestamp"));
    }
}
