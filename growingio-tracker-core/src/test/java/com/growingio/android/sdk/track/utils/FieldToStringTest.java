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

package com.growingio.android.sdk.track.utils;

import com.google.common.truth.Truth;
import com.growingio.android.sdk.Configurable;
import com.growingio.android.sdk.track.events.helper.EventExcludeFilter;
import com.growingio.android.sdk.track.events.helper.FieldIgnoreFilter;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static com.growingio.android.sdk.track.events.helper.EventExcludeFilter.EVENT_MASK_TRIGGER;

@RunWith(RobolectricTestRunner.class)
public class FieldToStringTest {

    @Test
    public void objectUtilsTest() {
        String result = ObjectUtils.reflectToString(new TestConfig(EVENT_MASK_TRIGGER, FieldIgnoreFilter.FIELD_IGNORE_ALL));
        Truth.assertThat(result.contains("VIEW_CLICK,VIEW_CHANGE,FORM_SUBMIT")).isTrue();
        Truth.assertThat(result.contains("networkState,screenHeight,screenWidth,deviceBrand,deviceModel,deviceType")).isTrue();
    }

    static class TestConfig implements Configurable {
        @ObjectUtils.FieldToString(clazz = EventExcludeFilter.class, method = "getEventFilterLog", parameterTypes = {int.class})
        private int eventFlag = 0;
        @ObjectUtils.FieldToString(clazz = FieldIgnoreFilter.class, method = "getFieldFilterLog", parameterTypes = {int.class})
        private int fieldFlag = 0;

        public TestConfig(int eventFlag, int fieldFlag) {
            this.eventFlag = eventFlag;
            this.fieldFlag = fieldFlag;
        }

        public int getEventFlag() {
            return eventFlag;
        }

        public int getFieldFlag() {
            return fieldFlag;
        }
    }

}
