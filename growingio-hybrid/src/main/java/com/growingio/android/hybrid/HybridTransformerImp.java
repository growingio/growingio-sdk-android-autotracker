/*
 * Copyright (C) 2023 Beijing Yishu Technology Co., Ltd.
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

import com.growingio.android.sdk.track.events.hybrid.HybridCustomEvent;
import com.growingio.android.sdk.track.events.hybrid.HybridPageEvent;
import com.growingio.android.sdk.track.events.hybrid.HybridViewElementEvent;
import com.growingio.android.sdk.track.events.AutotrackEventType;
import com.growingio.android.sdk.track.events.ConversionVariablesEvent;
import com.growingio.android.sdk.track.events.LoginUserAttributesEvent;
import com.growingio.android.sdk.track.events.TrackEventType;
import com.growingio.android.sdk.track.events.VisitorAttributesEvent;
import com.growingio.android.sdk.track.events.base.BaseEvent;
import com.growingio.android.sdk.track.log.Logger;
import com.growingio.android.sdk.track.providers.EventBuilderProvider;

import org.json.JSONException;
import org.json.JSONObject;

public class HybridTransformerImp implements HybridTransformer {
    private static final String TAG = "HybridTransformerImp";

    @Override
    public BaseEvent.BaseBuilder<?> transform(String hybridEvent) {
        try {
            JSONObject eventJson = new JSONObject(hybridEvent);
            String type = eventJson.getString(BaseEvent.EVENT_TYPE);
            if (AutotrackEventType.PAGE.equals(type)) {
                HybridPageEvent.Builder builder = new HybridPageEvent.Builder();
                EventBuilderProvider.parseFrom(builder, eventJson);
                return builder;
            } else if (AutotrackEventType.VIEW_CLICK.equals(type) ||
                    AutotrackEventType.VIEW_CHANGE.equals(type) ||
                    TrackEventType.FORM_SUBMIT.equals(type)
            ) {
                return transformViewElementEventBuilder(type, eventJson);
            } else if (TrackEventType.CUSTOM.equals(type)) {
                HybridCustomEvent.Builder builder = new HybridCustomEvent.Builder();
                EventBuilderProvider.parseFrom(builder, eventJson);
                return builder;
            } else if (TrackEventType.LOGIN_USER_ATTRIBUTES.equals(type)) {
                LoginUserAttributesEvent.Builder builder = new LoginUserAttributesEvent.Builder();
                EventBuilderProvider.parseFrom(builder, eventJson);
                return builder;

            } else if (TrackEventType.VISITOR_ATTRIBUTES.equals(type)) {
                VisitorAttributesEvent.Builder builder = new VisitorAttributesEvent.Builder();
                EventBuilderProvider.parseFrom(builder, eventJson);
                return builder;

            } else if (TrackEventType.CONVERSION_VARIABLES.equals(type)) {
                ConversionVariablesEvent.Builder builder = new ConversionVariablesEvent.Builder();
                EventBuilderProvider.parseFrom(builder, eventJson);
                return builder;
            }
        } catch (JSONException e) {
            Logger.e(TAG, e.getMessage(), e);
        }
        return null;
    }

    private HybridViewElementEvent.Builder transformViewElementEventBuilder(String eventType, JSONObject eventJson) throws JSONException {
        HybridViewElementEvent.Builder builder = new HybridViewElementEvent.Builder(eventType);
        EventBuilderProvider.parseFrom(builder, eventJson);
        return builder;
    }
}
