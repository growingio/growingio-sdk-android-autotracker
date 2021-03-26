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

import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.HashMap;
import java.util.Map;

@RunWith(PowerMockRunner.class)
public class VisitEventTest {

    @Test
    public void testVisitEvent() {
        Map<String, String> extraSdk = new HashMap<>();
        extraSdk.put("autotracker", "3.0.0");
        VisitEvent visitEvent = (new VisitEvent.Builder())
                .setExtraSdk(extraSdk)
                .setLatitude(0.1f)
                .setLongitude(0.1f)
                .setSessionId("sessionId")
                .setTimestamp(1)
                .build();
        visitEvent.getNetworkState();
        visitEvent.getAppChannel();
        visitEvent.getScreenHeight();
        visitEvent.getScreenWidth();
        visitEvent.getDeviceBrand();
        visitEvent.getDeviceModel();
        visitEvent.getDeviceType();
        visitEvent.getAppName();
        visitEvent.getAppVersion();
        visitEvent.getLanguage();
        visitEvent.getLatitude();
        visitEvent.getLongitude();
        visitEvent.getImei();
        visitEvent.getAndroidId();
        visitEvent.getOaid();
        visitEvent.getGoogleAdvertisingId();
        visitEvent.getSdkVersion();
        visitEvent.getExtraSdk();
    }
}
