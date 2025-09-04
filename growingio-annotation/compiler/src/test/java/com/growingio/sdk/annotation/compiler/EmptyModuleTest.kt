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
import java.nio.charset.Charset
import javax.tools.JavaFileObject

@RunWith(JUnit4::class)
class EmptyModuleTest : CompilationProvider {
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
        Truth.assertThat(compilation!!.generatedSourceFiles().size).isEqualTo(1)

        val expectedClassName =
            "GioIndexer_GIOLibraryModule_com_growingio_android_sdk_EmptyLibraryGioModule"
        assertThat(compilation!!).generatedSourceFile(TestUtil.annotation(expectedClassName))
            .contentsAsString(Charset.defaultCharset()).contains(expectedClassName)

        assertThat(compilation!!).generatedSourceFile(TestUtil.annotation(expectedClassName))
            .hasSourceEquivalentTo(forResource(expectedClassName + ".java"))
    }

    private fun forResource(name: String): JavaFileObject = TestUtil.forResource(javaClass.simpleName, name)

    companion object {
        private const val MODULE_NAME = "EmptyLibraryGioModule.java"
    }

    override fun getCompilation(): Compilation = compilation!!
}
