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
package com.growingio.android.sdk.track.events.helper;

import com.growingio.android.sdk.track.events.AutotrackEventType;
import com.growingio.android.sdk.track.events.EventFilterInterceptor;
import com.growingio.android.sdk.track.events.TrackEventType;

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
        return true;
    }

    @Override
    public boolean filterEventName(String eventName) {
        return true;
    }

    @Override
    public boolean filterEventPath(String path) {
        return true;
    }

    /**
     * filter event's field eg:
     * fieldArea.put(BaseField.NETWORK_STATE, false);
     *
     * @param type      event's type
     * @param fieldArea event's field map
     */
    @Override
    public Map<String, Boolean> filterEventField(String type, Map<String, Boolean> fieldArea) {
        return fieldArea;
    }

    @Override
    public Map<String, String> addDynamicProps() {
        return null;
    }

    public static class FilterEventType {
        public static final String VISIT = TrackEventType.VISIT;
        public static final String CUSTOM = TrackEventType.CUSTOM;
        public static final String VISITOR_ATTRIBUTES = TrackEventType.VISITOR_ATTRIBUTES;
        public static final String LOGIN_USER_ATTRIBUTES = TrackEventType.LOGIN_USER_ATTRIBUTES;
        public static final String CONVERSION_VARIABLES = TrackEventType.CONVERSION_VARIABLES;
        public static final String APP_CLOSED = TrackEventType.APP_CLOSED;
        public static final String PAGE = AutotrackEventType.PAGE;
        public static final String VIEW_CLICK = AutotrackEventType.VIEW_CLICK;
        public static final String VIEW_CHANGE = AutotrackEventType.VIEW_CHANGE;
        public static final String FORM_SUBMIT = TrackEventType.FORM_SUBMIT;
        public static final String REENGAGE = TrackEventType.REENGAGE;
        public static final String ACTIVATE = TrackEventType.ACTIVATE;
    }
}
