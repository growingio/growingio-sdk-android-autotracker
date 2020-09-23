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

package com.growingio.sdk.plugin.autotrack;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ByteCodeClassLoader extends ClassLoader {
    private static final String TAG = "ByteCodeClassLoader";
    private final ClassLoader mRealClassloader;

    public ByteCodeClassLoader(ClassLoader classLoader) {
        super(getSystemClassLoader().getParent());
        mRealClassloader = classLoader;
    }

    @Override
    protected Class<?> findClass(String s) throws ClassNotFoundException {
        try {
            return super.findClass(s);
        } catch (ClassNotFoundException e) {
            try {
                Method findClass = ClassLoader.class.getDeclaredMethod("loadClass", String.class);
                findClass.setAccessible(true);
                return (Class<?>) findClass.invoke(mRealClassloader, s);
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e2) {
                throw new ClassNotFoundException(s);
            }
        }
    }

    public Class defineClass(String name, byte[] bytes) {
        return defineClass(name, bytes, 0, bytes.length);
    }
}
