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

package com.growingio.android.sdk.track.variation;

public class MarshallerConstants {
    public static final class Key {
        public static final String TIMESTAMP = "tm";
        public static final String EVENT_TYPE = "t";
        public static final String DEVICE_ID = "u";
        public static final String SESSION_ID = "s";
        public static final String DOMAIN = "d";
        public static final String USER_ID = "cs1";
        // TODO: 2020/5/27  添加事件发送app是后台还是前台的字段
        public static final String IS_INTERACTIVE = "ac";

        public static final String GLOBAL_SEQUENCE_ID = "gesid";
        public static final String EVENT_SEQUENCE_ID = "esid";

        public static final String ATTRIBUTES = "var";

        public static final String EVENT_NAME = "n";
        public static final String PAGE_NAME = "p";
        public static final String PAGE_SHOW_TIMESTAMP = "ptm";

        public static final String APP_CHANNEL = "ch";
        public static final String SCREEN_HEIGHT = "sh";
        public static final String SCREEN_WIDTH = "sw";
        public static final String DEVICE_BRAND = "db";
        public static final String DEVICE_MODEL = "dm";
        public static final String IS_PHONE = "ph";
        public static final String OPERATING_SYSTEM = "os";
        public static final String OPERATING_SYSTEM_VERSION = "osv";
        public static final String SDK_VERSION = "av";
        public static final String APP_NAME = "sn";
        public static final String APP_VERSION = "cv";
        public static final String URL_SCHEME = "v";
        public static final String LANGUAGE = "l";
        public static final String LATITUDE = "lat";
        public static final String LONGITUDE = "lng";
        public static final String ANDROID_ID = "adrid";
        public static final String GOOGLE_ADVERTISING_ID = "gaid";
        public static final String IMEI = "imei";
        public static final String FEATURES_VERSION = "fv";

        public static final String ORIENTATION = "o";
        public static final String TITLE = "tl";
        public static final String NETWORK_STATE = "r";
        public static final String REFERRAL_PAGE = "rp";

        public static final String VIEW_ELEMENT = "e";
        public static final String INDEX = "i";
        public static final String TEXT_VALUE = "v";
        public static final String HYPERLINK = "h";
        public static final String XPATH = "x";

        //hybrid
        public static final String QUERY_PARAMETERS = "q";
        public static final String PROTOCOL_TYPE = "pt";
        public static final String WEB_REFERRER = "rf";

    }

    public static final class Value {

        public static final class EventType {
            public static final String VISIT = "vst";
            public static final String PAGE = "page";
            public static final String PAGE_ATTRIBUTES = "pvar";
            public static final String CLICK = "clck";
            public static final String CHANG = "chng";
            public static final String SUBMIT = "sbmt";
            public static final String CUSTOM = "cstm";
            public static final String CONVERSION_VARIABLES = "evar";
            public static final String LOGIN_USER_ATTRIBUTES = "ppl";
            public static final String VISITOR_ATTRIBUTES = "vstr";
            public static final String APP_CLOSE = "cls";
        }
    }
}
