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
import com.growingio.sdk.annotation.compiler.ProcessUtils.Companion.GIO_DEFAULT_CONFIGURABLE
import com.growingio.sdk.annotation.compiler.ProcessUtils.Companion.GIO_DEFAULT_CONFIGURATION
import com.growingio.sdk.annotation.compiler.ProcessUtils.Companion.GIO_DEFAULT_LIBRARY_MODULE
import com.squareup.javapoet.*
import java.lang.StringBuilder
import java.util.*
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.*
import javax.lang.model.type.TypeVariable
import javax.lang.model.util.ElementFilter
import kotlin.collections.HashMap

/**
 *
 * <p> The output file generated by this class with a AppModule looks like this:
 *
 * <pre>
 * <code>
 * public final class TrackConfiguration {
 *  private final CoreConfiguration coreConfiguration;
 *
 *  private final HashMap<Class<? extends Configurable>, Configurable> MODULE_CONFIGURATIONS = new HashMap<Class<? extends Configurable>, Configurable>();
 *
 *  public TrackConfiguration(String projectId, String urlScheme) {
 *    this.coreConfiguration = new CoreConfiguration(projectId,urlScheme);
 *  }
 *
 *  public CoreConfiguration core() {
 *    return coreConfiguration;
 *  }
 *
 *  public HashMap<Class<? extends Configurable>, Configurable> getConfigModules() {
 *    return MODULE_CONFIGURATIONS;
 *  }
 *
 *  public void addConfiguration(Configurable config) {
 *    if (config != null) {
 *      MODULE_CONFIGURATIONS.put(config.getClass(), config);
 *    }
 *  }
 *
 *  @SuppressWarnings("unchecked")
 *  private <T> T getConfiguration(Class<T> clazz) {
 *    return (T) MODULE_CONFIGURATIONS.get(clazz);
 *  }
 *
 *  public final TrackConfiguration setProject(String projectId, String urlScheme) {
 *    core().setProject(projectId, urlScheme);
 *    return this;
 *  }
 *
 *  public final String getProjectId() {
 *    return core().getProjectId();
 *  }
 *}
 * </code>
 * </pre>
 *
 * @author cpacm 4/29/21
 */
