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
import com.growingio.android.sdk.track.utils.LogUtil;

/**
 * Monitor的日志工具
 * VERBOSE, DEBUG, INFO, WARN, ERROR 仅记录到breadcrumbs中
 * ALARM直接上报sentry
 */
public class CrashUtil extends LogUtil.Util {
    /**
     * alarm报警等级，直接上报sentry，不进行日志输出
     */
    public static final int ALARM = Log.ASSERT + 1;

    private CrashUtil() {
    }

    public static CrashUtil getInstance() {
        return SingleInstance.INSTANCE;
    }

    @Override
    protected void log(int priority, @Nullable String tag, @NonNull String message, @Nullable Throwable t) {
        if (priority > CrashUtil.ALARM) {
            return;
        }

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
            case CrashUtil.ALARM:
                CrashManager.sendEvent(new EventBuilder()
                        .withMessage(message)
                        .withLevel(Event.Level.ERROR)
                        .withMonitorInterface(new ExceptionInterface(t)));
            default:
                break;
        }
    }

    private static class SingleInstance {
        private static final CrashUtil INSTANCE = new CrashUtil();

        private SingleInstance() {
        }
    }
}
