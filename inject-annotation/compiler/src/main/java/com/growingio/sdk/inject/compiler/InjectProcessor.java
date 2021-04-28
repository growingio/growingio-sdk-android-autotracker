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

import com.google.auto.service.AutoService;
import com.growingio.sdk.inject.annotation.After;
import com.growingio.sdk.inject.annotation.AfterSuper;
import com.growingio.sdk.inject.annotation.AfterSupers;
import com.growingio.sdk.inject.annotation.Afters;
import com.growingio.sdk.inject.annotation.Before;
import com.growingio.sdk.inject.annotation.BeforeSuper;
import com.growingio.sdk.inject.annotation.BeforeSupers;
import com.growingio.sdk.inject.annotation.Befores;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.sun.tools.javac.code.Attribute;
import com.sun.tools.javac.code.Symbol;

import org.apache.commons.io.IOUtils;
import org.objectweb.asm.Type;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;

@AutoService(Processor.class)
public class InjectProcessor extends AbstractProcessor {
    private static final String TAG = "InjectProcessor";
    private static final String LICENSE_HEADER = "/*\n" +
            " * Copyright (C) 2020 Beijing Yishu Technology Co., Ltd.\n" +
            " *\n" +
            " * Licensed under the Apache License, Version 2.0 (the \"License\");\n" +
            " * you may not use this file except in compliance with the License.\n" +
            " * You may obtain a copy of the License at\n" +
            " *\n" +
            " *      http://www.apache.org/licenses/LICENSE-2.0\n" +
            " *\n" +
            " * Unless required by applicable law or agreed to in writing, software\n" +
            " * distributed under the License is distributed on an \"AS IS\" BASIS,\n" +
            " * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.\n" +
            " * See the License for the specific language governing permissions and\n" +
            " * limitations under the License.\n" +
            " */";

    private final List<List<Object>> mAroundHookClassesArgs = new ArrayList<>();
    private final List<List<Object>> mSuperHookClassesArgs = new ArrayList<>();

    private Messager mMessager;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        mMessager = processingEnvironment.getMessager();
        log("InjectProcessor init finish");
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> types = new HashSet<>();
        types.add(Before.class.getCanonicalName());
        types.add(After.class.getCanonicalName());
        types.add(BeforeSuper.class.getCanonicalName());
        types.add(AfterSuper.class.getCanonicalName());

        types.add(Befores.class.getCanonicalName());
        types.add(Afters.class.getCanonicalName());
        types.add(BeforeSupers.class.getCanonicalName());
        types.add(AfterSupers.class.getCanonicalName());
        return types;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.RELEASE_8;
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        Class<? extends Annotation>[] allAnnotations = new Class[]{Before.class, After.class, BeforeSuper.class, AfterSuper.class,
                Befores.class, Afters.class, BeforeSupers.class, AfterSupers.class};
        for (Class<? extends Annotation> annotation : allAnnotations) {
            analyzeAnnotation(annotation, roundEnvironment);
        }

