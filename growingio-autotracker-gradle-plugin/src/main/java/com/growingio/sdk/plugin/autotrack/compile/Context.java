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

package com.growingio.sdk.plugin.autotrack.compile;

public class Context {
    private final Log mLog;
    private final ClassLoader mClassLoader;

    private String mClassName;
    private String mSuperClassName;
    private boolean mClassModified;
    private boolean mIsAbstract;

    public Context(Log log, ClassLoader classLoader) {
        mLog = log;
        mClassLoader = classLoader;
    }

    public ClassLoader getClassLoader() {
        return mClassLoader;
    }

    public Log getLog() {
        return mLog;
    }

    public String getClassName() {
        return mClassName;
    }

    public void setClassName(String className) {
        mClassName = className;
    }

    public String getSuperClassName() {
        return mSuperClassName;
    }

    public void setSuperClassName(String superClassName) {
        mSuperClassName = superClassName;
    }

    public boolean isClassModified() {
        return mClassModified;
    }

    public void markModified() {
        mClassModified = true;
    }

    public boolean isAbstract() {
        return mIsAbstract;
    }

    public void setAbstract(boolean anAbstract) {
        mIsAbstract = anAbstract;
    }

    public boolean isAssignable(String subClassName, String superClassName) {
        if (subClassName.contains("/")) {
            subClassName = subClassName.replace("/", ".");
        }
        if (superClassName.contains("/")) {
            superClassName = superClassName.replace("/", ".");
        }
        try {
            Class<?> subClass = getClassLoader().loadClass(subClassName);
            Class<?> superClass = getClassLoader().loadClass(superClassName);
            return superClass.isAssignableFrom(subClass);
        } catch (ClassNotFoundException ignored) {
        }
        return false;
    }
}
