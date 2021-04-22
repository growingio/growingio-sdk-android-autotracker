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

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class TargetClass {
    private final String mName;
    private final Set<TargetMethod> mTargetMethods = new HashSet<>();
    private boolean mInterface = false;

    public TargetClass(String name) {
        mName = name;
    }

    public void addTargetMethod(TargetMethod method) {
        mTargetMethods.add(method);
    }

    public String getName() {
        return mName;
    }

    public Set<TargetMethod> getTargetMethods() {
        return Collections.unmodifiableSet(mTargetMethods);
    }

    public TargetMethod getTargetMethod(String name, String desc) {
        for (TargetMethod method : mTargetMethods) {
            if (name.equals(method.getName()) && desc.equals(method.getDesc())) {
                return method;
            }
        }
        return null;
    }

    public void setInterface(boolean anInterface) {
        mInterface = anInterface;
    }

    public boolean isInterface() {
        return mInterface;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TargetClass that = (TargetClass) o;

        return mName.equals(that.mName);
    }

    @Override
    public int hashCode() {
        return mName.hashCode();
    }
}
