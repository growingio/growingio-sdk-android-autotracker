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

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.growingio.android.sdk.monitor.event.Event;
import com.growingio.android.sdk.monitor.event.EventBuilder;
import com.growingio.android.sdk.monitor.event.interfaces.ExceptionInterface;
import com.growingio.android.sdk.monitor.log.MonitorLogger;
import com.growingio.android.sdk.track.log.BaseLogger;

class CrashLogger extends BaseLogger {
    private static final String TYPE = "Monitor";

    @Override
    protected void print(int priority, @NonNull String tag, @NonNull String message, @Nullable Throwable t) {
        switch (priority) {
            case Log.VERBOSE:
                MonitorLogger.v(CrashManager.ALIAS, tag, message);
                break;
            case Log.DEBUG:
                MonitorLogger.d(CrashManager.ALIAS, tag, message);
                break;
            case Log.INFO:
            case Log.WARN:
                MonitorLogger.i(CrashManager.ALIAS, tag, message);
                break;
            case Log.ERROR:
                MonitorLogger.e(CrashManager.ALIAS, tag, message);
                break;
            case Log.ASSERT:
                CrashManager.sendEvent(new EventBuilder()
                        .withMessage(message)
                        .withLevel(Event.Level.ERROR)
                        .withMonitorInterface(new ExceptionInterface(t)));
            default:
                break;
        }
    }

    @Override
    public String getType() {
        return TYPE;
    }
}
