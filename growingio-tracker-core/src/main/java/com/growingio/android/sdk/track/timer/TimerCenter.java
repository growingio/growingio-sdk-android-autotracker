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

package com.growingio.android.sdk.track.timer;

import android.os.SystemClock;

import com.growingio.android.sdk.track.TrackMainThread;
import com.growingio.android.sdk.track.events.CustomEvent;
import com.growingio.android.sdk.track.ipc.PersistentDataProvider;
import com.growingio.android.sdk.track.listener.IActivityLifecycle;
import com.growingio.android.sdk.track.listener.event.ActivityLifecycleEvent;
import com.growingio.android.sdk.track.providers.ActivityStateProvider;
import com.growingio.android.sdk.track.providers.ConfigurationProvider;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TimerCenter implements IActivityLifecycle {
    private boolean mEnterBackground = false;
    private final Map<String, Timer> mTimers = new HashMap<>();

    private static class SingleInstance {
        private static final TimerCenter INSTANCE = new TimerCenter();
    }

    private TimerCenter() {
        ActivityStateProvider.get().registerActivityLifecycleListener(this);
    }

    public static TimerCenter get() {
        return TimerCenter.SingleInstance.INSTANCE;
    }

    public String startTimer(String eventName) {
        if (!ConfigurationProvider.core().isDataCollectionEnabled()) {
            return null;
        }
        long startTime = SystemClock.elapsedRealtime();
        String timerId = String.format("%s_%s", eventName, UUID.randomUUID().toString());
        Timer timer = new Timer(startTime, eventName);
        synchronized (mTimers) {
            if (!ConfigurationProvider.core().isDataCollectionEnabled()) {
                return null;
            }
            mTimers.put(timerId, timer);
        }

        return timerId;
    }

    public void updateTimer(String timerId, boolean isResume) {
        long currentTime = SystemClock.elapsedRealtime();
        synchronized (mTimers) {
            Timer timer = mTimers.get(timerId);
            if (timer != null) {
                timer.updateState(currentTime, isResume);
            }
        }
    }

    public void endTimer(String timerId) {
        endTimer(timerId, null);
    }

    public void endTimer(String timerId, Map<String, String> attributes) {
        long currentTime = SystemClock.elapsedRealtime();
        synchronized (mTimers) {
            Timer timer = mTimers.get(timerId);
            if (timer != null) {
                attributes = (attributes == null) ? new HashMap<>() : new HashMap<>(attributes);
                attributes.put("eventDuration", timer.computeElapsedTime(currentTime));
                TrackMainThread.trackMain().postEventToTrackMain(
                        new CustomEvent.Builder()
                                .setEventName(timer.getEventName())
                                .setAttributes(attributes)
                );
                mTimers.remove(timerId);
            }
        }
    }

    public void removeTimer(String timerId) {
        synchronized (mTimers) {
            mTimers.remove(timerId);
        }
    }

    public void clearTimer() {
        synchronized (mTimers) {
            mTimers.clear();
        }
    }

    @Override
    public void onActivityLifecycle(ActivityLifecycleEvent event) {
        if (event.eventType == ActivityLifecycleEvent.EVENT_TYPE.ON_STARTED) {
            if (mEnterBackground) {
                mEnterBackground = false;
                long currentTime = SystemClock.elapsedRealtime();
                synchronized (mTimers) {
                    for (Timer timer : mTimers.values()) {
                        if (timer != null) {
                            timer.resetStartTimeBeforeEnterForeground(currentTime);
                        }
                    }
                }
            }
        } else if (event.eventType == ActivityLifecycleEvent.EVENT_TYPE.ON_STOPPED) {
            if (PersistentDataProvider.get().getActivityCount() == 0) {
                mEnterBackground = true;
                long currentTime = SystemClock.elapsedRealtime();
                synchronized (mTimers) {
                    for (Timer timer : mTimers.values()) {
                        if (timer != null) {
                            timer.computeElapsedTimeBeforeEnterBackground(currentTime);
                        }
                    }
                }
            }
        }
    }
}
