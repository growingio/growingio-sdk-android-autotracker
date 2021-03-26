package com.growingio.sdk.plugin.autotrack.compile;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
public class SystemLogTest {
    @Test
    public void testSystemLog() {
        SystemLog systemLog = new SystemLog();
        systemLog.debug("debug");
        systemLog.error("error");
        systemLog.error("error", new Exception("exception message"));
        systemLog.info("info");
        systemLog.warning("warning");
        systemLog.warning("warning", new Exception("exception message"));
    }
}
