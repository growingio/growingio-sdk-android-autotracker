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
