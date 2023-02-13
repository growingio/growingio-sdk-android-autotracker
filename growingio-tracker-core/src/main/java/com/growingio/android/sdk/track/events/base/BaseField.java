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

package com.growingio.android.sdk.track.events.base;

/**
 * <p>
 *
 * @author cpacm 2022/8/8
 */
public final class BaseField {

    private BaseField() {
    }

    final static String PLATFORM = "platform";
    final static String PLATFORM_VERSION = "platformVersion";
    final static String USER_KEY = "userKey";
    final static String USER_ID = "userId";
    final static String SESSION_ID = "sessionId";
    final static String EVENT_TYPE = "eventType";
    final static String TIMESTAMP = "timestamp";
    final static String DOMAIN = "domain";
    final static String URL_SCHEME = "urlScheme";
    final static String APP_STATE = "appState";
    final static String GSID = "globalSequenceId";
    final static String ESID = "eventSequenceId";
    final static String DEVICE_ID = "deviceId";
    final static String DATA_SOURCE_ID = "dataSourceId";
    final static String GIO_ID = "gioId";
    public final static String NETWORK_STATE = "networkState";
    public final static String APP_CHANNEL = "appChannel";
    public final static String SCREEN_HEIGHT = "screenHeight";
    public final static String SCREEN_WIDTH = "screenWidth";
    public final static String DEVICE_BRAND = "deviceBrand";
    public final static String DEVICE_MODEL = "deviceModel";
    public final static String DEVICE_TYPE = "deviceType";
    public final static String APP_NAME = "appName";
    public final static String APP_VERSION = "appVersion";
    public final static String LANGUAGE = "language";
    public final static String LATITUDE = "latitude";
    public final static String LONGITUDE = "longitude";
    public final static String SDK_VERSION = "sdkVersion";
}
