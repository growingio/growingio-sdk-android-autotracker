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
import com.growingio.android.sdk.CoreConfiguration;
import com.growingio.android.sdk.TrackerContext;
import com.growingio.android.sdk.track.providers.ConfigurationProvider;
import com.growingio.android.sdk.track.utils.ConstantPool;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.HashMap;

@Config(manifest = Config.NONE)
@RunWith(RobolectricTestRunner.class)
public class FilterTest {

    Application application = ApplicationProvider.getApplicationContext();

    @Before
    public void setup() {
        TrackerContext.init(application);
        ConfigurationProvider.initWithConfig(
                new CoreConfiguration("test", ConstantPool.UNKNOWN)
                        .setProject("event", "filter")
                        .setExcludeEvent(EventExcludeFilter.VIEW_CLICK)
                        .setExcludeEvent(EventExcludeFilter.PAGE)
                        .setExcludeEvent(EventExcludeFilter.EVENT_MASK_TRIGGER)
                        .setIgnoreField(FieldIgnoreFilter.SCREEN_HEIGHT)
                        .setIgnoreField(FieldIgnoreFilter.of(FieldIgnoreFilter.SCREEN_WIDTH, FieldIgnoreFilter.DEVICE_BRAND)), new HashMap<>());
    }

    @Test
    public void eventFilterTest() {
        Truth.assertThat(EventExcludeFilter.isEventFilter("VIEW_CLICK")).isTrue();
        Truth.assertThat(EventExcludeFilter.isEventFilter("VIEW_CHANGE")).isTrue();
        Truth.assertThat(EventExcludeFilter.isEventFilter("PAGE")).isTrue();
        Truth.assertThat(EventExcludeFilter.isEventFilter("CUSTOM")).isFalse();
        Truth.assertThat(EventExcludeFilter.of(EventExcludeFilter.APP_CLOSED, EventExcludeFilter.PAGE)).isEqualTo(96);
        Truth.assertThat(EventExcludeFilter.isEventFilter("cpacm")).isFalse();

        String eventFilterLog = EventExcludeFilter.getFilterEventLog(12);
        Truth.assertThat(eventFilterLog).contains("VISITOR_ATTRIBUTES");
        Truth.assertThat(eventFilterLog).contains("LOGIN_USER_ATTRIBUTES");
        String zeroLog = EventExcludeFilter.getFilterEventLog(0);
        Truth.assertThat(zeroLog).isEmpty();

        ConfigurationProvider.core().setExcludeEvent(EventExcludeFilter.NONE);
        Truth.assertThat(EventExcludeFilter.isEventFilter("VIEW_CLICK")).isFalse();
    }

    @Test
    public void filedFilterTest() {
        Truth.assertThat(FieldIgnoreFilter.isFieldFilter("screenWidth")).isTrue();
        Truth.assertThat(FieldIgnoreFilter.isFieldFilter("screenHeight")).isTrue();
        Truth.assertThat(FieldIgnoreFilter.isFieldFilter("deviceBrand")).isTrue();
        Truth.assertThat(FieldIgnoreFilter.isFieldFilter("deviceType")).isFalse();
        Truth.assertThat(FieldIgnoreFilter.isFieldFilter("getGIO")).isFalse();
        Truth.assertThat(FieldIgnoreFilter.of(FieldIgnoreFilter.NETWORK_STATE, FieldIgnoreFilter.DEVICE_MODEL)).isEqualTo(17);
        Truth.assertThat((FieldIgnoreFilter.of(FieldIgnoreFilter.FIELD_IGNORE_ALL))).isEqualTo(63);

        String filedFilterLog = FieldIgnoreFilter.getFieldFilterLog(1 + 4 + 16);
        Truth.assertThat(filedFilterLog).contains("networkState");
        Truth.assertThat(filedFilterLog).contains("screenWidth");
        Truth.assertThat(filedFilterLog).contains("deviceModel");
        String zeroLog = FieldIgnoreFilter.getFieldFilterLog(0);
        Truth.assertThat(zeroLog).isEmpty();

        ConfigurationProvider.core().setIgnoreField(FieldIgnoreFilter.NONE);
        Truth.assertThat(FieldIgnoreFilter.isFieldFilter("screenWidth")).isFalse();
    }

}
