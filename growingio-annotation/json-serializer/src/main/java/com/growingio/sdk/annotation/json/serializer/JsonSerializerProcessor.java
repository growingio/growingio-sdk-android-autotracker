/*
 *   Copyright (C) 2020 Beijing Yishu Technology Co., Ltd.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package com.growingio.sdk.annotation.json.serializer;

import com.google.auto.service.AutoService;
import com.growingio.sdk.annotation.json.JsonSerializer;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;

@AutoService(Processor.class)
public class JsonSerializerProcessor extends AbstractProcessor {

    private ProcessUtils processUtils;

    private JsonSerializerGenerator jsonSerializerGenerator;
    private JsonFactoryGenerator jsonFactoryGenerator;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        this.processUtils = new ProcessUtils(processingEnv);
        jsonSerializerGenerator = new JsonSerializerGenerator(processingEnv, processUtils);
        jsonFactoryGenerator = new JsonFactoryGenerator(processingEnv, processUtils);
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> annotations = new HashSet<>();
        annotations.add(JsonSerializer.class.getName());
        return annotations;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        List<TypeElement> typeElements = processUtils.getElementsFor(JsonSerializer.class, roundEnv);

        for (TypeElement element : typeElements) {
            TypeElement parentElement = processUtils.containSuperElement(element.getSuperclass().toString(), typeElements);
            jsonSerializerGenerator.generate(element, parentElement != null);
        }

        jsonFactoryGenerator.generate(typeElements);
        return false;
    }

}