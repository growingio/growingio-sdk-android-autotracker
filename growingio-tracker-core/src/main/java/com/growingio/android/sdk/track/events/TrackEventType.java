/*
 * Copyright (C) 2020 Beijing Yishu Technology Co., Ltd.
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

package com.growingio.android.sdk.track.events;

public class TrackEventType {
    private TrackEventType() {
    }

    public static final String VISIT = "VISIT";
    public static final String CUSTOM = "CUSTOM";
    public static final String VISITOR_ATTRIBUTES = "VISITOR_ATTRIBUTES";
    public static final String LOGIN_USER_ATTRIBUTES = "LOGIN_USER_ATTRIBUTES";
    public static final String CONVERSION_VARIABLES = "CONVERSION_VARIABLES";
    public static final String APP_CLOSED = "APP_CLOSED";
    public static final String FORM_SUBMIT = "FORM_SUBMIT";

    public static final String ACTIVATE = "ACTIVATE";
    public static final String REENGAGE = "REENGAGE";
}
