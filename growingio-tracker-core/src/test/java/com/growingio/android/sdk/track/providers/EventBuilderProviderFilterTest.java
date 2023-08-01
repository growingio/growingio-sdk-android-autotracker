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

import android.app.Application;

import androidx.test.core.app.ApplicationProvider;

import com.google.common.truth.Truth;
import com.growingio.android.sdk.CoreConfiguration;
import com.growingio.android.sdk.Tracker;
import com.growingio.android.sdk.TrackerContext;
import com.growingio.android.sdk.track.events.CustomEvent;
import com.growingio.android.sdk.track.events.EventBuildInterceptor;
import com.growingio.android.sdk.track.events.PageEvent;
import com.growingio.android.sdk.track.events.TrackEventType;
import com.growingio.android.sdk.track.events.base.BaseEvent;
import com.growingio.android.sdk.track.events.base.BaseField;
import com.growingio.android.sdk.track.events.helper.DefaultEventFilterInterceptor;
import com.growingio.android.sdk.track.middleware.GEvent;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.Map;

@Config(manifest = Config.NONE)
@RunWith(RobolectricTestRunner.class)
public class EventBuilderProviderFilterTest {

    Application application = ApplicationProvider.getApplicationContext();
    CustomEventFilterInterceptor eventFilterInterceptor;

    static class CustomEventFilterInterceptor extends DefaultEventFilterInterceptor {
        @Override
        public boolean filterEventType(String eventType) {
            if ("PAGE".equals(eventType)) return false;
            if ("VIEW_CLICK".equals(eventType)) return false;
            if ("VIEW_CHANGE".equals(eventType)) return false;
            if (eventType.equals(TrackEventType.FORM_SUBMIT)) return false;
            return true;
        }

        @Override
        public Map<String, Boolean> filterEventField(String type, Map<String, Boolean> fieldArea) {
            fieldArea.put(BaseField.SCREEN_HEIGHT, false);
            fieldArea.put(BaseField.SCREEN_WIDTH, false);
            fieldArea.put(BaseField.DEVICE_BRAND, false);
            return fieldArea;
        }

        @Override
        public boolean filterEventPath(String path) {
            return !path.contains("MainActivity");
        }

        @Override
        public boolean filterEventName(String eventName) {
            return !"gio".equals(eventName);
        }
    }

    private TrackerContext context;

    @Before
    public void setup() {
        eventFilterInterceptor = new CustomEventFilterInterceptor();
        CoreConfiguration coreConfiguration = new CoreConfiguration("TrackMainFilterTest", "growingio://trackMainfilter").setEventFilterInterceptor(eventFilterInterceptor);
        TrackerLifecycleProviderFactory.create().createConfigurationProviderWithConfig(coreConfiguration, null);
        Tracker tracker = new Tracker(application);
        context = tracker.getContext();
    }


    @Test
    public void filterEventType() {
        EventBuilderProvider eventBuilderProvider = context.getProvider(EventBuilderProvider.class);

        eventBuilderProvider.addEventBuildInterceptor(new EventBuildInterceptor() {
            @Override
            public void eventWillBuild(BaseEvent.BaseBuilder<?> eventBuilder) {
            }

            @Override
            public void eventDidBuild(GEvent event) {
                if (event instanceof CustomEvent) {
                    CustomEvent customEvent = (CustomEvent) event;
                    Truth.assertThat(customEvent.getEventName()).isEqualTo("cpacm");
                }
                Truth.assertThat(event instanceof PageEvent).isFalse();
            }
        });
        eventBuilderProvider.onGenerateGEvent(new PageEvent.Builder());
        eventBuilderProvider.onGenerateGEvent(new CustomEvent.Builder().setEventName("cpacm"));
    }

    @Test
    public void filterEventName() {
        EventBuilderProvider eventBuilderProvider = context.getProvider(EventBuilderProvider.class);
        eventBuilderProvider.addEventBuildInterceptor(new EventBuildInterceptor() {
            @Override
            public void eventWillBuild(BaseEvent.BaseBuilder<?> eventBuilder) {
            }

            @Override
            public void eventDidBuild(GEvent event) {
                if (event instanceof CustomEvent) {
                    CustomEvent customEvent = (CustomEvent) event;
                    Truth.assertThat(customEvent.getEventName()).isEqualTo("cpacm");
                }
            }
        });
        eventBuilderProvider.onGenerateGEvent(new CustomEvent.Builder().setEventName("gio"));
        eventBuilderProvider.onGenerateGEvent(new CustomEvent.Builder().setEventName("cpacm"));
    }

    @Test
    public void filterEventField() {
        EventBuilderProvider eventBuilderProvider = context.getProvider(EventBuilderProvider.class);
        eventBuilderProvider.addEventBuildInterceptor(new EventBuildInterceptor() {
            @Override
            public void eventWillBuild(BaseEvent.BaseBuilder<?> eventBuilder) {
            }

            @Override
            public void eventDidBuild(GEvent event) {
                if (event instanceof BaseEvent) {
                    BaseEvent customEvent = (BaseEvent) event;
                    Truth.assertThat(customEvent.getScreenHeight()).isEqualTo(0);
                    Truth.assertThat(customEvent.getScreenWidth()).isEqualTo(0);
                    Truth.assertThat(customEvent.getDeviceBrand()).isEmpty();
                }
            }
        });
        eventBuilderProvider.onGenerateGEvent(new CustomEvent.Builder().setEventName("cpacm"));
    }

}
