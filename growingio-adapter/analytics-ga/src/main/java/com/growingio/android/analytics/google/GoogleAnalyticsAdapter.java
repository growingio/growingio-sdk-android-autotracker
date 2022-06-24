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

package com.growingio.android.analytics.google;

import android.content.res.XmlResourceParser;
import android.text.TextUtils;

import com.google.android.gms.analytics.Tracker;
import com.growingio.android.analytics.google.model.AnalyticsEvent;
import com.growingio.android.analytics.google.model.TrackerInfo;
import com.growingio.android.sdk.TrackerContext;
import com.growingio.android.sdk.track.TrackMainThread;
import com.growingio.android.sdk.track.events.AutotrackEventType;
import com.growingio.android.sdk.track.events.CustomEvent;
import com.growingio.android.sdk.track.events.EventBuildInterceptor;
import com.growingio.android.sdk.track.events.LoginUserAttributesEvent;
import com.growingio.android.sdk.track.events.PageEvent;
import com.growingio.android.sdk.track.events.TrackEventType;
import com.growingio.android.sdk.track.events.VisitEvent;
import com.growingio.android.sdk.track.events.base.BaseEvent;
import com.growingio.android.sdk.track.ipc.PersistentDataProvider;
import com.growingio.android.sdk.track.listener.IActivityLifecycle;
import com.growingio.android.sdk.track.listener.TrackThread;
import com.growingio.android.sdk.track.listener.event.ActivityLifecycleEvent;
import com.growingio.android.sdk.track.middleware.GEvent;
import com.growingio.android.sdk.track.providers.ActivityStateProvider;
import com.growingio.android.sdk.track.providers.ConfigurationProvider;
import com.growingio.android.sdk.track.providers.SessionProvider;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 不支持多进程
 */
public class GoogleAnalyticsAdapter implements IActivityLifecycle {
    private static final String TAG = "GoogleAnalyticsAdapter";
    private static final String USER_ID_KEY = "&uid";
    private static final String CLIENT_ID_KEY = "&cid";
    private static final String MEASUREMENT_ID_KEY = "&tid";

    private final Map<String, TrackerInfo> mTrackers = new HashMap<>();
    private final GoogleAnalyticsConfiguration mGoogleAnalyticsConfiguration;

    private VisitEvent mLastVisitEvent = null;
    private PageEvent mLastPageEvent = null;
    private long mSessionInterval = 30 * 1000L;


    private static class SingleInstance {
        private static final GoogleAnalyticsAdapter INSTANCE = new GoogleAnalyticsAdapter();
    }

    private GoogleAnalyticsAdapter() {
        mGoogleAnalyticsConfiguration = ConfigurationProvider.get().getConfiguration(GoogleAnalyticsConfiguration.class);
        mSessionInterval = ConfigurationProvider.core().getSessionInterval() * 1000L;
        ActivityStateProvider.get().registerActivityLifecycleListener(this);
        TrackMainThread.trackMain().addEventBuildInterceptor(new EventBuildInterceptor() {
            @Override
            public void eventWillBuild(BaseEvent.BaseBuilder<?> eventBuilder) {
            }

            @Override
            public void eventDidBuild(GEvent event) {
                if (event instanceof VisitEvent) {
                    mLastVisitEvent = (VisitEvent) event;
                } else if (event instanceof PageEvent) {
                    mLastPageEvent = (PageEvent) event;
                }
            }
        });
    }

    public static GoogleAnalyticsAdapter get() {
        return SingleInstance.INSTANCE;
    }

    @Override
    public void onActivityLifecycle(ActivityLifecycleEvent event) {
        if (PersistentDataProvider.get().getActivityCount() == 0) {
            long latestPauseTime = PersistentDataProvider.get().getLatestPauseTime();
            if (latestPauseTime != 0 && (System.currentTimeMillis() - latestPauseTime >= mSessionInterval)) {
                TrackMainThread.trackMain().postActionToTrackMain(new Runnable() {
                    @Override
                    public void run() {
                        // 更新所有 Tracker 的 session，并补发相应vst事件
                        for (TrackerInfo trackerInfo : mTrackers.values()) {
                            trackerInfo.setSessionId(UUID.randomUUID().toString());
                            TrackMainThread.trackMain().postGEventToTrackMain(newAnalyticsEvent(new VisitEvent.Builder(), trackerInfo));
                        }
                    }
                });
            }
        }

    }

