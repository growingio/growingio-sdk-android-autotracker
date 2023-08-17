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
package com.growingio.android.apm;


import com.growingio.android.gmonitor.GMonitor;
import com.growingio.android.gmonitor.GMonitorOption;
import com.growingio.android.sdk.CoreConfiguration;
import com.growingio.android.sdk.TrackerContext;
import com.growingio.android.sdk.track.log.Logger;
import com.growingio.android.sdk.track.middleware.apm.EventApm;
import com.growingio.android.sdk.track.modelloader.ModelLoader;
import com.growingio.android.sdk.track.modelloader.ModelLoaderFactory;

/**
 * <p>
 * when register apm module,init gmonitor sdk.
 *
 * @author cpacm 2022/9/27
 */
public class ApmDataLoader implements ModelLoader<EventApm, Void> {

    @Override
    public LoadData<Void> buildLoadData(EventApm eventApm) {
        return null;
    }

    public static class Factory implements ModelLoaderFactory<EventApm, Void> {

        protected Factory(TrackerContext context) {
            if (GMonitor.getInstance() == null) {
                CoreConfiguration core = context.getConfigurationProvider().core();
                ApmConfig apmConfig = context.getConfigurationProvider().getConfiguration(ApmConfig.class);
                if (apmConfig == null) apmConfig = new ApmConfig();

                GMonitorOption option = new GMonitorOption();
                option.setDebug(core.isDebugEnabled());
                option.setAvoidRunningAppProcesses(!core.isRequireAppProcessesEnabled());
                option.setEnableActivityLifecycleTracing(apmConfig.isActivityLifecycleTracing());

                option.setEnableFragmentXLifecycleTracing(apmConfig.isFragmentXLifecycleTracing());
                option.setEnableFragmentSupportLifecycleTracing(apmConfig.isFragmentSupportLifecycleTracing());
                option.setEnableFragmentSystemLifecycleTracing(apmConfig.isFragmentSystemLifecycleTracing());

                option.setEnableUncaughtExceptionHandler(apmConfig.isUncaughtException());
                option.setPrintUncaughtStackTrace(apmConfig.isPrintUncaughtException());

                option.setAnrInDebug(apmConfig.isAnrInDebug());
                option.setEnableAnr(apmConfig.isAnrTracing());
                option.setAnrTimeoutIntervalMillis(apmConfig.getAnrTimeoutIntervalMillis());

                ApmTracker apmTracker = new ApmTracker();
                Logger.d("Apm", "init gmonitor success");
                GMonitor.init(context.getApplicationContext(), new ApmLogger(), option, apmTracker);
            }
        }

        @Override
        public ModelLoader<EventApm, Void> build() {
            return new ApmDataLoader();
        }
    }
}
