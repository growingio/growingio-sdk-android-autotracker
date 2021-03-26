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
