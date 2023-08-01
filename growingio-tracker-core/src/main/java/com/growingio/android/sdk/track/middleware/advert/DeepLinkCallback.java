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
package com.growingio.android.sdk.track.middleware.advert;

import java.util.Map;

/**
 * <p>
 *
 * @author cpacm 2022/11/23
 */
public interface DeepLinkCallback {
    int SUCCESS = 0x0;
    int PARSE_ERROR = 0x1;
    int ILLEGAL_URI = 0X2;
    int NO_QUERY = 0X3;

    int ERROR_NET_FAIL = 0x5;
    int ERROR_EXCEPTION = 0x6;

    int ERROR_UNKNOWN = 400;
    int ERROR_LINK_NOT_EXIST = 404;
    int ERROR_TIMEOUT = 408;
    int ERROR_APP_NOT_ACCEPT = 406;
    int ERROR_URL_FORMAT_ERROR = 412;

    void onReceive(Map<String, String> params, int error, long appAwakePassedTime);
}
