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

import android.app.Activity;

import com.growingio.android.sdk.TrackerContext;
import com.growingio.android.sdk.track.TrackMainThread;
import com.growingio.android.sdk.track.events.TrackEventGenerator;
import com.growingio.android.sdk.track.listener.TrackThread;
import com.growingio.android.sdk.track.listener.IActivityLifecycle;
import com.growingio.android.sdk.track.listener.event.ActivityLifecycleEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class SessionProvider implements IActivityLifecycle, TrackerLifecycleProvider {
    private static final String TAG = "SessionProvider";

    private final List<String> activityList = new ArrayList<>();
    private long sessionInterval = 30000L;
    private ConfigurationProvider configurationProvider;
    private PersistentDataProvider persistentDataProvider;
    private ActivityStateProvider activityStateProvider;

    protected SessionProvider() {
    }


    @Override
    public void setup(TrackerContext context) {
        configurationProvider = context.getConfigurationProvider();
        sessionInterval = configurationProvider.core().getSessionInterval() * 1000L;
        persistentDataProvider = context.getProvider(PersistentDataProvider.class);
        activityList.clear();
        activityStateProvider = context.getActivityStateProvider();
        activityStateProvider.registerActivityLifecycleListener(this);
    }

    public void createVisitAfterAppStart() {
        if (persistentDataProvider.isFirstProcess() && !persistentDataProvider.isSendVisitAfterRefreshSessionId()) {
            generateVisit();
        }
    }


    @Override
    public void shutdown() {
        activityList.clear();
        activityStateProvider.unregisterActivityLifecycleListener(this);
    }

    /**
     * 刷新sessionId的场景:
     * 1. 第一个进程初始化时会刷新session
     * 2. 后台30s返回后刷新session
     * 3. 用户id发生改变刷新session
     */
    @TrackThread
    public void refreshSessionId() {
        persistentDataProvider.setSessionId(UUID.randomUUID().toString());
        persistentDataProvider.setSendVisitAfterRefreshSessionId(false);
    }

    @TrackThread
    public void generateVisit() {
        if (!configurationProvider.core().isDataCollectionEnabled()) {
            return;
        }
        persistentDataProvider.setSendVisitAfterRefreshSessionId(true);
        TrackEventGenerator.generateVisitEvent();
    }

    public void checkSessionIntervalAndSendVisit() {
        if (persistentDataProvider.getActivityCount() == 0) {
            long latestPauseTime = persistentDataProvider.getLatestPauseTime();
            if (latestPauseTime != 0 && (System.currentTimeMillis() - latestPauseTime >= sessionInterval)) {
                TrackMainThread.trackMain().postActionToTrackMain(() -> {
                    refreshSessionId();
                    generateVisit();
                });
                persistentDataProvider.setLatestPauseTime(System.currentTimeMillis());
            }
        }
    }

    @Override
    public void onActivityLifecycle(final ActivityLifecycleEvent event) {
        Activity activity = event.getActivity();
        if (activity == null) return;
        if (event.eventType == ActivityLifecycleEvent.EVENT_TYPE.ON_STARTED) {
            checkSessionIntervalAndSendVisit();
            activityList.add(activity.toString());
            persistentDataProvider.addActivityCount();
        } else if (event.eventType == ActivityLifecycleEvent.EVENT_TYPE.ON_STOPPED) {
            if (activityList.contains(activity.toString())) {
                if (activityList.remove(activity.toString())) {
                    persistentDataProvider.delActivityCount();
                }
                if (persistentDataProvider.getActivityCount() == 0) {
                    persistentDataProvider.setLatestPauseTime(System.currentTimeMillis());
                    TrackMainThread.trackMain().postActionToTrackMain(new Runnable() {
                        @Override
                        public void run() {
                            TrackEventGenerator.generateAppClosedEvent();
                        }
                    });
                }
            }
        }
    }
}