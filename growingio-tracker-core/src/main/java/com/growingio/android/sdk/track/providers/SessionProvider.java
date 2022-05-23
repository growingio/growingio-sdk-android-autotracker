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

package com.growingio.android.sdk.track.providers;

import android.app.Activity;

import com.growingio.android.sdk.track.TrackMainThread;
import com.growingio.android.sdk.track.ipc.PersistentDataProvider;
import com.growingio.android.sdk.track.events.TrackEventGenerator;
import com.growingio.android.sdk.track.listener.TrackThread;
import com.growingio.android.sdk.track.listener.IActivityLifecycle;
import com.growingio.android.sdk.track.listener.event.ActivityLifecycleEvent;
import com.growingio.android.sdk.track.log.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class SessionProvider implements IActivityLifecycle {
    private static final String TAG = "SessionProvider";

    private final List<String> mActivityList = new ArrayList<>();
    private final long mSessionInterval;
    private double mLatitude = 0;
    private double mLongitude = 0;

    private static class SingleInstance {
        private static final SessionProvider INSTANCE = new SessionProvider();
    }

    private SessionProvider() {
        mSessionInterval = ConfigurationProvider.core().getSessionInterval() * 1000L;
    }

    public void init() {
        mActivityList.clear();
        ActivityStateProvider.get().registerActivityLifecycleListener(this);
    }

    public static SessionProvider get() {
        return SingleInstance.INSTANCE;
    }

    @TrackThread
    void checkAndSendVisit(long resumeTime) {
        if (resumeTime - PersistentDataProvider.get().getLatestPauseTime() >= mSessionInterval) {
            refreshSessionId();
            generateVisit();
        }
    }

    /**
     * 刷新sessionId的场景:
     * 1. 第一个进程初始化时会刷新session
     * 2. 后台30s返回后刷新session
     * 3. 用户id发生改变刷新session
     */
    @TrackThread
    public void refreshSessionId() {
        PersistentDataProvider.get().setSessionId(UUID.randomUUID().toString());
        PersistentDataProvider.get().setSendVisitAfterRefreshSessionId(false);
    }

    @TrackThread
    public void generateVisit() {
        if (!ConfigurationProvider.core().isDataCollectionEnabled()) {
            return;
        }
        PersistentDataProvider.get().setSendVisitAfterRefreshSessionId(true);
        TrackEventGenerator.generateVisitEvent();
    }

    @TrackThread
    public void setLocation(double latitude, double longitude) {
        double eps = 1e-5;
        if (Math.abs(latitude) < eps && Math.abs(longitude) < eps) {
            Logger.d(TAG, "found invalid latitude and longitude, and return: ", latitude, ", ", longitude);
            return;
        }

        // 只有在第一次设置位置信息的时候才会补发Visit，重设位置信息只会更新，不会重发visit
        if ((mLatitude == 0 && Math.abs(latitude) > eps) ||
                (mLongitude == 0 && Math.abs(longitude) > eps)) {
            mLatitude = latitude;
            mLongitude = longitude;
            generateVisit();
        } else {
            mLatitude = latitude;
            mLongitude = longitude;
        }
    }

    @TrackThread
    public void cleanLocation() {
        mLatitude = 0;
        mLongitude = 0;
    }

    @TrackThread
    public double getLatitude() {
        return mLatitude;
    }

    @TrackThread
    public double getLongitude() {
        return mLongitude;
    }

    @Override
    public void onActivityLifecycle(final ActivityLifecycleEvent event) {
        Activity activity = event.getActivity();
        if (activity == null) return;
        if (event.eventType == ActivityLifecycleEvent.EVENT_TYPE.ON_STARTED) {
            if (PersistentDataProvider.get().getActivityCount() == 0) {
                TrackMainThread.trackMain().postActionToTrackMain(new Runnable() {
                    @Override
                    public void run() {
                        checkAndSendVisit(System.currentTimeMillis());
                    }
                });
            }
            mActivityList.add(activity.toString());
            PersistentDataProvider.get().addActivityCount();
        } else if (event.eventType == ActivityLifecycleEvent.EVENT_TYPE.ON_STOPPED) {
            if (mActivityList.contains(activity.toString())) {
                if (mActivityList.remove(activity.toString())) {
                    PersistentDataProvider.get().delActivityCount();
                }
                if (PersistentDataProvider.get().getActivityCount() == 0) {
                    TrackMainThread.trackMain().postActionToTrackMain(new Runnable() {
                        @Override
                        public void run() {
                            PersistentDataProvider.get().setLatestPauseTime(System.currentTimeMillis());
                            TrackEventGenerator.generateAppClosedEvent();
                        }
                    });
                }
            }
        }
    }
}