internal class ConfigurationGenerator(
    private val processEnv: ProcessingEnvironment, private val processUtils: ProcessUtils
) {

    private val methodMap = HashMap<String, String>()

    fun generate(appModule: TypeElement, gioConfigs: HashSet<String>) {
        findAppModuleConfig(appModule, gioConfigs) // add App's config
        gioConfigs.add(GIO_DEFAULT_CONFIGURATION) // add CoreConfiguration
        val configName = processUtils.getConfigName(appModule)
        val coreConfiguration = processEnv.elementUtils.getTypeElement(GIO_DEFAULT_CONFIGURATION)
            ?: throw IllegalStateException("Do you have import this class:$GIO_DEFAULT_CONFIGURATION?")
        val generatedCodePackageName = appModule.enclosingElement.toString()
        val generateClass = ClassName.get(generatedCodePackageName, configName)
        val coreConfigurationClass = ClassName.get(coreConfiguration)
        val configBuilder = TypeSpec.classBuilder(configName)
            .addJavadoc(
                """
                            The entry point for interacting with GrowingIO Tracker for Applications
                            <p>Includes all generated configs from Tracker in source.
                            
                            <p>This class is generated and should not be modified
                            
                            """.trimIndent()
            )
            .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
            //private final CoreConfiguration coreCoreConfiguration;
            .addField(
                coreConfigurationClass,
                "coreConfiguration",
                Modifier.PRIVATE,
                Modifier.FINAL
            )
        val libraryModuleClass = ClassName.get(
            processEnv.elementUtils.getTypeElement(
                GIO_DEFAULT_LIBRARY_MODULE
            )
        )
        //private final Map<Class<? extends Configurable>, Configurable> MODULE_CONFIGURATIONS = new HashMap<>();
        val configurable = processEnv.elementUtils.getTypeElement(GIO_DEFAULT_CONFIGURABLE)
        val configurableClass = ClassName.get(configurable)
        val wildcard: TypeName = WildcardTypeName.subtypeOf(configurableClass)
        val classAny = ParameterizedTypeName.get(ClassName.get(Class::class.java), wildcard)
        val mapOfConfigAndClass = ParameterizedTypeName.get(
            ClassName.get(HashMap::class.java),
            classAny,
            configurableClass
        )
        configBuilder.addField(
            FieldSpec.builder(mapOfConfigAndClass, "MODULE_CONFIGURATIONS")
                .addModifiers(Modifier.PRIVATE, Modifier.FINAL)
                .initializer("new \$T()", mapOfConfigAndClass)
                .build()
        )
            .addMethod(generateConstructor(coreConfigurationClass, gioConfigs))//构造方法
            .addMethod(generateCore(coreConfigurationClass))
            .addMethod(generateGetConfigModules(mapOfConfigAndClass))
            .addMethod(generateAddConfiguration(generateClass, configurableClass))
            .addMethod(
                generateAddModuleWithConfiguration(
                    generateClass,
                    libraryModuleClass,
                    configurableClass,
                )
            )
            .addMethod(generateGetConfiguration())

        methodMap.clear()
        for (config in gioConfigs) {
            val configType = processEnv.elementUtils.getTypeElement(config) ?: continue
            val methods = getAllMethods(configType)
            for (method in methods) {
                processUtils.debugLog("${method.returnType} ${method.simpleName}(${method.parameters}){}")
                val isCore = config == GIO_DEFAULT_CONFIGURATION
                overriding(generateClass, configType, method, isCore)?.let {
                    configBuilder.addMethod(it)
                }
            }
        }
        writeConfig(generatedCodePackageName, configBuilder.build())
    }

    private fun findAppModuleConfig(appModule: TypeElement, gioConfigs: HashSet<String>) {
        // add config
        val annotationClassName = GIOAppModule::class.java.name
        for (annotationMirror in appModule.annotationMirrors) {
            if (annotationClassName != annotationMirror.annotationType.toString()) {
                continue
            }
            for ((key, value) in annotationMirror.elementValues) {
                if (key.simpleName.toString() == GIOAppModule::config.name) {
                    val mirrorValue = value!!.value.toString()
                    if (mirrorValue != Void::class.java.name) {
                        gioConfigs.add(mirrorValue)
                    }
                    processUtils.debugLog(key!!.simpleName.toString() + ":" + value.value)
                    break
                }
            }

        }
    }

    private fun generateConstructor(
        coreConfigurationClass: ClassName,
        gioConfigs: Set<String>
    ): MethodSpec {
        val methodSpec = MethodSpec.constructorBuilder()
            .addModifiers(Modifier.PUBLIC)
            .addParameter(String::class.java, "projectId")
            .addParameter(String::class.java, "urlScheme")
            .addStatement(
                "this.coreConfiguration = new \$T(projectId,urlScheme)",
                coreConfigurationClass
            )

        for (config in gioConfigs) {
            if (config == GIO_DEFAULT_CONFIGURATION) continue
            val configType = processEnv.elementUtils.getTypeElement(config) ?: continue
            if (!processUtils.isGioConfig(configType)) {
                throw IllegalStateException("Implement Error: $config should implement $GIO_DEFAULT_CONFIGURABLE?")
            }
            methodSpec.addStatement(
                "addConfiguration(new \$T())", ClassName.get(configType)
            )
        }

        return methodSpec.build()
    }

    private fun generateCore(coreConfigurationClass: ClassName): MethodSpec {
        val coreMethod = MethodSpec.methodBuilder("core")
            .addModifiers(Modifier.PUBLIC)
            .returns(coreConfigurationClass)
            .addStatement("return coreConfiguration")
        return coreMethod.build()
    }

    private fun generateGetConfigModules(typeName: ParameterizedTypeName): MethodSpec {
        val modulesMethod = MethodSpec.methodBuilder("getConfigModules")
            .addModifiers(Modifier.PUBLIC)
            .returns(typeName)
            .addStatement("return MODULE_CONFIGURATIONS")
        return modulesMethod.build()
    }

    private fun generateAddConfiguration(generateClass: ClassName, config: ClassName): MethodSpec {
        val addMethod = MethodSpec.methodBuilder("addConfiguration")
            .addParameter(config, "config")
            .addModifiers(Modifier.PUBLIC)
            .beginControlFlow("if (config != null)")
            .addStatement("MODULE_CONFIGURATIONS.put(config.getClass(), config)")
            .endControlFlow()
            .returns(generateClass)
            .addStatement("return this")
        return addMethod.build()
    }

    private fun generateAddModuleWithConfiguration(
        generateClass: ClassName,
        libraryModule: ClassName,
        config: ClassName
    ): MethodSpec {
        val addMethod = MethodSpec.methodBuilder("addPreloadComponent")
            .addParameter(libraryModule, "module")
            .addParameter(config, "config")
            .addModifiers(Modifier.PUBLIC)
            .beginControlFlow("if (module != null && config != null)")
            .addStatement("coreConfiguration.addPreloadComponent(module)")
            .addStatement("MODULE_CONFIGURATIONS.put(config.getClass(), config)")
            .endControlFlow()
            .returns(generateClass)
            .addStatement("return this")
        return addMethod.build()
    }


    private fun generateGetConfiguration(): MethodSpec {
        val wildcard: TypeName = TypeVariableName.get("T")
        val classAny = ParameterizedTypeName.get(ClassName.get(Class::class.java), wildcard)

        val getMethod = MethodSpec.methodBuilder("getConfiguration")
            .addTypeVariable(TypeVariableName.get("T"))
            .addModifiers(Modifier.PRIVATE)
            .addAnnotation(
                AnnotationSpec.builder(SuppressWarnings::class.java)
                    .addMember("value", "\$S", "unchecked")
                    .build()
            )
            .returns(TypeVariableName.get("T"))
            .addParameter(classAny, "clazz")
            .addStatement("return (T) MODULE_CONFIGURATIONS.get(clazz)")
        return getMethod.build()
    }

    private fun overriding(
        generateClass: ClassName,
        config: TypeElement,
        method: ExecutableElement,
        isCore: Boolean = false
    ): MethodSpec? {
        val enclosingClass = method.enclosingElement
        require(!enclosingClass.modifiers.contains(Modifier.ABSTRACT)) { "Cannot transform method on abstract class $enclosingClass" }
        var modifiers = method.modifiers
        if (modifiers.contains(Modifier.PRIVATE) || modifiers.contains(Modifier.PROTECTED)) {
            processUtils.debugLog("cannot transform method with modifiers: $modifiers")
            return null
        }
        val methodName = method.simpleName.toString()
        if (methodMap.containsKey(methodName)) {
            throw IllegalStateException(
                "Duplicate method name \"$methodName\" with ${
                    methodMap[methodName]
                } and ${config.qualifiedName}"
            )
        }
        methodMap[methodName] = config.qualifiedName.toString()
        val methodBuilder = MethodSpec.methodBuilder(methodName)
        modifiers = LinkedHashSet(modifiers)
        modifiers.remove(Modifier.FINAL)
        modifiers.remove(Modifier.ABSTRACT)
        modifiers.remove(Modifier.DEFAULT)
        methodBuilder.addModifiers(modifiers)
        methodBuilder.addModifiers(Modifier.FINAL)
        for (typeParameterElement in method.typeParameters) {
            val `var` = typeParameterElement.asType() as TypeVariable
            methodBuilder.addTypeVariable(TypeVariableName.get(`var`))
        }
        methodBuilder.addParameters(parametersOf(method))
        methodBuilder.varargs(method.isVarArgs)
        for (thrownType in method.thrownTypes) {
            methodBuilder.addException(TypeName.get(thrownType))
        }
        for (annotationMirror in method.annotationMirrors) {
            methodBuilder.addAnnotation(AnnotationSpec.get(annotationMirror))
        }

        // getConfiguration(CrashConfig.class).setDNS(dns);
        val stateSb = StringBuilder()
        if (!isCore) {
            stateSb.append("getConfiguration(\$T.class).").append(methodName).append("(")
            val paramCount = method.parameters.size
            for (index in 0 until paramCount) {
                val param = method.parameters[index]
                stateSb.append(param.simpleName)
                if (index < paramCount - 1) {
                    stateSb.append(", ")
                }
            }
            stateSb.append(")")
        } else {
            stateSb.append("core().").append(methodName).append("(")
            val paramCount = method.parameters.size
            for (index in 0 until paramCount) {
                val param = method.parameters[index]
                stateSb.append(param.simpleName)
                if (index < paramCount - 1) {
                    stateSb.append(", ")
                }
            }
            stateSb.append(")")
        }
        processUtils.debugLog(
            "[config method]:$generateClass----${TypeName.get(method.returnType)}"
        )
        if (TypeName.get(method.returnType) != TypeName.VOID) {
            if (ClassName.get(config).canonicalName() == TypeName.get(method.returnType)
                    .toString()
            ) {
                methodBuilder.returns(generateClass)
                methodBuilder.addStatement(stateSb.toString(), ClassName.get(config))
                methodBuilder.addStatement("return this")
            } else {
                methodBuilder.returns(TypeName.get(method.returnType))
                methodBuilder.addStatement("return $stateSb", ClassName.get(config))
            }
        } else {
            methodBuilder.returns(TypeName.get(method.returnType))
            methodBuilder.addStatement(stateSb.toString(), ClassName.get(config))
        }

        return methodBuilder.build()
    }

    private fun parametersOf(method: ExecutableElement): List<ParameterSpec> {
        val result: MutableList<ParameterSpec> = ArrayList()
        for (parameter in method.parameters) {
            // ParameterSpec.get 不会复制annotations
            // 参考 https://github.com/square/javapoet/issues/482
            // https://github.com/square/javapoet/pull/501 合并时放弃了getAll方法
            result.add(getAllParameters(parameter))
        }
        return result
    }

    private fun getAllParameters(element: VariableElement): ParameterSpec {
        require(element.kind == ElementKind.PARAMETER) { String.format("element is not a parameter") }
        val type = TypeName.get(element.asType())
        val name = element.simpleName.toString()

        val builder = ParameterSpec.builder(type, name).addModifiers(element.modifiers)

        for (annotationMirror in element.annotationMirrors) {
            builder.addAnnotation(AnnotationSpec.get(annotationMirror))
        }

        return builder.build()
    }

    private fun writeConfig(packageName: String, gio: TypeSpec) {
        processUtils.writeClass(packageName, gio)
    }

    private fun getAllMethods(type: TypeElement): List<ExecutableElement> {
        return ElementFilter.methodsIn(type.enclosedElements)
    }
}