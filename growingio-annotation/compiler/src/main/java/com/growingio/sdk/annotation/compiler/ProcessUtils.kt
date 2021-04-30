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

package com.growingio.sdk.annotation.compiler

import com.squareup.javapoet.JavaFile
import com.squareup.javapoet.TypeSpec
import com.sun.tools.javac.code.Attribute.UnresolvedClass
import com.sun.tools.javac.code.Type
import java.util.*
import javax.annotation.processing.ProcessingEnvironment
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.element.*
import javax.lang.model.util.ElementFilter
import javax.tools.Diagnostic


/**
 * <p>
 *
 * @author cpacm 2021/4/22
 */
class ProcessUtils(val processEnv: ProcessingEnvironment) {

    val gioModuleType: TypeElement
    val gioAppModuleType: TypeElement
    var round: Int = 0

    fun process() {
        round++
    }

    init {
        gioAppModuleType = processEnv.elementUtils.getTypeElement(APP_GIO_MODULE_QUALIFIED_NAME)
        gioModuleType = processEnv.elementUtils.getTypeElement(GIO_MODULE_QUALIFIED_NAME)
    }

    fun isGioModule(element: TypeElement): Boolean {
        return processEnv.typeUtils.isAssignable(element.asType(), gioModuleType.asType())
    }

    fun debugLog(msg: String) {
        if (DEBUG) {
            infoLog(msg)
        }
    }

    fun infoLog(msg: String) {
        processEnv.messager.printMessage(Diagnostic.Kind.NOTE, "$TAG:$msg")
    }

    fun isAppGioModule(element: TypeElement): Boolean {
        return processEnv.getTypeUtils().isAssignable(element.asType(), gioAppModuleType.asType())
//        debugLog("isAppGioModule:" + element.toString())
//        var isApp = true
//        val annotation = element.getAnnotation(GIOModule::class.java)
//        debugLog(element.toString() + "----" + annotation.toString())
//        if (annotation != null) {
//            isApp = !annotation.isLibrary
//        }
//
//        return isApp
    }

    fun getElementsFor(clazz: Class<out Annotation>, env: RoundEnvironment): List<TypeElement> {
        val annotatedElements: Collection<Element> = env.getElementsAnnotatedWith(clazz)
        return ElementFilter.typesIn(annotatedElements)
    }

    fun findAnnotatedElementsInClasses(inClass: TypeElement, annotationClass: Class<out Annotation>): List<ExecutableElement> {
        val result: MutableList<ExecutableElement> = ArrayList()
        for (element in inClass.enclosedElements) {
            if (element.getAnnotation(annotationClass) != null) {
                result.add(element as ExecutableElement)
            }
        }
        return result
    }

    fun writeIndexer(indexer: TypeSpec) {
        writeClass(COMPILER_PACKAGE_NAME, indexer)
    }

    fun writeClass(packageName: String?, clazz: TypeSpec) {
        try {
            debugLog("Writing class:\n$clazz")
            JavaFile.builder(packageName, clazz)
                    .skipJavaLangImports(true)
                    .build()
                    .writeTo(processEnv.getFiler())

        } catch (e: Throwable) {
            throw RuntimeException(e)
        }
    }


    companion object {
        const val GROWINGIO_MODULE_PACKAGE_NAME = "com.growingio.android.sdk"
        const val GROWINGIO_MODULE_NAME = "LibraryGioModule"
        private const val GROWINGIO_APP_MODULE_NAME = "AppGioModule"
        const val DEBUG = true

        const val APP_GIO_MODULE_QUALIFIED_NAME = "$GROWINGIO_MODULE_PACKAGE_NAME.$GROWINGIO_APP_MODULE_NAME"
        const val GIO_MODULE_QUALIFIED_NAME = "$GROWINGIO_MODULE_PACKAGE_NAME.$GROWINGIO_MODULE_NAME"

        const val GIO_LOG_TAG = "GIO"
        const val TAG = "[GioModuleProcessor]"

        @JvmField
        val COMPILER_PACKAGE_NAME: String = GioModuleProcessor::class.java.getPackage().getName()
        const val GENERATED_APP_MODULE_IMPL_SIMPLE_NAME = "GeneratedGioModuleImpl"
        const val GENERATED_ROOT_MODULE_SIMPLE_NAME = "GeneratedGioModule"

        const val GIO_TRACKER_REGISTRY_PACKAGE_NAME = "com.growingio.android.sdk.track.modelloader"
        const val GIO_TRACKER_REGISTRY_NAME = "TrackerRegistry"

        const val GIO_DEFAULT_TRACKER = "com.growingio.android.sdk.Tracker"
        const val GIO_DEFAULT_CONFIGURATION = "com.growingio.android.sdk.TrackConfiguration"
    }
}