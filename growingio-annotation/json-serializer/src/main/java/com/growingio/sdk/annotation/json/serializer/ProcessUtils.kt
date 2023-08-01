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

import com.squareup.javapoet.JavaFile
import com.squareup.javapoet.TypeSpec
import javax.annotation.processing.ProcessingEnvironment
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.element.Element
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.Modifier
import javax.lang.model.element.TypeElement
import javax.lang.model.element.VariableElement
import javax.lang.model.type.TypeKind
import javax.lang.model.util.ElementFilter
import javax.tools.Diagnostic

class ProcessUtils(val processEnv: ProcessingEnvironment) {

    fun debugLog(msg: String) {
        if (DEBUG) {
            infoLog(msg)
        }
    }

    fun infoLog(msg: String) {
        processEnv.messager.printMessage(Diagnostic.Kind.NOTE, "$TAG:$msg")
    }

    fun findSetFieldMethod(field: VariableElement, clazz: TypeElement): String? {
        val upMethod = field.simpleName.toString().replaceFirstChar { it.uppercaseChar() }
        val fieldMethod = "set$upMethod"
        val findMethodResult = clazz.enclosedElements.filterIsInstance<ExecutableElement>().find {
            it.simpleName.toString() == fieldMethod && it.parameters.size == 1
        }
        if (findMethodResult != null) {
            return "$fieldMethod($FIELD_REPLACE_REG)"
        }
        val findFieldResult = findField(field, clazz)
        if (findFieldResult != null) {
            return "$findFieldResult = $FIELD_REPLACE_REG"
        }
        return null
    }

    fun findGetFieldMethod(field: VariableElement, clazz: TypeElement): String? {
        val upMethod = field.simpleName.toString().replaceFirstChar { it.uppercaseChar() }
        val fieldMethod = if (field.asType().kind == TypeKind.BOOLEAN) {
            "is$upMethod"
        } else {
            "get$upMethod"
        }
        val findMethodResult = clazz.enclosedElements.filterIsInstance<ExecutableElement>().find {
            it.simpleName.toString() == fieldMethod && it.returnType.kind == field.asType().kind && it.parameters.isEmpty()
        }
        if (findMethodResult != null) {
            return "$fieldMethod()"
        }

        return findField(field, clazz)
    }

    private fun findField(
        field: VariableElement,
        clazz: TypeElement,
    ): String? {
        val findFieldResult = clazz.enclosedElements.filterIsInstance<VariableElement>().find {
            it.simpleName.toString() == field.simpleName.toString() && field.asType().kind == it.asType().kind
        }
        if (findFieldResult != null &&
            !findFieldResult.modifiers.contains(Modifier.PRIVATE) &&
            !findFieldResult.modifiers.contains(Modifier.FINAL) &&
            !findFieldResult.modifiers.contains(Modifier.STATIC)
        ) {
            return findFieldResult.simpleName.toString()
        }
        return null
    }

    fun getElementsFor(clazz: Class<out Annotation>, env: RoundEnvironment): List<TypeElement> {
        val annotatedElements: Collection<Element> = env.getElementsAnnotatedWith(clazz)
        return ElementFilter.typesIn(annotatedElements)
    }

    fun containSuperElement(targetSuperClass: String, typeElements: List<TypeElement>): TypeElement? {
        for (element in typeElements) {
            if (element.enclosingElement.toString() + "." + element.simpleName.toString() == targetSuperClass) {
                return element
            }
        }
        return null
    }

    fun writeClass(packageName: String?, clazz: TypeSpec) {
        try {
            debugLog("Writing class:\n$clazz")
            JavaFile.builder(packageName, clazz).skipJavaLangImports(true).build()
                .writeTo(processEnv.getFiler())
        } catch (e: Throwable) {
            throw RuntimeException(e)
        }
    }

    companion object {
        const val DEBUG = false

        const val TAG = "[JsonSerializerProcessor]"
        const val GENERATE_CLASS_APPEND = "JsonSerializableFactory"

        const val JSON_SERIALIZABLE_PACKAGE = "com.growingio.android.sdk.track.events.helper"
        const val JSON_SERIALIZABLE_CLASS = "JsonSerializable"

        const val JSON_OBJECT_PACKAGE = "org.json"
        const val JSON_OBJECT_CLASS = "JSONObject"
        const val JSON_OBJECT_EXCEPTION = "JSONException"

        const val TEXT_UTILS_PACKAGE = "android.text"
        const val TEXT_UTILS_CLASS = "TextUtils"

        const val FIELD_REPLACE_REG = "#_FIELD_#"
    }
}
