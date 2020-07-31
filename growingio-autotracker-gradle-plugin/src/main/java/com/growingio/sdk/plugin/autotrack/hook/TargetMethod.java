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

public class TargetMethod {
    private final String mName;
    private final String mDesc;
    private final Set<InjectMethod> mInjectMethods = new HashSet<>();

    public TargetMethod(String name, String desc) {
        mName = name;
        mDesc = desc;
    }

    public String getName() {
        return mName;
    }

    public String getDesc() {
        return mDesc;
    }

    public void addInjectMethod(InjectMethod method) {
        mInjectMethods.add(method);
    }

    public void addInjectMethods(Set<InjectMethod> methods) {
        mInjectMethods.addAll(methods);
    }

    public Set<InjectMethod> getInjectMethods() {
        return Collections.unmodifiableSet(mInjectMethods);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TargetMethod that = (TargetMethod) o;

        if (!mName.equals(that.mName)) return false;
        return mDesc.equals(that.mDesc);
    }

    @Override
    public int hashCode() {
        int result = mName.hashCode();
        result = 31 * result + mDesc.hashCode();
        return result;
    }
}
