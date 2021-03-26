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

package com.growingio.android.sdk.track.crash;

import android.util.Log;

import com.google.common.truth.Truth;
import com.growingio.android.sdk.monitor.log.MonitorLogger;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest({CrashManager.class, MonitorLogger.class})
public class CrashLoggerTest {

    @Test
    public void testCrashLogger() {
        PowerMockito.spy(CrashManager.class);
        PowerMockito.spy(MonitorLogger.class);

        CrashLogger crashLogger = new CrashLogger();
        Truth.assertThat(crashLogger.getType()).isEqualTo("Monitor");
        crashLogger.print(Log.VERBOSE, "tag", "message", new Exception("exception"));
        crashLogger.print(Log.DEBUG, "tag", "message", new Exception("exception"));
        crashLogger.print(Log.INFO, "tag", "message", new Exception("exception"));
        crashLogger.print(Log.WARN, "tag", "message", new Exception("exception"));
        crashLogger.print(Log.ERROR, "tag", "message", new Exception("exception"));
        crashLogger.print(Log.ASSERT, "tag", "message", new Exception("exception"));

        crashLogger.print(-1, "tag", "message", new Exception("exception"));

        PowerMockito.verifyStatic(MonitorLogger.class, Mockito.times(1));
        MonitorLogger.v(Mockito.eq(CrashManager.ALIAS), Mockito.eq("tag"), Mockito.eq("message"));
        PowerMockito.verifyStatic(MonitorLogger.class, Mockito.times(1));
        MonitorLogger.d(Mockito.eq(CrashManager.ALIAS), Mockito.eq("tag"), Mockito.eq("message"));
        PowerMockito.verifyStatic(MonitorLogger.class, Mockito.times(2));
        MonitorLogger.i(Mockito.eq(CrashManager.ALIAS), Mockito.eq("tag"), Mockito.eq("message"));
        PowerMockito.verifyStatic(MonitorLogger.class, Mockito.times(1));
        MonitorLogger.e(Mockito.eq(CrashManager.ALIAS), Mockito.eq("tag"), Mockito.eq("message"));

        PowerMockito.verifyStatic(CrashManager.class, Mockito.times(1));
        CrashManager.sendEvent(Mockito.any());

    }
}
