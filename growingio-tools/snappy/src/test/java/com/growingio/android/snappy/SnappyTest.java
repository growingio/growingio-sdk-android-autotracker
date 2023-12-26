/*
 * Copyright (C) 2023 Beijing Yishu Technology Co., Ltd.
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

import org.junit.Test;

import static com.google.common.truth.Truth.assertThat;
import static com.growingio.android.snappy.SnappyCompressor.maxCompressedLength;

public class SnappyTest {

    @Test
    public void compressTest() {
        long currentTimeMillis = 1622168955981L;
        String c = "cpacm";
        byte[] compressData = Snappy.compress(c.getBytes());
        compressData = XORUtils.encrypt(compressData, (int) (currentTimeMillis & 0xFF));
        assertThat("H].=,. ".equals(new String(compressData))).isTrue();
        assertThat(XORUtils.encrypt(new byte[0], 0)).hasLength(0);

        String content = "this is snappy test data:\n" +
                "水古譚-suikotan-\n" +
                "うたわれるもの 偽りの仮面 ゲーム&アニメ オリジナルサウンドトラック\n" +
                "secret base～君がくれたもの～\n" +
                "虹ヶ咲学園スクールアイドル同好会\n" +
                "輪るピングドラム キャラクターソングアルバム\n";
        StringBuilder sb = new StringBuilder(content);
        for (int i = 0; i < 100; i++) {
            sb.append(content);
        }
        byte[] data = sb.toString().getBytes();
        byte[] compressedOut = new byte[maxCompressedLength(data.length)];
        Snappy.compress(data, 0, data.length, compressedOut, 12);
    }

    @Test
    public void snappyUtils() {
        assertThat(SnappyInternalUtils.equals("cpacm".getBytes(), 1, "cpacm".getBytes(), 1, 1)).isTrue();
        assertThat(SnappyInternalUtils.equals("cpacm".getBytes(), 1, "asdjka".getBytes(), 1, 1)).isFalse();
        short[] data = new short[2];
        data[0] = 1;
        data[1] = 2;
        assertThat(SnappyInternalUtils.lookupShort(data, 1)).isEqualTo(2);
        assertThat(SnappyInternalUtils.loadByte("cpacm".getBytes(), 1)).isEqualTo(112);
        assertThat(SnappyInternalUtils.loadInt("cpacm".getBytes(), 1)).isEqualTo(1835229552);
        assertThat(SnappyInternalUtils.loadLong("cpacm_len_8".getBytes(), 0)).isEqualTo(7308321218738876515L);

        byte[] input = "cpacm_08".getBytes();
        int length = input.length;
        byte[] dest = new byte[length];
        SnappyInternalUtils.copyLong(input, 0, dest, 0);
        assertThat(new String(dest)).isEqualTo("cpacm_08");

        byte[] dest2 = new byte[3];
        SnappyInternalUtils.copyMemory(input, 0, dest2, 0, 3);
        assertThat(new String(dest2)).isEqualTo("cpa");

        try {
            SnappyInternalUtils.checkPositionIndexes(-1, 9, 10);
        } catch (Exception e) {
            assertThat(e).hasMessageThat().contains("must not be negative");
        }

        try {
            SnappyInternalUtils.checkPositionIndexes(1, 9, -10);
        } catch (Exception e) {
            assertThat(e).hasMessageThat().contains("negative size:");
        }

        try {
            SnappyInternalUtils.checkPositionIndexes(0, 11, 10);
        } catch (Exception e) {
            assertThat(e).hasMessageThat().contains("must not be greater");
        }

        try {
            SnappyInternalUtils.checkPositionIndexes(10, 1, 10);
        } catch (Exception e) {
            assertThat(e).hasMessageThat().contains("must not be less than");
        }
    }

}
