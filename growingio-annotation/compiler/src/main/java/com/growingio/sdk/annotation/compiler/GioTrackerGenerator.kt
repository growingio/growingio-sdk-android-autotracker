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
import com.growingio.sdk.annotation.GIOTracker
import com.growingio.sdk.annotation.compiler.ProcessUtils.Companion.GIO_CONFIGURATION_PROVIDER
import com.growingio.sdk.annotation.compiler.ProcessUtils.Companion.GIO_DEFAULT_LOGGER
import com.growingio.sdk.annotation.compiler.ProcessUtils.Companion.GIO_DEFAULT_TRACKER
import com.squareup.javapoet.ClassName
import com.squareup.javapoet.FieldSpec
import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.TypeSpec
import java.lang.Deprecated
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.Modifier
import javax.lang.model.element.TypeElement
import kotlin.IllegalStateException
import kotlin.String
import kotlin.apply
import kotlin.check

/**
 * <p>
 *
 * <code>
 *     public final class GrowingTracker {
 *    private static final String TAG = "GrowingTracker";
 *
 *    private static volatile Tracker _gioTracker;
 *
 *    public static Tracker get() {
 *        if (_gioTracker == null) {
 *            Logger.e(TAG, "Tracker is UNINITIALIZED, please initialized before use API");
 *            return empty();
 *        }
 *        synchronized (GrowingTracker.class) {
 *            if (_gioTracker != null) {
 *                return _gioTracker;
 *            }
 *            Logger.e(TAG, "Tracker is UNINITIALIZED, please initialized before use API");
 *            return empty();
 *        }
 *    }
 *
 *    public static void start(Context context) {
 *        if (_gioTracker != null) {
 *            Logger.e(TAG, "Tracker is running");
 *            return;
 *        }
 *        throw new IllegalStateException("If you want use start() method, you must use @GIOTracker in GIOAppModule and rebuild project.");
 *    }
 *
 *    public static void startWithConfiguration(Context context,
 *    TrackConfiguration trackConfiguration) {
 *        if (_gioTracker != null) {
 *            Logger.e(TAG, "Tracker is running");
 *            return;
 *        }
 *        ConfigurationProvider.initWithConfig(trackConfiguration.core(),trackConfiguration.getConfigModules());
 *        _gioTracker = new Tracker(context);
 *        initSuccess();
 *    }
 *
 *    private static Tracker empty() {
 *        return new Tracker(null);
 *    }
 *
 *    private static void initSuccess() {
 *        Logger.i(TAG, "!!! Thank you very much for using GrowingIO. We will do our best to provide you with the best service. !!!");
 *        Logger.i(TAG, "!!! GrowingIO Tracker version: "+SDKConfig.SDK_VERSION+" !!!");
 *    }
 *}
 * </code>
 * @author cpacm 4/29/21
 */
