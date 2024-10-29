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
package com.growingio.sdk.annotation.json.serializer

import com.growingio.sdk.annotation.json.JsonSerializer
import com.growingio.sdk.annotation.json.serializer.ProcessUtils.Companion.JSON_SERIALIZABLE_PACKAGE
import com.squareup.javapoet.ClassName
import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.ParameterizedTypeName
import com.squareup.javapoet.TypeName
import com.squareup.javapoet.TypeSpec
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.Modifier
import javax.lang.model.element.TypeElement

/**
 * <p>
 *
 * @author cpacm 2023/7/14
 */
internal class JsonFactoryGenerator(
    private val processEnv: ProcessingEnvironment,
    private val processUtils: ProcessUtils,
) {
    fun generate(typeElements: List<TypeElement>) {
        val sortedList = arrayListOf<TypeElement>()
        typeElements.forEach { element ->
            val findElement = sortedList.find { e -> e == element }
            if (findElement != null) return@forEach
            val superClass = element.superclass
            val findParentElement =
                processUtils.containSuperElement(superClass.toString(), typeElements)
            if (findParentElement == null) {
                sortedList.add(element)
            } else {
                val index = searchElementInSortedList(findParentElement, sortedList, typeElements)
                if (index == -1) {
                    sortedList.add(element)
                } else {
                    sortedList.add(index, element)
                }
            }
        }
        if (sortedList.isEmpty()) return

        val baseEventElement = sortedList.last()
        val annotation: JsonSerializer = baseEventElement.getAnnotation(
            JsonSerializer::class.java,
        )

        val builderName: String = annotation.builder

        val generateClass =
            ClassName.get(JSON_SERIALIZABLE_PACKAGE, ProcessUtils.GENERATE_CLASS_APPEND)

        val eventType =
            processEnv.elementUtils.getTypeElement(baseEventElement.qualifiedName.toString())
        val eventBuilderType =
            processEnv.elementUtils.getTypeElement(baseEventElement.qualifiedName.toString() + "." + builderName)

        val builderType = ClassName.get(eventBuilderType)

        val superinterface = ParameterizedTypeName.get(
            ClassName.get(
                ProcessUtils.JSON_SERIALIZABLE_PACKAGE,
                ProcessUtils.JSON_SERIALIZABLE_CLASS,
            ),
            ClassName.get(eventType),
            builderType,
        )

        val jonSerialBuilder = TypeSpec.classBuilder(generateClass).addJavadoc(
            """
                            <p>This class is generated and should not be modified
                            
            """.trimIndent(),
        ).addModifiers(Modifier.PUBLIC, Modifier.FINAL).addSuperinterface(superinterface)
            .addMethod(
                generateToJsonMethod(
                    ClassName.get(eventType),
                    sortedList,
                ),
            )
            .addMethod(
                generateParseFromMethod(
                    builderType,
                    sortedList,
                ),
            )

        processUtils.writeClass(JSON_SERIALIZABLE_PACKAGE, jonSerialBuilder.build())

        sortedList.forEach {
            processUtils.debugLog(it.simpleName.toString())
            /**
             * if (event instanceof CustomEvent) {
             CustomEventJsonSerializableFactory.create().toJson(jsonObject, (CustomEvent) event);
             return;
             }
             */
        }
    }

    private fun generateToJsonMethod(
        eventType: TypeName,
        sortedList: MutableList<TypeElement>,
    ): MethodSpec {
        val toJsonMethod = MethodSpec.methodBuilder("toJson")
            .addParameter(
                ClassName.get(
                    ProcessUtils.JSON_OBJECT_PACKAGE,
                    ProcessUtils.JSON_OBJECT_CLASS,
                ),
                "jsonObject",
            )
            .addParameter(eventType, "event").addModifiers(Modifier.PUBLIC)
            .addAnnotation(Override::class.java)

        sortedList.forEach { element ->
            val eventPackageName = element.enclosingElement.toString()
            val eventName = element.simpleName.toString() + ProcessUtils.GENERATE_CLASS_APPEND
            val eventClass = ClassName.get(eventPackageName, eventName)

            toJsonMethod.beginControlFlow("if (event instanceof \$T)", element)
            toJsonMethod.addStatement(
                "\$T.create().toJson(jsonObject, (\$T) event)",
                eventClass,
                element,
            )
            toJsonMethod.addStatement("return")
            toJsonMethod.endControlFlow()
        }
        return toJsonMethod.build()
    }

    private fun generateParseFromMethod(
        builderType: TypeName,
        sortedList: MutableList<TypeElement>,
    ): MethodSpec {
        val parseFromMethod =
            MethodSpec.methodBuilder("parseFrom").addParameter(builderType, "builder")
                .addParameter(
                    ClassName.get(
                        ProcessUtils.JSON_OBJECT_PACKAGE,
                        ProcessUtils.JSON_OBJECT_CLASS,
                    ),
                    "jsonObject",
                )
                .addModifiers(Modifier.PUBLIC).addAnnotation(Override::class.java)
        sortedList.forEach { element ->
            val eventPackageName = element.enclosingElement.toString()
            val eventName = element.simpleName.toString() + ProcessUtils.GENERATE_CLASS_APPEND
            val eventClass = ClassName.get(eventPackageName, eventName)

            val annotation: JsonSerializer = element.getAnnotation(
                JsonSerializer::class.java,
            )
            val builderName: String = annotation.builder
            val eventBuilderType =
                processEnv.elementUtils.getTypeElement(element.qualifiedName.toString() + "." + builderName)
            val elementBuilderType = ClassName.get(eventBuilderType)

            parseFromMethod.beginControlFlow("if (builder instanceof \$T)", elementBuilderType)
            parseFromMethod.addStatement(
                "\$T.create().parseFrom((\$T) builder, jsonObject)",
                eventClass,
                elementBuilderType,
            )
            parseFromMethod.addStatement("return")
            parseFromMethod.endControlFlow()
        }

        return parseFromMethod.build()
    }

    private fun searchElementInSortedList(
        targetElement: TypeElement,
        sortedList: List<TypeElement>,
        typeElements: List<TypeElement>,
    ): Int {
        val index = sortedList.indexOf(targetElement)
        if (index == -1) {
            val superClass = targetElement.superclass
            val findParentElement =
                processUtils.containSuperElement(superClass.toString(), typeElements)
            if (findParentElement != null) {
                return searchElementInSortedList(findParentElement, sortedList, typeElements)
            }
            return -1
        }
        return index
    }
}
