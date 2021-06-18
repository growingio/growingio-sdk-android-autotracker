/*
 *
 *  Copyright (C) 2020 Beijing Yishu Technology Co., Ltd.
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
 *
 */

package com.growingio.android.sdk.track;


import android.app.Application;

import androidx.test.core.app.ApplicationProvider;

import com.google.common.truth.Truth;
import com.google.common.util.concurrent.Uninterruptibles;
import com.growingio.android.sdk.track.ipc.MultiProcessDataSharer;
import com.growingio.android.sdk.track.ipc.ProcessLock;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.io.IOException;
import java.nio.channels.OverlappingFileLockException;
import java.util.concurrent.TimeUnit;

@Config(manifest = Config.NONE)
@RunWith(RobolectricTestRunner.class)
public class IpcTest {

    Application application = ApplicationProvider.getApplicationContext();

    @Before
    public void setup() {
        MultiProcessDataSharer dataSharer = new MultiProcessDataSharer(application, "test", 100);
        dataSharer.putInt("key1", 1);
        dataSharer.putBoolean("key2", true);
        dataSharer.putFloat("key3", 1245434.54343F);
        dataSharer.putLong("key4", 124543454343L);
        for (int i = 5; i < 100; i++) {
            dataSharer.putString("key" + i, "value" + i);
        }
    }

    @Test
    public void dataSharerTest() {
        MultiProcessDataSharer dataSharer = new MultiProcessDataSharer(application, "test", 10);
        Truth.assertThat(dataSharer.getInt("key1", 0)).isEqualTo(1);
        Truth.assertThat(dataSharer.getBoolean("key2", false)).isTrue();
        Truth.assertThat(dataSharer.getFloat("key3", 0)).isEqualTo(1245434.54343F);
        Truth.assertThat(dataSharer.getLong("key4", 0L)).isEqualTo(124543454343L);
        Truth.assertThat(dataSharer.getString("key11", "error")).isEqualTo("error");
        Truth.assertThat(dataSharer.getAndAdd("key4", 10, 0)).isEqualTo(124543454353L);
    }

    @Test
    public void lockTest() {
        ProcessLock lock1 = new ProcessLock(application, "test");
        try {
            lock1.lock();
            new Thread() {
                @Override
                public void run() {
                    try {
                        ProcessLock lock2 = new ProcessLock(application, "test");
                        lock2.tryLock(1000);
                    } catch (Exception e) {
                        Truth.assertThat(e).isInstanceOf(OverlappingFileLockException.class);
                    }
                }
            }.start();
            Uninterruptibles.sleepUninterruptibly(500, TimeUnit.MILLISECONDS);
            lock1.release();

            ProcessLock lock3 = new ProcessLock(application, "test");
            Truth.assertThat(lock3.tryLock()).isTrue();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
