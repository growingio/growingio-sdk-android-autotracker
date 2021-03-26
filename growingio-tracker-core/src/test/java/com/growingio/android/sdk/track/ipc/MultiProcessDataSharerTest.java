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
    TemporaryFolder tempDir = new TemporaryFolder();

    @Test
    public void testMultiProcessDataSharer() throws IOException {
        Context context = PowerMockito.mock(Context.class);
        File file = tempDir.newFile("data.txt");
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
