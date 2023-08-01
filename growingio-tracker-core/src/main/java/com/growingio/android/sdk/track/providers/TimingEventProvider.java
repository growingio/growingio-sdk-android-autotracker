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

import android.os.SystemClock;

import com.growingio.android.sdk.TrackerContext;
import com.growingio.android.sdk.track.TrackMainThread;
import com.growingio.android.sdk.track.events.CustomEvent;
import com.growingio.android.sdk.track.utils.TimerEvent;
import com.growingio.android.sdk.track.listener.IActivityLifecycle;
import com.growingio.android.sdk.track.listener.event.ActivityLifecycleEvent;
import com.growingio.android.sdk.track.log.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TimingEventProvider implements IActivityLifecycle, TrackerLifecycleProvider {
    private static final String TAG = "TimerCenter";
    public static final String ATTR_EVENT_DURATION = "event_duration";
    private final Map<String, TimerEvent> timers = new HashMap<>();
    private boolean enterBackground = false;

    private ActivityStateProvider activityStateProvider;
    private ConfigurationProvider configurationProvider;
    private PersistentDataProvider persistentDataProvider;


    @Override
    public void setup(TrackerContext context) {
        configurationProvider = context.getConfigurationProvider();
        persistentDataProvider = context.getProvider(PersistentDataProvider.class);
        activityStateProvider = context.getActivityStateProvider();
        activityStateProvider.registerActivityLifecycleListener(this);
    }

    @Override
    public void shutdown() {
        activityStateProvider.unregisterActivityLifecycleListener(this);
    }

    TimingEventProvider() {

    }

    public String startTimer(String eventName) {
        if (!configurationProvider.core().isDataCollectionEnabled()) {
            return null;
        }
        long startTime = SystemClock.elapsedRealtime();
        String timerId = String.format("%s_%s", eventName, UUID.randomUUID().toString());
        TimerEvent timer = new TimerEvent(startTime, eventName);
        synchronized (timers) {
            if (!configurationProvider.core().isDataCollectionEnabled()) {
                return null;
            }
            timers.put(timerId, timer);
            Logger.d(TAG, "start a timer with %s", timerId);
        }

        return timerId;
    }

    public void updateTimer(String timerId, boolean isResume) {
        long currentTime = SystemClock.elapsedRealtime();
        synchronized (timers) {
            TimerEvent timer = timers.get(timerId);
            if (timer != null) {
                timer.updateState(currentTime, isResume);
                Logger.d(TAG, "update a timer with %s", timerId);
            }
        }
    }

    public void endTimer(String timerId) {
        endTimer(timerId, null);
    }

    public void endTimer(String timerId, Map<String, String> attributes) {
        long currentTime = SystemClock.elapsedRealtime();
        synchronized (timers) {
            TimerEvent timer = timers.get(timerId);
            if (timer != null) {
                attributes = (attributes == null) ? new HashMap<>() : new HashMap<>(attributes);
                attributes.put(ATTR_EVENT_DURATION, timer.computeElapsedTime(currentTime));
                TrackMainThread.trackMain().postEventToTrackMain(
                        new CustomEvent.Builder()
                                .setEventName(timer.getEventName())
                                .setAttributes(attributes)
                );
                timers.remove(timerId);
                Logger.d(TAG, "remove a timer with %s", timerId);
            }
        }
    }

    public void removeTimer(String timerId) {
        synchronized (timers) {
            timers.remove(timerId);
        }
    }

    public void clearTimer() {
        synchronized (timers) {
            timers.clear();
        }
    }

    @Override
    public void onActivityLifecycle(ActivityLifecycleEvent event) {
        if (event.eventType == ActivityLifecycleEvent.EVENT_TYPE.ON_STARTED) {
            if (enterBackground) {
                enterBackground = false;
                long currentTime = SystemClock.elapsedRealtime();
                synchronized (timers) {
                    for (TimerEvent timer : timers.values()) {
                        if (timer != null) {
                            timer.resetStartTimeBeforeEnterForeground(currentTime);
                        }
                    }
                }
            }
        } else if (event.eventType == ActivityLifecycleEvent.EVENT_TYPE.ON_STOPPED) {
            if (persistentDataProvider.getActivityCount() == 0) {
                enterBackground = true;
                long currentTime = SystemClock.elapsedRealtime();
                synchronized (timers) {
                    for (TimerEvent timer : timers.values()) {
                        if (timer != null) {
                            timer.computeElapsedTimeBeforeEnterBackground(currentTime);
                        }
                    }
                }
            }
        }
    }
}
