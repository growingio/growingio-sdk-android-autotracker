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
import com.growingio.android.sdk.track.events.ViewElementEvent;
import com.growingio.android.sdk.track.events.base.BaseAttributesEvent;
import com.growingio.android.sdk.track.events.base.BaseEvent;
import com.growingio.android.sdk.track.events.helper.DefaultEventFilterInterceptor;
import com.growingio.android.sdk.track.events.helper.DynamicGeneralPropsGenerator;
import com.growingio.android.sdk.track.events.helper.JsonSerializableFactory;
import com.growingio.android.sdk.track.listener.TrackThread;
import com.growingio.android.sdk.track.log.Logger;
import com.growingio.android.sdk.track.middleware.GEvent;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
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
    private DynamicGeneralPropsGenerator dynamicGeneralPropsGenerator;

    private static final JsonSerializableFactory serializableFactory = new JsonSerializableFactory();

    private ConfigurationProvider configurationProvider;
    private TrackerContext context;

    private final AttributesBuilder generalProps = new AttributesBuilder();
    private final CustomEventReferPage customEventReferPage = new CustomEventReferPage();
    private static final int MAX_PENDING_NATIVE_PAGES = 16;
    private final LinkedList<NativePage> pendingNativePages = new LinkedList<>();

    EventBuilderProvider() {
    }

    @Override
    public void setup(TrackerContext context) {
        configurationProvider = context.getConfigurationProvider();
        customEventReferPage.isPageRefer = configurationProvider.core().isCustomEventWithPath();
        this.context = context;
    }

    @Override
    public void shutdown() {
        mEventBuildInterceptors.clear();
        generalProps.clear();
        dynamicGeneralPropsGenerator = null;
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
                Logger.d(TAG, "add general props:[" + key + "-" + value + "]");
            }
        }
    }

    public void clearGeneralProps() {
        generalProps.clear();
    }

    public void removeGeneralProps(String... keys) {
        for (String key : keys) {
            generalProps.removeAttribute(key);
            Logger.d(TAG, "remove general props of: " + key);
        }
    }

    @TrackThread
    public BaseEvent onGenerateGEvent(BaseEvent.BaseBuilder<?> gEvent) {
        updateCustomEventReferPage(gEvent);
        dispatchEventWillBuild(gEvent);

        BaseEvent.BaseBuilder<?> eventBuilder = transformEventBuilder(gEvent);
        if (!filterEvent(eventBuilder)) return null;

        addDynamicPropsToAllEvent(eventBuilder);
        addGeneralPropsToAllEvent(eventBuilder);

        eventBuilder.readPropertyInTrackThread(context);
        if (!configurationProvider.isDowngrade()) {
            eventBuilder.readNewPropertyInTrackThread(context);
        }

        BaseEvent event = eventBuilder.build();
        dispatchEventDidBuild(event);

        return event;
    }


    /**
     * 登记原生页面：由 PageProvider 在发送原生 PAGE 前调用（UI 线程）。
     * 只有登记过的 PAGE 在 track 线程处理时才会更新 CUSTOM 的页面信息，Flutter / Compose 等其他来源的 PAGE 不更新，与 master 一致；
     * 页面信息在 track 线程按事件处理顺序更新，同一 CUSTOM 的 path 与 referralPage 来自同一个 PAGE。
     */
    public void setCustomEventReferPage(String pagePath, long timeStamp) {
        synchronized (pendingNativePages) {
            pendingNativePages.addLast(new NativePage(pagePath, timeStamp));
            // 未进入 track 线程的 PAGE（如数据采集关闭时）不会被消费，限制登记数量
            while (pendingNativePages.size() > MAX_PENDING_NATIVE_PAGES) {
                pendingNativePages.removeFirst();
            }
        }
    }

    private void setCustomEventReferPage(String pagePath, String referralPage, long timeStamp) {
        customEventReferPage.pagePath = pagePath;
        customEventReferPage.referralPage = referralPage;
        customEventReferPage.timeStamp = timeStamp;
    }

    private void updateCustomEventReferPage(BaseEvent.BaseBuilder<?> gEvent) {
        if (gEvent.getClass() == PageEvent.Builder.class) {
            PageEvent.Builder pageBuilder = (PageEvent.Builder) gEvent;
            if (consumeNativePage(pageBuilder.getPath(), pageBuilder.getTimestamp())) {
                setCustomEventReferPage(pageBuilder.getPath(), pageBuilder.getReferralPage(), pageBuilder.getTimestamp());
            }
        }
    }

    /**
     * PAGE 是否为登记过的原生页面；命中时移除该登记及其之前未被消费的登记。
     */
    private boolean consumeNativePage(String pagePath, long timeStamp) {
        synchronized (pendingNativePages) {
            int index = 0;
            for (NativePage page : pendingNativePages) {
                if (page.timeStamp == timeStamp && TextUtils.equals(page.pagePath, pagePath)) {
                    for (int i = 0; i <= index; i++) {
                        pendingNativePages.removeFirst();
                    }
                    return true;
                }
                index++;
            }
            return false;
        }
    }

    private BaseEvent.BaseBuilder<?> transformEventBuilder(BaseEvent.BaseBuilder<?> gEvent) {
        // only for custom event, exclude page level custom event
        if (gEvent instanceof CustomEvent.Builder
                && !(gEvent instanceof PageLevelCustomEvent.Builder)) {
            CustomEvent.Builder customBuilder = (CustomEvent.Builder) gEvent;
            if (!configurationProvider.isDowngrade()
                    && configurationProvider.core().isCustomEventWithReferralPath()) {
                customBuilder.setReferralPage(customEventReferPage.referralPage);
            }
            if (customEventReferPage.isPageRefer) {
                PageLevelCustomEvent.Builder newBuilder = new PageLevelCustomEvent.Builder();
                newBuilder.setAttributes(customBuilder.getAttributes());
                newBuilder.setEventName(customBuilder.getEventName());
                newBuilder.setReferralPage(customBuilder.getReferralPage());
                if (configurationProvider.isDowngrade()) {
                    newBuilder.setPageShowTimestamp(customEventReferPage.timeStamp);
                }
                newBuilder.setPath(customEventReferPage.pagePath);
                return newBuilder;
            }
        }
        return gEvent;
    }

    private void addDynamicPropsToAllEvent(BaseEvent.BaseBuilder<?> gEvent) {
        if (dynamicGeneralPropsGenerator == null) return;
        try {
            if (gEvent instanceof BaseAttributesEvent.Builder) {
                BaseAttributesEvent.Builder attrEventBuilder = (BaseAttributesEvent.Builder) gEvent;
                Map<String, String> dynamicProps = dynamicGeneralPropsGenerator.generateDynamicGeneralProps();
                attrEventBuilder.setGeneralProps(dynamicProps);
            }
        } catch (Exception e) {
            Logger.e(TAG, "dynamicGeneralPropGenerator generateDynamicGeneralProps error", e);
        }
    }

    private void addGeneralPropsToAllEvent(BaseEvent.BaseBuilder<?> gEvent) {
        if (gEvent instanceof BaseAttributesEvent.Builder) {
            BaseAttributesEvent.Builder attrEventBuilder = (BaseAttributesEvent.Builder) gEvent;
            attrEventBuilder.setGeneralProps(generalProps.build());
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

    public void setDynamicGeneralPropGenerator(DynamicGeneralPropsGenerator dynamicGeneralPropsGenerator) {
        this.dynamicGeneralPropsGenerator = dynamicGeneralPropsGenerator;
    }

    private static class CustomEventReferPage {
        private boolean isPageRefer = false;
        private String pagePath = "/";
        private String referralPage;
        private long timeStamp = 0L;
    }

    private static class NativePage {
        private final String pagePath;
        private final long timeStamp;

        private NativePage(String pagePath, long timeStamp) {
            this.pagePath = pagePath;
            this.timeStamp = timeStamp;
        }
    }
}
