/*
 *  Copyright (C) 2025 Beijing Yishu Technology Co., Ltd.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.growingio.android.platform;

import com.growingio.android.sdk.Configurable;

public class PlatformConfig implements Configurable {
    private boolean harmonyPlatformEnabled = true;
    private boolean deviceTypeCheckEnabled = true;

    private boolean gmsIdEnabled = false;
    private boolean firebaseIdEnabled = false;

    public boolean isHarmonyPlatformEnabled() {
        return harmonyPlatformEnabled;
    }

    public void setHarmonyPlatformEnabled(boolean harmonyPlatformEnabled) {
        this.harmonyPlatformEnabled = harmonyPlatformEnabled;
    }

    public boolean isGmsIdEnabled() {
        return gmsIdEnabled;
    }

    public void setGmsIdEnabled(boolean gmsIdEnabled) {
        this.gmsIdEnabled = gmsIdEnabled;
    }

    public boolean isFirebaseIdEnabled() {
        return firebaseIdEnabled;
    }

    public void setFirebaseIdEnabled(boolean firebaseIdEnabled) {
        this.firebaseIdEnabled = firebaseIdEnabled;
    }

    public boolean isDeviceTypeCheckEnabled() {
        return deviceTypeCheckEnabled;
    }

    public void setDeviceTypeCheckEnabled(boolean deviceTypeCheckEnabled) {
        this.deviceTypeCheckEnabled = deviceTypeCheckEnabled;
    }
}
