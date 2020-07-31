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

package com.growingio.android.sdk.track.events.base;

import com.growingio.android.sdk.track.CoreAppState;
import com.growingio.android.sdk.track.models.EsidProperty;
import com.growingio.android.sdk.track.providers.EsidProvider;

import org.json.JSONException;
import org.json.JSONObject;

public abstract class BaseEventWithSequenceId extends BaseEvent {
    private final int mGlobalSequenceId;
    private final int mEventSequenceId;

    protected BaseEventWithSequenceId(EventBuilder<?> eventBuilder) {
        super(eventBuilder);
        mGlobalSequenceId = eventBuilder.mGlobalRequestedId;
        mEventSequenceId = eventBuilder.mRequestedId;
    }

    public int getGlobalSequenceId() {
        return mGlobalSequenceId;
    }

    public int getEventSequenceId() {
        return mEventSequenceId;
    }

    @Override
    public JSONObject toJSONObject() {
        JSONObject json = super.toJSONObject();
        try {
            json.put("mGlobalSequenceId", mGlobalSequenceId);
            json.put("mEventSequenceId", mEventSequenceId);
        } catch (JSONException ignored) {
        }
        return json;
    }

    public abstract static class EventBuilder<T extends BaseEventWithSequenceId> extends BaseEvent.BaseEventBuilder<T> {
        private int mGlobalRequestedId;
        private int mRequestedId;

        protected EventBuilder(CoreAppState coreAppState) {
            super(coreAppState);
        }

        @Override
        public void readPropertyInGMain() {
            super.readPropertyInGMain();
            EsidProperty esidProperty = EsidProvider.EsidPolicy.get(mCoreAppState.getGlobalContext()).getAndAddEsid(getEventType(), 1);
            mGlobalRequestedId = esidProperty.getGsid();
            mRequestedId = esidProperty.getEsid();
        }
    }
}
