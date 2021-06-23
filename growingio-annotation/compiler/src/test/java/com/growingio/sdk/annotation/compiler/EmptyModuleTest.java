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


package com.growingio.sdk.annotation.compiler;


import com.google.testing.compile.Compilation;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import javax.tools.JavaFileObject;

@RunWith(JUnit4.class)
public class EmptyModuleTest {

    private static final String MODULE_NAME = "EmptyLibraryGioModule.java";

    private Compilation compilation;

    @Before
    public void setUp() {
//        compilation =
//                javac().withProcessors(new GioModuleProcessor()).compile(forResource(MODULE_NAME));
//        assertThat(compilation).succeededWithoutWarnings();

    }

    @Test
    public void geneatedFileTest() {
        //System.out.println(compilation.generatedFiles().size());
    }

    private JavaFileObject forResource(String name) {
        return TestUtil.forResource(getClass().getSimpleName(), name);
    }
}
