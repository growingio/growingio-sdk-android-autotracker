package com.growingio.sdk.annotation.compiler;

import com.google.auto.service.AutoService;
import com.growingio.sdk.annotation.GIOModule;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeSpec;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.ElementFilter;

import static com.growingio.sdk.annotation.compiler.ProcessUtils.*;

/**
 * <p>
 *
 * @author cpacm 4/27/21
 */
@AutoService(Processor.class)
public class GioModuleProcessor extends AbstractProcessor {

    private ProcessUtils processUtils;
    private IndexerGenerator indexerGenerator;
    private final List<TypeElement> appGioModules = new ArrayList<>();
    private boolean isGeneratedAppGioModuleWritten;

    @Override
    public synchronized void init(ProcessingEnvironment env) {
        super.init(env);
        this.processUtils = new ProcessUtils(env);
        indexerGenerator = new IndexerGenerator(processUtils);
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return Collections.singleton(GIOModule.class.getName());
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        processUtils.process();
        Set<TypeElement> typeElements = ElementFilter.typesIn(roundEnvironment.getElementsAnnotatedWith(GIOModule.class));
        List<TypeElement> typeList = new ArrayList<>(typeElements);
        if (typeList.size() > 0) {
            indexerGenerator.generate(typeList);
        }

        for (TypeElement element : processUtils.getElementsFor(GIOModule.class, roundEnvironment)) {
            if (processUtils.isAppGioModule(element)) {
                appGioModules.add(element);
            }
        }
        processUtils.debugLog("got app modules: " + appGioModules);

        if (appGioModules.size() > 1) {
            throw new IllegalStateException(
                    "You cannot have more than one AppGioModule, found: " + appGioModules);
        }

        if (!isGeneratedAppGioModuleWritten) {
            isGeneratedAppGioModuleWritten = maybeWriteAppModule();
        }

        return false;
    }

    private boolean maybeWriteAppModule() {
        if (appGioModules.isEmpty()) {
            return false;
        }
        processUtils.debugLog("writeAppModule:" + processUtils.getRound());
        TypeElement appModule = appGioModules.get(0);
        ClassName appModuleClassName = ClassName.get(appModule);
        PackageElement glideGenPackage =
                processingEnv.getElementUtils().getPackageElement(COMPILER_PACKAGE_NAME);

        Set<String> gioModules = new HashSet<>();
        List<? extends Element> gioGeneratedElements = glideGenPackage.getEnclosedElements();
        for (Element indexer : gioGeneratedElements) {
            Index annotation = indexer.getAnnotation(Index.class);
            // If the annotation is null, it means we've come across another class in the same package
            // that we can safely ignore.
            if (annotation != null) {
                Collections.addAll(gioModules, annotation.modules());
            }
        }

        // constructor
        MethodSpec.Builder constructorBuilder =
                MethodSpec.constructorBuilder()
                        .addModifiers(Modifier.PUBLIC)
                        .addParameter(
                                ParameterSpec.builder(ClassName.get("android.content", "Context"), "context")
                                        .build());
        ClassName androidLogName = ClassName.get("android.util", "Log");
        for (String moduleName : gioModules) {
            constructorBuilder.addStatement(
                    "$T.d($S, $S)",
                    androidLogName,
                    GIO_LOG_TAG,
                    "Discovered GIOModule from annotation: " + moduleName);
        }
        constructorBuilder.addStatement("appModule = new $T()", appModule);
        MethodSpec constructor = constructorBuilder.build();

        //register component
        MethodSpec.Builder registerComponents =
                MethodSpec.methodBuilder("registerComponents")
                        .addModifiers(Modifier.PUBLIC)
                        .addAnnotation(Override.class)
                        .addParameter(
                                ParameterSpec.builder(ClassName.get("android.content", "Context"), "context")
                                        //.addAnnotation(processorUtil.nonNull())
                                        .build())
                        .addParameter(
                                ParameterSpec.builder(ClassName.get(GIO_TRACKER_REGISTRY_PACKAGE_NAME, GIO_TRACKER_REGISTRY_NAME), "registry")
                                        .build());
        for (String module : gioModules) {
            ClassName moduleClassName = ClassName.bestGuess(module);
            registerComponents.addStatement(
                    "new $T().registerComponents(context,registry)", moduleClassName);
        }
        registerComponents.addStatement("appModule.registerComponents(context,registry)");
        MethodSpec registerMethod = registerComponents.build();

        TypeSpec.Builder builder =
                TypeSpec.classBuilder(GENERATED_APP_MODULE_IMPL_SIMPLE_NAME)
                        .addModifiers(Modifier.FINAL)
                        .addAnnotation(
                                AnnotationSpec.builder(SuppressWarnings.class)
                                        .addMember("value", "$S", "deprecation")
                                        .build())
                        .superclass(
                                ClassName.get(
                                        GENERATED_ROOT_MODULE_PACKAGE_NAME, GENERATED_ROOT_MODULE_SIMPLE_NAME))
                        .addField(appModuleClassName, "appModule", Modifier.PRIVATE, Modifier.FINAL)
                        .addMethod(constructor)
                        .addMethod(registerMethod);

        TypeSpec generatedGIOModule = builder.build();
        writeGioModule(generatedGIOModule);
        return true;
    }

    private void writeGioModule(TypeSpec appModule) {
        processUtils.writeClass(GENERATED_ROOT_MODULE_PACKAGE_NAME, appModule);
    }
}
