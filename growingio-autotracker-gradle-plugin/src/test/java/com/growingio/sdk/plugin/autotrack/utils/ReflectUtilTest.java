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

package com.growingio.sdk.plugin.autotrack.utils;

import com.google.common.truth.Truth;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
public class ReflectUtilTest {

    @Test
    public void testReflectUtil() throws IllegalAccessException {
        Truth.assertThat(ReflectUtil.getMethod(TempClass.class, "tempMethod")).isNotNull();
        Truth.assertThat(ReflectUtil.findFieldObj(TempClass.class, "temp").get(new TempClass())).isEqualTo("temp");
        Truth.assertThat(ReflectUtil.<String>findField(new TempClass(), "temp")).isEqualTo("temp");

        Truth.assertThat(ReflectUtil.getMethod(TempClass.class, "noSuchMethod")).isNull();
        Truth.assertThat(ReflectUtil.findFieldObj(TempClass.class, "noVariable")).isNull();
        Truth.assertThat(ReflectUtil.<String>findField(new TempClass(), "noVariable")).isNull();
    }
}

class TempClass {
    public String temp = "temp";

    public void tempMethod() {
    }
}
