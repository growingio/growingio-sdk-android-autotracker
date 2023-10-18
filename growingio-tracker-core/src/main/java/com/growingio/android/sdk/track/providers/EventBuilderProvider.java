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
package com.growingio.android.sdk.track.providers;

import android.text.TextUtils;

import com.growingio.android.sdk.TrackerContext;
import com.growingio.android.sdk.track.events.AttributesBuilder;
import com.growingio.android.sdk.track.events.CustomEvent;
import com.growingio.android.sdk.track.events.EventBuildInterceptor;
import com.growingio.android.sdk.track.events.EventFilterInterceptor;
import com.growingio.android.sdk.track.events.PageEvent;
import com.growingio.android.sdk.track.events.PageLevelCustomEvent;
import com.growingio.android.sdk.track.events.TrackEventType;
import com.growingio.android.sdk.track.events.ViewElementEvent;
import com.growingio.android.sdk.track.events.base.BaseEvent;
import com.growingio.android.sdk.track.events.helper.DefaultEventFilterInterceptor;
import com.growingio.android.sdk.track.events.helper.JsonSerializableFactory;
import com.growingio.android.sdk.track.listener.TrackThread;
import com.growingio.android.sdk.track.log.Logger;
import com.growingio.android.sdk.track.middleware.GEvent;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * <p>
 *
 * @author cpacm 2023/3/21
 */
public class EventBuilderProvider implements TrackerLifecycleProvider {

    private static final String TAG = "EventBuilderProvider";

    private final List<EventBuildInterceptor> mEventBuildInterceptors = new ArrayList<>();
    private EventFilterInterceptor defaultFilterInterceptor;

    private static final JsonSerializableFactory serializableFactory = new JsonSerializableFactory();

    private ConfigurationProvider configurationProvider;
    private TrackerContext context;

    private final AttributesBuilder generalProps = new AttributesBuilder();

    EventBuilderProvider() {
    }

    @Override
    public void setup(TrackerContext context) {
        configurationProvider = context.getConfigurationProvider();
        this.context = context;
    }

    @Override
    public void shutdown() {
        mEventBuildInterceptors.clear();
    }

    public static JSONObject toJson(BaseEvent event) {
        JSONObject jsonObject = new JSONObject();
        serializableFactory.toJson(jsonObject, event);
        return jsonObject;
    }

    public static void toJson(JSONObject jsonObject, BaseEvent event) {
        serializableFactory.toJson(jsonObject, event);
    }

    public static void parseFrom(BaseEvent.BaseBuilder builder, JSONObject jsonObject) {
        serializableFactory.parseFrom(builder, jsonObject);
    }

    public void setGeneralProps(Map<String, String> properties) {
        if (properties != null && properties.keySet() != null) {
            for (String key : properties.keySet()) {
                String value = properties.get(key);
                generalProps.addAttribute(key, value);
            }
        }
    }

    public void clearGeneralProps() {
        generalProps.clear();
    }

    public void clearGeneralProps(String... keys) {
        for (String key : keys) {
            generalProps.removeAttribute(key);
        }
    }

    @TrackThread
    public BaseEvent onGenerateGEvent(BaseEvent.BaseBuilder<?> gEvent) {
        dispatchEventWillBuild(gEvent);

        if (!filterEvent(gEvent)) return null;

        addGeneralPropsToEvent(gEvent);
        gEvent.readPropertyInTrackThread(context);

        BaseEvent event = gEvent.build();
        dispatchEventDidBuild(event);

        return event;
    }

    private void addGeneralPropsToEvent(BaseEvent.BaseBuilder<?> gEvent) {
        if (gEvent.getEventType().equals(TrackEventType.CUSTOM)) {
            if (gEvent instanceof CustomEvent.Builder) {
                CustomEvent.Builder customEventBuilder = (CustomEvent.Builder) gEvent;
                customEventBuilder.setGeneralProps(generalProps.build());
            }
        }
    }

    public void removeEventBuildInterceptor(EventBuildInterceptor interceptor) {
        synchronized (mEventBuildInterceptors) {
            if (interceptor != null) {
                mEventBuildInterceptors.remove(interceptor);
            }
        }
    }

