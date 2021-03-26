package com.growingio.sdk.plugin.autotrack.compile;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
public class ErrorLogTest {

    @Test
    public void testErrorLog() {
        ErrorLog errorLog = new ErrorLog();
        errorLog.debug("debug");
        errorLog.info("info");
        errorLog.warning("warning");
        errorLog.warning("warning", new Exception("exception message"));
    }
}
