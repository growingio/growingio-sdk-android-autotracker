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

import com.growingio.sdk.annotation.GIOAppModule
import com.squareup.javapoet.JavaFile
import com.squareup.javapoet.TypeSpec
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
    val gioConfigType: TypeElement
    var round: Int = 0

    fun process() {
        round++
    }

    init {
        gioAppModuleType = processEnv.elementUtils.getTypeElement(APP_GIO_MODULE_QUALIFIED_NAME)
        gioModuleType = processEnv.elementUtils.getTypeElement(GIO_MODULE_QUALIFIED_NAME)
        gioConfigType = processEnv.elementUtils.getTypeElement(GIO_CONFIG_QUALIFIED_NAME)
    }

    fun debugLog(msg: String) {
        if (DEBUG) {
            infoLog(msg)
        }
    }

    fun infoLog(msg: String) {
        processEnv.messager.printMessage(Diagnostic.Kind.NOTE, "$TAG:$msg")
    }

    fun isGioModule(element: TypeElement): Boolean {
        return processEnv.typeUtils.isAssignable(element.asType(), gioModuleType.asType())
    }

    fun isGioConfig(element: TypeElement): Boolean {
        return processEnv.typeUtils.isAssignable(element.asType(), gioConfigType.asType())
    }

    fun isAppGioModule(element: TypeElement): Boolean {
        return processEnv.getTypeUtils().isAssignable(element.asType(), gioAppModuleType.asType())
    }

    fun getElementsFor(clazz: Class<out Annotation>, env: RoundEnvironment): List<TypeElement> {
        val annotatedElements: Collection<Element> = env.getElementsAnnotatedWith(clazz)
        return ElementFilter.typesIn(annotatedElements)
    }

    fun findAnnotatedElementsInClasses(
        inClass: TypeElement,
        annotationClass: Class<out Annotation>
    ): List<ExecutableElement> {
        val result: MutableList<ExecutableElement> = ArrayList()
        for (element in inClass.enclosedElements) {
            if (element.getAnnotation(annotationClass) != null) {
                result.add(element as ExecutableElement)
            }
        }
        return result
    }

    fun getConfigName(appModule: TypeElement): String {
        val annotation = appModule.getAnnotation(GIOAppModule::class.java)
        val growingName = annotation.name
        val configName = with(annotation.configName) {
            if (this.isEmpty()) {
                growingName + "Configuration"
            } else if (this.endsWith("Config") || this.endsWith("Configuration")) {
                this
            } else {
                this + "Configuration"
            }
        }
        return configName
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
        private const val GROWINGIO_CONFIG_NAME = "Configurable"
        const val DEBUG = false

        const val APP_GIO_MODULE_QUALIFIED_NAME =
            "$GROWINGIO_MODULE_PACKAGE_NAME.$GROWINGIO_APP_MODULE_NAME"
        const val GIO_MODULE_QUALIFIED_NAME =
            "$GROWINGIO_MODULE_PACKAGE_NAME.$GROWINGIO_MODULE_NAME"
        const val GIO_CONFIG_QUALIFIED_NAME =
            "$GROWINGIO_MODULE_PACKAGE_NAME.$GROWINGIO_CONFIG_NAME"

        const val GIO_LOG_TAG = "GIO"
        const val TAG = "[GioModuleProcessor]"

        @JvmField
        val COMPILER_PACKAGE_NAME: String = GioModuleProcessor::class.java.getPackage().getName()
        const val GENERATED_APP_MODULE_IMPL_SIMPLE_NAME = "GeneratedGioModuleImpl"
        const val GENERATED_ROOT_MODULE_SIMPLE_NAME = "GeneratedGioModule"

        const val GIO_TRACKER_REGISTRY_PACKAGE_NAME = "com.growingio.android.sdk.track.modelloader"
        const val GIO_TRACKER_REGISTRY_NAME = "TrackerRegistry"

        const val GIO_DEFAULT_TRACKER = "com.growingio.android.sdk.Tracker"
        const val GIO_DEFAULT_CONFIGURATION = "com.growingio.android.sdk.CoreConfiguration"
        const val GIO_DEFAULT_CONFIGURABLE = "com.growingio.android.sdk.Configurable"
        const val GIO_DEFAULT_LOGGER = "com.growingio.android.sdk.track.log.Logger"
        const val GIO_CONFIGURATION_PROVIDER="com.growingio.android.sdk.track.providers.ConfigurationProvider"

        const val GIO_INDEX_ANNOTATION_MODULES = "modules"
        const val GIO_INDEX_ANNOTATION_CONFIGS = "configs"
    }
}