    public void addEventBuildInterceptor(EventBuildInterceptor interceptor) {
        synchronized (mEventBuildInterceptors) {
            boolean needsAdd = true;
            Iterator<EventBuildInterceptor> refIterator = mEventBuildInterceptors.iterator();
            while (refIterator.hasNext()) {
                EventBuildInterceptor storedInterceptor = refIterator.next();
                if (null == storedInterceptor) {
                    refIterator.remove();
                } else if (storedInterceptor == interceptor) {
                    needsAdd = false;
                }
            }
            if (needsAdd) {
                mEventBuildInterceptors.add(interceptor);
            }
        }
    }

    private void dispatchEventWillBuild(BaseEvent.BaseBuilder<?> eventBuilder) {
        synchronized (mEventBuildInterceptors) {
            Iterator<EventBuildInterceptor> refIter = mEventBuildInterceptors.iterator();
            while (refIter.hasNext()) {
                EventBuildInterceptor interceptor = refIter.next();
                if (null == interceptor) {
                    refIter.remove();
                } else {
                    try {
                        interceptor.eventWillBuild(eventBuilder);
                    } catch (Exception e) {
                        Logger.e(TAG, e);
                    }
                }
            }
        }
    }

    private void dispatchEventDidBuild(GEvent event) {
        synchronized (mEventBuildInterceptors) {
            Iterator<EventBuildInterceptor> refIterator = mEventBuildInterceptors.iterator();
            while (refIterator.hasNext()) {
                EventBuildInterceptor interceptor = refIterator.next();
                if (null == interceptor) {
                    refIterator.remove();
                } else {
                    try {
                        interceptor.eventDidBuild(event);
                    } catch (Exception e) {
                        Logger.e(TAG, e);
                    }
                }
            }
        }
    }


    private EventFilterInterceptor getEventFilterInterceptor() {
        if (configurationProvider.core().getEventFilterInterceptor() != null) {
            return configurationProvider.core().getEventFilterInterceptor();
        } else {
            if (defaultFilterInterceptor == null) {
                defaultFilterInterceptor = new DefaultEventFilterInterceptor();
            }
            return defaultFilterInterceptor;
        }
    }

    @TrackThread
    boolean filterEvent(BaseEvent.BaseBuilder<?> eventBuilder) {
        EventFilterInterceptor eventFilterInterceptor = getEventFilterInterceptor();
        if (eventFilterInterceptor == null) return true;

        if (!eventFilterInterceptor.filterEventType(eventBuilder.getEventType())) {
            Logger.w(TAG, "filter [" + eventBuilder.getEventType() + "] event by type");
            return false;
        }

        String eventPath = getEventPath(eventBuilder);
        if (!TextUtils.isEmpty(eventPath) && !eventFilterInterceptor.filterEventPath(eventPath)) {
            Logger.w(TAG, "filter [" + eventBuilder.getEventType() + "] event by path=" + eventPath);
            return false;
        }

        String eventName = getEventName(eventBuilder);
        if (!TextUtils.isEmpty(eventName) && !eventFilterInterceptor.filterEventName(eventName)) {
            Logger.w(TAG, "filter [CUSTOM] event by name=" + eventName);
            return false;
        }

        Map<String, Boolean> filterFields = eventFilterInterceptor.filterEventField(eventBuilder.getEventType(), eventBuilder.getFilterMap());
        eventBuilder.filterFieldProperty(filterFields);

        return true;
    }

    String getEventName(BaseEvent.BaseBuilder<?> eventBuilder) {
        if (eventBuilder instanceof CustomEvent.Builder) {
            return ((CustomEvent.Builder) eventBuilder).getEventName();
        }
        return null;
    }

    String getEventPath(BaseEvent.BaseBuilder<?> eventBuilder) {
        if (eventBuilder instanceof PageEvent.Builder) {
            return ((PageEvent.Builder) eventBuilder).getPath();
        }
        if (eventBuilder instanceof ViewElementEvent.Builder) {
            return ((ViewElementEvent.Builder) eventBuilder).getPath();
        }
        if (eventBuilder instanceof PageLevelCustomEvent.Builder) {
            return ((PageLevelCustomEvent.Builder) eventBuilder).getPath();
        }
        return null;
    }
}
