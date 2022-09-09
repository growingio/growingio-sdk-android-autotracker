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

import com.google.common.truth.Truth;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

@Config(manifest = Config.NONE)
@RunWith(RobolectricTestRunner.class)
public class TimerTest {

    @Test
    public void startThenEnd() {
        Robolectric.getForegroundThreadScheduler().advanceTo(1);
        long startTime = SystemClock.elapsedRealtime();
        String eventName = "Test1";
        Timer timer = new Timer(startTime, eventName);
        Robolectric.getForegroundThreadScheduler().advanceTo(1001);
        long endTime = SystemClock.elapsedRealtime();
        float elapsedTime = (endTime - startTime) / 1000F;

        Truth.assertThat(eventName).isEqualTo(timer.getEventName());
        Truth.assertThat(String.valueOf(elapsedTime)).isEqualTo(timer.computeElapsedTime(endTime));
    }

    @Test
    public void pauseThenResume() {
        long totalTime = 0;
        Robolectric.getForegroundThreadScheduler().advanceTo(1);
        long startTime = SystemClock.elapsedRealtime();
        String eventName = "Test2";
        Timer timer = new Timer(startTime, eventName);
        Robolectric.getForegroundThreadScheduler().advanceTo(1001);
        long pauseTime = SystemClock.elapsedRealtime();
        timer.updateState(pauseTime, false);
        totalTime += pauseTime - startTime;
        Robolectric.getForegroundThreadScheduler().advanceTo(2001);
        long resumeTime = SystemClock.elapsedRealtime();
        timer.updateState(resumeTime, true);
        Robolectric.getForegroundThreadScheduler().advanceTo(3001);
        long endTime = SystemClock.elapsedRealtime();
        totalTime += endTime - resumeTime;
        float elapsedTime = totalTime / 1000F;

        Truth.assertThat(eventName).isEqualTo(timer.getEventName());
        Truth.assertThat(String.valueOf(elapsedTime)).isEqualTo(timer.computeElapsedTime(endTime));
    }

    @Test
    public void backgroundThenForeground() {
        long totalTime = 0;
        Robolectric.getForegroundThreadScheduler().advanceTo(1);
        long startTime = SystemClock.elapsedRealtime();
        String eventName = "Test3";
        Timer timer = new Timer(startTime, eventName);
        Robolectric.getForegroundThreadScheduler().advanceTo(1001);
        long backgroundTime = SystemClock.elapsedRealtime();
        timer.computeElapsedTimeBeforeEnterBackground(backgroundTime);
        totalTime += backgroundTime - startTime;
        Robolectric.getForegroundThreadScheduler().advanceTo(2001);
        long foregroundTime = SystemClock.elapsedRealtime();
        timer.resetStartTimeBeforeEnterForeground(foregroundTime);
        Robolectric.getForegroundThreadScheduler().advanceTo(3001);
        long endTime = SystemClock.elapsedRealtime();
        totalTime += endTime - foregroundTime;
        float elapsedTime = totalTime / 1000F;

        Truth.assertThat(eventName).isEqualTo(timer.getEventName());
        Truth.assertThat(String.valueOf(elapsedTime)).isEqualTo(timer.computeElapsedTime(endTime));
    }
}
