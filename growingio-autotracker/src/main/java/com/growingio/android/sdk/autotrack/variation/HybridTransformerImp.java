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

package com.growingio.android.sdk.autotrack.variation;

import androidx.annotation.Nullable;

import com.growingio.android.sdk.autotrack.hybrid.HybridTransformer;
import com.growingio.android.sdk.autotrack.hybrid.event.HybridCustomEvent;
import com.growingio.android.sdk.autotrack.hybrid.event.HybridPageAttributesEvent;
import com.growingio.android.sdk.autotrack.hybrid.event.HybridPageEvent;
import com.growingio.android.sdk.autotrack.hybrid.event.HybridViewElement;
import com.growingio.android.sdk.autotrack.hybrid.event.HybridViewElementEvent;
import com.growingio.android.sdk.track.events.ConversionVariablesEvent;
import com.growingio.android.sdk.track.events.EventType;
import com.growingio.android.sdk.track.events.LoginUserAttributesEvent;
import com.growingio.android.sdk.track.events.VisitorAttributesEvent;
import com.growingio.android.sdk.track.events.base.BaseEvent;
import com.growingio.android.sdk.track.utils.JsonUtil;
import com.growingio.android.sdk.track.utils.LogUtil;
import com.growingio.android.sdk.track.variation.MarshallerConstants.Key;
import com.growingio.android.sdk.track.variation.MarshallerConstants.Value;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class HybridTransformerImp implements HybridTransformer {
    private static final String TAG = "HybridTransformerImp";


    @Nullable
    @Override
    public BaseEvent.BaseEventBuilder<?> transform(String hybridEvent) {
        try {
            JSONObject evenJson = new JSONObject(hybridEvent);
            String type = evenJson.getString(Key.EVENT_TYPE);
            if (Value.EventType.PAGE.equals(type)) {
                return new HybridPageEvent.EventBuilder()
                        .setDomain(evenJson.getString(Key.DOMAIN))
                        .setProtocolType(evenJson.getString(Key.PROTOCOL_TYPE))
                        .setQueryParameters(evenJson.getString(Key.QUERY_PARAMETERS))
                        .setPageName(evenJson.getString(Key.PAGE_NAME))
                        .setReferralPage(evenJson.getString(Key.REFERRAL_PAGE))
                        .setTitle(evenJson.getString(Key.TITLE))
                        .setTimestamp(evenJson.getLong(Key.TIMESTAMP));

            } else if (Value.EventType.PAGE_ATTRIBUTES.equals(type)) {
                return new HybridPageAttributesEvent.EventBuilder()
                        .setDomain(evenJson.getString(Key.DOMAIN))
                        .setQueryParameters(evenJson.getString(Key.QUERY_PARAMETERS))
                        .setPageName(evenJson.getString(Key.PAGE_NAME))
                        .setPageShowTimestamp(evenJson.getLong(Key.PAGE_SHOW_TIMESTAMP))
                        .setAttributes(JsonUtil.copyToMap(evenJson.getJSONObject(Key.ATTRIBUTES)));

            } else if (Value.EventType.CLICK.equals(type)) {
                return transformViewElementEventBuilder(evenJson)
                        .setEventType(EventType.CLICK);

            } else if (Value.EventType.CHANG.equals(type)) {
                return transformViewElementEventBuilder(evenJson)
                        .setEventType(EventType.CHANGE);

            } else if (Value.EventType.SUBMIT.equals(type)) {
                return transformViewElementEventBuilder(evenJson)
                        .setEventType(EventType.SUBMIT);

            } else if (Value.EventType.CUSTOM.equals(type)) {
                return new HybridCustomEvent.EventBuilder()
                        .setDomain(evenJson.getString(Key.DOMAIN))
                        .setQueryParameters(evenJson.getString(Key.QUERY_PARAMETERS))
                        .setPageName(evenJson.getString(Key.PAGE_NAME))
                        .setPageShowTimestamp(evenJson.getLong(Key.PAGE_SHOW_TIMESTAMP))
                        .setEventName(evenJson.getString(Key.EVENT_NAME))
                        .setAttributes(JsonUtil.copyToMap(evenJson.getJSONObject(Key.ATTRIBUTES)));

            } else if (Value.EventType.LOGIN_USER_ATTRIBUTES.equals(type)) {
                return new LoginUserAttributesEvent.EventBuilder()
                        .setAttributes(JsonUtil.copyToMap(evenJson.getJSONObject(Key.ATTRIBUTES)));

            } else if (Value.EventType.VISITOR_ATTRIBUTES.equals(type)) {
                return new VisitorAttributesEvent.EventBuilder()
                        .setAttributes(JsonUtil.copyToMap(evenJson.getJSONObject(Key.ATTRIBUTES)));

            } else if (Value.EventType.CONVERSION_VARIABLES.equals(type)) {
                return new ConversionVariablesEvent.EventBuilder()
                        .setAttributes(JsonUtil.copyToMap(evenJson.getJSONObject(Key.ATTRIBUTES)));
            }
        } catch (JSONException e) {
            LogUtil.e(TAG, e.getMessage(), e);
        }


        return null;
    }

    private HybridViewElementEvent.EventBuilder transformViewElementEventBuilder(JSONObject json) throws JSONException {
        HybridViewElementEvent.EventBuilder eventBuilder = (HybridViewElementEvent.EventBuilder) new HybridViewElementEvent.EventBuilder()
                .setDomain(json.getString(Key.DOMAIN))
                .setQueryParameters(json.getString(Key.QUERY_PARAMETERS))
                .setPageName(json.getString(Key.PAGE_NAME))
                .setPageShowTimestamp(json.getLong(Key.PAGE_SHOW_TIMESTAMP));

        JSONArray elements = json.getJSONArray(Key.VIEW_ELEMENT);
        for (int i = 0; i < elements.length(); i++) {
            eventBuilder.addElementBuilder(transformElementBuilder(elements.getJSONObject(i)));
        }

        return eventBuilder;
    }

    private HybridViewElement.ElementBuilder transformElementBuilder(JSONObject json) {
        return (HybridViewElement.ElementBuilder) new HybridViewElement.ElementBuilder()
                .setHyperlink(json.optString(Key.HYPERLINK))
                .setIndex(json.optInt(Key.INDEX))
                .setTextValue(json.optString(Key.TEXT_VALUE))
                .setXpath(json.optString(Key.XPATH))
                .setTimestamp(json.optLong(Key.TIMESTAMP));
    }
}