        generateJavaFile();
        return false;
    }

    private void analyzeAnnotation(Class<? extends Annotation> clazz, RoundEnvironment roundEnvironment) {
        Set<? extends Element> elements = roundEnvironment.getElementsAnnotatedWith(clazz);
        for (Element element : elements) {
            if (clazz == Befores.class || clazz == Afters.class || clazz == BeforeSupers.class || clazz == AfterSupers.class) {
                AnnotationMirror mirror = AnnotationUtil.findAnnotationMirror(element, clazz);
                if (mirror == null) {
                    continue;
                }
                List<Attribute.Compound> annotations = AnnotationUtil.getAnnotations(mirror, "value");
                if (annotations != null && !annotations.isEmpty()) {
                    for (Attribute.Compound annotation : annotations) {
                        stashHookClassesArgs(element, annotation);
                    }
                }
            } else {
                stashHookClassesArgs(element, AnnotationUtil.findAnnotationMirror(element, clazz));
            }
        }
    }

    private void stashHookClassesArgs(Element element, AnnotationMirror annotationMirror) {
        String originalTargetClassName = AnnotationUtil.getClassValue(annotationMirror, "clazz");
        String targetClassName = TypeUtil.getInternalName(originalTargetClassName);

        String targetMethodName = AnnotationUtil.getStringValue(annotationMirror, "method");
        List<String> targetParameters = AnnotationUtil.getClassesValue(annotationMirror, "parameterTypes");
        if (targetParameters == null) {
            targetParameters = new ArrayList<>();
        }
        Type[] argumentTypes = new Type[targetParameters.size()];
        for (int i = 0; i < targetParameters.size(); i++) {
            argumentTypes[i] = TypeUtil.getType(targetParameters.get(i));
        }
        String targetReturnType = AnnotationUtil.getClassValue(annotationMirror, "returnType");
        if (targetReturnType == null) {
            targetReturnType = void.class.getName();
        }
        Type returnType = TypeUtil.getType(targetReturnType);
        String targetDesc = Type.getMethodDescriptor(returnType, argumentTypes);

        Symbol.ClassSymbol enclosingElement = (Symbol.ClassSymbol) element.getEnclosingElement();
        String originalInjectClass = enclosingElement.flatName().toString();
        String injectClass = TypeUtil.getInternalName(originalInjectClass);
        String injectMethod = element.getSimpleName().toString();

        if (element instanceof Symbol.MethodSymbol) {
            List<String> injectParameters = new ArrayList<>();
            Symbol.MethodSymbol methodSymbol = (Symbol.MethodSymbol) element;
            List<Symbol.VarSymbol> parameters = methodSymbol.getParameters();
            for (Symbol.VarSymbol parameter : parameters) {
                injectParameters.add(parameter.asType().asElement().flatName().toString());
            }
            argumentTypes = new Type[injectParameters.size()];
            for (int i = 0; i < injectParameters.size(); i++) {
                argumentTypes[i] = TypeUtil.getType(injectParameters.get(i));
            }
            String injectReturnType = void.class.getName();
            returnType = TypeUtil.getType(injectReturnType);
            String injectDesc = Type.getMethodDescriptor(returnType, argumentTypes);

            String annotationClass = annotationMirror.getAnnotationType().toString();
            boolean isAfter = After.class.getName().equals(annotationClass) || AfterSuper.class.getName().equals(annotationClass);

            log(originalInjectClass + "#" + injectMethod + " ===" + annotationClass + "===> " + originalTargetClassName + "#" + targetMethodName);
            if (Before.class.getName().equals(annotationClass) || After.class.getName().equals(annotationClass)) {
                mAroundHookClassesArgs.add(Arrays.<Object>asList(targetClassName, targetMethodName, targetDesc, injectClass, injectMethod, injectDesc, isAfter));
            } else {
                mSuperHookClassesArgs.add(Arrays.<Object>asList(targetClassName, targetMethodName, targetDesc, injectClass, injectMethod, injectDesc, isAfter));
            }
        }
    }


    private void log(String msg) {
        mMessager.printMessage(Diagnostic.Kind.NOTE, TAG + ": " + msg);
    }

    private void generateJavaFile() {
        try {
            TypeSpec.Builder builder = TypeSpec.classBuilder("HookClassesConfig")
                    .addModifiers(Modifier.PUBLIC);

            builder.addJavadoc("该Class是由 inject-compiler 自动生成的，请不要随意更改！");

            ClassName mapClass = ClassName.get(Map.class);
            ClassName hashMapClass = ClassName.get(HashMap.class);
            ClassName stringClass = ClassName.get(String.class);
            ClassName collectionsClass = ClassName.get(Collections.class);
            ClassName targetClassClass = ClassName.get("com.growingio.sdk.plugin.autotrack.hook", "TargetClass");
            ClassName targetMethodClass = ClassName.get("com.growingio.sdk.plugin.autotrack.hook", "TargetMethod");
            ClassName injectMethodClass = ClassName.get("com.growingio.sdk.plugin.autotrack.hook", "InjectMethod");
            TypeName mapOfTargetClass = ParameterizedTypeName.get(mapClass, stringClass, targetClassClass);

            FieldSpec aroundMethodMapField = FieldSpec.builder(mapOfTargetClass, "AROUND_HOOK_CLASSES")
                    .addModifiers(Modifier.PRIVATE)
                    .addModifiers(Modifier.STATIC)
                    .addModifiers(Modifier.FINAL)
                    .initializer("new $T<>()", hashMapClass)
                    .build();
            builder.addField(aroundMethodMapField);

            FieldSpec superMethodMapField = FieldSpec.builder(mapOfTargetClass, "SUPER_HOOK_CLASSES")
                    .addModifiers(Modifier.PRIVATE)
                    .addModifiers(Modifier.STATIC)
                    .addModifiers(Modifier.FINAL)
                    .initializer("new $T<>()", hashMapClass)
                    .build();
            builder.addField(superMethodMapField);

            CodeBlock.Builder staticBlock = CodeBlock.builder();
            for (List<Object> arg : mAroundHookClassesArgs) {
                staticBlock.addStatement("putAroundHookMethod($S, $S, $S, $S, $S, $S, $L)",
                        arg.get(0), arg.get(1), arg.get(2), arg.get(3), arg.get(4), arg.get(5), arg.get(6));
            }
            for (List<Object> arg : mSuperHookClassesArgs) {
                staticBlock.addStatement("putSuperHookMethod($S, $S, $S, $S, $S, $S, $L)",
                        arg.get(0), arg.get(1), arg.get(2), arg.get(3), arg.get(4), arg.get(5), arg.get(6));
            }
            builder.addStaticBlock(staticBlock.build());

            MethodSpec constructor = MethodSpec.constructorBuilder()
                    .addModifiers(Modifier.PRIVATE)
                    .build();
            builder.addMethod(constructor);

            MethodSpec putAroundHookMethod = MethodSpec.methodBuilder("putAroundHookMethod")
                    .addModifiers(Modifier.PRIVATE)
                    .addModifiers(Modifier.STATIC)
                    .returns(void.class)
                    .addParameter(String.class, "targetClassName")
                    .addParameter(String.class, "targetMethodName")
                    .addParameter(String.class, "targetMethodDesc")
                    .addParameter(String.class, "injectClassName")
                    .addParameter(String.class, "injectMethodName")
                    .addParameter(String.class, "injectMethodDesc")
                    .addParameter(boolean.class, "isAfter")
                    .addStatement("putHookMethod(AROUND_HOOK_CLASSES, targetClassName, targetMethodName, targetMethodDesc, injectClassName, injectMethodName, injectMethodDesc, isAfter)")
                    .build();
            builder.addMethod(putAroundHookMethod);

            MethodSpec putSuperHookMethod = MethodSpec.methodBuilder("putSuperHookMethod")
                    .addModifiers(Modifier.PRIVATE)
                    .addModifiers(Modifier.STATIC)
                    .returns(void.class)
                    .addParameter(String.class, "targetClassName")
                    .addParameter(String.class, "targetMethodName")
                    .addParameter(String.class, "targetMethodDesc")
                    .addParameter(String.class, "injectClassName")
                    .addParameter(String.class, "injectMethodName")
                    .addParameter(String.class, "injectMethodDesc")
                    .addParameter(boolean.class, "isAfter")
                    .addStatement("putHookMethod(SUPER_HOOK_CLASSES, targetClassName, targetMethodName, targetMethodDesc, injectClassName, injectMethodName, injectMethodDesc, isAfter)")
                    .build();
            builder.addMethod(putSuperHookMethod);

            MethodSpec putHookMethod = MethodSpec.methodBuilder("putHookMethod")
                    .addModifiers(Modifier.PRIVATE)
                    .addModifiers(Modifier.STATIC)
                    .returns(void.class)
                    .addParameter(mapOfTargetClass, "classMap")
                    .addParameter(String.class, "targetClassName")
                    .addParameter(String.class, "targetMethodName")
                    .addParameter(String.class, "targetMethodDesc")
                    .addParameter(String.class, "injectClassName")
                    .addParameter(String.class, "injectMethodName")
                    .addParameter(String.class, "injectMethodDesc")
                    .addParameter(boolean.class, "isAfter")
                    .addCode("$T targetClass = classMap.get(targetClassName);\n" +
                                    "    if (targetClass == null) {\n" +
                                    "        targetClass = new $T(targetClassName);\n" +
                                    "        classMap.put(targetClassName, targetClass);\n" +
                                    "    }\n" +
                                    "    $T targetMethod = targetClass.getTargetMethod(targetMethodName, targetMethodDesc);\n" +
                                    "    if (targetMethod == null) {\n" +
                                    "        targetMethod = new $T(targetMethodName, targetMethodDesc);\n" +
                                    "        targetClass.addTargetMethod(targetMethod);\n" +
                                    "    }\n" +
                                    "    targetMethod.addInjectMethod(new $T(injectClassName, injectMethodName, injectMethodDesc, isAfter));\n",
                            targetClassClass, targetClassClass, targetMethodClass, targetMethodClass, injectMethodClass)
                    .build();
            builder.addMethod(putHookMethod);

            MethodSpec getAroundHookClasses = MethodSpec.methodBuilder("getAroundHookClasses")
                    .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                    .returns(mapOfTargetClass)
                    .addStatement("return $T.unmodifiableMap(AROUND_HOOK_CLASSES)", collectionsClass)
                    .build();
            builder.addMethod(getAroundHookClasses);

            MethodSpec getSuperHookClasses = MethodSpec.methodBuilder("getSuperHookClasses")
                    .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                    .returns(mapOfTargetClass)
                    .addStatement("return $T.unmodifiableMap(SUPER_HOOK_CLASSES)", collectionsClass)
                    .build();
            builder.addMethod(getSuperHookClasses);

            JavaFile javaFile = JavaFile.builder("com.growingio.sdk.plugin.autotrack.hook", builder.build())
                    .skipJavaLangImports(true)
                    .build();
            generatePerfectJava(javaFile);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void generatePerfectJava(JavaFile javaFile) throws IOException {
        String classPath = getProjectRootPath() + "/growingio-autotracker-gradle-plugin/src/main/java/" + javaFile.packageName.replace(".", "/") + "/" + javaFile.typeSpec.name + ".java";
        File file = new File(classPath);
        OutputStream out = new FileOutputStream(file);

        StringBuilder builder = new StringBuilder(LICENSE_HEADER);
        builder.append(System.lineSeparator())
                .append(javaFile.toString());
        IOUtils.write(builder, out);
    }

    private String getProjectRootPath() {
        String jarPath = getClass().getProtectionDomain().getCodeSource().getLocation().getPath();
        return new File(jarPath).getParentFile().getParentFile().getParentFile().getParent();
    }
}