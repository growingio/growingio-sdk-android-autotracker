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

import com.growingio.android.sdk.track.events.EventFilterInterceptor;
import com.growingio.android.sdk.track.events.base.BaseField;
import com.growingio.android.sdk.track.providers.ConfigurationProvider;

import java.util.Map;

/**
 * <p>
 * SDK use DefaultEventFilterInterceptor to filter event.
 *
 * @author cpacm 2022/8/5
 */
public class DefaultEventFilterInterceptor implements EventFilterInterceptor {
    @Override
    public boolean filterEventType(String eventType) {
        // 判断当前事件类型是否被过滤
        return !EventExcludeFilter.isEventFilter(eventType, getExcludeEvent());
    }

    @Override
    public boolean filterEventName(String eventName) {
        return true;
    }

    @Override
    public boolean filterEventPath(String path) {
        return true;
    }

    @Override
    public Map<String, Boolean> filterEventField(String type, Map<String, Boolean> fieldArea) {
        fieldArea.put(BaseField.NETWORK_STATE, !FieldIgnoreFilter.isFieldFilter(BaseField.NETWORK_STATE, getIgnoreField()));
        fieldArea.put(BaseField.SCREEN_HEIGHT, !FieldIgnoreFilter.isFieldFilter(BaseField.SCREEN_HEIGHT, getIgnoreField()));
        fieldArea.put(BaseField.SCREEN_WIDTH, !FieldIgnoreFilter.isFieldFilter(BaseField.SCREEN_WIDTH, getIgnoreField()));
        fieldArea.put(BaseField.DEVICE_BRAND, !FieldIgnoreFilter.isFieldFilter(BaseField.DEVICE_BRAND, getIgnoreField()));
        fieldArea.put(BaseField.DEVICE_MODEL, !FieldIgnoreFilter.isFieldFilter(BaseField.DEVICE_MODEL, getIgnoreField()));
        fieldArea.put(BaseField.DEVICE_TYPE, !FieldIgnoreFilter.isFieldFilter(BaseField.DEVICE_TYPE, getIgnoreField()));

        fieldArea.put(BaseField.APP_CHANNEl, !FieldIgnoreFilter.isFieldFilter(BaseField.APP_CHANNEl, getIgnoreField()));
        fieldArea.put(BaseField.APP_NAME, !FieldIgnoreFilter.isFieldFilter(BaseField.APP_NAME, getIgnoreField()));
        fieldArea.put(BaseField.APP_VERSION, !FieldIgnoreFilter.isFieldFilter(BaseField.APP_VERSION, getIgnoreField()));
        fieldArea.put(BaseField.LANGUAGE, !FieldIgnoreFilter.isFieldFilter(BaseField.LANGUAGE, getIgnoreField()));
        fieldArea.put(BaseField.LATITUDE, !FieldIgnoreFilter.isFieldFilter(BaseField.LATITUDE, getIgnoreField()));
        fieldArea.put(BaseField.LONGITUDE, !FieldIgnoreFilter.isFieldFilter(BaseField.LONGITUDE, getIgnoreField()));
        fieldArea.put(BaseField.SDK_VERSION, !FieldIgnoreFilter.isFieldFilter(BaseField.SDK_VERSION, getIgnoreField()));
        return fieldArea;
    }

    @Override
    public boolean filterEventGroup(String group) {
        return true;
    }

    @Deprecated
    protected int getIgnoreField() {
        return ConfigurationProvider.core().getIgnoreField();
    }

    @Deprecated
    protected int getExcludeEvent() {
        return ConfigurationProvider.core().getExcludeEvent();
    }
}
