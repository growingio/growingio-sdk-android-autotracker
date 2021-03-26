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

import com.google.common.truth.Truth;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.File;
import java.io.IOException;

@RunWith(PowerMockRunner.class)
public class MultiProcessDataSharerTest {
    @Rule
    TemporaryFolder mTempDir = new TemporaryFolder();

    @Test
    public void testMultiProcessDataSharer() throws IOException {
        Context context = PowerMockito.mock(Context.class);
        File file = mTempDir.newFile("data.txt");
        if (file.exists()) file.delete();
        PowerMockito.when(context.getFileStreamPath(Mockito.anyString())).thenReturn(file);
        MultiProcessDataSharer multiProcessDataSharer = new MultiProcessDataSharer(context, "data", 1024);
        multiProcessDataSharer.putString("string", "value");
        Truth.assertThat(multiProcessDataSharer.getString("string", "default")).isEqualTo("value");
        multiProcessDataSharer.putBoolean("boolean", true);
        Truth.assertThat(multiProcessDataSharer.getBoolean("boolean", false)).isTrue();
        multiProcessDataSharer.putFloat("float", 0.1f);
        Truth.assertThat(Math.abs(multiProcessDataSharer.getFloat("float", 0) - 0.1f) < 1e-7).isTrue();
        multiProcessDataSharer.putInt("int", 1);
        Truth.assertThat(multiProcessDataSharer.getInt("int", 0) == 1).isTrue();
        multiProcessDataSharer.putLong("long", 1);
        Truth.assertThat(multiProcessDataSharer.getLong("long", 0) == 1).isTrue();
    }
}
