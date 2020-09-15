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

package com.growingio.sdk.plugin.autotrack.compile.visitor;

import com.google.common.truth.Truth;
import com.growingio.sdk.plugin.autotrack.ClassUtils;
import com.growingio.sdk.plugin.autotrack.compile.Context;
import com.growingio.sdk.plugin.autotrack.compile.SystemLog;
import com.growingio.sdk.plugin.autotrack.hook.HookClassesConfig;
import com.growingio.sdk.plugin.autotrack.hook.InjectMethod;
import com.growingio.sdk.plugin.autotrack.hook.TargetClass;
import com.growingio.sdk.plugin.autotrack.hook.TargetMethod;
import com.growingio.sdk.plugin.autotrack.tmp.Callback;
import com.growingio.sdk.plugin.autotrack.tmp.SubExample;
import com.growingio.sdk.plugin.autotrack.tmp.SuperExample;
import com.growingio.sdk.plugin.autotrack.tmp.inject.InjectAgent;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

@RunWith(PowerMockRunner.class)
@PrepareForTest({HookClassesConfig.class})
public class InjectSuperClassVisitorTest {
    @Before
    public void setUp() {
        PowerMockito.mockStatic(HookClassesConfig.class);
    }

    private void mockSuperHookClasses() {
        Map<String, TargetClass> targetClassMap = new HashMap<>();
        String className = ClassUtils.getClassName(SuperExample.class);
        TargetClass targetClass = new TargetClass(className);
        targetClassMap.put(className, targetClass);
        TargetMethod targetMethod = new TargetMethod("onExecute", "()V");
        targetClass.addTargetMethod(targetMethod);
        targetMethod.addInjectMethod(new InjectMethod(ClassUtils.getClassName(InjectAgent.class), "onExecute", "(L" + className + ";)V", false));
        PowerMockito.when(HookClassesConfig.getSuperHookClasses()).thenReturn(targetClassMap);
    }

    @Test
    public void injectSuperBefore() throws IOException, IllegalAccessException, InstantiationException, NoSuchMethodException, InvocationTargetException, NoSuchFieldException {
        mockSuperHookClasses();

        InputStream resourceAsStream = ClassUtils.classToInputStream(SubExample.class);
        ClassReader cr = new ClassReader(resourceAsStream);
        ClassWriter cw = new ClassWriter(cr, ClassWriter.COMPUTE_MAXS);
        Context context = new Context(new SystemLog(), getClass().getClassLoader());
        cr.accept(new InjectSuperClassVisitor(cw, context), ClassReader.SKIP_FRAMES | ClassReader.EXPAND_FRAMES);
        Class<?> aClass = new ByteCodeClassLoader(getClass().getClassLoader()).defineClass(SubExample.class.getName(), cw.toByteArray());
        Object obj = aClass.newInstance();
        final SuperExample[] callbackResult = new SuperExample[1];
        InjectAgent.setsCallback(new Callback() {
            @Override
            public void onCallback(SuperExample example) {
                Truth.assertThat(obj == example).isTrue();
                Truth.assertThat(example.isExecuted()).isFalse();
                callbackResult[0] = example;
            }
        });
        obj.getClass().getMethod("onExecute").invoke(obj);
        Field isExecuted = SuperExample.class.getDeclaredField("mIsExecuted");
        isExecuted.setAccessible(true);
        Truth.assertThat(callbackResult[0].isExecuted()).isTrue();
    }
}