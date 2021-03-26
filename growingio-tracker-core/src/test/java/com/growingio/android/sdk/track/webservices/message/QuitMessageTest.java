package com.growingio.android.sdk.track.webservices.message;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
public class QuitMessageTest {

    @Test
    public void testQuitMessage() {
        QuitMessage quitMessage = new QuitMessage();
        System.out.println(quitMessage.toJSONObject());
    }
}
