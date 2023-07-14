/*
 *   Copyright (C) 2020 Beijing Yishu Technology Co., Ltd.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package com.growingio.android.sdk.track.providers;

import com.growingio.android.sdk.TrackerContext;
import com.growingio.android.sdk.track.TrackMainThread;
import com.growingio.android.sdk.track.events.base.BaseEvent;
import com.growingio.android.sdk.track.events.helper.JsonSerializableFactory;
import com.growingio.android.sdk.track.log.CircularFifoQueue;
import com.growingio.android.sdk.track.log.Logger;

import org.json.JSONObject;

/**
 * <p>
 *
 * @author cpacm 2023/3/21
 */
public class EventStateProvider {

    private static final String TAG = "EventStateProvider";

    private static class SingleInstance {
        private static final EventStateProvider INSTANCE = new EventStateProvider();
    }

    private EventStateProvider() {
        serializableFactory = new JsonSerializableFactory();
    }

    private final CircularFifoQueue<BaseEvent.BaseBuilder<?>> caches = new CircularFifoQueue<>(200);

    private final JsonSerializableFactory serializableFactory;

    public static EventStateProvider get() {
        return EventStateProvider.SingleInstance.INSTANCE;
    }

    public JSONObject toJson(BaseEvent event) {
        JSONObject jsonObject = new JSONObject();
        serializableFactory.toJson(jsonObject, event);
        return jsonObject;
    }

    public void toJson(JSONObject jsonObject, BaseEvent event) {
        serializableFactory.toJson(jsonObject, event);
    }

    public void parseFrom(BaseEvent.BaseBuilder builder, JSONObject jsonObject) {
        serializableFactory.parseFrom(builder, jsonObject);
    }

    public void releaseCaches() {
        if (caches.size() > 0 && TrackerContext.initializedSuccessfully() && ConfigurationProvider.core().isDataCollectionEnabled()) {
            for (BaseEvent.BaseBuilder<?> eventBuilder : caches) {
                TrackMainThread.trackMain().postEventToTrackMain(eventBuilder);
            }
            Logger.d(TAG, "release cache events after sdk init: count-" + caches.size());
            caches.clear();
        } else if (caches.size() > 0) {
            Logger.w(TAG, "drop events when data collect disabled");
            caches.clear();
        }
    }

    public void cacheEvent(BaseEvent.BaseBuilder<?> eventBuilder) {
        if (!TrackerContext.initializedSuccessfully()) {
            Logger.w(TAG, "cache event before sdk init: " + eventBuilder.getEventType());
            caches.add(eventBuilder);
        } else {
            TrackMainThread.trackMain().postEventToTrackMain(eventBuilder);
        }
    }

}
