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

import androidx.annotation.Nullable;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * 该数据结构最大存储长度为{@link SharedEntry#MAX_SIZE}，所以该数据结构不适于存储对象序列化或者长字符串。
 * ┌───────────┬───────────┬─────┐
 * │size       │name       │count│
 * ├───────────┼───────────┼─────┤
 * │u2         │keyLength  │  1  │
 * ├───────────┼───────────┼─────┤
 * │keyLength  │key        │  1  │
 * ├───────────┼───────────┼─────┤
 * │u1         │valueType  │  1  │
 * ├───────────┼───────────┼─────┤
 * │u2         │valueLength│  1  │
 * ├───────────┼───────────┼─────┤
 * │ux         │value      │  1  │
 * └───────────┴───────────┴─────┘
 */
class SharedEntry {
    public static final int MAX_SIZE = 1024;

    public static final byte VALUE_TYPE_INT = 1;
    public static final byte VALUE_TYPE_LONG = 2;
    public static final byte VALUE_TYPE_FLOAT = 3;
    public static final byte VALUE_TYPE_BOOLEAN = 4;
    public static final byte VALUE_TYPE_STRING = 5;
    public static final byte VALUE_TYPE_INT_ARRAY = 6;

    private final int mPosition;
    private final String mKey;

    private final int mValuePosition;

    SharedEntry(ByteBuffer byteBuffer, int position) throws IllegalArgumentException {
        mPosition = position;
        byteBuffer.position(mPosition);
        short keyLength = byteBuffer.getShort();
        if (keyLength == 0) {
            throw new IllegalArgumentException("This position is not valid");
        }
        byte[] bytes = new byte[keyLength];
        byteBuffer.get(bytes);
        mKey = new String(bytes);
        mValuePosition = mPosition + 2 + keyLength;
    }

    SharedEntry(ByteBuffer byteBuffer, int position, String key) {
        mPosition = position;
        mKey = key;
        byteBuffer.position(mPosition);
        short keyLength = (short) mKey.length();
        byteBuffer.putShort(keyLength);
        byteBuffer.put(mKey.getBytes());
        mValuePosition = mPosition + 2 + keyLength;
    }

    public int getPosition() {
        return mPosition;
    }

    public String getKey() {
        return mKey;
    }

    @Nullable
    public Object getValue(ByteBuffer byteBuffer) {
        byteBuffer.position(mValuePosition);
        byte valueType = byteBuffer.get();
        short valueLength = byteBuffer.getShort();
        switch (valueType) {
            case VALUE_TYPE_INT:
                if (valueLength == (Integer.SIZE / Byte.SIZE)) {
                    return byteBuffer.getInt();
                }
                break;
            case VALUE_TYPE_LONG:
                if (valueLength == (Long.SIZE / Byte.SIZE)) {
                    return byteBuffer.getLong();
                }
                break;
            case VALUE_TYPE_FLOAT:
                if (valueLength == (Float.SIZE / Byte.SIZE)) {
                    return byteBuffer.getFloat();
                }
                break;
            case VALUE_TYPE_BOOLEAN:
                int size = Byte.SIZE;
                if (valueLength == (Byte.SIZE / size)) {
                    return byteBuffer.get() == 1;
                }
                break;
            case VALUE_TYPE_STRING:
                if (valueLength > 0) {
                    byte[] bytes = new byte[valueLength];
                    byteBuffer.get(bytes);
                    return new String(bytes);
                }
                break;
            case VALUE_TYPE_INT_ARRAY:
                if (valueLength > 0) {
                    List<Integer> array = new ArrayList<>();
                    for (int i = 0; i < valueLength; i++) {
                        array.add(byteBuffer.getInt());
                    }
                    return array;
                }
                break;
            default:
                break;
        }

        return null;
    }

    public void putIntArray(ByteBuffer byteBuffer, List<Integer> values) {
        byteBuffer.position(mValuePosition);
        byteBuffer.put(VALUE_TYPE_INT_ARRAY);
        if ((values.size() * (Integer.SIZE / Byte.SIZE)) > (MAX_SIZE - (mValuePosition - mPosition))) {
            throw new IllegalArgumentException("value is too long, value.length() = " + values.size());
        }
        byteBuffer.putShort((short) values.size());
        for (int value : values) {
            byteBuffer.putInt(value);
        }
    }

    public void putString(ByteBuffer byteBuffer, @Nullable String value) throws IllegalArgumentException {
        byteBuffer.position(mValuePosition);
        byteBuffer.put(VALUE_TYPE_STRING);
        if (value == null) {
            value = "";
        }
        byte[] valueBytes = value.getBytes();
        short valueLength = valueBytes == null ? 0 : ((short) valueBytes.length);
        if (valueLength > (MAX_SIZE - (mValuePosition - mPosition))) {
            throw new IllegalArgumentException("value is too long, value.length() = " + value.length());
        }
        byteBuffer.putShort(valueLength);
        if (valueLength > 0) {
            byteBuffer.put(valueBytes);
        }
    }

    public void putInt(ByteBuffer byteBuffer, int value) {
        byteBuffer.position(mValuePosition);
        byteBuffer.put(VALUE_TYPE_INT);
        byteBuffer.putShort((short) (Integer.SIZE / Byte.SIZE));
        byteBuffer.putInt(value);
    }

    public void putLong(ByteBuffer byteBuffer, long value) {
        byteBuffer.position(mValuePosition);
        byteBuffer.put(VALUE_TYPE_LONG);
        byteBuffer.putShort((short) (Long.SIZE / Byte.SIZE));
        byteBuffer.putLong(value);
    }

    public void putFloat(ByteBuffer byteBuffer, float value) {
        byteBuffer.position(mValuePosition);
        byteBuffer.put(VALUE_TYPE_FLOAT);
        byteBuffer.putShort((short) (Float.SIZE / Byte.SIZE));
        byteBuffer.putFloat(value);
    }

    public void putBoolean(ByteBuffer byteBuffer, boolean value) {
        int size = Byte.SIZE;
        byteBuffer.position(mValuePosition);
        byteBuffer.put(VALUE_TYPE_BOOLEAN);
        byteBuffer.putShort((short) (Byte.SIZE / size));
        byteBuffer.put((byte) (value ? 1 : 0));
    }

    public void putObject(ByteBuffer byteBuffer, int valueType, Object value) {
        switch (valueType) {
            case VALUE_TYPE_INT:
                putInt(byteBuffer, (Integer) value);
                break;
            case VALUE_TYPE_LONG:
                putLong(byteBuffer, (Long) value);
                break;
            case VALUE_TYPE_FLOAT:
                putFloat(byteBuffer, (Float) value);
                break;
            case VALUE_TYPE_BOOLEAN:
                putBoolean(byteBuffer, (Boolean) value);
                break;
            case VALUE_TYPE_STRING:
                putString(byteBuffer, (String) value);
                break;
            case VALUE_TYPE_INT_ARRAY:
                putIntArray(byteBuffer, (List<Integer>) value);
                break;
            default:
                break;
        }
    }
}
