/*
 *   Copyright (C) 2020 Beijing Yishu Technology Co., Ltd.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package com.growingio.sdk.inject.compiler;import com.google.common.truth.Truth;
import com.google.testing.compile.Compilation;
import com.google.testing.compile.Compiler;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import javax.tools.JavaFileObject;

@RunWith(JUnit4.class)
public class InjectTest {
    private static final String MODULE_NAME = "TestInjector.java";

    private Compilation compilation;

    @Before
    public void setup() {
        compilation = Compiler.javac().withProcessors(new InjectProcessor()).compile(forResource(MODULE_NAME));
    }

    @Test
    public void generatedFileTest() {
        Truth.assertThat(compilation.generatedSourceFiles().size()).isEqualTo(1);
    }

    private JavaFileObject forResource(String name) {
        return TestUtil.forResource(getClass().getSimpleName(), name);
    }
}
