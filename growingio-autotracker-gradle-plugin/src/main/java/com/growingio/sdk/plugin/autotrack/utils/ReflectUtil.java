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

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public abstract class ReflectUtil {

    public static Method getMethod(Class<?> clazz, String methodName, Class<?>... params) {
        try {
            return clazz.getMethod(methodName, params);
        } catch (NoSuchMethodException e) {
            return null;
        }
    }

    public static Field findFieldObj(Class<?> current, String fieldName) {
        while (current != Object.class) {
            try {
                Field field = current.getDeclaredField(fieldName);
                field.setAccessible(true);
                return field;
            } catch (NoSuchFieldException e) {
                current = current.getSuperclass();
            }
        }
        return null;
    }

    public static <T> T findField(Object instance, String fieldName) {
        Field field = findFieldObj(instance.getClass(), fieldName);
        if (field != null) {
            try {
                return (T) field.get(instance);
            } catch (IllegalAccessException ignored) {
            }
        }
        return null;
    }
}
