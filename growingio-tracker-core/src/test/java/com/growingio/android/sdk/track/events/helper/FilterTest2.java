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

package com.growingio.android.sdk.track.events.helper;

import android.app.Application;

import androidx.test.core.app.ApplicationProvider;

import com.google.common.truth.Truth;
import com.google.common.util.concurrent.Uninterruptibles;
import com.growingio.android.sdk.CoreConfiguration;
import com.growingio.android.sdk.TrackerContext;
import com.growingio.android.sdk.track.TrackMainThread;
import com.growingio.android.sdk.track.events.AutotrackEventType;
import com.growingio.android.sdk.track.events.CustomEvent;
import com.growingio.android.sdk.track.events.EventBuildInterceptor;
import com.growingio.android.sdk.track.events.PageAttributesEvent;
import com.growingio.android.sdk.track.events.PageEvent;
import com.growingio.android.sdk.track.events.base.BaseEvent;
import com.growingio.android.sdk.track.events.base.BaseField;
import com.growingio.android.sdk.track.middleware.GEvent;
import com.growingio.android.sdk.track.providers.ConfigurationProvider;
import com.growingio.android.sdk.track.utils.ConstantPool;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Config(manifest = Config.NONE)
@RunWith(RobolectricTestRunner.class)
public class FilterTest2 {

    Application application = ApplicationProvider.getApplicationContext();
    CustomEventFilterInterceptor eventFilterInterceptor;

    static class CustomEventFilterInterceptor extends DefaultEventFilterInterceptor {

        @Override
        public boolean filterEventType(String eventType) {
            if (eventType.equals(EventExcludeFilter.EVENT_PAGE)) return false;
            if (eventType.equals(EventExcludeFilter.EVENT_VIEW_CLICK)) return false;
            if (eventType.equals(EventExcludeFilter.EVENT_VIEW_CHANGE)) return false;
            if (eventType.equals(EventExcludeFilter.EVENT_FORM_SUBMIT)) return false;
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
            return !eventName.equals("gio");
        }
    }

    @Before
    public void setup() {
        TrackerContext.init(application);
        eventFilterInterceptor = new CustomEventFilterInterceptor();
        ConfigurationProvider.initWithConfig(
                new CoreConfiguration("test", ConstantPool.UNKNOWN)
                        .setProject("event", "filter")
                        .setEventFilterInterceptor(eventFilterInterceptor), new HashMap<>());

    }

    @Test
    public void filterEventType1() {
        TrackMainThread.trackMain().addEventBuildInterceptor(new EventBuildInterceptor() {
            @Override
            public void eventWillBuild(BaseEvent.BaseBuilder<?> eventBuilder) {
                Truth.assertThat(eventBuilder.getEventType()).isEqualTo(EventExcludeFilter.EVENT_PAGE);
            }

            @Override
            public void eventDidBuild(GEvent event) {
            }
        });
        TrackMainThread.trackMain().postEventToTrackMain(new PageEvent.Builder());
    }

    @Test
    public void filterEventType2() {
        TrackMainThread.trackMain().addEventBuildInterceptor(new EventBuildInterceptor() {
            @Override
            public void eventWillBuild(BaseEvent.BaseBuilder<?> eventBuilder) {
                Truth.assertThat(eventBuilder.getEventType()).isEqualTo(EventExcludeFilter.EVENT_CUSTOM);
            }

            @Override
            public void eventDidBuild(GEvent event) {
                Truth.assertThat(event.getEventType()).isEqualTo(EventExcludeFilter.EVENT_CUSTOM);
            }
        });
        TrackMainThread.trackMain().postEventToTrackMain(new CustomEvent.Builder().setEventName("cpacm"));
    }

    @Test
    public void filterEventPath() {
        TrackMainThread.trackMain().addEventBuildInterceptor(new EventBuildInterceptor() {
            @Override
            public void eventWillBuild(BaseEvent.BaseBuilder<?> eventBuilder) {
                Truth.assertThat(eventBuilder.getEventType()).isEqualTo(EventExcludeFilter.EVENT_PAGE_ATTRIBUTES);
            }

            @Override
            public void eventDidBuild(GEvent event) {
                Truth.assertThat(event.getEventType()).isEqualTo(EventExcludeFilter.EVENT_PAGE_ATTRIBUTES);
                Truth.assertThat(event.toString()).contains("/SubActivity");
            }
        });
        TrackMainThread.trackMain().postEventToTrackMain(new PageAttributesEvent.Builder().setPath("/MainActivity"));
        TrackMainThread.trackMain().postEventToTrackMain(new PageAttributesEvent.Builder().setPath("/SubActivity"));
    }

    @Test
    public void filterEventName() {
        TrackMainThread.trackMain().addEventBuildInterceptor(new EventBuildInterceptor() {
            @Override
            public void eventWillBuild(BaseEvent.BaseBuilder<?> eventBuilder) {
                Truth.assertThat(eventBuilder.getEventType()).isEqualTo(EventExcludeFilter.EVENT_CUSTOM);
            }

            @Override
            public void eventDidBuild(GEvent event) {
                Truth.assertThat(event instanceof CustomEvent).isTrue();
                CustomEvent customEvent = (CustomEvent) event;
                Truth.assertThat(event.getEventType()).isEqualTo(EventExcludeFilter.EVENT_CUSTOM);
                Truth.assertThat(customEvent.getEventName()).isEqualTo("cpacm");
            }
        });
        TrackMainThread.trackMain().postEventToTrackMain(new CustomEvent.Builder().setEventName("gio"));
        TrackMainThread.trackMain().postEventToTrackMain(new CustomEvent.Builder().setEventName("cpacm"));
    }

    @Test
    public void filterEventField() {
        TrackMainThread.trackMain().addEventBuildInterceptor(new EventBuildInterceptor() {
            @Override
            public void eventWillBuild(BaseEvent.BaseBuilder<?> eventBuilder) {
                Truth.assertThat(eventBuilder.getEventType()).isEqualTo(EventExcludeFilter.EVENT_CUSTOM);
            }

            @Override
            public void eventDidBuild(GEvent event) {
                Truth.assertThat(event instanceof CustomEvent).isTrue();
                CustomEvent customEvent = (CustomEvent) event;
                Truth.assertThat(customEvent.getEventName()).isEqualTo("cpacm");
                Truth.assertThat(customEvent.getScreenHeight()).isEqualTo(0);
                Truth.assertThat(customEvent.getScreenWidth()).isEqualTo(0);
                Truth.assertThat(customEvent.getDeviceBrand()).isNull();
            }
        });
        TrackMainThread.trackMain().postEventToTrackMain(new CustomEvent.Builder().setEventName("cpacm"));
        //Uninterruptibles.sleepUninterruptibly(1, TimeUnit.SECONDS);
    }

}
