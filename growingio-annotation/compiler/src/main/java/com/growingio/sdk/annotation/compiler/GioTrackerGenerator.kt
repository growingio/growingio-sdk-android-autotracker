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

import com.growingio.sdk.annotation.GIOConfig
import com.growingio.sdk.annotation.GIOModule
import com.growingio.sdk.annotation.compiler.ProcessUtils.Companion.GIO_DEFAULT_CONFIGURATION
import com.growingio.sdk.annotation.compiler.ProcessUtils.Companion.GIO_DEFAULT_TRACKER
import com.squareup.javapoet.*
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.Modifier
import javax.lang.model.element.TypeElement

/**
 * <p>
 *
 * @author cpacm 4/29/21
 */
class GioTrackerGenerator(private val processEnv: ProcessingEnvironment, private val processUtils: ProcessUtils) {

    private lateinit var trackerClassName: String
    private lateinit var configClassName: String
    private var trackerMethodName: String? = null

    fun generate(appModule: TypeElement) {
        initGIOConfig(appModule)

        val tracker = processEnv.elementUtils.getTypeElement(trackerClassName)
                ?: throw IllegalStateException("Do you have implement this class:$trackerClassName?")
        val config = processEnv.elementUtils.getTypeElement(configClassName)
                ?: throw IllegalStateException("Do you have implement this class:$configClassName?")
        val trackerClass = ClassName.get(tracker)
        val configClass = ClassName.get(config)
        val annotation = appModule.getAnnotation(GIOModule::class.java)
        val growingName = annotation.gioName
        val trackerBuilder = TypeSpec.classBuilder(growingName)
                .addJavadoc(
                        """
                            The entry point for interacting with GrowingIO Tracker for Applications
                            <p>Includes all generated APIs from Tracker in source.
                            
                            <p>This class is generated and should not be modified
                            
                            """.trimIndent())
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addField(FieldSpec
                        .builder(String::class.java, "TAG", Modifier.STATIC, Modifier.FINAL, Modifier.PRIVATE)
                        .initializer("\$S", growingName)
                        .build()
                )
                .addField(trackerClass, "_gioTracker", Modifier.STATIC, Modifier.PRIVATE, Modifier.VOLATILE)
                .addMethod(generateGetMethod(trackerClass, growingName))
                .addMethod(generateStartMethod(appModule, trackerClass, configClass))
                .addMethod(generateStartConfigurationMethod(appModule, trackerClass, configClass))
                .addMethod(generateEmptyMethod(trackerClass))
                .addMethod(generateSuccessMethod())
                .build()

        val generatedCodePackageName = appModule.enclosingElement.toString()
        writeTracker(generatedCodePackageName, trackerBuilder)
    }


    private fun generateGetMethod(trackerClass: ClassName, growingName: String): MethodSpec {
        val logger = processEnv.elementUtils.getTypeElement("com.growingio.android.sdk.track.log.Logger")
        val getMethod = MethodSpec.methodBuilder("get")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .beginControlFlow("if (_gioTracker == null)")
                .addStatement("\$T.e(TAG, \$S)", ClassName.get(logger), "${trackerClass.simpleName()} is UNINITIALIZED, please initialized before use API")
                .addStatement("return empty()")
                .endControlFlow()
                .beginControlFlow("synchronized ($growingName.class)")
                .beginControlFlow("if (_gioTracker != null)")
                .addStatement("return _gioTracker")
                .endControlFlow()
                .addStatement("\$T.e(TAG, \$S)", ClassName.get(logger), "${trackerClass.simpleName()} is UNINITIALIZED, please initialized before use API")
                .addStatement("return empty()")
                .endControlFlow()
                .returns(trackerClass)
        return getMethod.build()
    }

    private fun generateStartMethod(appModule: TypeElement, trackerClass: ClassName, configClass: ClassName): MethodSpec {
        val logger = processEnv.elementUtils.getTypeElement("com.growingio.android.sdk.track.log.Logger")
        val application = processEnv.elementUtils.getTypeElement("android.app.Application")
        val getMethod = MethodSpec.methodBuilder("startWithConfiguration")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                //.addAnnotation(java.lang.Deprecated::class.java)
                .addParameter(ClassName.get(application), "application")
                .addParameter(configClass, "trackConfiguration")
                .beginControlFlow("if (_gioTracker != null)")
                .addStatement("\$T.e(TAG, \$S)", ClassName.get(logger), "${trackerClass.simpleName()} is running")
                .addStatement("return")
                .endControlFlow()
        if (trackerMethodName != null) {
            //GrowingAppModule appModule = new GrowingAppModule();
            getMethod.addStatement("\$T appModule = new \$T()", appModule, appModule)
                    //appModule.config(trackConfiguration);
                    .addStatement("appModule.${trackerMethodName}(trackConfiguration)")
        }

        getMethod.addStatement("_gioTracker = new \$T(application,trackConfiguration)", trackerClass)
        getMethod.addStatement("initSuccess()")
        return getMethod.build()
    }

