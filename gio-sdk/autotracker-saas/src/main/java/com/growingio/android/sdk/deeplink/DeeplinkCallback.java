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
package com.growingio.android.sdk.deeplink;

import java.util.Map;

/**
 * Created by denghuaxin on 2018/4/9.
 */

public interface DeeplinkCallback {
    public final static int SUCCESS = 0x0;
    public final static int PRASE_ERROR = 0x1;
    public final static int ILLEGAL_URI = 0X2;
    public final static int NO_QUERY = 0X3;
    public final static int APPLINK_GET_PARAMS_FAILED = 0X4;
    public void onReceive(Map<String, String> params, int error , long appAwakePassedTime);
}