class GioTrackerGenerator(
    private val processEnv: ProcessingEnvironment,
    private val processUtils: ProcessUtils
) {

    private lateinit var trackerClassName: String
    private var projectId: String? = null
    private var urlScheme: String? = null
    private var trackerMethodName: String? = null

    fun generate(appModule: TypeElement) {
        initGIOConfig(appModule)
        val generatedCodePackageName = appModule.enclosingElement.toString()
        val tracker = processEnv.elementUtils.getTypeElement(trackerClassName)
            ?: throw IllegalStateException("Do you have import this class:$trackerClassName?")
        val trackerClass = ClassName.get(tracker)
        val annotation = appModule.getAnnotation(GIOAppModule::class.java)
        val growingName = annotation.name
        val isDeprecated = appModule.getAnnotation(Deprecated::class.java) != null
        val configPath = processUtils.getConfigName(appModule)
        val configClass = ClassName.get(generatedCodePackageName, configPath)
        val trackerBuilder = TypeSpec.classBuilder(growingName)
            .apply {
                if (isDeprecated) addJavadoc(
                    """<p>In version 4.0, we use GrowingAutotracker to fully replace GrowingTracker. 
                        |<p>For details, please visit our version migration document: @see <a href="https://growingio.github.io/growingio-sdk-docs/docs/android/version#release-400">https://growingio.github.io/growingio-sdk-docs/docs/android/version#release-400</a>
                        |
                        |""".trimMargin()
                )
            }
            .addJavadoc(
                """
                            The entry point for interacting with GrowingIO Tracker for Applications
                            <p>Includes all generated APIs from Tracker in source.
                            
                            <p>This class is generated and should not be modified
                            
                            """.trimIndent()
            )
            .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
            .addField(
                FieldSpec
                    .builder(
                        String::class.java,
                        "TAG",
                        Modifier.STATIC,
                        Modifier.FINAL,
                        Modifier.PRIVATE
                    )
                    .initializer("\$S", growingName)
                    .build()
            )
            .addField(
                trackerClass,
                "_gioTracker",
                Modifier.STATIC,
                Modifier.PRIVATE,
                Modifier.VOLATILE
            )
            .apply { if (isDeprecated) this.addAnnotation(Deprecated::class.java) }
            .addMethod(generateGetMethod(trackerClass, growingName))
            .addMethod(generateStartMethod(appModule, trackerClass, configClass))
            .addMethod(generateStartConfigurationMethod(appModule, trackerClass, configClass))
            .addMethod(generateEmptyMethod(trackerClass))
            .addMethod(generateSuccessMethod())
            .build()

        writeTracker(generatedCodePackageName, trackerBuilder)
    }


    private fun generateGetMethod(trackerClass: ClassName, growingName: String): MethodSpec {
        val logger =
            processEnv.elementUtils.getTypeElement("com.growingio.android.sdk.track.log.Logger")
        val getMethod = MethodSpec.methodBuilder("get")
            .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
            .beginControlFlow("if (_gioTracker == null)")
            .addStatement(
                "\$T.e(TAG, \$S)",
                ClassName.get(logger),
                "${trackerClass.simpleName()} is UNINITIALIZED, please initialized before use API"
            )
            .addStatement("return empty()")
            .endControlFlow()
            .beginControlFlow("synchronized ($growingName.class)")
            .beginControlFlow("if (_gioTracker != null)")
            .addStatement("return _gioTracker")
            .endControlFlow()
            .addStatement(
                "\$T.e(TAG, \$S)",
                ClassName.get(logger),
                "${trackerClass.simpleName()} is UNINITIALIZED, please initialized before use API"
            )
            .addStatement("return empty()")
            .endControlFlow()
            .returns(trackerClass)
        return getMethod.build()
    }

    private fun generateStartConfigurationMethod(
        appModule: TypeElement,
        trackerClass: ClassName,
        configClass: ClassName
    ): MethodSpec {
        val logger =
            processEnv.elementUtils.getTypeElement(GIO_DEFAULT_LOGGER)
        val context = processEnv.elementUtils.getTypeElement("android.content.Context")
        val getMethod = MethodSpec.methodBuilder("startWithConfiguration")
            .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
            //.addAnnotation(java.lang.Deprecated::class.java)
            .addParameter(ClassName.get(context), "context")
            .addParameter(configClass, "trackConfiguration")
            .beginControlFlow("if (_gioTracker != null)")
            .addStatement(
                "\$T.e(TAG, \$S)",
                ClassName.get(logger),
                "${trackerClass.simpleName()} is running"
            )
            .addStatement("return")
            .endControlFlow()
        if (trackerMethodName != null) {
            //GrowingAppModule appModule = new GrowingAppModule();
            getMethod.addStatement("\$T appModule = new \$T()", appModule, appModule)
                //appModule.config(trackConfiguration);
                .addStatement("appModule.${trackerMethodName}(trackConfiguration)")
        }
        val configProvider = processEnv.elementUtils.getTypeElement(GIO_CONFIGURATION_PROVIDER)
        //ConfigurationProvider.initWithConfig(trackConfiguration.core(),trackConfiguration.getConfigModules());
        getMethod.addStatement(
            "\$T.initWithConfig(trackConfiguration.core(),trackConfiguration.getConfigModules())",
            ClassName.get(configProvider)
        )

        getMethod.addStatement(
            "_gioTracker = new \$T(context)",
            trackerClass
        )
        getMethod.addStatement("initSuccess()")
        return getMethod.build()
    }

    private fun generateStartMethod(
        appModule: TypeElement,
        trackerClass: ClassName,
        configClass: ClassName
    ): MethodSpec {
        val logger =
            processEnv.elementUtils.getTypeElement(GIO_DEFAULT_LOGGER)
        val context = processEnv.elementUtils.getTypeElement("android.content.Context")
        val getMethod = MethodSpec.methodBuilder("start")
            .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
            .addParameter(ClassName.get(context), "context")
            .beginControlFlow("if (_gioTracker != null)")
            .addStatement(
                "\$T.e(TAG, \$S)",
                ClassName.get(logger),
                "${trackerClass.simpleName()} is running"
            )
            .addStatement("return")
            .endControlFlow()

        if (trackerMethodName == null) {
            getMethod.addStatement("throw new IllegalStateException(\"If you want use start() method, you must use @GIOTracker in GIOAppModule and rebuild project.\")");
            return getMethod.build()
        }

        if (projectId.isNullOrEmpty() || urlScheme.isNullOrEmpty()) {
            getMethod.addStatement("throw new IllegalStateException(\"If you want use start() method, you must define ProjectId and UrlScheme in @GIOTracker annotation\")");
            return getMethod.build()
        }

        getMethod.addStatement(
            "\$T trackConfiguration = new \$T(\$S,\$S)",
            configClass,
            configClass,
            projectId,
            urlScheme
        )

        //GrowingAppModule appModule = new GrowingAppModule();
        getMethod.addStatement("\$T appModule = new \$T()", appModule, appModule)
            //appModule.config(trackConfiguration);
            .addStatement("appModule.${trackerMethodName}(trackConfiguration)")

        val configProvider = processEnv.elementUtils.getTypeElement(GIO_CONFIGURATION_PROVIDER)
        //ConfigurationProvider.initWithConfig(trackConfiguration.core(),trackConfiguration.getConfigModules());
        getMethod.addStatement(
            "\$T.initWithConfig(trackConfiguration.core(),trackConfiguration.getConfigModules())",
            ClassName.get(configProvider)
        )

        getMethod.addStatement(
            "_gioTracker = new \$T(context)",
            trackerClass
        )
        getMethod.addStatement("initSuccess()")
        return getMethod.build()
    }

    private fun generateEmptyMethod(trackerClass: ClassName): MethodSpec {
        val emptyMethod = MethodSpec.methodBuilder("empty")
            .addModifiers(Modifier.PRIVATE, Modifier.STATIC)
            .addStatement("return new \$T(null)", trackerClass)
            .returns(trackerClass)
        return emptyMethod.build()
    }

    private fun generateSuccessMethod(): MethodSpec {
        val logger =
            processEnv.elementUtils.getTypeElement(GIO_DEFAULT_LOGGER)
        val sdk =
            processEnv.elementUtils.getTypeElement("com.growingio.android.sdk.track.SDKConfig")
        val configProvider = processEnv.elementUtils.getTypeElement(GIO_CONFIGURATION_PROVIDER)
        val successMethod = MethodSpec.methodBuilder("initSuccess")
            .addModifiers(Modifier.PRIVATE, Modifier.STATIC)
            .addStatement(
                "\$T.i(TAG, \$S)",
                ClassName.get(logger),
                "!!! Thank you very much for using GrowingIO. We will do our best to provide you with the best service. !!!"
            )
            .addStatement(
                "\$T.i(TAG, \"!!! GrowingIO Tracker version: \"+\$T.SDK_VERSION+\" !!!\")",
                ClassName.get(logger),
                ClassName.get(sdk)
            )
            .addStatement(
                "\$T.d(TAG, \$T.get().getAllConfigurationInfo())",
                ClassName.get(logger),
                ClassName.get(configProvider)
            )
        return successMethod.build()
    }


    private fun initGIOConfig(appModule: TypeElement) {
        val tracker = processUtils.findAnnotatedElementsInClasses(appModule, GIOTracker::class.java)
        processUtils.debugLog(tracker.toString())

        if (tracker.isEmpty()) {
            processUtils.debugLog("hasn't inject custom tracker")
            trackerClassName = GIO_DEFAULT_TRACKER
            trackerMethodName = null
            return
        }
        check(tracker.size <= 1) { "You cannot have more than one GIOTracker, found: $appModule" }
        val gioTracker = tracker.get(0)
        val annotationClassName = GIOTracker::class.java.name
        for (mirror in gioTracker.annotationMirrors) {
            // Two different AnnotationMirrors the same class might not be equal, so compare Strings
            // instead. This check is necessary because a given class may have multiple Annotations.
            if (annotationClassName != mirror.annotationType.toString()) {
                continue
            }
            trackerMethodName = gioTracker.simpleName.toString()
            for ((key, value) in mirror.elementValues) {
                if (key.simpleName.toString() == GIOTracker::path.name) {
                    trackerClassName = value.value.toString()
                } else if (key.simpleName.toString() == GIOTracker::projectId.name) {
                    projectId = value.value.toString()
                } else if (key.simpleName.toString() == GIOTracker::urlScheme.name) {
                    urlScheme = value.value.toString()
                }
            }
        }
        processUtils.debugLog(trackerClassName)
    }

    private fun writeTracker(packageName: String, gio: TypeSpec) {
        processUtils.writeClass(packageName, gio)
    }

}