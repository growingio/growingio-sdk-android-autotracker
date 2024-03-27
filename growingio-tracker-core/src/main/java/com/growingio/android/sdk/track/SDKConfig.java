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
package com.growingio.android.sdk.track;

public class SDKConfig {
    private SDKConfig() {
    }

    public static final String SDK_VERSION = BuildConfig.VERSION_NAME;
    public static final int SDK_VERSION_CODE = BuildConfig.VERSION_CODE;
    public static final String SDK_BUILD_TIME = BuildConfig.BUILD_TIME;
    public static final String SDK_BUILD_GIT_SHA = BuildConfig.GIT_SHA;

    public static final String SDK_VERSION_DOWNGRADE = "3.5.3";
    public static final int SDK_VERSION_CODE_DOWNGRADE = 30503;
}
