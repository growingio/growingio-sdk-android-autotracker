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
import java.io.FileOutputStream;
import java.io.IOException;

@RunWith(PowerMockRunner.class)
public class ProcessLockTest {
    @Rule
    TemporaryFolder mTempDir = new TemporaryFolder();

    @Test
    public void testProcessLock() throws IOException {
        Context context = PowerMockito.mock(Context.class);
        ProcessLock processLock = new ProcessLock(context, "data");
        File file = mTempDir.newFile("data.lock");
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