    private fun generateStartConfigurationMethod(appModule: TypeElement, trackerClass: ClassName, configClass: ClassName): MethodSpec {
        val logger = processEnv.elementUtils.getTypeElement("com.growingio.android.sdk.track.log.Logger")
        val application = processEnv.elementUtils.getTypeElement("android.app.Application")
        val getMethod = MethodSpec.methodBuilder("start")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addParameter(ClassName.get(application), "application")
                .beginControlFlow("if (_gioTracker != null)")
                .addStatement("\$T.e(TAG, \$S)", ClassName.get(logger), "${trackerClass.simpleName()} is running")
                .addStatement("return")
                .endControlFlow()
                .addStatement("\$T trackConfiguration = new \$T()", configClass, configClass)
        if (trackerMethodName != null) {
            //GrowingAppModule appModule = new GrowingAppModule();
            getMethod.addStatement("\$T appModule = new \$T()", appModule, appModule)
                    //appModule.config(trackConfiguration);
                    .addStatement("appModule.${trackerMethodName}(trackConfiguration)")
        }

        getMethod.addStatement("_gioTracker = new \$T(application,trackConfiguration)", trackerClass)
        getMethod.addStatement("initSuccess()")
        return getMethod.build()
    }

    private fun generateEmptyMethod(trackerClass: ClassName): MethodSpec {
        val emptyMethod = MethodSpec.methodBuilder("empty")
                .addModifiers(Modifier.PRIVATE, Modifier.STATIC)
                .addStatement("return new \$T(null,null)", trackerClass)
                .returns(trackerClass)
        return emptyMethod.build()
    }

    private fun generateSuccessMethod(): MethodSpec {
        val logger = processEnv.elementUtils.getTypeElement("com.growingio.android.sdk.track.log.Logger")
        val sdk = processEnv.elementUtils.getTypeElement("com.growingio.android.sdk.track.SDKConfig")
        val successMethod = MethodSpec.methodBuilder("initSuccess")
                .addModifiers(Modifier.PRIVATE, Modifier.STATIC)
                .addStatement("\$T.i(TAG, \$S)", ClassName.get(logger), "!!! Thank you very much for using GrowingIO. We will do our best to provide you with the best service. !!!")
                .addStatement("\$T.i(TAG, \"!!! GrowingIO Tracker version: \"+\$T.SDK_VERSION+\" !!!\")", ClassName.get(logger), ClassName.get(sdk))
        return successMethod.build()
    }


    private fun initGIOConfig(appModule: TypeElement) {
        val configs = processUtils.findAnnotatedElementsInClasses(appModule, GIOConfig::class.java)
        processUtils.debugLog(configs.toString())

        if (configs.isEmpty()) {
            processUtils.debugLog("hasn't inject custom tracker and config")
            trackerClassName = GIO_DEFAULT_TRACKER
            configClassName = GIO_DEFAULT_CONFIGURATION
            trackerMethodName = null
            return
        }
        check(configs.size <= 1) { "You cannot have more than one GIOConfig, found: $appModule" }
        val gioConfig = configs.get(0)
        val annotationClassName = GIOConfig::class.java.name
        for (mirror in gioConfig.annotationMirrors) {
            // Two different AnnotationMirrors the same class might not be equal, so compare Strings
            // instead. This check is necessary because a given class may have multiple Annotations.
            if (annotationClassName != mirror.annotationType.toString()) {
                continue
            }
            trackerMethodName = gioConfig.simpleName.toString()
            for ((key, value) in mirror.elementValues) {
                if (key.simpleName.toString() == GIOConfig::tracker.name) {
                    trackerClassName = value.value.toString()
                }
                if (key.simpleName.toString() == GIOConfig::config.name) {
                    configClassName = value.value.toString()
                }
            }
        }
        processUtils.debugLog(trackerClassName)
        processUtils.debugLog(configClassName)
    }

    private fun writeTracker(packageName: String, gio: TypeSpec) {
        processUtils.writeClass(packageName, gio)
    }

}