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

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class ObjectUtils {

    private ObjectUtils() {
    }

    public static boolean equals(Object objL, Object objR) {
        return objL == objR || (objL != null && objL.equals(objR));
    }

    public static int hashCode(Object o) {
        return o != null ? o.hashCode() : 0;
    }

    public static String toString(Object o) {
        return String.valueOf(o);
    }

    // 参考 commons-lang ReflectionToStringBuilder
    public static String reflectToString(Object o) {
        try {
            StringBuilder objectInfo = new StringBuilder();
            Class<?> clazz = o.getClass();
            objectInfo.append(clazz.getSimpleName()).append(" {\n");
            appendFields(objectInfo, o, clazz);

            while (clazz.getSuperclass() != null) {
                clazz = clazz.getSuperclass();
                appendFields(objectInfo, o, clazz);
            }

            return objectInfo.append("}\n").toString();
        } catch (Throwable ignored) {
        }

        return o.toString();
    }

    private static void appendFields(StringBuilder builder, Object o, final Class<?> clazz) throws IllegalAccessException {
        final Field[] fields = clazz.getDeclaredFields();
        AccessibleObject.setAccessible(fields, true);
        for (final Field field : fields) {
            final String fieldName = field.getName();
            // Reject field from inner class
            if (!fieldName.contains("$") && !customProcess(builder, o, field)) {
                final Object fieldValue = field.get(o);
                builder.append("\t").append(fieldName).append(": ").append(fieldValue).append("\n");
            }
        }
    }

    private static boolean customProcess(StringBuilder builder, Object o, Field field) {
        try {
            if (field.isAnnotationPresent(FieldToString.class)) {
                for (Annotation annotation : field.getAnnotations()) {
                    if (annotation instanceof FieldToString) {
                        FieldToString fieldToStringAnnotation = (FieldToString) annotation;
                        Method fieldToStringMethod = fieldToStringAnnotation.clazz().getMethod(fieldToStringAnnotation.method(), fieldToStringAnnotation.parameterTypes());
                        fieldToStringMethod.setAccessible(true);
                        builder.append("\t").append(field.getName()).append(": ").append(fieldToStringMethod.invoke(o, field.get(o))).append("\n");
                        return true;
                    }
                }
            }
        } catch (Throwable ignored) {
        }

        return false;
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface FieldToString {
        Class<?> clazz();

        String method();

        Class<?>[] parameterTypes() default {};
    }
}
