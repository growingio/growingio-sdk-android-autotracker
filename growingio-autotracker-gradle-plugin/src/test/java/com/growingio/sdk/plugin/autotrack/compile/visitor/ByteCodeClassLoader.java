package com.growingio.sdk.plugin.autotrack.compile.visitor;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

class ByteCodeClassLoader extends ClassLoader {
    private static final String TAG = "ByteCodeClassLoader";
    private final ClassLoader mRealClassloader;

    public ByteCodeClassLoader(ClassLoader classLoader) {
        super(getSystemClassLoader().getParent());
        mRealClassloader = classLoader;
    }

    @Override
    protected Class<?> findClass(String s) throws ClassNotFoundException {
        System.out.println("xxxxx ClassNotFoundException findClass " + s);

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
