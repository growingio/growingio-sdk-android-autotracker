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

public enum EventType {
    VISIT("visit"),
    PAGE("page"),
    PAGE_ATTRIBUTES("pageAttributes"),
    CLICK("click"),
    SUBMIT("submit"),
    CHANGE("change"),
    CUSTOM("custom"),
    CONVERSION_VARIABLES("conversionVariables"),
    LOGIN_USER_ATTRIBUTES("loginUserAttributes"),
    VISITOR_ATTRIBUTES("visitorAttributes"),
    APP_CLOSE("appClose");

    private final String mType;

    EventType(String type) {
        mType = type;
    }


    @Override
    public String toString() {
        return mType;
    }
}