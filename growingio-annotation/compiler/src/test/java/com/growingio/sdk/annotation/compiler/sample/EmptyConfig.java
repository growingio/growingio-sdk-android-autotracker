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
package com.growingio.sdk.annotation.compiler.sample;

import com.growingio.android.sdk.Configurable;

public class EmptyConfig implements Configurable {

    private String testValue;

    @Deprecated
    public EmptyConfig setTestValue(String testValue) {
        this.testValue = testValue;
        return this;
    }

    protected EmptyConfig replaceTestValue(String testValue) {
        this.testValue = testValue;
        return this;
    }

    public String getTestValue() {
        return testValue;
    }
}
