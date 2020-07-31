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

package com.growingio.android.sdk.track.snappy;

final class SnappyInternalUtils {
    private static final SlowMemory MEMORY = new SlowMemory();
    static final boolean HAS_UNSAFE = MEMORY.fastAccessSupported();

    private SnappyInternalUtils() {
    }

    static boolean equals(byte[] left, int leftIndex, byte[] right, int rightIndex, int length) {
        checkPositionIndexes(leftIndex, leftIndex + length, left.length);
        checkPositionIndexes(rightIndex, rightIndex + length, right.length);

        for (int i = 0; i < length; i++) {
            if (left[leftIndex + i] != right[rightIndex + i]) {
                return false;
            }
        }
        return true;
    }

    public static int lookupShort(short[] data, int index) {
        return MEMORY.lookupShort(data, index);
    }

    public static int loadByte(byte[] data, int index) {
        return MEMORY.loadByte(data, index);
    }

    static int loadInt(byte[] data, int index) {
        return MEMORY.loadInt(data, index);
    }

    static void copyLong(byte[] src, int srcIndex, byte[] dest, int destIndex) {
        MEMORY.copyLong(src, srcIndex, dest, destIndex);
    }

    static long loadLong(byte[] data, int index) {
        return MEMORY.loadLong(data, index);
    }

    static void checkPositionIndexes(int start, int end, int size) {
        // Carefully optimized for execution by hotspot (explanatory comment above)
        if (start < 0 || end < start || end > size) {
            throw new IndexOutOfBoundsException(badPositionIndexes(start, end, size));
        }
    }

    static String badPositionIndexes(int start, int end, int size) {
        if (start < 0 || start > size) {
            return badPositionIndex(start, size, "start index");
        }
        if (end < 0 || end > size) {
            return badPositionIndex(end, size, "end index");
        }
        // end < start
        return String.format("end index (%s) must not be less than start index (%s)", end, start);
    }

    static String badPositionIndex(int index, int size, String desc) {
        if (index < 0) {
            return String.format("%s (%s) must not be negative", desc, index);
        } else if (size < 0) {
            throw new IllegalArgumentException("negative size: " + size);
        } else { // index > size
            return String.format("%s (%s) must not be greater than size (%s)", desc, index, size);
        }
    }

}
