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

package com.growingio.android.sdk.autotrack.util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

/**
 * 该class通过一些技术手段绕过了Android 9 及其以后版本的 "针对非 SDK 接口的限制"，请谨慎使用
 */
public class HurtLocker {
    private HurtLocker() {
    }

    public static Field getField(String clazz, String fieldName) throws Exception {
        return getField(Class.forName(clazz), fieldName);
    }

    public static Field getField(Class<?> type, String fieldName) throws Exception {
        LinkedList<Class<?>> examine = new LinkedList<>();
        examine.add(type);
        Set<Class<?>> done = new HashSet<>();
        while (!examine.isEmpty()) {
            Class<?> thisType = examine.removeFirst();
            done.add(thisType);
            final Field[] declaredField = getDeclaredFields(thisType);
            for (Field field : declaredField) {
                if (fieldName.equals(field.getName())) {
                    field.setAccessible(true);
                    return field;
                }
            }
            Set<Class<?>> potential = new HashSet<>();
            final Class<?> clazz = thisType.getSuperclass();
            if (clazz != null) {
                potential.add(thisType.getSuperclass());
            }
            potential.addAll(Arrays.asList(thisType.getInterfaces()));
            potential.removeAll(done);
            examine.addAll(potential);
        }

        throw new NoSuchFieldException("No field was found with name '" + fieldName + "' in class "
                + type.getName() + ".");
    }

    public static <T> T getInternalState(Object object, String fieldName) throws Exception {
        Field foundField = getField(object.getClass(), fieldName);
        return (T) foundField.get(object);
    }

    public static <T> T invokeMethod(Object object, Class<?> declaringClass, String methodToExecute,
                                     Class<?>[] parameterTypes, Object... arguments) throws Exception {
        Method declaredMethod = declaringClass.getDeclaredMethod(methodToExecute, parameterTypes);
        declaredMethod.setAccessible(true);
        return (T) declaredMethod.invoke(object, arguments);
    }

    public static <T> T invokeStaticMethod(Class<?> declaringClass, String methodToExecute, Class<?>[] parameterTypes, Object... arguments) throws Exception {
        return invokeMethod(null, declaringClass, methodToExecute, parameterTypes, arguments);
    }

    public static Field[] getDeclaredFields(Class<?> type) throws Exception {
        return invokeMethod(type, Class.class, "getDeclaredFields", null);
    }
}
