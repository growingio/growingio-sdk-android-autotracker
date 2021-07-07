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

package com.growingio.android.hybrid;

import android.app.Activity;
import android.content.res.Configuration;
import android.text.TextUtils;

import com.growingio.android.hybrid.event.HybridCustomEvent;
import com.growingio.android.hybrid.event.HybridEventType;
import com.growingio.android.hybrid.event.HybridPageAttributesEvent;
import com.growingio.android.hybrid.event.HybridPageEvent;
import com.growingio.android.hybrid.event.HybridViewElementEvent;
import com.growingio.android.sdk.track.events.AutotrackEventType;
import com.growingio.android.sdk.track.events.ConversionVariablesEvent;
import com.growingio.android.sdk.track.events.LoginUserAttributesEvent;
import com.growingio.android.sdk.track.events.PageEvent;
import com.growingio.android.sdk.track.events.TrackEventType;
import com.growingio.android.sdk.track.events.VisitorAttributesEvent;
import com.growingio.android.sdk.track.events.base.BaseEvent;
import com.growingio.android.sdk.track.log.Logger;
import com.growingio.android.sdk.track.providers.ActivityStateProvider;
import com.growingio.android.sdk.track.providers.AppInfoProvider;
import com.growingio.android.sdk.track.utils.JsonUtil;

import org.json.JSONException;
import org.json.JSONObject;

public class HybridTransformerImp implements HybridTransformer {
    private static final String TAG = "HybridTransformerImp";

    private static final String KEY_EVENT_TYPE = "eventType";
    private static final String KEY_DOMAIN = "domain";
    private static final String KEY_PATH = "path";
    private static final String KEY_PROTOCOL_TYPE = "protocolType";
    private static final String KEY_QUERY = "query";
    private static final String KEY_REFERRAL_PAGE = "referralPage";
    private static final String KEY_TITLE = "title";
    private static final String KEY_TIMESTAMP = "timestamp";
    private static final String KEY_PAGE_SHOW_TIMESTAMP = "pageShowTimestamp";
    private static final String KEY_ATTRIBUTES = "attributes";
    private static final String KEY_EVENT_NAME = "eventName";
    private static final String KEY_HYPERLINK = "hyperlink";
    private static final String KEY_INDEX = "index";
    private static final String KEY_TEXT_VALUE = "textValue";
    private static final String KEY_XPATH = "xpath";

    @Override
    public BaseEvent.BaseBuilder<?> transform(String hybridEvent) {
        try {
            JSONObject eventJson = new JSONObject(hybridEvent);
            String type = eventJson.getString(KEY_EVENT_TYPE);
            if (AutotrackEventType.PAGE.equals(type)) {
                String orientation = PageEvent.ORIENTATION_PORTRAIT;
                Activity activity = ActivityStateProvider.get().getForegroundActivity();
                if (activity != null) {
                    orientation = activity.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT
                            ? PageEvent.ORIENTATION_PORTRAIT : PageEvent.ORIENTATION_LANDSCAPE;
                }
                return new HybridPageEvent.Builder()
                        .setDomain(getDomain(eventJson))
                        .setProtocolType(eventJson.getString(KEY_PROTOCOL_TYPE))
                        .setQuery(eventJson.optString(KEY_QUERY))
                        .setPath(eventJson.getString(KEY_PATH))
                        .setReferralPage(eventJson.optString(KEY_REFERRAL_PAGE))
                        .setTitle(eventJson.optString(KEY_TITLE))
                        .setTimestamp(eventJson.getLong(KEY_TIMESTAMP))
                        .setOrientation(orientation);

            } else if (AutotrackEventType.PAGE_ATTRIBUTES.equals(type)) {
                return new HybridPageAttributesEvent.Builder()
                        .setDomain(getDomain(eventJson))
                        .setQuery(eventJson.optString(KEY_QUERY))
                        .setPath(eventJson.getString(KEY_PATH))
                        .setPageShowTimestamp(eventJson.getLong(KEY_PAGE_SHOW_TIMESTAMP))
                        .setAttributes(JsonUtil.copyToMap(eventJson.getJSONObject(KEY_ATTRIBUTES)));

            } else if (AutotrackEventType.VIEW_CLICK.equals(type)) {
                return transformViewElementEventBuilder(eventJson)
                        .setEventType(type);

            } else if (AutotrackEventType.VIEW_CHANGE.equals(type)) {
                return transformViewElementEventBuilder(eventJson)
                        .setEventType(type);

            } else if (HybridEventType.FORM_SUBMIT.equals(type)) {
                return transformViewElementEventBuilder(eventJson)
                        .setEventType(type);

            } else if (TrackEventType.CUSTOM.equals(type)) {
                return new HybridCustomEvent.Builder()
                        .setDomain(getDomain(eventJson))
                        .setQuery(eventJson.optString(KEY_QUERY))
                        .setPath(eventJson.getString(KEY_PATH))
                        .setPageShowTimestamp(eventJson.getLong(KEY_PAGE_SHOW_TIMESTAMP))
                        .setEventName(eventJson.getString(KEY_EVENT_NAME))
                        .setAttributes(JsonUtil.copyToMap(eventJson.optJSONObject(KEY_ATTRIBUTES)));

            } else if (TrackEventType.LOGIN_USER_ATTRIBUTES.equals(type)) {
                return new LoginUserAttributesEvent.Builder()
                        .setAttributes(JsonUtil.copyToMap(eventJson.getJSONObject(KEY_ATTRIBUTES)));

            } else if (TrackEventType.VISITOR_ATTRIBUTES.equals(type)) {
                return new VisitorAttributesEvent.Builder()
                        .setAttributes(JsonUtil.copyToMap(eventJson.getJSONObject(KEY_ATTRIBUTES)));

            } else if (TrackEventType.CONVERSION_VARIABLES.equals(type)) {
                return new ConversionVariablesEvent.Builder()
                        .setAttributes(JsonUtil.copyToMap(eventJson.getJSONObject(KEY_ATTRIBUTES)));
            }
        } catch (JSONException e) {
            Logger.e(TAG, e.getMessage(), e);
        }


        return null;
    }

    private String getDomain(JSONObject event) {
        String domain = event.optString(KEY_DOMAIN);
        if (TextUtils.isEmpty(domain)) {
            domain = AppInfoProvider.get().getPackageName();
        }
        return domain;
    }

    private HybridViewElementEvent.Builder transformViewElementEventBuilder(JSONObject eventJson) throws JSONException {
        return new HybridViewElementEvent.Builder()
                .setHyperlink(eventJson.optString(KEY_HYPERLINK))
                .setDomain(getDomain(eventJson))
                .setQuery(eventJson.optString(KEY_QUERY))
                .setIndex(eventJson.optInt(KEY_INDEX, -1))
                .setTextValue(eventJson.optString(KEY_TEXT_VALUE))
                .setXpath(eventJson.getString(KEY_XPATH))
                .setPath(eventJson.getString(KEY_PATH))
                .setPageShowTimestamp(eventJson.getLong(KEY_PAGE_SHOW_TIMESTAMP));
    }
}
