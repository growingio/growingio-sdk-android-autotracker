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

import static com.growingio.android.apm.ApmEventBuilder.EVENT_APP_LAUNCHTIME_NAME;
import static com.growingio.android.apm.ApmEventBuilder.EVENT_ERROR_NAME;

import android.app.Application;

import androidx.test.core.app.ApplicationProvider;

import com.google.common.truth.Truth;
import com.growingio.android.gmonitor.GMonitor;
import com.growingio.android.gmonitor.GMonitorOption;
import com.growingio.android.gmonitor.event.Breadcrumb;
import com.growingio.android.gmonitor.utils.ExceptionHelper;
import com.growingio.android.sdk.Configurable;
import com.growingio.android.sdk.CoreConfiguration;
import com.growingio.android.sdk.TrackerContext;
import com.growingio.android.sdk.track.events.helper.DefaultEventFilterInterceptor;
import com.growingio.android.sdk.track.middleware.apm.EventApm;
import com.growingio.android.sdk.track.providers.ConfigurationProvider;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.HashMap;

/**
 * <p>
 *
 * @author cpacm 2022/10/8
 */
@Config(manifest = Config.NONE)
@RunWith(RobolectricTestRunner.class)
public class ApmTest {

    private final Application application = ApplicationProvider.getApplicationContext();

    @Before
    public void setup() {
        TrackerContext.init(application);
        ApmConfig config = new ApmConfig();
        config.setActivityLifecycleTracing(true)
                .setFragmentXLifecycleTracing(true)
                .setFragmentSupportLifecycleTracing(true)
                .setFragmentSystemLifecycleTracing(true)
                .setUncaughtException(true)
                .setPrintUncaughtException(false);
        HashMap<Class<? extends Configurable>, Configurable> configs = new HashMap<>();
        configs.put(config.getClass(), config);
        ConfigurationProvider.initWithConfig(new CoreConfiguration("ApmTest", "growingio://apm"), configs);
        TrackerContext.initSuccess();
    }

    @Test
    public void apmModuleTest() {
        ApmLibraryGioModule module = new ApmLibraryGioModule();
        module.registerComponents(application, TrackerContext.get().getRegistry());
        Truth.assertThat(GMonitor.getInstance()).isNotNull();

        ApmConfig config = ConfigurationProvider.get().getConfiguration(ApmConfig.class);
        GMonitorOption options = GMonitor.getInstance().getOption();
        Truth.assertThat(options.getAvoidRunningAppProcesses()).isEqualTo(!ConfigurationProvider.core().isRequireAppProcessesEnabled());
        Truth.assertThat(options.getEnableActivityLifecycleTracing()).isEqualTo(config.isActivityLifecycleTracing());
        Truth.assertThat(options.getEnableFragmentXLifecycleTracing()).isEqualTo(config.isFragmentXLifecycleTracing());
        Truth.assertThat(options.getEnableFragmentSupportLifecycleTracing()).isEqualTo(config.isFragmentSupportLifecycleTracing());
        Truth.assertThat(options.getEnableFragmentSystemLifecycleTracing()).isEqualTo(config.isFragmentSystemLifecycleTracing());
        Truth.assertThat(options.getPrintUncaughtStackTrace()).isEqualTo(config.isPrintUncaughtException());
        Truth.assertThat(options.getEnableUncaughtExceptionHandler()).isEqualTo(config.isUncaughtException());
        Truth.assertThat(options.getAnrTimeoutIntervalMillis()).isEqualTo(config.getAnrTimeoutIntervalMillis());
        Truth.assertThat(options.getAnrInDebug()).isEqualTo(config.isAnrInDebug());
        Truth.assertThat(options.getEnableAnr()).isEqualTo(config.isAnrTracing());
    }

