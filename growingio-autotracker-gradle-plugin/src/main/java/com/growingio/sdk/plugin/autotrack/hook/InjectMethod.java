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

package com.growingio.sdk.plugin.autotrack.hook;

public class InjectMethod {
    private final String mClassName;
    private final String mMethodName;
    private final String mMethodDesc;
    private final boolean mIsAfter;

    public InjectMethod(String className, String methodName, String methodDesc, boolean isAfter) {
        mClassName = className;
        mMethodName = methodName;
        mMethodDesc = methodDesc;
        mIsAfter = isAfter;
    }

    public String getClassName() {
        return mClassName;
    }

    public String getMethodName() {
        return mMethodName;
    }

    public String getMethodDesc() {
        return mMethodDesc;
    }

    public boolean isAfter() {
        return mIsAfter;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        InjectMethod that = (InjectMethod) o;

        if (mIsAfter != that.mIsAfter) return false;
        if (!mClassName.equals(that.mClassName)) return false;
        if (!mMethodName.equals(that.mMethodName)) return false;
        return mMethodDesc.equals(that.mMethodDesc);
    }

    @Override
    public int hashCode() {
        int result = mClassName.hashCode();
        result = 31 * result + mMethodName.hashCode();
        result = 31 * result + mMethodDesc.hashCode();
        result = 31 * result + (mIsAfter ? 1 : 0);
        return result;
    }
}
