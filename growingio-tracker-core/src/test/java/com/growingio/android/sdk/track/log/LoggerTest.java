package com.growingio.android.sdk.track.log;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
public class LoggerTest {

    @Test
    public void testLog() {
        Logger.v("tag", "message");
        Logger.v("tag", new Exception("exception message"));
        Logger.v("tag", new Exception("exception message"), "message");
        Logger.d("tag", "message");
        Logger.d("tag", new Exception("exception message"));
        Logger.d("tag", new Exception("exception message"), "message");
        Logger.i("tag", "message");
        Logger.i("tag", new Exception("exception message"));
        Logger.i("tag", new Exception("exception message"), "message");
        Logger.w("tag", "message");
        Logger.w("tag", new Exception("exception message"));
        Logger.w("tag", new Exception("exception message"), "message");
        Logger.e("tag", "message");
        Logger.e("tag", new Exception("exception message"));
        Logger.e("tag", new Exception("exception message"), "message");
        Logger.wtf("tag", "message");
        Logger.wtf("tag", new Exception("exception message"));
        Logger.wtf("tag", new Exception("exception message"), "message");
        Logger.printJson("tag", "headString", "{\"name\": \"BeJson\"}");
    }
}
