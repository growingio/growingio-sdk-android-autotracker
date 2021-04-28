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

package com.growingio.sdk.inject.compiler;

import org.objectweb.asm.Type;

public class TypeUtil {
    private TypeUtil() {
    }

    public static Type getType(final String clazz) {
        return Type.getType(getDescriptor(clazz));
    }

    public static String getInternalName(final String clazz) {
        if (isPrimitive(clazz)) {
            return getDescriptor(clazz);
        }
        return getType(clazz).getInternalName();
    }

    public static String getDescriptor(final String clazz) {
        StringBuilder buf = new StringBuilder();
        String d = clazz;
        while (true) {
            if (isPrimitive(d)) {
                char car;
                if (d.equals(int.class.getName())) {
                    car = 'I';
                } else if (d.equals(void.class.getName())) {
                    car = 'V';
                } else if (d.equals(boolean.class.getName())) {
                    car = 'Z';
                } else if (d.equals(byte.class.getName())) {
                    car = 'B';
                } else if (d.equals(char.class.getName())) {
                    car = 'C';
                } else if (d.equals(short.class.getName())) {
                    car = 'S';
                } else if (d.equals(double.class.getName())) {
                    car = 'D';
                } else if (d.equals(float.class.getName())) {
                    car = 'F';
                } else /* if (d == Long.TYPE) */ {
                    car = 'J';
                }
                buf.append(car);
                return buf.toString();
            } else if (isArray(d)) {
                buf.append('[');
                d = d.replace("[]", "");
            } else {
                buf.append('L');
                int len = d.length();
                for (int i = 0; i < len; ++i) {
                    char car = d.charAt(i);
                    buf.append(car == '.' ? '/' : car);
                }
                buf.append(';');
                return buf.toString();
            }
        }
    }

    public static boolean isPrimitive(final String clazz) {
        return clazz.equals(int.class.getName())
                || clazz.equals(void.class.getName())
                || clazz.equals(boolean.class.getName())
                || clazz.equals(byte.class.getName())
                || clazz.equals(char.class.getName())
                || clazz.equals(short.class.getName())
                || clazz.equals(double.class.getName())
                || clazz.equals(float.class.getName())
                || clazz.equals(long.class.getName());
    }

    public static boolean isArray(final String clazz) {
        return clazz.endsWith("[]");
    }
}
