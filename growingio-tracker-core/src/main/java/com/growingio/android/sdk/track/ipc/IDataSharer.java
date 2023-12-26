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
package com.growingio.android.sdk.track.ipc;

import androidx.annotation.Nullable;

import java.util.List;
import java.util.Map;

public interface IDataSharer {
    @Nullable
    String getString(String key, @Nullable String defValue);

    int getInt(String key, int defValue);

    long getLong(String key, long defValue);

    float getFloat(String key, float defValue);

    boolean getBoolean(String key, boolean defValue);

    List<Integer> getIntArray(String key, List<Integer> defValue);

    void putString(String key, @Nullable String value);

    void putMultiString(Map<String, String> values);

    void putInt(String key, int value);

    void putLong(String key, long value);

    void putFloat(String key, float value);

    void putBoolean(String key, boolean value);

    void putIntArray(String key, List<Integer> value);

    long getAndIncrementLong(String key, long startValue);

    long getAndAddLong(String key, long delta, long startValue);

    int getAndIncrementInt(String key, int startValue);

    int getAndAddInt(String key, int delta, int startValue);

    int getAndDecrementInt(String key, int startValue);

    int getAndDelInt(String key, int delta, int startValue);

    void release();
}
