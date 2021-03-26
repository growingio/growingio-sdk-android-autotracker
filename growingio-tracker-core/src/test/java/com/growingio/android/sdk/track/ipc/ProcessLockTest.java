package com.growingio.android.sdk.track.ipc;

import android.content.Context;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

@RunWith(PowerMockRunner.class)
public class ProcessLockTest {
    @Rule
    TemporaryFolder tempDir = new TemporaryFolder();

    @Test
    public void testProcessLock() throws IOException {
        Context context = PowerMockito.mock(Context.class);
        ProcessLock processLock = new ProcessLock(context, "data");
        File file = tempDir.newFile("data.lock");
        PowerMockito.when(context.openFileOutput(Mockito.anyString(), Mockito.anyInt())).thenReturn(new FileOutputStream(file));
        processLock.lock();
        processLock.release();
        processLock.tryLock();
        processLock.release();
        processLock.lock();
        processLock.tryLock(10);
        processLock.release();
    }
}
