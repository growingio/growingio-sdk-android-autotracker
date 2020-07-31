package com.growingio.sdk.plugin.autotrack;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;

public class ClassUtils {
    public static InputStream classToInputStream(Class<?> clazz) {
        String className = clazz.getName();
        String classAsPath = className.replace('.', '/') + ".class";
        return clazz.getClassLoader().getResourceAsStream(classAsPath);
    }

    public static byte[] classToByteArray(Class<?> clazz) throws IOException {
        return IOUtils.toByteArray(classToInputStream(clazz));
    }

    public static String getClassName(Class<?> clazz) {
        return clazz.getName().replace(".", "/");
    }
}