    // 解析 GA3 配置xml
    void newTracker(Tracker tracker, int resId) {
        try {
            XmlResourceParser parser = TrackerContext.get().getResources().getXml(resId);
            boolean foundTag = false;
            while (parser.getEventType() != XmlResourceParser.END_DOCUMENT) {
                if (parser.getEventType() == XmlResourceParser.START_TAG) {
                    String tagName = parser.getName();
                    String nameAttr = parser.getAttributeValue(null, "name");
                    foundTag = "string".equals(tagName) && "ga_trackingId".equals(nameAttr);
                }
                if (parser.getEventType() == XmlResourceParser.TEXT) {
                    if (foundTag) {
                        String measurementId = parser.getText();
                        if (!TextUtils.isEmpty(measurementId)) {
                            newTracker(tracker, measurementId);
                            return;
                        }
                    }
                }
                parser.next();
            }
        } catch (Exception ignored) {
        }
    }

    @TrackThread
    void newTracker(Tracker tracker, String measurementId) {
        TrackMainThread.trackMain().postActionToTrackMain(new Runnable() {
            @Override
            public void run() {
                String datasourceId = mGoogleAnalyticsConfiguration.getDatasourceIds().get(measurementId);
                if (measurementId != null && !TextUtils.isEmpty(datasourceId) && !mTrackers.containsKey(measurementId)) {
                    TrackerInfo trackerInfo = new TrackerInfo(datasourceId, UUID.randomUUID().toString());
                    trackerInfo.setEventBuildInterceptor(new EventBuildInterceptor() {
                        @Override
                        public void eventWillBuild(BaseEvent.BaseBuilder<?> eventBuilder) {
                        }

                        @Override
                        public void eventDidBuild(GEvent event) {
                            // 转发所有无埋点事件, VST不进行再次转发
                            if (TrackEventType.APP_CLOSED.equals(event.getEventType()) ||
                                TrackEventType.FORM_SUBMIT.equals(event.getEventType()) ||
                                AutotrackEventType.PAGE.equals(event.getEventType()) ||
                                AutotrackEventType.PAGE_ATTRIBUTES.equals(event.getEventType()) ||
                                AutotrackEventType.VIEW_CHANGE.equals(event.getEventType()) ||
                                AutotrackEventType.VIEW_CLICK.equals(event.getEventType())) {
                                if (event instanceof BaseEvent) {
                                    TrackMainThread.trackMain().postGEventToTrackMain(transformAnalyticsEvent((BaseEvent) event, trackerInfo));
                                }
                            }
                        }
                    });
                    // 增加对应Tracker拦截器
                    TrackMainThread.trackMain().addEventBuildInterceptor(trackerInfo.getEventBuildInterceptor());

                    // 补发vst，page，更新时间为Tracker创建时间
                    // 直接入库，不经过Interceptor
                    if (mLastVisitEvent != null) {
                        TrackMainThread.trackMain().postGEventToTrackMain(transformAnalyticsEvent(mLastVisitEvent, trackerInfo, System.currentTimeMillis()));
                    }
                    if (mLastPageEvent != null) {
                        TrackMainThread.trackMain().postGEventToTrackMain(transformAnalyticsEvent(mLastPageEvent, trackerInfo, System.currentTimeMillis()));
                    }

                    // 发送 用户属性事件 用于关联历史数据
                    String clientId = getClientId(tracker);
                    if (!TextUtils.isEmpty(clientId)) {
                        Map<String, String> attributes = new HashMap<>();
                        attributes.put(CLIENT_ID_KEY, clientId);
                        TrackMainThread.trackMain().postGEventToTrackMain(newAnalyticsEvent(new LoginUserAttributesEvent.Builder()
                                .setAttributes(attributes), trackerInfo));
                    }

                    mTrackers.put(measurementId, trackerInfo);
                }
            }
        });
    }

