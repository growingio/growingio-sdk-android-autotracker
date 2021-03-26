package com.growingio.android.sdk.track.encrypt;

import com.google.common.truth.Truth;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.Arrays;

@RunWith(PowerMockRunner.class)
public class XORUtilsTest {

    @Test
    public void testEncrypt() {
        Truth.assertThat(XORUtils.encrypt(null, 0)).isNull();
        byte[] encryptedDataOnce = XORUtils.encrypt("data".getBytes(), 0);
        byte[] encryptedDataTwice = XORUtils.encrypt("data".getBytes(), 0);
        Truth.assertThat(Arrays.equals(encryptedDataOnce, encryptedDataTwice));
    }
}
