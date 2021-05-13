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

package com.growingio.sdk.inject.compiler;

import com.sun.tools.javac.code.Attribute;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;

public final class AnnotationUtil {
    private AnnotationUtil() {
    }

    @Nullable
    public static String getClassValue(AnnotationMirror annotationMirror, String key) {
        AnnotationValue annotationValue = getAnnotationValue(annotationMirror, key);
        if (annotationValue == null) {
            return null;
        }

        if (annotationValue instanceof Attribute.Class) {
            return ((Attribute.Class) annotationValue).classType.asElement().flatName().toString();
        }

        return null;
    }

    public static String getStringValue(AnnotationMirror annotationMirror, String key) {
        AnnotationValue annotationValue = getAnnotationValue(annotationMirror, key);
        if (annotationValue == null) {
            return null;
        }

        if (annotationValue instanceof Attribute.Constant) {
            return annotationValue.getValue().toString();
        }

        return null;
    }

    @Nullable
    public static List<String> getClassesValue(AnnotationMirror annotationMirror, String key) {
        AnnotationValue annotationValue = getAnnotationValue(annotationMirror, key);
        if (annotationValue == null) {
            return null;
        }

        if (annotationValue instanceof Attribute.Array) {
            Attribute.Array arrayValue = (Attribute.Array) annotationValue;
            List<String> valueList = new ArrayList<>();
            for (Attribute value : arrayValue.values) {
                if (value instanceof Attribute.Class) {
                    valueList.add(((Attribute.Class) value).classType.asElement().flatName().toString());
                } else {
                    throw new IllegalArgumentException(value + " is NOT CLASS");
                }
            }
            return valueList;
        }

        return null;
    }

    @Nullable
    public static List<Attribute.Compound> getAnnotations(AnnotationMirror annotationMirror, String key) {
        AnnotationValue annotationValue = getAnnotationValue(annotationMirror, key);
        if (annotationValue == null) {
            return null;
        }

        if (annotationValue instanceof Attribute.Array) {
            Attribute.Array arrayValue = (Attribute.Array) annotationValue;
            List<Attribute.Compound> valueList = new ArrayList<>();
            for (Attribute value : arrayValue.values) {
                if (value instanceof Attribute.Compound) {
                    valueList.add((Attribute.Compound) value);
                }
            }
            return valueList;
        }

        return null;
    }

    public static AnnotationMirror findAnnotationMirror(Element element, Class<?> annotationClazz) {
        for (AnnotationMirror annotationMirror : element.getAnnotationMirrors()) {
            if (annotationClazz.getName().equals(annotationMirror.getAnnotationType().toString())) {
                return annotationMirror;
            }
        }
        return null;
    }

    private static AnnotationValue getAnnotationValue(AnnotationMirror annotationMirror, String key) {
        Map<? extends ExecutableElement, ? extends AnnotationValue> valueMap = annotationMirror.getElementValues();
        for (ExecutableElement executableElement : valueMap.keySet()) {
            if (key.equals(executableElement.getSimpleName().toString())) {
                return valueMap.get(executableElement);
            }
        }
        return null;
    }
}
