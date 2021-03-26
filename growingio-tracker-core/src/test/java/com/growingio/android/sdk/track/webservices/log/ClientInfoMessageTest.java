package com.growingio.android.sdk.track.webservices.log;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
public class ClientInfoMessageTest {

    @Test
    public void testClientInfoMessage() {
        ClientInfoMessage clientInfoMessage = new ClientInfoMessage("version", null);
        System.out.println(clientInfoMessage.toJSONObject());
        System.out.println(ClientInfoMessage.createMessage().toJSONObject());
    }
}
