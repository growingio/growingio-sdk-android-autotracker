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
package com.growingio.android.snappy;

import com.google.common.truth.Truth;

import org.junit.Test;

public class SnappyTest {

    @Test
    public void compressTest() {
        //long currentTimeMillis = System.currentTimeMillis();
        long currentTimeMillis = 1622168955981L;
        String content = "cpacm";
        byte[] compressData = Snappy.compress(content.getBytes());
        compressData = XORUtils.encrypt(compressData, (int) (currentTimeMillis & 0xFF));
        Truth.assertThat("H].=,. ".equals(new String(compressData))).isTrue();
    }

    @Test
    public void slowMemory() {
        Memory memory = new SlowMemory();
        int i = memory.loadInt("cpacm".getBytes(), 1);
        Truth.assertThat(i == 1835229552).isTrue();
        int b = memory.loadByte("cpacm".getBytes(), 1);
        Truth.assertThat(112 == b).isTrue();
        long l = memory.loadLong("cpacm_len_8".getBytes(), 0);
        Truth.assertThat(7308321218738876515L == l).isTrue();
 }

}
