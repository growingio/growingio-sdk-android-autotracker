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

import android.app.Application;
import android.os.SystemClock;

import androidx.test.core.app.ApplicationProvider;

import com.google.common.truth.Truth;
import com.growingio.android.sdk.TrackerContext;
import com.growingio.android.sdk.track.TrackMainThread;
import com.growingio.android.sdk.track.events.CustomEvent;
import com.growingio.android.sdk.track.events.EventBuildInterceptor;
import com.growingio.android.sdk.track.events.base.BaseEvent;
import com.growingio.android.sdk.track.middleware.GEvent;
import com.growingio.android.sdk.track.providers.ActivityStateProvider;
import com.growingio.android.sdk.track.providers.RobolectricActivity;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.Config;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@Config(manifest = Config.NONE)
@RunWith(RobolectricTestRunner.class)
public class TimerCenterTest {

    private final Application application = ApplicationProvider.getApplicationContext();

    @Before
    public void setup() {
        TrackerContext.init(application);
    }

    @Test
    public void backgroundThenForeground() throws InterruptedException {
        ActivityController<RobolectricActivity> activityController = Robolectric.buildActivity(RobolectricActivity.class);
        application.registerActivityLifecycleCallbacks(ActivityStateProvider.get());
        activityController.create().start().resume();

        final class TimerResult {
            public String eventName;
            public float elapsedTime;
        }
        final TimerResult timerResult = new TimerResult();
        timerResult.eventName = "Test";
        long totalTime = 0;
        Robolectric.getForegroundThreadScheduler().advanceTo(1);
        long startTime = SystemClock.elapsedRealtime();
        String timerId = TimerCenter.get().startTimer(timerResult.eventName);

        Robolectric.getForegroundThreadScheduler().advanceTo(1001);
        long backgroundTime = SystemClock.elapsedRealtime();
        totalTime += backgroundTime - startTime;
        activityController.pause().stop();

        Robolectric.getForegroundThreadScheduler().advanceTo(2001);
        long foregroundTime = SystemClock.elapsedRealtime();
        activityController.restart().start().resume();

        CountDownLatch countDownLatch = new CountDownLatch(1);
        TrackMainThread.trackMain().addEventBuildInterceptor(new EventBuildInterceptor() {
            @Override
            public void eventWillBuild(BaseEvent.BaseBuilder<?> eventBuilder) {
            }

            @Override
            public void eventDidBuild(GEvent event) {
                if (event instanceof CustomEvent) {
                    CustomEvent timerEvent = (CustomEvent) event;
                    Truth.assertThat(timerEvent.getEventName()).isEqualTo(timerResult.eventName);
                    Truth.assertThat(timerEvent.getAttributes().get("eventDuration")).isEqualTo(String.valueOf(timerResult.elapsedTime));
                    countDownLatch.countDown();
                }
            }
        });
        Robolectric.getForegroundThreadScheduler().advanceTo(3001);
        long endTime = SystemClock.elapsedRealtime();
        totalTime += endTime - foregroundTime;
        timerResult.elapsedTime = totalTime / 1000F;
        TimerCenter.get().endTimer(timerId);
        countDownLatch.await(3000, TimeUnit.MILLISECONDS);
    }

    @Test
    public void pauseThenResume() throws InterruptedException {
        final class TimerResult {
            public String eventName;
            public float elapsedTime;
        }

        final TimerResult timerResult = new TimerResult();
        timerResult.eventName = "Test";
        long totalTime = 0;
        Robolectric.getForegroundThreadScheduler().advanceTo(1);
        long startTime = SystemClock.elapsedRealtime();
        String timerId = TimerCenter.get().startTimer(timerResult.eventName);

        Robolectric.getForegroundThreadScheduler().advanceTo(1001);
        long pauseTime = SystemClock.elapsedRealtime();
        totalTime += pauseTime - startTime;
        TimerCenter.get().updateTimer(timerId, false);

        Robolectric.getForegroundThreadScheduler().advanceTo(2001);
        long resumeTime = SystemClock.elapsedRealtime();
        TimerCenter.get().updateTimer(timerId, true);

        CountDownLatch countDownLatch = new CountDownLatch(1);
        TrackMainThread.trackMain().addEventBuildInterceptor(new EventBuildInterceptor() {
            @Override
            public void eventWillBuild(BaseEvent.BaseBuilder<?> eventBuilder) {
            }

            @Override
            public void eventDidBuild(GEvent event) {
                if (event instanceof CustomEvent) {
                    CustomEvent timerEvent = (CustomEvent) event;
                    Truth.assertThat(timerEvent.getEventName()).isEqualTo(timerResult.eventName);
                    Truth.assertThat(timerEvent.getAttributes().get("eventDuration")).isEqualTo(String.valueOf(timerResult.elapsedTime));
                    countDownLatch.countDown();
                }
            }
        });
        Robolectric.getForegroundThreadScheduler().advanceTo(3001);
        long endTime = SystemClock.elapsedRealtime();
        totalTime += endTime - resumeTime;
        timerResult.elapsedTime = totalTime / 1000F;
        TimerCenter.get().endTimer(timerId);
        countDownLatch.await(3000, TimeUnit.MILLISECONDS);
    }
}
