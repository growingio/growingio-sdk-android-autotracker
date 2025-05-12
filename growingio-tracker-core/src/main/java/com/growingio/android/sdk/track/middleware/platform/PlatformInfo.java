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
package com.growingio.android.sdk.track.middleware.platform;

public class PlatformInfo {
    private final String platform;
    private final String platformVersion;

    private final String deviceType;
    private final String gmsId;
    private final String firebaseId;

    public PlatformInfo(
            String platform, String platformVersion,
            String deviceType, String gmsId, String firebaseId) {
        this.platform = platform;
        this.platformVersion = platformVersion;
        this.deviceType = deviceType;
        this.gmsId = gmsId;
        this.firebaseId = firebaseId;
    }

    public String getPlatform() {
        return platform;
    }

    public String getPlatformVersion() {
        return platformVersion;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public String getGmsId() {
        return gmsId;
    }

    public String getFirebaseId() {
        return firebaseId;
    }
}
