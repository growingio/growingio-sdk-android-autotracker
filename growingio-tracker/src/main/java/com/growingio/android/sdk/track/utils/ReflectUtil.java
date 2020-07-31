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

package com.growingio.android.sdk.track.utils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * 反射相关的工具类
 * <strong>改类使用Task initSrc映射到了plugin中有使用， 做出更改时需要手动调用initSrc的Task</strong>
 */
public abstract class ReflectUtil {

    private static final String TAG = "GIO.ReflectUtil";

    @SuppressWarnings("unchecked")
    public static <T> T getFiledValue(Field field, Object instance) {
        try {
            return field == null ? null : (T) field.get(instance);
        } catch (IllegalAccessException e) {
            LogUtil.e(TAG, e, e.getMessage());
            return null;
        }
    }

    public static Object callMethod(Object instance, String methodName, Object... args) {
        Class[] argsClass = new Class[args.length];
        for (int i = 0; i < args.length; i++) {
            if (args[i] != null) {
                argsClass[i] = args[i].getClass();
            }
        }
        Method method = getMethod(instance.getClass(), methodName, argsClass);
        if (method != null) {
            try {
                return method.invoke(instance, args);
            } catch (Exception e) {
                LogUtil.e(TAG, e, e.getMessage());
            }
        }
        return null;
    }

    public static Class<?> getMethodFromSignature(String signature) {
        throw new RuntimeException("NOT implementation");
    }

    public static Method getDeclaredRecur(Class<?> clazz, String methodName, Class<?>... params) {
        while (clazz != Object.class) {
            try {
                Method method = clazz.getDeclaredMethod(methodName, params);
                if (method != null) {
                    return method;
                }
            } catch (NoSuchMethodException e) {
                clazz = clazz.getSuperclass();
            }
        }
        return null;
    }

    public static Method getMethod(Class<?> clazz, String methodName, Class<?>... params) {
        try {
            return clazz.getMethod(methodName, params);
        } catch (NoSuchMethodException e) {
            LogUtil.d(TAG, e);
            return null;
        }
    }

    public static Field findFieldObj(Class<?> clazz, String fieldName) {
        try {
            Field field = clazz.getDeclaredField(fieldName);
            field.setAccessible(true);
            return field;
        } catch (NoSuchFieldException e) {
            LogUtil.d(TAG, e);
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> T findField(Class<?> clazz, Object instance, String fieldName) {
        Field field = findFieldObj(clazz, fieldName);
        if (field == null)
            return null;
        try {
            return (T) field.get(instance);
        } catch (IllegalAccessException e) {
            LogUtil.d(TAG, e);
            return null;
        }
    }


    public static Field findFieldObjRecur(Class<?> current, String fieldName) {
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

    @SuppressWarnings("unchecked")
    public static <T> T findFieldRecur(Object instance, String fieldName) {
        Field field = findFieldObjRecur(instance.getClass(), fieldName);
        if (field != null) {
            try {
                return (T) field.get(instance);
            } catch (IllegalAccessException e) {
                LogUtil.d(TAG, e);
            }
        }
        return null;
    }
}
