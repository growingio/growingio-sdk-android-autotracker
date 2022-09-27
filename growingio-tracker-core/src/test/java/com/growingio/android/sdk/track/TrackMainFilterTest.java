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

package com.growingio.android.sdk.track;

import android.app.Application;

import androidx.test.core.app.ApplicationProvider;

import com.google.common.truth.Truth;
import com.growingio.android.sdk.CoreConfiguration;
import com.growingio.android.sdk.TrackerContext;
import com.growingio.android.sdk.track.events.CustomEvent;
import com.growingio.android.sdk.track.events.EventBuildInterceptor;
import com.growingio.android.sdk.track.events.PageAttributesEvent;
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
public class TrackMainFilterTest {

    Application application = ApplicationProvider.getApplicationContext();
    TrackMainThread trackMainThread;
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

    @Before
    public void setup() {
        TrackerContext.init(application);
        eventFilterInterceptor = new CustomEventFilterInterceptor();
        CoreConfiguration coreConfiguration = new CoreConfiguration("TrackMainFilterTest", "growingio://trackMainfilter").setEventFilterInterceptor(eventFilterInterceptor);
        trackMainThread = new TrackMainThread(coreConfiguration);
    }


    @Test
    public void filterEventType() {
        trackMainThread.addEventBuildInterceptor(new EventBuildInterceptor() {
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
        trackMainThread.onGenerateGEvent(new PageEvent.Builder());
        trackMainThread.onGenerateGEvent(new CustomEvent.Builder().setEventName("cpacm"));
    }

    @Test
    public void filterEventPath() {
        trackMainThread.addEventBuildInterceptor(new EventBuildInterceptor() {
            @Override
            public void eventWillBuild(BaseEvent.BaseBuilder<?> eventBuilder) {
            }

            @Override
            public void eventDidBuild(GEvent event) {
                if (event instanceof PageAttributesEvent) {
                    Truth.assertThat(((PageAttributesEvent) event).getPath()).isEqualTo("/SubActivity");
                }
            }
        });
        trackMainThread.onGenerateGEvent(new PageAttributesEvent.Builder().setPath("/MainActivity"));
        trackMainThread.onGenerateGEvent(new PageAttributesEvent.Builder().setPath("/SubActivity"));
    }

    @Test
    public void filterEventName() {
        trackMainThread.addEventBuildInterceptor(new EventBuildInterceptor() {
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
        trackMainThread.onGenerateGEvent(new CustomEvent.Builder().setEventName("gio"));
        trackMainThread.onGenerateGEvent(new CustomEvent.Builder().setEventName("cpacm"));
    }

    @Test
    public void filterEventField() {
        trackMainThread.addEventBuildInterceptor(new EventBuildInterceptor() {
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
        trackMainThread.onGenerateGEvent(new CustomEvent.Builder().setEventName("cpacm"));
    }

}
