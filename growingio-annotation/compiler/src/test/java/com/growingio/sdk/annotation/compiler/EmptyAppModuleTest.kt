/*
 * Copyright (C) 2023 Beijing Yishu Technology Co., Ltd.
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
package com.growingio.sdk.annotation.compiler

import com.google.common.truth.Truth
import com.google.testing.compile.Compilation
import com.google.testing.compile.CompilationSubject.assertThat
import com.google.testing.compile.Compiler
import com.growingio.sdk.annotation.compiler.rule.CompilationProvider
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import javax.tools.JavaFileObject

@RunWith(JUnit4::class)
class EmptyAppModuleTest : CompilationProvider {
    private var compilation: Compilation? = null

    @Before
    fun setUp() {
        compilation = Compiler.javac().withProcessors(GioModuleProcessor()).compile(
            forResource(MODULE_NAME),
        )
        assertThat(compilation).succeededWithoutWarnings()
    }

    @Test
    fun generatedFileTest() {
        Truth.assertThat(compilation!!.generatedSourceFiles().size).isEqualTo(3)
        System.out.println(compilation!!.generatedSourceFiles())

        assertThat(compilation!!).generatedSourceFile(TestUtil.subpackage("GrowingTracker"))
            .hasSourceEquivalentTo(forResource("GrowingTracker.java"))

        assertThat(compilation!!).generatedSourceFile(TestUtil.growingio("GeneratedGioModuleImpl"))
            .hasSourceEquivalentTo(forResource("GeneratedGioModuleImpl.java"))

        assertThat(compilation!!).generatedSourceFile(TestUtil.subpackage("TestTrackConfiguration"))
            .hasSourceEquivalentTo(forResource("TestTrackConfiguration.java"))
    }

    private fun forResource(name: String): JavaFileObject {
        return TestUtil.forResource(javaClass.simpleName, name)
    }

    companion object {
        private const val MODULE_NAME = "EmptyAppGioModule.java"
    }

    override fun getCompilation(): Compilation {
        return compilation!!
    }
}