    @TrackThread
    void setClientId(Tracker tracker, String clientId) {
        TrackMainThread.trackMain().postActionToTrackMain(new Runnable() {
            @Override
            public void run() {
                TrackerInfo trackerInfo = mTrackers.get(getMeasurementId(tracker));
                if (trackerInfo != null) {
                    Map<String, String> attributes = new HashMap<>();
                    attributes.put(CLIENT_ID_KEY, clientId);
                    TrackMainThread.trackMain().postGEventToTrackMain(newAnalyticsEvent(new LoginUserAttributesEvent.Builder()
                            .setAttributes(attributes), trackerInfo));
                }
            }
        });
    }

    @TrackThread
    void send(Tracker tracker, Map<String, String> params) {
        TrackMainThread.trackMain().postActionToTrackMain(new Runnable() {
            @Override
            public void run() {
                TrackerInfo trackerInfo = mTrackers.get(getMeasurementId(tracker));
                if (trackerInfo != null) {
                    TrackMainThread.trackMain().postGEventToTrackMain(newAnalyticsEvent(new CustomEvent.Builder()
                            .setEventName("GAEvent")
                            .setAttributes(params), trackerInfo));
                }
            }
        });
    }

    @TrackThread
    void set(Tracker tracker, String key, String value) {
        TrackMainThread.trackMain().postActionToTrackMain(new Runnable() {
            @Override
            public void run() {
                TrackerInfo trackerInfo = mTrackers.get(getMeasurementId(tracker));
                if (trackerInfo != null) {
                    if (USER_ID_KEY.equals(key)) {
                        setUserId(trackerInfo, value);
                        return;
                    }
                    setDefaultParam(trackerInfo, key, value);
                }
            }
        });
    }

    private void setUserId(TrackerInfo trackerInfo, String userId) {
        if (TextUtils.isEmpty(userId)) {
            trackerInfo.setUserId(null);
            return;
        }

        String oldUserId = trackerInfo.getUserId();
        // A -> A 直接返回
        if (userId.equals(oldUserId)) {
            return;
        }
        trackerInfo.setUserId(userId);

        String lastUserId = trackerInfo.getLastUserId();
        // null -> A 补发vst
        // A -> null -> A 不做session变更, 与3.0保持一致，不补发vst
        if (TextUtils.isEmpty(lastUserId)) {
            SessionProvider.get().generateVisit();
        } else {
            if (!userId.equals(lastUserId)) {
                // 更新session， 补发vst事件
                trackerInfo.setSessionId(UUID.randomUUID().toString());
                TrackMainThread.trackMain().postGEventToTrackMain(newAnalyticsEvent(new VisitEvent.Builder(), trackerInfo));
            }
        }
        trackerInfo.setLastUserId(userId);
    }

    private void setDefaultParam(TrackerInfo trackerInfo, String key, String value) {
        trackerInfo.addParam(key, value);
    }

    private String getMeasurementId(Tracker tracker) {
        try {
            // GA 未初始化完成可能抛出IllegalStateException
            return tracker.get(MEASUREMENT_ID_KEY);
        } catch (Exception e) {
        }

        return null;
    }

    private String getClientId(Tracker tracker) {
        try {
            return tracker.get(CLIENT_ID_KEY);
        } catch (Exception e) {
        }

        return null;
    }

    // 主动构造的事件需要执行readPropertyInTrackThread读取通用参数
    private <T extends BaseEvent> AnalyticsEvent newAnalyticsEvent(BaseEvent.BaseBuilder<T> baseBuilder, TrackerInfo trackerInfo) {
        baseBuilder.readPropertyInTrackThread();
        return new AnalyticsEvent(baseBuilder.build(), trackerInfo);
    }

    // 转发事件已经执行过readPropertyInTrackThread
    private AnalyticsEvent transformAnalyticsEvent(BaseEvent event, TrackerInfo trackerInfo, long timestamp) {
        return new AnalyticsEvent(event, trackerInfo, timestamp);
    }

    private AnalyticsEvent transformAnalyticsEvent(BaseEvent event, TrackerInfo trackerInfo) {
        return new AnalyticsEvent(event, trackerInfo);
    }
}
