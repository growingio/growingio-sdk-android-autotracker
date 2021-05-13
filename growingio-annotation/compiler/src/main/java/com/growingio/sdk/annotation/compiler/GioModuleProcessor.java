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

package com.growingio.sdk.annotation.compiler;

import com.google.auto.service.AutoService;
import com.growingio.sdk.annotation.GIOModule;

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
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.ElementFilter;

import static com.growingio.sdk.annotation.compiler.ProcessUtils.COMPILER_PACKAGE_NAME;


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
    private AppModuleGenerator appModuleGenerator;
    private GioTrackerGenerator gioTrackerGenerator;

    @Override
    public synchronized void init(ProcessingEnvironment env) {
        super.init(env);
        this.processUtils = new ProcessUtils(env);
        indexerGenerator = new IndexerGenerator(processUtils);
        appModuleGenerator = new AppModuleGenerator(processUtils);
        gioTrackerGenerator = new GioTrackerGenerator(env, processUtils);
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
        PackageElement gioGenPackage =
                processingEnv.getElementUtils().getPackageElement(COMPILER_PACKAGE_NAME);

        Set<String> gioModules = new HashSet<>();
        List<? extends Element> gioGeneratedElements = gioGenPackage.getEnclosedElements();
        for (Element indexer : gioGeneratedElements) {
            Index annotation = indexer.getAnnotation(Index.class);
            // If the annotation is null, it means we've come across another class in the same package
            // that we can safely ignore.
            if (annotation != null) {
                Collections.addAll(gioModules, annotation.modules());
            }
        }
        appModuleGenerator.generate(appModule, gioModules);

        processUtils.debugLog("writeGioTracker:" + processUtils.getRound());
        gioTrackerGenerator.generate(appModule);

        return true;
    }

}