    @Test
    public void apmLifecycleTest() {

        class ActivityFilter extends DefaultEventFilterInterceptor {
            @Override
            public boolean filterEventName(String eventName) {
                Truth.assertThat(eventName).isEqualTo(EVENT_APP_LAUNCHTIME_NAME);
                return super.filterEventName(eventName);
            }
        }

        ConfigurationProvider.core().setEventFilterInterceptor(new ActivityFilter());
        TrackerContext.get().getRegistry().register(EventApm.class, Void.class, new ApmDataLoader.Factory(application));

        Breadcrumb breadcrumb = new Breadcrumb(Breadcrumb.TYPE_PERFORMANCE, Breadcrumb.CATEGORY_PERFORMANCE_ACTIVITY, null);
        breadcrumb.putData(Breadcrumb.ATTR_PERFORMANCE_PAGE_NAME, "TestActivity");
        breadcrumb.putData(Breadcrumb.ATTR_PERFORMANCE_DURATION, 100L);
        GMonitor.getInstance().trackBreadcrumb(breadcrumb);

        Breadcrumb breadcrumb1 = new Breadcrumb(Breadcrumb.TYPE_PERFORMANCE, Breadcrumb.CATEGORY_PERFORMANCE_ACTIVITY, null);
        breadcrumb1.putData(Breadcrumb.ATTR_PERFORMANCE_PAGE_NAME, "TestActivity");
        breadcrumb1.putData(Breadcrumb.ATTR_PERFORMANCE_DURATION, 100L);
        breadcrumb1.putData(Breadcrumb.ATTR_PERFORMANCE_APP_COLD, false);
        GMonitor.getInstance().trackBreadcrumb(breadcrumb1);

        Breadcrumb breadcrumb2 = new Breadcrumb(Breadcrumb.TYPE_PERFORMANCE, Breadcrumb.CATEGORY_PERFORMANCE_FRAGMENT, null);
        breadcrumb2.putData(Breadcrumb.ATTR_PERFORMANCE_PAGE_NAME, "TestFragment");
        breadcrumb2.putData(Breadcrumb.ATTR_PERFORMANCE_DURATION, 100L);
        GMonitor.getInstance().trackBreadcrumb(breadcrumb2);

        Breadcrumb breadcrumb3 = new Breadcrumb(Breadcrumb.TYPE_PERFORMANCE, Breadcrumb.CATEGORY_PERFORMANCE_APP, null);
        breadcrumb3.putData(Breadcrumb.ATTR_PERFORMANCE_APP_COLD, true);
        breadcrumb3.putData(Breadcrumb.ATTR_PERFORMANCE_DURATION, 100L);
        GMonitor.getInstance().trackBreadcrumb(breadcrumb3);
    }

    @Test
    public void apmErrorTest() {
        class ActivityFilter extends DefaultEventFilterInterceptor {
            @Override
            public boolean filterEventName(String eventName) {
                Truth.assertThat(eventName).isEqualTo(EVENT_ERROR_NAME);
                return super.filterEventName(eventName);
            }
        }

        ConfigurationProvider.core().setEventFilterInterceptor(new ActivityFilter());
        TrackerContext.get().getRegistry().register(EventApm.class, Void.class, new ApmDataLoader.Factory(application));

        NullPointerException e = new NullPointerException("NPE FOR TEST");
        Breadcrumb breadcrumb = new Breadcrumb(Breadcrumb.TYPE_ERROR, Breadcrumb.CATEGORY_ERROR_EXCEPTION, e.getMessage());
        breadcrumb.putData(Breadcrumb.ATTR_ERROR_TYPE, ExceptionHelper.INSTANCE.getThrowableType(e));
        breadcrumb.putData(Breadcrumb.ATTR_ERROR_MESSAGE, ExceptionHelper.INSTANCE.getThrowableMessage(e));
        GMonitor.getInstance().trackBreadcrumb(breadcrumb);
    }

    @Test
    public void growingioApmTest() {
        if (GMonitor.getInstance() != null) {
            GMonitor.getInstance().close();
        }
        ApmConfig config = ConfigurationProvider.get().getConfiguration(ApmConfig.class);
        GrowingApm.startWithConfiguration(application, config);
        Truth.assertThat(GMonitor.getInstance()).isNotNull();

        Truth.assertThat(GMonitor.getInstance().getOption().getLogger().isEnabled(0)).isTrue();
    }
}
