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

package com.growingio.android.apm;

import android.content.Context;

import com.growingio.android.gmonitor.GMonitor;
import com.growingio.android.gmonitor.GMonitorOption;

/**
 * <p>
 *
 * @author cpacm 2022/9/29
 */
public class GrowingApm {
    public static void startWithConfiguration(Context context, ApmConfig apmConfig) {
        if (GMonitor.getInstance() == null) {
            GMonitorOption option = new GMonitorOption();
            option.setDebug(false);
            option.setAvoidRunningAppProcesses(false);
            option.setEnableActivityLifecycleTracing(apmConfig.isActivityLifecycleTracing());

            option.setEnableFragmentXLifecycleTracing(apmConfig.isFragmentXLifecycleTracing());
            option.setEnableFragmentSupportLifecycleTracing(apmConfig.isFragmentSupportLifecycleTracing());
            option.setEnableFragmentSystemLifecycleTracing(apmConfig.isFragmentSystemLifecycleTracing());

            option.setEnableUncaughtExceptionHandler(apmConfig.isUncaughtException());
            option.setPrintUncaughtStackTrace(apmConfig.isPrintUncaughtException());

            GMonitor.init(context, new ApmLogger(), option, new ApmTracker());
        }
    }
}
