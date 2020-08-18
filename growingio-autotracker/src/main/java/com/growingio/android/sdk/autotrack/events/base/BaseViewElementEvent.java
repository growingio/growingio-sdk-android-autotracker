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

package com.growingio.android.sdk.autotrack.events.base;

import com.growingio.android.sdk.track.data.EventSequenceId;
import com.growingio.android.sdk.track.data.PersistentDataProvider;
import com.growingio.android.sdk.track.events.base.BaseEvent;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public abstract class BaseViewElementEvent extends BaseEvent {
    private final String mPageName;
    private final long mPageShowTimestamp;
    private final List<BaseViewElement> mViewElements = new ArrayList<>();

    protected BaseViewElementEvent(EventBuilder<?> eventBuilder) {
        super(eventBuilder);
        mPageName = eventBuilder.mPageName;
        mPageShowTimestamp = eventBuilder.mPageShowTimestamp;
        for (BaseViewElement.BaseElementBuilder<?> elementBuilder : eventBuilder.mElementBuilders) {
            mViewElements.add(elementBuilder.build());
        }
    }

    public String getPageName() {
        return mPageName;
    }

    public long getPageShowTimestamp() {
        return mPageShowTimestamp;
    }

    public List<BaseViewElement> getViewElements() {
        return mViewElements;
    }

    @Override
    public JSONObject toJSONObject() {
        JSONObject json = super.toJSONObject();
        try {
            json.put("mPageName", mPageName);
            json.put("mPageShowTimestamp", mPageShowTimestamp);
            JSONArray jsonArray = new JSONArray();
            for (BaseViewElement element : mViewElements) {
                jsonArray.put(element.toJSONObject());
            }
            json.put("mViewElements", jsonArray);
        } catch (JSONException ignored) {
        }
        return json;
    }

    public abstract static class EventBuilder<T extends BaseViewElementEvent> extends BaseEvent.BaseEventBuilder<T> {
        private final List<BaseViewElement.BaseElementBuilder<?>> mElementBuilders = new ArrayList<>();
        private String mPageName;
        private long mPageShowTimestamp;

        protected EventBuilder() {
            super();
        }

        public EventBuilder<T> addElementBuilder(BaseViewElement.BaseElementBuilder<?> elementBuilder) {
            mElementBuilders.add(elementBuilder);
            return this;
        }

        public EventBuilder<T> addElementBuilders(List<BaseViewElement.BaseElementBuilder<?>> elementBuilders) {
            mElementBuilders.addAll(elementBuilders);
            return this;
        }

        public List<BaseViewElement.BaseElementBuilder<?>> getElementBuilders() {
            return mElementBuilders;
        }

        public String getPageName() {
            return mPageName;
        }

        public EventBuilder<T> setPageName(String pageName) {
            mPageName = pageName;
            return this;
        }

        public long getPageShowTimestamp() {
            return mPageShowTimestamp;
        }

        public EventBuilder<T> setPageShowTimestamp(long pageShowTimestamp) {
            mPageShowTimestamp = pageShowTimestamp;
            return this;
        }

        @Override
        public void readPropertyInTrackThread() {
            super.readPropertyInTrackThread();
            for (BaseViewElement.BaseElementBuilder<?> elementBuilder : mElementBuilders) {
                EventSequenceId sequenceId = PersistentDataProvider.get().getAndIncrement(getEventType());
                elementBuilder.setGlobalSequenceId(sequenceId.getGlobalId())
                        .setEventSequenceId(sequenceId.getEventTypeId());
            }
        }
    }
}
